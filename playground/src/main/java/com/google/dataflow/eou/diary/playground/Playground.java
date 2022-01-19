package com.google.dataflow.eou.diary.playground;

import com.google.cloud.healthcare.etl.xmltojson.XmlToJsonCDARev2;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.json.JSONObject;

import java.io.IOException;


public class Playground {

    public static void main(String[] args) {
        Playground playground = new Playground();
        playground.buildPipeline(PipelineOptionsFactory.fromArgs(args).create()).run().waitUntilFinish();
    }

    public Pipeline buildPipeline(PipelineOptions options) {
        Pipeline p = Pipeline.create(options);

        ReadFile r = new ReadFile();
        PCollection<FileIO.ReadableFile> readables = r.readPattern(p, "input-xml.txt");
        PCollection<KV<String, String>> read = readables.apply(ParDo.of(new ReadXmlFn()));
        PCollection<String> parsed = read.apply(ParDo.of(new ParseFn()));
        parsed.apply(TextIO.write().to("/tmp/ok"));
        return p;
    }
}

class ReadXmlFn extends DoFn<FileIO.ReadableFile, KV<String, String>> {
    @ProcessElement
    public void process(@Element FileIO.ReadableFile rf, OutputReceiver<KV<String, String>> out) {
        try {
            out.output(KV.of(rf.getMetadata().resourceId().toString(), rf.readFullyAsUTF8String()));
        } catch (IOException e) {}
    }
}

class ParseFn extends DoFn<KV<String, String>, String> {
    @ProcessElement
    public void process(@Element KV<String, String> kv, OutputReceiver<String> out) {
        String retJsonStr = "";
        try {
            JSONObject retJson = new JSONObject(
                    new XmlToJsonCDARev2()
                            .parse(kv.getValue().toString())
            ).put("__data_source__", kv.getKey());
            retJsonStr = retJson.toString();
        } catch (Exception e) {}
        out.output(retJsonStr);
    }
}


