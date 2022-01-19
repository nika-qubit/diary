package com.google.cloud.healthcare.etl.util.library;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;

public class CHFLogHandler extends StreamHandler {

    private  String appName = "default_app";
    private  String gcp_project = "default_gcp_project";
    private  String projectName = "default_project";
    private  String methodName = "default_method";
    private  String jobName = "default_job";
    private  String info = "default_info";
    private  String timestamp = "default_timestamp";
    private  String status = "default_status";
    private  String corr_id = "default_corr_id";
    private  List<String> error_files = new ArrayList<String>();
    private  List<String> input_files = new ArrayList<String>();
    private  String error_path = "default_error_path";
    private  String attempt_number = "1";
    private  String retry_number = "0";
    private  String status_code = "0";
    private  String description = "default_description";
    private  String stepName = "default_step_name";
    
    //<TODO> parameterize attempt_number and retry_number
    public void setConfigurations(final String a_name, final String gcp_name, final String p_name) {
        this.appName = a_name;
        this.gcp_project = gcp_name;
        this.projectName = p_name;
        this.attempt_number = "1";
        this.retry_number = "0";
    }

    public void setErrorFiles(final List<String> err_files) {
        if (error_files == null) {
            this.error_files = new ArrayList<String>();
        } else {
            this.error_files = err_files;
        }
    }

    /**
     * @param appName the appName to set
     */
    public void setAppName(final String appName) {
        this.appName = appName;
    }

    public void setJobName(final String jobName) {
        this.jobName = jobName;
    }

    /**
     * @param projectName the projectName to set
     */
    public void setGCPProjectName(final String projectName) {
        this.gcp_project = projectName;
    }

    public void setProjectName(final String workstream_name) {
        this.projectName = workstream_name;
    }

    public void setInputFiles(final List<String> file_list) {
        this.input_files = file_list;
    }

    /**
     * @param methodName the methodName to set
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @param corr_id the corr_id to set
     */
    public void setCorrId(String corr_id) {
        this.corr_id = corr_id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatus() {
        String level = getLevel().toString();
        String status = "";
        if (level != null) {
            switch(level) {
                case "INFO":
                    status = "success";
                    break;
                case "SEVERE":
                    status = "failed";
                    break;
                default:
                    status = level;
            }
        }
        setStatus(status);
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setErrorPath(String path) {
        this.error_path = path;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public void setDescription() {
        if (getMethodName() != null && getStatus() != null) {
            setDescription(getMethodName()+" "+getStatus());
        }
    }

    public String getStatusCode() {
        return this.status_code;
    }

    public void setStatusCode(String status_code) {
        this.status_code = status_code;
    }

    public void setStepName(String step) {
        this.stepName = step;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public String getDescription() {
        return this.description;
    }

    public String getStatus() {
        return this.status;
    }

    public String getStepName() {
        return this.stepName;
    }

    @Override
    public void publish(final LogRecord record) {
        // add own logic to publish

        final Date date = new Date();

        final JSONObject json_log = new JSONObject();
        final JSONObject meta_details = new JSONObject();

        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        final String msg = new String(record.getMessage());

        json_log.put("APP_NAME", this.appName);
        json_log.put("GCP_PROJECT", this.gcp_project);
        json_log.put("PROJECT_NAME", this.projectName);
        json_log.put("TIMESTAMP", formatter.format(date));
        json_log.put("INFO", msg);
        json_log.put("LEVEL", record.getLevel());
        json_log.put("CORR_ID", this.corr_id);
        json_log.put("STATUS_CODE", this.status_code);
        json_log.put("STATUS", this.status);
        json_log.put("DESCRIPTION", this.description);
        
        meta_details.put("METHOD_NAME", this.methodName);
        // meta_details.put("METHOD_NAME", record.getSourceMethodName());
        meta_details.put("CLASS_NAME", record.getSourceClassName());
        meta_details.put("ERROR_FILES", this.error_files);
        meta_details.put("ERROR_PATH", this.error_path);
        meta_details.put("INPUT_FILES", this.input_files);
        meta_details.put("ATTEMPT_NUMBER", this.attempt_number);
        meta_details.put("RETRY_NUMBER", this.retry_number);
        meta_details.put("STEP_NAME", this.stepName);

        json_log.put("META", meta_details);

        record.setMessage(json_log.toString());

        super.publish(record);
    }

    @Override
    public void flush() {
        super.flush();
    }

    @Override
    public void close() throws SecurityException {
        super.close();
    }

    public CHFLogHandler cloneConfigs(CHFLogConfigurations config) {
        //CHFLogHandler handler = new CHFLogHandler();
        if(config != null) {
            this.setAppName(config.getAppName());
            this.setGCPProjectName(config.getGCPProjectName());
            this.setProjectName(config.getWorkstreamName());
            this.setJobName(config.getJobName());
            this.setMethodName(config.getMethodName());
            this.setInfo(config.getInfo());
            this.setTimestamp(config.getTimestamp());
            this.setStatus(config.getStatus());
            this.setCorrId(config.getCorrId());
            this.setErrorFiles(config.getErrorFiles());
            this.setInputFiles(config.getInputFiles());
            this.setErrorPath(config.getErrorPath());
            this.setStatusCode(config.getStatusCode());
            this.setDescription(config.getDescription());
            this.setStepName(config.getStepName());
        }

        return this;
    }

}