package com.google.dataflow.eou.diary.crypto;

import com.google.common.base.Joiner;
import com.google.dataflow.eou.diary.crypto.model.ClusterKey;
import com.google.dataflow.eou.diary.crypto.model.Currency;
import com.google.dataflow.eou.diary.crypto.model.NewsSentiment;
import com.google.dataflow.eou.diary.crypto.model.Price;
import com.google.dataflow.eou.diary.crypto.model.PriceChange;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
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
import org.apache.beam.sdk.transforms.ProcessFunction;
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
    @Default.String("gs://dataflow-eou-diary/prices_*.csv")
    String getCurrencyPriceInputFile();

    void setCurrencyPriceInputFile(String value);

    @Description("Path of the file to read news articles from")
    @Default.String("gs://dataflow-eou-diary/news_*.csv")
    String getNewsInputFile();

    void setNewsInputFile(String value);

    /**
     * A branching raw data output location for the pipeline.
     */
    @Description("Path of the file to write price changes to")
    @Default.String("gs://dataflow-eou-diary/output_price_change.csv")
    String getCurrencyPriceChangeOutput();

    void setCurrencyPriceChangeOutput(String value);

    @Description("Path to the file to write price change and news sentiment correlation to")
    @Default.String("gs://dataflow-eou-diary/output_correlation.csv")
    String getCorrelationOutput();

    void setCorrelationOutput(String value);
  }

  public static ProcessFunction<String, Price> readPriceData = line -> {
    String[] columns = line.split(",");
    if (Currency.fromCode(columns[0]) == Currency.DEFAULT) {
      return Price.getDefault();
    }
    try {
      // The date read should be in ISO-8061 format.
      return Price
          .create(Currency.fromCode(columns[0]),
              Date.from(DatatypeConverter.parseDateTime(columns[1]).toInstant()),
              Double.valueOf(columns[2]),
              Double.valueOf(columns[3]), Double.valueOf(columns[4]),
              Double.valueOf(columns[5]), Long.valueOf(columns[6]),
              Long.valueOf(columns[7]));
    } catch (Exception e) {
      logger.debug("Cannot parse the price data: %s", line);
      return Price.getDefault();
    }
  };

  public static ProcessFunction<String, NewsSentiment> readNewsData = line -> {
    String[] columns = line.split(",");
    if (Currency.fromCode(columns[0]) == Currency.DEFAULT) {
      return NewsSentiment.getDefault();
    }
    try {
      return NewsSentiment
          .create(ClusterKey.create(Currency.fromCode(columns[0]), columns[1]),
              Double.valueOf(columns[5]), Double.valueOf(columns[6]), Double.valueOf(columns[7]),
              Double.valueOf(columns[8]));
    } catch (Exception e) {
      logger.debug("Cannot parse the news data: %s", line);
      return NewsSentiment.getDefault();
    }
  };

  public static ProcessFunction<Price, Price> forwardPriceDateByOneDay = price -> {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(price.date());
    calendar.add(Calendar.DATE, 1);
    return Price
        .create(price.currency(), calendar.getTime(), price.open(), price.high(),
            price.low(),
            price.close(), price.volume(), price.marketCap());
  };

  /**
   * Cluster price values by a tuple/list as (date, currency). If there are duplicated values for
   * the same (date, currency), the cluster would contain multiple price values with the same price.
   * If there is no duplicate from input, each cluster should be singleton list.
   */
  public static class ClusterPriceByDay extends DoFn<Price, KV<List<String>, Price>> {

    private final Counter priceCount = Metrics.counter(ClusterPriceByDay.class, "priceCount");

    @ProcessElement
    public void processElement(ProcessContext c) {
      priceCount.inc();
      c.output(KV.of(
          ClusterKey.create(c.element().currency(),
              c.element().date().toInstant().toString()).truncateDateToDay().serialize(),
          c.element()));
    }
  }

  public static class SerializeClusterKeyForPriceChange extends
      DoFn<PriceChange, KV<List<String>, PriceChange>> {

    @ProcessElement
    public void processElement(ProcessContext c) {
      c.output(KV.of(c.element().clusterKey().serialize(), c.element()));
    }
  }

  public static class SerializeClusterKeyForNewsSentiment extends
      DoFn<NewsSentiment, KV<List<String>, NewsSentiment>> {

    @ProcessElement
    public void processElement(ProcessContext c) {
      c.output(KV.of(c.element().clusterKey().truncateDateToDay().serialize(), c.element()));
    }
  }

  public static class CalculatePriceChangesByDay extends
      DoFn<KV<List<String>, CoGbkResult>, PriceChange> {

    private final TupleTag<Price> yesterdayPriceTag;
    private final TupleTag<Price> todayPriceTag;

    public CalculatePriceChangesByDay(TupleTag<Price> yesterdayPriceTag,
        TupleTag<Price> todayPriceTag) {
      this.yesterdayPriceTag = yesterdayPriceTag;
      this.todayPriceTag = todayPriceTag;
    }

    @ProcessElement
    public void processElement(
        @Element KV<List<String>, CoGbkResult> consecutivePriceByDay,
        OutputReceiver<PriceChange> out) {
      // Just get the 1st value from the group to ignore duplicate items since
      // (date, currency) should identify a unique price value in the history.
      double yesterdayClose = consecutivePriceByDay.getValue().getAll(yesterdayPriceTag)
          .iterator().next()
          .close();
      double todayClose = consecutivePriceByDay.getValue().getAll(todayPriceTag)
          .iterator()
          .next().close();
      out.output(PriceChange
          .create(ClusterKey.deserialize(consecutivePriceByDay.getKey()),
              PriceChange.compare(yesterdayClose, todayClose),
              PriceChange.diffInValue(yesterdayClose, todayClose),
              PriceChange.diffInPercentile(yesterdayClose, todayClose)));
    }
  }

  public static class CorrelateNewsSentimentByPriceChange extends
      DoFn<KV<List<String>, CoGbkResult>, KV<PriceChange, NewsSentiment>> {

    private final TupleTag<PriceChange> priceChangeTag;
    private final TupleTag<NewsSentiment> newsSentimentTag;

    public CorrelateNewsSentimentByPriceChange(TupleTag<PriceChange> priceChangeTag,
        TupleTag<NewsSentiment> newsSentimentTag) {
      this.priceChangeTag = priceChangeTag;
      this.newsSentimentTag = newsSentimentTag;
    }

    @ProcessElement
    public void processElement(@Element KV<List<String>, CoGbkResult> correlation,
        OutputReceiver<KV<PriceChange, NewsSentiment>> out) {

      PriceChange priceChange = correlation.getValue().getAll(priceChangeTag).iterator().next();
      correlation.getValue().getAll(newsSentimentTag)
          .forEach(newsSentiment -> out.output(KV.of(priceChange, newsSentiment)));
    }
  }

  static void runPipeline(CryptoResearchBatchOptions options) {
    Pipeline p = Pipeline.create(options);

    PCollection<Price> prices = p
        .apply("ReadCryptoCurrencyPrices",
            TextIO.read().from(options.getCurrencyPriceInputFile()))
        .apply("ConvertCsvLineToPriceObject",
            MapElements.into(TypeDescriptor.of(Price.class)).via(readPriceData))
        .apply("FilterOutPricesWithoutValidData",
            Filter.by(price -> !price.equals(Price.getDefault())));

    PCollection<KV<List<String>, Price>> priceByYesterday = prices
        .apply("ClusterPricesByYesterday", ParDo.of(new ClusterPriceByDay()));
    PCollection<KV<List<String>, Price>> priceByToday = prices
        .apply("ForwardDateByOneDayForEachPrice",
            MapElements.into(TypeDescriptor.of(Price.class)).via(forwardPriceDateByOneDay))
        .apply("ClusterPricesByToday", ParDo.of(new ClusterPriceByDay()));
    final TupleTag<Price> yesterdayPriceTag = new TupleTag<>();
    final TupleTag<Price> todayPriceTag = new TupleTag<>();
    PCollection<PriceChange> priceChangeByDay = KeyedPCollectionTuple
        .of(yesterdayPriceTag, priceByYesterday).and(todayPriceTag, priceByToday).apply(
            CoGroupByKey.create()).
            apply("FilterOutNonConsecutiveDays", Filter.by(consecutivePriceByDay ->
                consecutivePriceByDay.getValue().getAll(yesterdayPriceTag).iterator().hasNext()
                    && consecutivePriceByDay.getValue().getAll(todayPriceTag).iterator().hasNext()))
        .apply("CalculatePriceChangesByDay",
            ParDo.of(new CalculatePriceChangesByDay(yesterdayPriceTag, todayPriceTag)));

    priceChangeByDay
        .apply("FormatPriceChangeToCsv", MapElements.via(new SimpleFunction<PriceChange, String>() {
          @Override
          public String apply(PriceChange input) {
            return Joiner.on(',')
                .join(input.clusterKey().currency(), input.clusterKey().date(), input.change(),
                    input.priceDiffValue(), input.priceDiffPercentile());
          }
        }))
        .apply("WriteFormattedPriceChange",
            TextIO.write().to(options.getCurrencyPriceChangeOutput()));

    PCollection<NewsSentiment> newsSentiment = p
        .apply("ReadCryptoNewsSentiment", TextIO.read().from(options.getNewsInputFile()))
        .apply("ConvertCsvLineToNewsSentimentObject",
            MapElements.into(TypeDescriptor.of(NewsSentiment.class)).via(readNewsData))
        .apply("FilterOutNewsSentimentWithoutValidData",
            Filter.by(sentiment -> !sentiment.equals(NewsSentiment.getDefault())));

    final TupleTag<PriceChange> priceChangeTag = new TupleTag<>();
    final TupleTag<NewsSentiment> newsSentimentTag = new TupleTag<>();

    PCollection<KV<PriceChange, NewsSentiment>> normalizedCorrelation = KeyedPCollectionTuple
        .of(priceChangeTag, priceChangeByDay
            .apply("SerializeClusterKeyForPriceChange",
                ParDo.of(new SerializeClusterKeyForPriceChange())))
        .and(newsSentimentTag, newsSentiment
            .apply("SerializeClusterKeyForNewsSentiment",
                ParDo.of(new SerializeClusterKeyForNewsSentiment()))).apply(CoGroupByKey.create())
        .apply("FilterOutPriceChangeWithoutNews", Filter
            .by(correlation -> correlation.getValue().getAll(priceChangeTag).iterator().hasNext()
                && correlation.getValue().getAll(newsSentimentTag).iterator().hasNext()))
        .apply("CorrelateNewsSentimentByPriceChange",
            ParDo.of(new CorrelateNewsSentimentByPriceChange(priceChangeTag, newsSentimentTag)));

    normalizedCorrelation.apply("FormatNormalizedCorrelationToCsv", MapElements.via(
        new SimpleFunction<KV<PriceChange, NewsSentiment>, String>() {
          @Override
          public String apply(KV<PriceChange, NewsSentiment> input) {
            return Joiner.on(',')
                .join(input.getKey().clusterKey().currency(), input.getKey().clusterKey().date(),
                    input.getKey().change(), input.getKey().priceDiffValue(),
                    input.getKey().priceDiffPercentile(), input.getValue().neg(),
                    input.getValue().neu(), input.getValue().pos(), input.getValue().compound());
          }
        })).apply("WriteFormattedPriceChangeAndNewsSentimentCorrelation",
        TextIO.write().to(options.getCorrelationOutput()));

    p.run().waitUntilFinish();
  }

  public static void main(String[] args) {
    PipelineOptionsFactory.register(CryptoResearchBatchOptions.class);
    CryptoResearchBatchOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
        .as(CryptoResearchBatchOptions.class);

    runPipeline(options);

  }
}
