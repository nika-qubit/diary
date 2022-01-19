package com.google.cloud.healthcare.etl.util.library;


/*

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;
import org.json.JSONObject;
*/

public abstract interface CHFLogInit {

    //public CHFLogConfigurations newConfig();
    public CHFLogConfigurations copyConfig(CHFLogConfigurations CHFLogConfig);
  /*private abstract CHFLogConfigurations CHFLogConfigs;

  protected CHFLogInit() {
      this.CHFLogConfigs = new CHFLogConfigurations();
  }

  protected CHFLogInit(CHFLogConfigurations CHFLogConfig) {
    if (CHFLogConfig == null) {
        this.CHFLogConfigs = new CHFLogConfigurations();
    } else {
        //TODO: figure out how to deep copy config
        this.CHFLogConfigs = CHFLogConfig;
    }
  }*/


}