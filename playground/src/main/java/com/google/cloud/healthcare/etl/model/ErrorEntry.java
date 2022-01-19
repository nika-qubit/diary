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

package com.google.cloud.healthcare.etl.model;

import com.google.common.collect.Lists;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.values.TupleTag;

/** Includes the information about an error for logging and debugging purposes. */
public class ErrorEntry implements Serializable {
  public static final TupleTag<ErrorEntry> ERROR_ENTRY_TAG = new TupleTag<ErrorEntry>("errors") {};
  public static final Coder<ErrorEntry> CODER = new ErrorEntryCoder();

  private String errorResource;
  private String stackTrace;
  private String errorMessage;
  private String timestamp;
  private String className;
  private String step;
  private List<String> sources;
  private String uid;
  private String errorData;
  private String errorFilePath;
  private List<String> inputList;
  private List<String> errorFileList;
  private String appName;
  private String gcpProject;
  private String projectName;
  private String level;
  private String attemptNumber;
  private String retryNumber;
  private String status;
  private String description;
  private String stepName;
  private String statusCode;


  protected ErrorEntry(
      String errorResource, String errorMessage, String stackTrace, String timestamp) {
    this.errorResource = errorResource;
    this.stackTrace = stackTrace;
    this.errorMessage = errorMessage;
    this.timestamp = timestamp;
  }

  /**
   * Creates an {@link ErrorEntry} from a {@link Throwable}, and records the occuring time of the
   * event based on the default system clock.
   */
  public static ErrorEntry of(Throwable t) {
    return of(t, "", Clock.systemDefaultZone());
  }

  /**
   * Creates an {@link ErrorEntry} from a {@link Throwable}, with the resource that caused the
   * error, and records the occurring time of the event based on the default system clock.
   */
  public static ErrorEntry of(Throwable t, String errorResource) {
    return of(t, errorResource, Clock.systemDefaultZone());
  }

  /**
   * Creates an {@link ErrorEntry} from a {@link Throwable}, and records the occuring time of the
   * event based on the {@code clock}.
   */
  public static ErrorEntry of(Throwable t, String errorResource, Clock clock) {
    StringWriter stringWriter = new StringWriter();
    t.printStackTrace(new PrintWriter(stringWriter));
    return new ErrorEntry(
        errorResource,
        t.getMessage(),
        stringWriter.toString(),
        ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_DATE_TIME));
  }

  public ErrorEntry setStep(String step) {
    this.step = step;
    return this;
  }

  
  public ErrorEntry setClassName(String className) {
    this.className = className;
    return this;
  }
  

  public ErrorEntry setAppName(String appName) {
    this.appName = appName;
    return this;
  }

  public ErrorEntry setGcpProject(String gcpProject) {
    this.gcpProject = gcpProject;
    return this;
  }

  public ErrorEntry setProjectName(String projectName) {
    this.projectName = projectName;
    return this;
  }

  public ErrorEntry setLevel(String level) {
    this.level = level;
    return this;
  }

  public ErrorEntry setAttemptNumber(String attemptNumber) {
    this.attemptNumber = attemptNumber;
    return this;
  }

  public ErrorEntry setRetryNumber(String retryNumber) {
    this.retryNumber = retryNumber;
    return this;
  }

  public ErrorEntry setSources(List<String> sources) {
    this.sources = sources;
    return this;
  }

   public ErrorEntry setUid(String uid) {
      this.uid = uid;
      return this;
  }

  public ErrorEntry setErrorData(String errorData) {
      this.errorData = errorData;
      return this;
  }

  public ErrorEntry setErrorFilePath(String errorFilePath) {
      this.errorFilePath = errorFilePath;
      return this;
  }

  public ErrorEntry setErrorFileList(List<String> errorFileList) {
    this.errorFileList = errorFileList;
    return this;
  }

  public ErrorEntry setStatus(String status) {
      this.status = status;
      return this;
  }

  public ErrorEntry setStatusCode(String statusCode) {
      this.statusCode = statusCode;
      return this;
  }

  public ErrorEntry setDescription(String description) {
      this.description = description;
      return this;
  }

  public ErrorEntry setStepName(String stepName) {
      this.stepName = stepName;
      return this;
  }


  public String getErrorResource() {
    return errorResource;
  }

  public List<String> getSources() {
    if (sources == null) {
      return Lists.newArrayList();
    }
    return sources;
  }

  public String getStep() {
    return step;
  }

  public String getStackTrace() {
    return stackTrace;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public String getUid() {
      return uid;
  }

  public String getErrorData() {
      return errorData;
  }
  
  public String getAttemptNumber() {
      return attemptNumber;
  }

  public String getRetryNumber() {
      return retryNumber;
  }

  public String getErrorFilePath() {
      return errorFilePath;
  }

  public List<String> getErrorFiles() {
    if (errorFileList == null) {
      return Lists.newArrayList();
    }
    return errorFileList;
  }

  public String getClassName() {
      return className;
  }

  public String getAppName() {
      return appName;
  }

  public String getGcpProject() {
      return gcpProject;
  }

  public String getProjectName() {
      return projectName;
  }

  public String getLevel() {
      return level;
  }

  public String getStatus() {
      return status;
  }

  public String getStatusCode() {
      return statusCode;
  }

  public String getDescription() {
      return description;
  }

  public String getStepName() {
      return stepName;
  }


  public boolean equals(Object other) {
    if (!(other instanceof ErrorEntry)) {
      return false;
    }
    if (!(Objects.equals(this.errorResource, ((ErrorEntry) other).errorResource))) {
      return false;
    }
    if (!(Objects.equals(this.stackTrace, ((ErrorEntry) other).stackTrace))) {
      return false;
    }
    if (!(Objects.equals(this.errorMessage, ((ErrorEntry) other).errorMessage))) {
      return false;
    }
    if (!(Objects.equals(this.timestamp, ((ErrorEntry) other).timestamp))) {
      return false;
    }
    if (!(Objects.equals(this.step, ((ErrorEntry) other).step))) {
      return false;
    }
    if (!(Objects.equals(this.sources, ((ErrorEntry) other).sources))) {
      return false;
    }
    if (!(Objects.equals(this.status, ((ErrorEntry) other).status))) {
      return false;
    }
    if (!(Objects.equals(this.stepName, ((ErrorEntry) other).stepName))) {
      return false;
    }
    if (!(Objects.equals(this.description, ((ErrorEntry) other).description))) {
      return false;
    }
    
    return true;
  }
}
