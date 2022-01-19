package com.google.dataflow.eou.diary.playground;

import com.google.common.collect.ImmutableList;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.fs.EmptyMatchTreatment;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;


public class ReadFile {

    public PCollection<FileIO.ReadableFile> readPattern(Pipeline p, String pattern) {
        PCollection<String> files = p.apply(Create.of(ImmutableList.of("input-xml.txt")));
        PCollection<FileIO.ReadableFile> readables = files.apply(FileIO.matchAll().withEmptyMatchTreatment(
                EmptyMatchTreatment.ALLOW)).apply(FileIO.readMatches());
        return readables;
    }
}

