package com.google.cloud.healthcare.etl.util.library;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

public class CHFLogFormatter extends Formatter {

    private static String app_name = "default_app";
    private static String project_name = "default_project";
    private static String job_name = "default_job";
    private static String info = "default_info";
    private static String timestamp = "default_timestamp";
    private static String status = "default_status";
    private static String corr_id = "default_corr_id";
    private static String[] file_list;

    @Override
    public String format(LogRecord record) {
        
        Date date = new Date();

        JSONObject json_log = new JSONObject();
        JSONObject meta_datails = new JSONObject();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        json_log.put("APP_NAME", app_name);
        json_log.put("PROJECT_NAME", project_name);
        json_log.put("TIMESTAMP", formatter.format(date));
        json_log.put("INFO", record.getMessage());
        

        String[] files;
        files = new String[]{"filea", "fileb"};              
        
        meta_datails.put("METHOD_NAME", record.getSourceMethodName());
        meta_datails.put("CLASS_NAME", record.getSourceClassName());
        meta_datails.put("SOURCE_FILES", file_list);

        json_log.put("META", meta_datails);

        return json_log.toString();
    }

}