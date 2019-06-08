package com.google.dataflow.eou.diary.crypto.model;

/**
 * Enums of crypto currencies.
 */
public enum Currency {
  // A default value that doesn't represent any real currency.
  DEFAULT(""),

  // Bit coin.
  BTC("btc");

  private final String code;

  Currency(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }
}