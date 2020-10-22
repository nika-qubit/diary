package com.google.dataflow.eou.diary.playground;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.options.ValueProvider;

public class TemplatePlayground {

  public interface Options extends PipelineOptions {
    @Description("The input IO type: text or file IO.")
    @Required
    ValueProvider<String> getInputIOType();
    void setInputIOType(ValueProvider<String> value);

    @Description("The output IO type: text or file IO.")
    @Required
    ValueProvider<String> getOutputIOType();
    void setOutputIOType(ValueProvider<String> value);
  }

  public static PipelineResult run(Options options) {
    Pipeline p = Pipeline.create(options);
    if (options.getInputIOType().get().equals("text")) {
      p.apply("Read from TextIO", TextIO.read().from("text.text"));
    } else if (options.getInputIOType().get().equals("file")) {
      p.apply("Read from FileIO", FileIO.match().filepattern("text.text"));
    } else {
      throw RuntimeException("Unknown IO type.");
    }
    p.apply("Print", MapElements.into())
  }

  public static void main(String[] args) {
    Options options = PipelineOptionsFactory
        .fromArgs(args)
        .withValidation()
        .as(Options.class);

    run(options);
  }
}
