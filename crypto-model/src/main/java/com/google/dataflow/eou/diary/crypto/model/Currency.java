package com.google.dataflow.eou.diary.crypto.model;

/**
 * Enums of crypto currency codes.
 */
public enum Currency {
  // A default value that doesn't represent any real currency.
  DEFAULT,

  // bitcoin
  BTC,

  // litecoin
  LTC,

  // namecoin
  NMC,

  // peercoin
  PPC,

  // dogecoin
  DOGE,

  // gridcoin
  GRC,

  // primecoin
  XPM,

  // ripple
  XRP,

  // nxt
  NXT,

  // auroracoin
  AUR,

  // dash
  DASH,

  // neo
  NEO,

  // monero
  XMR,

  //nem
  XEM,

  // titcoin
  TIT,

  // verge
  XVG,

  // stellar
  XLM,

  //vertcoin
  VTC,

  //ether
  ETH,

  //ethereumclassic
  ETC,

  //tether
  USDT,

  //zcash
  ZEC,

  //bitcoincash
  BCH,

  //eos.io
  EOS;

  /**
   * Returns the enum of a crypto currency from its code. Code is case-insensitive. If a code is not
   * valid or supported, returns the default currency enum. Consumers should ignore any default
   * currency enum returned.
   */
  public static Currency fromCode(String code) {
    try {
      return Currency.valueOf(code.toUpperCase());
    } catch (IllegalArgumentException e) {
      return Currency.DEFAULT;
    }
  }
}