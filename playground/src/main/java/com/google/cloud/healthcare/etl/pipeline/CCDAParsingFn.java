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
import org.json.JSONObject;
/**
 * The core function of the mapping pipeline. Input is expected to be a parsed message. At this
 * moment, only higher level language (whistle) is supported.
 */
//public class MappingFn<M extends Mappable> extends ErrorEnabledDoFn<M, MappingOutput>
public class CCDAParsingFn extends ErrorEnabledDoFn<KV<String,String>,String> {
  // TODO(b/173141038): refactor the class for capturing performance metrics better.
  // private static final Logger LOGGER = Logger.getLogger(CCDAParsingFn.class.getName());

  // TODO: figure out if this should be static
  //public static final TupleTag<MappingOutput> MAPPING_TAG = new TupleTag<>("mapping");
  public static final TupleTag<String> CCDAPARSING_TAG = new TupleTag<>("parsing");
  
  // The config parameter should be the string representation of the whole mapping config, including
  // harmonization and libraries.
  public CCDAParsingFn() {
  }

  public static CCDAParsingFn of() {
    return new CCDAParsingFn();
  }

/*
  @Override
  public static void setCHFLogConfigs(CHFLogConfigurations CHFLogConfig) {
    CHFLogConfig = CHFLogConfig;
  }
  */

  @Override
  public void process(ProcessContext ctx) throws XmlToJsonException {
    CHFLogWriter logw = new CHFLogWriter();
    KV<String, String> input = ctx.element();
    String retJsonStr = "";

    CHFLogConfigurations elementCHFLog = MethodCHFLogConfig.clone();
    elementCHFLog.setCorrId(extractUID(input));
    elementCHFLog.setInputFiles(getSources(input));
    elementCHFLog.setErrorFiles(getErrorFiles(input));
    elementCHFLog = addErrorFile(input, elementCHFLog);
    elementCHFLog.setDescription();
    elementCHFLog.setStepName(elementCHFLog.getMethodName());

    try {
        JSONObject retJson = new JSONObject(
            new XmlToJsonCDARev2()
                .parse(input.getValue().toString())
        ).put("__data_source__", input.getKey());
        retJsonStr = retJson.toString();
    } catch (Exception e) {
        elementCHFLog.setStatus("failed");
        elementCHFLog.setDescription();
        logw.writeLog(Level.WARNING, "In CCDAParsingFn, found exception: "+e.getMessage()+": "+ExceptionUtils.getStackTrace(e), elementCHFLog);
        throw e;
    } finally {
        elementCHFLog.reset();
    }

    ctx.output(retJsonStr);
  }

  @Override
  protected List<String> getSources(KV<String,String> input) {
    return Collections.singletonList(input.getKey());
  }

  @Override
  protected boolean reportOnly(Throwable e) {
    return true;
  }

  @Override
  public String extractUID(KV<String,String> input) {
    return FileNameUtils.getBaseName(input.getKey());
  }

  @Override
  protected String getData(KV<String,String> input) {
    return input.getValue();
  }

  @Override
  protected String getErrorFilePath() {
    return MethodCHFLogConfig.getErrorPath();
  }

 /*
  public void setErrorFiles(KV<String,String> input) {
       List<String> errorFiles = new ArrayList<String>();
       String file_error_path = "error_path";
        if (MethodCHFLogConfig.getErrorPath() != null) {
            file_error_path = MethodCHFLogConfig.getErrorPath();
        }
        if (getClass().getSimpleName() != null) {
            file_error_path += "/"+getClass().getSimpleName();
        }
        if (input != null && extractUID(input) != null){
            file_error_path += "/"+extractUID(input);
        }
        file_error_path += ".err";
        Collections.addAll(errorFiles, file_error_path);
        MethodCHFLogConfig.setErrorFiles(errorFiles);
  }
  */

    @Override
    public List<String> getErrorFiles(KV<String, String> input) {
        List<String> errorFiles = new ArrayList<String>();
        String fileErrorPath = "";
        if (getClass().getSimpleName() != null) {
            fileErrorPath = getClass().getSimpleName();
        }
        if (input != null && extractUID(input) != null) {
            fileErrorPath += "/" + extractUID(input);
        }
        fileErrorPath += ".err";
        Collections.addAll(errorFiles, fileErrorPath);
        return errorFiles;
    }


  @Override
  protected String getFnClassName() {
    return getClass().getName();
  }
}
