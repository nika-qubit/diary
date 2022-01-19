// Copyright 2020 Google LLC.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.cloud.healthcare.etl.pipeline;

import static com.google.cloud.healthcare.etl.model.ErrorEntry.ERROR_ENTRY_TAG;

import com.google.cloud.healthcare.etl.model.ErrorEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.cloud.healthcare.etl.util.library.CHFLogHandler;
import com.google.cloud.healthcare.etl.util.library.CHFLogWriter;
import com.google.cloud.healthcare.etl.xmltojson.XmlToJsonException;
import com.google.cloud.healthcare.etl.util.library.CHFLogConfigurations;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a base {@link DoFn} class with error reporting enabled automatically,
 * classes which inherit from this class can choose what kind of errors are
 * recoverable, and thus not crashing the whole pipeline. All exceptions will be
 * logged to a separate dataset.
 */
public abstract class ErrorEnabledDoFn<Input, Output> extends DoFn<Input, Output> {

    public CHFLogConfigurations MethodCHFLogConfig;

    private static final Logger LOGGER = Logger.getLogger(ErrorEnabledDoFn.class.getName());



    // this output function uses elementCHFLog
    @ProcessElement
    public void output(ProcessContext ctx) throws Exception, XmlToJsonException {
        Input input = ctx.element();
        List<String> inputFiles = getSources(input);
        CHFLogWriter logw = new CHFLogWriter();

        CHFLogConfigurations elementCHFLog = MethodCHFLogConfig.clone();
        
        elementCHFLog.setCorrId(extractUID(input));

        if (inputFiles != null && inputFiles.size() > 0) {
            
            elementCHFLog.setInputFiles(inputFiles);
        }

        elementCHFLog.setErrorFiles(getErrorFiles(input));
        elementCHFLog = addErrorFile(input, elementCHFLog);
        elementCHFLog.setStatus("started");
        elementCHFLog.setDescription();
        elementCHFLog.setStepName(elementCHFLog.getMethodName());
        try {
            logw.writeLog(Level.INFO, "Starting " + elementCHFLog.getMethodName(), elementCHFLog);
            process(ctx);
            elementCHFLog.setStatus("completed");
            elementCHFLog.setDescription();
            logw.writeLog(Level.INFO, "Successfully Completed " + elementCHFLog.getMethodName(),
                    elementCHFLog);
                    
            elementCHFLog.reset();

        } catch (Exception e) {
            elementCHFLog = addErrorFile(input, elementCHFLog);
            elementCHFLog.setStatus("failed");
            ErrorEntry error = ErrorEntry.of(e, getErrorResource(input)).setStep(getClass().getSimpleName())
                    .setSources(getSources(input)).setUid(extractUID(input)).setErrorData(getData(input))
                    .setErrorFilePath(getErrorFilePath()).setErrorFileList(elementCHFLog.getErrorFiles())
                    .setClassName(getFnClassName()) // <TODO> See if there's a way to get subclass name instead
                    .setAppName(elementCHFLog.getAppName()).setGcpProject(elementCHFLog.getGCPProjectName())
                    .setProjectName(elementCHFLog.getWorkstreamName()).setLevel(Level.SEVERE.toString())
                    .setAttemptNumber("1") // <TODO> Implement this with retry logic
                    .setRetryNumber("0") // <TODO> Implement this with retry logic
                    .setDescription(elementCHFLog.getDescription()).setStatus(elementCHFLog.getStatus())
                    .setStepName(elementCHFLog.getStepName());

            logw.writeLog(Level.SEVERE, ExceptionUtils.getStackTrace(e), elementCHFLog);
            ctx.output(ERROR_ENTRY_TAG, error);

            elementCHFLog.reset();

            // Re-throw if it is not recoverable.
            if (!reportOnly(e)) {
                throw e;
            }
        }
    }



