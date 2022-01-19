package com.google.cloud.healthcare.etl.util.library;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import org.apache.beam.repackaged.core.org.apache.commons.compress.utils.FileNameUtils;
import java.util.UUID;
/*
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;
*/

public class CHFLogConfigurations implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(CHFLogConfigurations.class.getName());

    private String appName = "default_app";
    private String gcpProjectName = "default_gcp_project";
    private String workstreamName = "default_project";
    private String jobName = "default_job";
    private String methodName = "default_method";
    private String info = "default_info";
    private String timestamp = "default_timestamp";
    private String status = "default_status";
    private String corrId = "default_corr_id";
    private String errorPath = "default_error_path";
    private List<String> errorFiles = new ArrayList<String>();
    private List<String> inputFiles = new ArrayList<String>();
    private String attemptNumber = "1";
    private String retryNumber = "0";
    private String statusCode = "0";
    private String stepName = "default_step_name";
    private String description = "default_description";

    // AppName
    public void setAppName(String value) {
        this.appName = value;
    }
    
    public String getAppName() {  
        return this.appName;
    }


    // AttemptNumber
    public String getAttemptNumber() {
        return this.attemptNumber;
    }
    
    public void setAttemptNumber(String attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    // StatusCode
    public String getStatusCode() {
        return this.statusCode;
    }
    
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

     // RetryNumber
    public String getRetryNumber() {
        return this.retryNumber;
    }

    public void setRetryNumber(String retryNumber) {
        this.retryNumber = retryNumber;
    }

    // WorkstreamName
    public void setWorkstreamName(String value) {
        this.workstreamName = value;
    }

    public String getWorkstreamName() {
        return this.workstreamName;
    }

    // GCPProjectName
    public void setGCPProjectName(String value) {
        this.gcpProjectName = value;
    }

    public String getGCPProjectName() {
        return this.gcpProjectName;
    }

    // jobName
    public void setJobName(String value) {
        this.jobName = value;
    }

    public String getJobName() {
        return this.jobName;
    }

    // errorPath
    public void setErrorPath(String value) {
        this.errorPath = value;
    }

    public String getErrorPath() {
        return this.errorPath;
    }

    // errorFiles
    public void setErrorFiles(List<String> value) {
        if (value != null) {
            this.errorFiles = value;
        } else {
            this.errorFiles = new ArrayList<String>();
        }
    }

    public List<String> getErrorFiles() {
        if (this.errorFiles != null) {
            return this.errorFiles;
        } else {
            return new ArrayList<String>();
        }
    }

    // inputFiles
    public void setInputFiles(List<String> value) {
        if (value != null) {
            this.inputFiles = value;
        } else {
            this.inputFiles = new ArrayList<String>();
        }
    }

    public List<String> getInputFiles() {
        if (this.inputFiles != null) {
            return this.inputFiles;
        } else {
            return new ArrayList<String>();
        }
    }

    // methodName
    public void setMethodName(String value) {
        this.methodName = value;
    }

    public String getMethodName() {
        return this.methodName;
    }

    // info
    public void setInfo(String value) {
        this.info = value;
    }

    public String getInfo() {
        return this.info;
    }

    // timestamp
    public void setTimestamp(String value) {
        this.timestamp = value;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    // status
    public void setStatus(String value) {
        this.status = value;
    }

    public String getStatus() {
        return this.status;
    }

    // corrId
    public void setCorrId(String value) {
        this.corrId = value;
    }

    public String getCorrId() {
        return this.corrId;
    }

    // step name
    public void setStepName(String value) {
        this.stepName = value;
    }

    public String getStepName() {
        return this.stepName;
    }

    // description
    public void setDescription(String value) {
        this.description = value;
    }

    public void setDescription() {
        String methodName = getMethodName();
        String status = getStatus();
        if(methodName != null && status != null) {
            setDescription(methodName +" "+ status);
        } else {
            setDescription("default_description");
        }
    }

    public String getDescription() {
        return this.description;
    }

    public CHFLogConfigurations clone() {
        CHFLogConfigurations configDeepCopy = new CHFLogConfigurations();
        configDeepCopy.setAppName(this.getAppName());
        configDeepCopy.setGCPProjectName(this.getGCPProjectName());
        configDeepCopy.setWorkstreamName(this.getWorkstreamName());
        configDeepCopy.setJobName(this.getJobName());
        configDeepCopy.setMethodName(this.getMethodName());
        configDeepCopy.setInfo(this.getInfo());
        configDeepCopy.setTimestamp(this.getTimestamp());
        configDeepCopy.setStatus(this.getStatus());
        configDeepCopy.setCorrId(this.getCorrId());
        configDeepCopy.setErrorFiles(this.getErrorFiles());
        configDeepCopy.setInputFiles(this.getInputFiles());
        configDeepCopy.setErrorPath(this.getErrorPath());
        configDeepCopy.setAttemptNumber(this.getAttemptNumber());
        configDeepCopy.setRetryNumber(this.getRetryNumber());
        configDeepCopy.setStatusCode(this.getStatusCode());
        configDeepCopy.setStepName(this.getStepName());
        configDeepCopy.setDescription(this.getDescription());

        /*LOGGER.log(Level.INFO,
                "clone complete. copy: " + configDeepCopy.toString() + " and original: " + this.toString());*/

        return configDeepCopy;
    }

    public CHFLogConfigurations reset() {
        String defaultString = "";
        List<String> defaultStringList = new ArrayList<String>();
        //this.setAppName(defaultString);
        //this.setGCPProjectName(defaultString);
        //this.setWorkstreamName(defaultString);
        //this.setJobName(defaultString);
        this.setMethodName(defaultString);
        this.setInfo(defaultString);
        this.setTimestamp(defaultString);
        this.setStatus(defaultString);
        this.setCorrId(defaultString);
        this.setErrorFiles(defaultStringList);
        this.setInputFiles(defaultStringList);
        this.setErrorPath(defaultString);
        this.setAttemptNumber(defaultString);
        this.setRetryNumber(defaultString);
        this.setStatusCode(defaultString);
        this.setStepName(defaultString);
        this.setDescription(defaultString);

        return this;
    }

    public String buildErrorFilePath(String fileName, String filePath, String dtFormatted, String stepName) {
        String uuid = UUID.randomUUID().toString();
        SimpleDateFormat date_formatter = new SimpleDateFormat("yyyyMMdd");
		String dateStr = date_formatter.format(new Date());
        return dateStr + "/" + stepName + "/err_" + dtFormatted + "_" + fileName +"_" +uuid + ".json";
    }

    /*public String createErrorFilePath(String filename, CHFLogConfigurations CHFLogConfig) {
        // import org.apache.commons.io.FilenameUtils;
        String baseFilename = FileNameUtils.getBaseName(filename);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
		
		String filePath = CHFLogConfig.getErrorPath();
		String stepName = CHFLogConfig.getMethodName();
		String dtFormatted = formatter.format(new Date());
		
		return buildErrorFilePath(baseFilename, filePath, dtFormatted, stepName);
    }
    */
    public String createErrorFilePath(String filename) {
        // import org.apache.commons.io.FilenameUtils;
        String baseFilename = FileNameUtils.getBaseName(filename);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
		
		String filePath = this.getErrorPath();
		String stepName = this.getMethodName();
		String dtFormatted = formatter.format(new Date());
		
		return buildErrorFilePath(baseFilename, filePath, dtFormatted, stepName);
    }

    public String createErrorFilePath(String filename, Boolean addConfig) {
        // import org.apache.commons.io.FilenameUtils;
        String errorFilePath = createErrorFilePath(filename);
        if(addConfig) {
            List<String> errorFilePaths = new ArrayList<String>();
            Collections.addAll(errorFilePaths, errorFilePath);
            this.setErrorFiles(errorFilePaths);
        }
        return errorFilePath;
    }
}