package com.google.dataflow.eou.diary.crypto;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.values.PCollection;

public final class CryptoResearchBatch {

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

    /**
     * A general output location for the pipeline.
     */
    @Description("Path of the file to write to")
    @Default.String("gs://dataflow-eou-diary/output")
    String getOutput();

    void setOutput(String value);
  }

  public static void main(String[] args) {
    PipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
        .as(CryptoResearchBatchOptions.class);
    Pipeline p = Pipeline.create(options);

    PCollection<String> lines = p
        .apply("ReadBtcPrices",
            TextIO.read().from(((CryptoResearchBatchOptions) options).getCurrencyPriceInputFile()));

    p.run().waitUntilFinish();
  }
}