    // this output function uses MethodCHFLogConfig
    /*
    @ProcessElement
    public void output(ProcessContext ctx) throws Exception, XmlToJsonException {
        // writeLog(Level.INFO, "start method", CHFLogConfig);
        Input input = ctx.element();
        //List<String> inputFiles = new ArrayList<String>();
        //List<String> inputSources = getSources(input);
        List<String> inputFiles = getSources(input);
        
        MethodCHFLogConfig.setCorrId(extractUID(input));

        if (inputFiles != null && inputFiles.size() > 0) {
            CHFLogWriter.writeLog(Level.INFO,
                "before process, input files is not empty " + MethodCHFLogConfig.getMethodName(),
                    MethodCHFLogConfig);
            //Collections.addAll(inputFiles, inputSources.get(0));
            
            MethodCHFLogConfig.setInputFiles(inputFiles);
            CHFLogWriter.writeLog(Level.INFO,
                "before process, after set input files " + MethodCHFLogConfig.getMethodName(), MethodCHFLogConfig);
        }

        MethodCHFLogConfig.setErrorFiles(getErrorFiles(input));
        MethodCHFLogConfig = addErrorFile(input, MethodCHFLogConfig);
        MethodCHFLogConfig.setStatus("started");
        MethodCHFLogConfig.setDescription();
        MethodCHFLogConfig.setStepName(MethodCHFLogConfig.getMethodName());
        try {
            CHFLogWriter.writeLog(Level.INFO, "Starting " + MethodCHFLogConfig.getMethodName(), MethodCHFLogConfig);
            process(ctx);
            MethodCHFLogConfig.setStatus("completed");
            MethodCHFLogConfig.setDescription();
            CHFLogWriter.writeLog(Level.INFO, "Successfully Completed " + MethodCHFLogConfig.getMethodName(),
                    MethodCHFLogConfig);
        } catch (Exception e) {
            MethodCHFLogConfig = addErrorFile(input, MethodCHFLogConfig);
            MethodCHFLogConfig.setStatus("failed");
            ErrorEntry error = ErrorEntry.of(e, getErrorResource(input)).setStep(getClass().getSimpleName())
                    .setSources(getSources(input)).setUid(extractUID(input)).setErrorData(getData(input))
                    .setErrorFilePath(getErrorFilePath()).setErrorFileList(MethodCHFLogConfig.getErrorFiles())
                    .setClassName(getFnClassName()) // <TODO> See if there's a way to get subclass name instead
                    .setAppName(MethodCHFLogConfig.getAppName()).setGcpProject(MethodCHFLogConfig.getGCPProjectName())
                    .setProjectName(MethodCHFLogConfig.getWorkstreamName()).setLevel(Level.SEVERE.toString())
                    .setAttemptNumber("1") // <TODO> Implement this with retry logic
                    .setRetryNumber("0") // <TODO> Implement this with retry logic
                    .setDescription(MethodCHFLogConfig.getDescription()).setStatus(MethodCHFLogConfig.getStatus())
                    .setStepName(MethodCHFLogConfig.getStepName());

            /*
             * if (elementCHFLogConfig.getErrorFiles() != null) { writeLog(Level.INFO,
             * "confirmed there were error files", elementCHFLogConfig); if
             * (elementCHFLogConfig.getErrorFiles().size() > 0) { writeLog(Level.INFO,
             * "confirmation: "+elementCHFLogConfig.getErrorFiles().get(0),
             * elementCHFLogConfig); } }
             */
           
           /*  CHFLogWriter.writeLog(Level.SEVERE, e.getStackTrace().toString(), MethodCHFLogConfig);
            ctx.output(ERROR_ENTRY_TAG, error);

            MethodCHFLogConfig.reset();

            // Re-throw if it is not recoverable.
            if (!reportOnly(e)) {
                throw e;
            }
        }
    }
*/

    /*
     * @ProcessElement public void output(ProcessContext ctx) throws Exception,
     * XmlToJsonException { //writeLog(Level.INFO, "start method", CHFLogConfig);
     * Input input = ctx.element(); CHFLogConfigurations elementCHFLogConfig = new
     * CHFLogConfigurations(); if (MethodCHFLogConfig != null) { elementCHFLogConfig
     * = MethodCHFLogConfig.clone(); } List<String> inputFiles = new
     * ArrayList<String>(); List<String> inputSources = getSources(input); if
     * (inputSources != null && inputSources.size() > 0) {
     * Collections.addAll(inputFiles, inputSources.get(0)); }
     * elementCHFLogConfig.setInputFiles(inputFiles);
     * elementCHFLogConfig.setCorrId(extractUID(input));
     * elementCHFLogConfig.setErrorFiles(getErrorFiles(input)); elementCHFLogConfig
     * = addErrorFile(input, elementCHFLogConfig);
     * 
     * try { writeLog(Level.INFO, "Starting "+elementCHFLogConfig.getMethodName(),
     * elementCHFLogConfig); process(ctx); writeLog(Level.INFO,
     * "Succesfully Completed "+elementCHFLogConfig.getMethodName(),
     * elementCHFLogConfig); } catch (Exception e) { ErrorEntry error =
     * ErrorEntry.of(e, getErrorResource(input))
     * .setStep(getClass().getSimpleName()) .setSources(getSources(input))
     * .setUid(extractUID(input)) .setErrorData(getData(input))
     * .setErrorFilePath(getErrorFilePath())
     * .setErrorFileList(elementCHFLogConfig.getErrorFiles())
     * .setClassName(getClass().getName()) // <TODO> See if there's a way to get
     * subclass name instead .setAppName(elementCHFLogConfig.getAppName())
     * .setGcpProject(elementCHFLogConfig.getGCPProjectName())
     * .setProjectName(elementCHFLogConfig.getWorkstreamName())
     * .setLevel(Level.SEVERE.toString()) .setAttemptNumber("1")
     * .setRetryNumber("0");
     * 
     * /*if (elementCHFLogConfig.getErrorFiles() != null) { writeLog(Level.INFO,
     * "confirmed there were error files", elementCHFLogConfig); if
     * (elementCHFLogConfig.getErrorFiles().size() > 0) { writeLog(Level.INFO,
     * "confirmation: "+elementCHFLogConfig.getErrorFiles().get(0),
     * elementCHFLogConfig); } }
     */
    /*
     * writeLog(Level.SEVERE, e.getStackTrace().toString(), elementCHFLogConfig);
     * ctx.output(ERROR_ENTRY_TAG, error); // Re-throw if it is not recoverable. if
     * (!reportOnly(e)) { throw e; } } }
     */

