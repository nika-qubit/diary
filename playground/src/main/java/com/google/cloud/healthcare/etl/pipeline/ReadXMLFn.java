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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.beam.repackaged.core.org.apache.commons.compress.utils.FileNameUtils;
import org.apache.beam.sdk.io.FileIO.ReadableFile;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.KV;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * The core function of the mapping pipeline. Input is expected to be a parsed message. At this
 * moment, only higher level language (whistle) is supported.
 */
//public class MappingFn<M extends Mappable> extends ErrorEnabledDoFn<M, MappingOutput>
public class ReadXMLFn extends ErrorEnabledDoFn<ReadableFile,KV<String,String>> {
  // TODO(b/173141038): refactor the class for capturing performance metrics better.
  //private static final Logger LOGGER = Logger.getLogger(ReadXMLFn.class.getName());

  // TODO: figure out if this should be static
  public static final TupleTag<KV<String,String>> EXTRACTINGXML_TAG = new TupleTag<>("extracting");
  //public static CHFLogConfigurations CHFLogConfig;
  
  // The config parameter should be the string representation of the whole mapping config, including
  // harmonization and libraries.
  public ReadXMLFn() {
    //CHFLogConfig = new CHFLogConfigurations();
  }

  public static ReadXMLFn of() {
    return new ReadXMLFn();
  }

/*
  @Override
  public static void setCHFLogConfigs(CHFLogConfigurations CHFLogConfig) {
    CHFLogConfig = CHFLogConfig;
  }
  */

  @Override
  public void process(ProcessContext ctx) throws JSONException, RuntimeException, IOException {
        ReadableFile rf = ctx.element();
        ctx.output(KV.of(rf.getMetadata().resourceId().toString(), rf.readFullyAsUTF8String()));
  }
  
  @Override
  protected List<String> getSources(ReadableFile input) {
    List<String> fileList = new ArrayList<String>();
    String fileName = input.getMetadata().resourceId().toString();
    Collections.addAll(fileList, fileName);
    return fileList;
  }

  @Override
  protected boolean reportOnly(Throwable e) {
    return true;
  }

  @Override
  public String extractUID(ReadableFile input) {
    String fileName = input.getMetadata().resourceId().toString();
    return FileNameUtils.getBaseName(fileName);
  }

  //<TODO>: implement this 
  
  @Override
  protected String getData(ReadableFile input) {
    String fileName = "";
    String fileContent = "";
    try {
        fileName = input.getMetadata().resourceId().toString();
        fileContent = input.readFullyAsUTF8String();
    } catch(Exception e) {
        return ("In ReadXMLFn.getData, found exception: "+ExceptionUtils.getStackTrace(e).toString());
    }
    return "File Name: "+fileName+" Content: "+fileContent;
  }

  @Override
  protected String getErrorFilePath() {
    return MethodCHFLogConfig.getErrorPath();
  }

 /* @Override
  public void setErrorFiles(KV<String,String> input) {
      List<String> errorFiles = new ArrayList<String>();
       String file_error_path = "error_path";
        if (CHFLogConfig.getErrorPath() != null) {
            file_error_path = CHFLogConfig.getErrorPath();
        }
        if (getClass().getSimpleName() != null) {
            file_error_path += "/"+getClass().getSimpleName();
        }
        if (input != null && extractUID(input) != null){
            file_error_path += "/"+extractUID(input);
        }
        file_error_path += ".err";
        Collections.addAll(errorFiles, file_error_path);
        CHFLogConfig.setErrorFiles(errorFiles);
  }*/

 /* @Override
  public List<String> getErrorFiles(KV<String,String> input) {
      if (CHFLogConfig.getErrorFiles() == null) {
        createErrorFilePath(getSources(input).get(0), true);
      }
      return CHFLogConfig.getErrorFiles();
  }*/
  
  @Override
  protected String getFnClassName() {
    return getClass().getName();
  }
}
