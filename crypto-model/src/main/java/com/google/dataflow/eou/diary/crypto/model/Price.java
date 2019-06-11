package com.google.dataflow.eou.diary.crypto.model;

import com.google.auto.value.AutoValue;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

/**
 * Daily price info of crypto currencies.
 */
@AutoValue
public abstract class Price implements Serializable {

  private static final Price DEFAULT = create(Currency.DEFAULT, Date.from(Instant.EPOCH), 0.0, 0.0,
      0.0, 0.0, 0l, 0l);

  public static Price create(Currency currency, Date date, double open, double high, double low,
      double close,
      long volume, long marketCap) {
    return new AutoValue_Price(currency, date, open, high, low, close, volume, marketCap);
  }

  public static Price getDefault() {
    return DEFAULT;
  }

  public abstract Currency currency();

  public abstract Date date();

  public abstract double open();

  public abstract double high();

  public abstract double low();

  public abstract double close();

  public abstract long volume();

  public abstract long marketCap();
}