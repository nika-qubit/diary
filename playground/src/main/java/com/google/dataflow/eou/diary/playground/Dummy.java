package com.google.dataflow.eou.diary.playground;

import com.google.auto.value.AutoValue;
import java.io.Serializable;

@AutoValue
public abstract  class Dummy implements Serializable{
  public static Dummy create(String name) {
    return new AutoValue_Dummy(name);
  }
  public abstract String getName();
}
