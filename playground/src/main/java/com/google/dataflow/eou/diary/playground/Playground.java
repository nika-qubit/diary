package com.google.dataflow.eou.diary.playground;

import com.google.dataflow.eou.diary.playground.Dummy;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.EntityQuery;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.StructuredQuery;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Playground {

  public Pipeline buildPipeline(PipelineOptions options) {
    Pipeline p = Pipeline.create(options);
    
    List<Dummy> dummies = new ArrayList<>();
    for (int i=0; i < 50000; i++) {
      dummies.add(Dummy.create("lol"));
    }

    PCollection<Dummy> dummyPColl = p.apply("Create Dummies", Create.of(dummies));
    dummyPColl.apply(ParDo.of(new DsQueryTest()));
    return p;
  }

  public static class DsQueryTest extends DoFn<Dummy, Void> {
    private Datastore datastore;

    @Setup
    public void initialize() throws Exception {
      datastore = DatastoreOptions.newBuilder().setProjectId("ningk-test-project").build().getService(); 
    }

    @ProcessElement
    public void processElement(ProcessContext processContext) {
      EntityQuery.Builder queryBuilder = Query.newEntityQueryBuilder().setKind("Dummy");
      queryBuilder.setFilter(PropertyFilter.eq("name", processContext.element().getName()));
      datastore.run(queryBuilder.build());
    }
  }

  public static void main(String[] args) {
    Playground playground = new Playground();
    playground.buildPipeline(PipelineOptionsFactory.fromArgs(args).create()).run().waitUntilFinish();
  }
}


