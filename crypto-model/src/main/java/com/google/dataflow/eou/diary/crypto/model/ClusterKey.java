package com.google.dataflow.eou.diary.crypto.model;

import com.google.auto.value.AutoValue;
import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.DatatypeConverter;

/**
 * A helper class to build deterministic serializable key for Beam grouping since Java serialization
 * is not deterministic with respect to Object.equals(java.lang.Object) for all types.
 */
@AutoValue
public abstract class ClusterKey implements Serializable {

  public static ClusterKey create(Currency currency, String date) {
    return new AutoValue_ClusterKey(currency, date);
  }

  public static ClusterKey deserialize(List<String> key) {
    return create(Currency.fromCode(key.get(0)), key.get(1));
  }

  public List<String> serialize() {
    return Arrays
        .asList(currency().toString(), date());
  }

  public ClusterKey truncateDateToDay() {
    return create(currency(),
        DatatypeConverter.parseDateTime(date()).toInstant().truncatedTo(ChronoUnit.DAYS)
            .toString());
  }

  public abstract Currency currency();

  // ISO-8061 format.
  public abstract String date();
}