    /**
     * The main processing logic, the sub-class is expected to implement this method
     * and output the results.
     */
    public abstract void process(ProcessContext ctx) throws Exception;

    /**
     * Defines how to extract the error resource for the ErrorEntry from the input.
     * The default populate is empty, override this method to populate the
     * ErrorEntry errorResource.
     */
    protected String getErrorResource(Input input) {
        return "";
    }

    /**
     * Defines how to extract the error source for the ErrorEntry from the input.
     * The default populate is empty, override this method to populate the
     * ErrorEntry sources.
     */
    protected List<String> getSources(Input input) {
        return Collections.emptyList();
    }

    public void setCHFLogConfigs(CHFLogConfigurations CHFLogConfig) {
        this.MethodCHFLogConfig = CHFLogConfig.clone();
    }

    public CHFLogConfigurations getCHFLogConfigs() {
        return this.MethodCHFLogConfig;
    }

    /**
     * Check whether a {@link Throwable} is recoverable, i.e. the pipeline needs to
     * report the error only, rather than crashing. Sub-classes can override this
     * method if they want to report different errors.
     */
    protected boolean reportOnly(Throwable e) {
        return e.getClass() == RuntimeException.class;
    }

    /*
     * public static final void writeLog(Level level, String message,
     * CHFLogConfigurations CHFLogConfig) { CHFLogHandler copyHandler = new
     * CHFLogHandler(); if (CHFLogConfig == null) { LOGGER.log(Level.SEVERE,
     * "empty config!"); } //contextLog.setConfigurations("CHF_BATCH", "EPIC",
     * "edp-dev-eds"); //contextLog.setConfigurations(CHFLogConfig.getAppName(),
     * CHFLogConfig.getWorkstreamName(), CHFLogConfig.getGCPProjectName());
     * //contextLog.setInputList(CHFLogConfig.getInputFiles());
     * //contextLog.setErrorList(CHFLogConfig.getErrorFiles()); CHFLogHandler
     * contextLog = copyHandler.cloneConfigs(CHFLogConfig);
     * LOGGER.addHandler(contextLog); LOGGER.log(level, message);
     * LOGGER.removeHandler(contextLog); }
     */

    /**
     * Defines how to get the uid from an Input. The default populate is empty,
     * override this method to populate the ErrorEntry uid.
     */
    protected String extractUID(Input input) {
        return "";
    }

    /**
     * Defines how to get the errorData from an Input. The default populate is
     * empty, override this method to populate the ErrorEntry errorData.
     */
    protected String getData(Input input) {
        return "";
    }

    /**
     * Defines how to get the errorFilePath from an Input. The default populate is
     * empty, override this method to populate the ErrorEntry errorFilePath.
     */
    protected String getErrorFilePath() {
        return "";
    }

    /**
     * Defines how to get the errorFilePath from an Input. The default populate is
     * empty, override this method to populate the ErrorEntry errorFilePath.
     */
    protected List<String> getErrorFiles(Input input) {
        return Collections.emptyList();
    }

    protected CHFLogConfigurations addErrorFile(Input input, CHFLogConfigurations elementCHFLogConfig) {
        // build file error path and add it to the config log
        List<String> sourceFiles = getSources(input);
        if (sourceFiles != null && sourceFiles.size() > 0) {
            elementCHFLogConfig.createErrorFilePath(sourceFiles.get(0), true);
        }
        return elementCHFLogConfig;
    }

    protected String getFnClassName() {
        return getClass().getName();
    }

    protected CHFLogConfigurations getProcessLogConfig() {
        return new CHFLogConfigurations();
    }
}
