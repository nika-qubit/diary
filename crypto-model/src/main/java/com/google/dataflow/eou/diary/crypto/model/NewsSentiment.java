package com.google.dataflow.eou.diary.crypto.model;

import com.google.auto.value.AutoValue;
import java.io.Serializable;
import java.time.Instant;

@AutoValue
public abstract class NewsSentiment implements Serializable {

  private static final NewsSentiment DEFAULT = NewsSentiment
      .create(ClusterKey.create(Currency.DEFAULT,
          Instant.EPOCH.toString()), 0.0, 0.0, 0.0, 0.0);

  public static NewsSentiment create(ClusterKey clusterKey, double neg, double neu, double pos,
      double compound) {
    return new AutoValue_NewsSentiment(clusterKey, neg, neu, pos, compound);
  }

  public static NewsSentiment getDefault() {
    return DEFAULT;
  }

  public abstract ClusterKey clusterKey();

  public abstract double neg();

  public abstract double neu();

  public abstract double pos();

  public abstract double compound();
}
