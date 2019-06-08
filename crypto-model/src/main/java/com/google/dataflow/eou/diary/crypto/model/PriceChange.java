package com.google.dataflow.eou.diary.crypto.model;

import com.google.auto.value.AutoValue;
import java.io.Serializable;

@AutoValue
public abstract class PriceChange implements Serializable {

  public static PriceChange create(String dateTruncatedToDay, Change change, double priceDiffValue,
      double priceDiffPercentile) {
    return new AutoValue_PriceChange(dateTruncatedToDay, change, priceDiffValue,
        priceDiffPercentile);
  }

  public static Change compare(double oldVal, double newVal) {
    if (oldVal == newVal) {
      return Change.EQ;
    } else if (oldVal < newVal) {
      return Change.INC;
    } else {
      return Change.DEC;
    }
  }

  public static double diffInValue(double oldVal, double newVal) {
    return newVal - oldVal;
  }

  public static double diffInPercentile(double oldVal, double newVal) {
    switch (compare(oldVal, newVal)) {
      case EQ:
        return 0.0;
      case INC:
      case DEC:
      default:
        return (newVal - oldVal) / oldVal;
    }
  }

  public enum Change {
    INC,
    DEC,
    EQ
  }

  // Using ISO-8061 format which can be parsed by java.time.Instant.
  public abstract String dateTruncatedToDay();

  public abstract Change change();

  public abstract double priceDiffValue();

  public abstract double priceDiffPercentile();
}
