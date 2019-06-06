package com.google.dataflow.eou.diary.crypto;

import com.google.dataflow.eou.diary.crypto.model.Currency;
import com.google.dataflow.eou.diary.crypto.model.Price;
import com.google.dataflow.eou.diary.crypto.model.PriceChange;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.transforms.join.CoGroupByKey;
import org.apache.beam.sdk.transforms.join.KeyedPCollectionTuple;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoResearchBatch {

  private static final Logger logger = LoggerFactory.getLogger(CryptoResearchBatch.class);

  public interface CryptoResearchBatchOptions extends PipelineOptions {

    /**
     * By default, this pipeline reads from a dataset in csv containing the Date, Open, High, Low,
     * Close, Volume, Market Cap information of BTC. Set this option to choose a different input
     * file.
     */
    @Description("Path of the file to read crypto currency prices from")
    @Default.String("gs://dataflow-eou-diary/btc_prices.csv")
    String getCurrencyPriceInputFile();

    void setCurrencyPriceInputFile(String value);

    @Description("Path of the file to read news articles from")
    @Default.String("gs://dataflow-eou-diary/btc_news.csv")
    String getNewsInputFile();

    void setNewsInputFile(String value);

    /**
     * A general output location for the pipeline.
     */
    @Description("Path of the file to write to")
    @Default.String("gs://dataflow-eou-diary/output")
    String getOutput();

    void setOutput(String value);
  }

  public static class ClusterPriceByDay extends DoFn<Price, KV<String, Price>> {

    private final Counter priceCount = Metrics.counter(ClusterPriceByDay.class, "priceCount");

    @ProcessElement
    public void processElement(ProcessContext c) {
      priceCount.inc();
      c.output(KV.of(c.element().date().toInstant().truncatedTo(ChronoUnit.DAYS).toString(),
          c.element()));
    }
  }

  static void runPipeline(CryptoResearchBatchOptions options) {
    Pipeline p = Pipeline.create(options);

    PCollection<String> lines = p
        .apply("ReadBtcPrices",
            TextIO.read().from(options.getCurrencyPriceInputFile()));
    PCollection<Price> prices = lines
        .apply("ConvertCsvLineToPriceObject",
            MapElements.into(TypeDescriptor.of(Price.class)).via((String line) -> {
              String[] columns = line.split(",");
              try {
                // The date read should be in ISO-8061 format.
                return Price
                    .create(Date.from(Instant.parse(columns[0])), Double.valueOf(columns[1]),
                        Double.valueOf(columns[2]), Double.valueOf(columns[3]),
                        Double.valueOf(columns[4]), Long.valueOf(columns[5]),
                        Long.valueOf(columns[6]),
                        Currency.BTC);
              } catch (Exception e) {
                logger.debug("Cannot parse the date, not in ISO-8061: %s", columns[0]);
                return Price.getDefault();
              }
            }));
    PCollection<Price> parsablePrices = prices
        .apply("FilterOutPricesWithoutValidData",
            Filter.by(price -> !price.equals(Price.getDefault())));
    PCollection<Price> pricesWithDateMovedForwardOneDay = parsablePrices
        .apply("ForwardDateByOneDayForEachPrice",
            MapElements.into(TypeDescriptor.of(Price.class)).via((Price price) -> {
              Calendar calendar = Calendar.getInstance();
              calendar.setTime(price.date());
              calendar.add(Calendar.DATE, 1);
              return Price
                  .create(calendar.getTime(), price.open(), price.high(), price.low(),
                      price.close(), price.volume(), price.marketCap(), price.currency());
            }));

    final TupleTag<Price> oldPriceTag = new TupleTag<>();
    final TupleTag<Price> newPriceTag = new TupleTag<>();

    PCollection<KV<String, Price>> priceByYesterday = parsablePrices
        .apply("ClusterPricesByYesterday", ParDo.of(new ClusterPriceByDay()));
    PCollection<KV<String, Price>> priceByToday = pricesWithDateMovedForwardOneDay
        .apply("ClusterPricesByToday", ParDo.of(new ClusterPriceByDay()));

    PCollection<KV<String, CoGbkResult>> consecutivePricesByDay = KeyedPCollectionTuple
        .of(oldPriceTag, priceByYesterday).and(newPriceTag, priceByToday).apply(
            CoGroupByKey.create());

    PCollection<PriceChange> priceChangeByDay = consecutivePricesByDay.
        apply("FilterOutNonConsecutiveDays", Filter.by(consecutivePriceByDay ->
            consecutivePriceByDay.getValue().getAll(oldPriceTag).iterator().hasNext()
                && consecutivePriceByDay.getValue().getAll(newPriceTag).iterator().hasNext()))
        .apply("CalculatePriceChangesByDay",
            ParDo.of(new DoFn<KV<String, CoGbkResult>, PriceChange>() {
              @ProcessElement
              public void processElement(@Element KV<String, CoGbkResult> consecutivePriceByDay,
                  OutputReceiver<PriceChange> out) {
                double yesterdayClose = consecutivePriceByDay.getValue().getOnly(oldPriceTag)
                    .close();
                double todayClose = consecutivePriceByDay.getValue().getOnly(newPriceTag).close();
                out.output(PriceChange.create(consecutivePriceByDay.getKey(),
                    PriceChange.compare(yesterdayClose, todayClose),
                    PriceChange.diffInValue(yesterdayClose, todayClose),
                    PriceChange.diffInPercentile(yesterdayClose, todayClose)));
              }
            })

        );

    priceChangeByDay
        .apply("FormatToString", MapElements.via(new SimpleFunction<PriceChange, String>() {
          @Override
          public String apply(PriceChange input) {
            return input.toString();
          }
        }))
        .apply("Write", TextIO.write().to(options.getOutput()));

    p.run().waitUntilFinish();
  }

  public static void main(String[] args) {
    PipelineOptionsFactory.register(CryptoResearchBatchOptions.class);
    CryptoResearchBatchOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
        .as(CryptoResearchBatchOptions.class);

    runPipeline(options);

  }
}
