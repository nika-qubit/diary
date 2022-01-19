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

import com.google.cloud.healthcare.etl.model.mapping.Mappable;
import com.google.cloud.healthcare.etl.model.mapping.MappedFhirMessageWithSourceTime;
import com.google.cloud.healthcare.etl.model.mapping.MappingOutput;
import com.google.cloud.healthcare.etl.provider.mapping.MappingConfigProvider;
import com.google.cloud.healthcare.etl.provider.mapping.MappingConfigProviderFactory;
import com.google.cloud.healthcare.etl.util.library.TransformWrapper;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.apache.beam.repackaged.core.org.apache.commons.compress.utils.FileNameUtils;
import org.apache.beam.sdk.metrics.Distribution;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.options.ValueProvider;
import org.apache.beam.sdk.options.ValueProvider.StaticValueProvider;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.Strings;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.cloud.healthcare.etl.util.library.CHFLogConfigurations;
import com.google.cloud.healthcare.etl.util.library.CHFLogWriter;

import org.apache.beam.sdk.values.KV;

import com.google.cloud.healthcare.etl.xmltojson.XmlToJsonCDARev2;
import com.google.cloud.healthcare.etl.xmltojson.XmlToJsonException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The core function of the mapping pipeline. Input is expected to be a parsed
 * message. At this moment, only higher level language (whistle) is supported.
 */
// public class MappingFn<M extends Mappable> extends ErrorEnabledDoFn<M,
// MappingOutput>
public class GetNotificationFn extends ErrorEnabledDoFn<String, String> {
    // TODO(b/173141038): refactor the class for capturing performance metrics
    // better.

    // TODO: figure out if this should be static
    public static final TupleTag<String> NOTIFICATION_TAG = new TupleTag<>("notification");
    //public static CHFLogConfigurations CHFLogConfig;
    public String inputBkt;
    public String outPath;
    public CHFLogConfigurations processLogConfig;

    // The config parameter should be the string representation of the whole mapping
    // config, including
    // harmonization and libraries.
    public GetNotificationFn(String inputBucket) {
        this.inputBkt = inputBucket;
    }

    public static GetNotificationFn of(String inputBucket) {
        return new GetNotificationFn(inputBucket);
    }

    /*@Override
    public static void setCHFLogConfigs(CHFLogConfigurations CHFLogConfig) {
        this.CHFLogConfig = CHFLogConfig;
    }*/

    @Override
    public void process(ProcessContext ctx) throws JSONException, RuntimeException {
        String outPath = "";
        
        try {
            JSONObject jsonObject = new JSONObject(ctx.element());
            outPath = this.inputBkt + jsonObject.getString("name");
        } catch (Exception e) {
            //CHFLogWriter.writeLog(Level.SEVERE, "Notification contents not expected json value: " + ExceptionUtils.getStackTrace(e), MethodCHFLogConfig);
            throw e;
        }
        
        ctx.output(outPath);
    }

    
    @Override
    protected List<String> getSources(String input) {
        CHFLogWriter logw = new CHFLogWriter();
        //if (this.outPath != null) {
        //    CHFLogWriter.writeLog(Level.INFO, "in getSources, returning value of outpath", this.MethodCHFLogConfig);
        //    return Collections.singletonList(this.outPath);
        //}
        /*
        if (MethodCHFLogConfig.getInputFiles() != null && MethodCHFLogConfig.getInputFiles().size() > 0) {
            CHFLogWriter.writeLog(Level.INFO, "in getSources, returning value of method log config input files", this.MethodCHFLogConfig);
            return MethodCHFLogConfig.getInputFiles();
        }
        */
        String outPath = "";
        try {
            JSONObject jsonObject = new JSONObject(input);
            outPath = this.inputBkt + jsonObject.getString("name");
        } catch (Exception e) {
            logw.writeLog(Level.WARNING, "Notification contents not expected json value: " + ExceptionUtils.getStackTrace(e), MethodCHFLogConfig);
        }
        List<String> inputFiles = new ArrayList<String>();
        Collections.addAll(inputFiles, outPath);
        return inputFiles;
    }
    

    @Override
    protected boolean reportOnly(Throwable e) {
        return true;
    }

    @Override
    public String extractUID(String input) {
        String uid = "default_uid";
        try {
            JSONObject jsonObject = new JSONObject(input);
            uid = jsonObject.getString("name");
            uid = FileNameUtils.getBaseName(uid);
        } catch (Exception e) {
            // <TODO> write out to log
        }
        return uid;
    }

    @Override
    protected String getData(String input) {
        return input;
    }

    @Override
    protected String getErrorFilePath() {
        return MethodCHFLogConfig.getErrorPath();
    }

    
    public void setErrorFiles(String input) {
        List<String> errorFiles = new ArrayList<String>();
        String file_error_path = "error_path";
        if (MethodCHFLogConfig.getErrorPath() != null) {
            file_error_path = MethodCHFLogConfig.getErrorPath();
        }
        if (getClass().getSimpleName() != null) {
            file_error_path += "/" + getClass().getSimpleName();
        }
        if (input != null && extractUID(input) != null) {
            file_error_path += "/" + extractUID(input);
        }
        file_error_path += ".err";
        Collections.addAll(errorFiles, file_error_path);
        MethodCHFLogConfig.setErrorFiles(errorFiles);
    }

    /*
     * @Override public List<String> getErrorFiles(KV<String,String> input) { if
     * (CHFLogConfig.getErrorFiles() == null) {
     * createErrorFilePath(getSources(input).get(0), true); } return
     * CHFLogConfig.getErrorFiles(); }
     */

  @Override
  protected String getFnClassName() {
    return getClass().getName();
  }

  @Override
  protected CHFLogConfigurations getProcessLogConfig() {
      return new CHFLogConfigurations();
  }

  void setProcessLogConfigurations(CHFLogConfigurations processLogConfig) {
    this.processLogConfig = processLogConfig;
  }
}
