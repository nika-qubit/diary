package com.google.cloud.healthcare.etl.util.library;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

public class CHFLogWriter implements Serializable {

    public void writeLog(Level level, String message, CHFLogConfigurations CHFLogConfig) {
        Logger LOGGER = Logger.getLogger(CHFLogWriter.class.getName());
        CHFLogHandler contextLog = new CHFLogHandler();
        
        contextLog.cloneConfigs(CHFLogConfig);
        Handler[] handlers = LOGGER.getHandlers();
        for (int i =0; i < handlers.length; i++) {
            LOGGER.removeHandler(handlers[i]);
        }
        
        LOGGER.addHandler(contextLog);
        LOGGER.log(level, message);
        LOGGER.removeHandler(contextLog);
    }   
}