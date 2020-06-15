package com.google.dataflow.eou.diary.playground;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Playground {

  public Pipeline buildPipeline(PipelineOptions options) {
    Pipeline p = Pipeline.create(options);

    final List<Person> personList = Arrays.asList(new Person[]{
        new Person("20200101", "1", "1"),
        new Person("20200202", "2", "1"),
        new Person("20200303", "3", "1")
    });

    final List<Person> personList2 = Arrays.asList(new Person[]{
        new Person("20200101", "1", "2"),
        new Person("20200202", "2", "2"),
        new Person("20200303", "3", "2")
    });

    List<List<Person>> personListList = new ArrayList<List<Person>>();
    personListList.add(personList);
    personListList.add(personList2);

    PCollection<List<Person>> person = p.apply(Create.of(personListList));
    PCollection<KV<String, List<Person>>> personByCompany = person.apply(ParDo.of(new MarkPersonListByCompany()));
    PCollection<KV<String, Iterable<List<Person>>>> personGroupedByCompany = personByCompany.apply(GroupByKey.<String, List<Person>>create());
    return p;

  }

  public static class MarkPersonListByCompany extends DoFn<List<Person>, KV<String, List<Person>>> {

    @ProcessElement
    public void processElement(ProcessContext c) {
      c.output(KV.of(c.element().get(0).companyId(), c.element()));
    }

  }

  public static void main(String[] args) {
    Playground playground = new Playground();
    playground.buildPipeline(PipelineOptionsFactory.fromArgs(args).create()).run().waitUntilFinish();
  }
}

class Person implements Serializable {
  public Person(
      String businessDay,
      String departmentId,
      String companyId
  ) {
    this.businessDay = businessDay;
    this.departmentId = departmentId;
    this.companyId = companyId;
  }

  public String companyId() {
    return companyId;
  }

  public String businessDay() {
    return businessDay;
  }

  public String departmentId() {
    return departmentId;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }
    if (getClass() != other.getClass()) {
      return false;
    }
    Person otherPerson = (Person) other;
    return this.businessDay.equals(otherPerson.businessDay)
        && this.departmentId.equals(otherPerson.departmentId)
        && this.companyId.equals(otherPerson.companyId);
  }

  @Override
  public int hashCode(){
    return Objects.hash(this.businessDay, this.departmentId, this.companyId);
  }

  private final String businessDay;
  private final String departmentId;
  private final String companyId;
}
