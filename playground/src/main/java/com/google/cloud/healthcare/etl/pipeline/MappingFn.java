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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.apache.beam.sdk.metrics.Distribution;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.options.ValueProvider;
import org.apache.beam.sdk.options.ValueProvider.StaticValueProvider;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.Strings;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.beam.repackaged.core.org.apache.commons.compress.utils.FileNameUtils;


/**
 * The core function of the mapping pipeline. Input is expected to be a parsed message. At this
 * moment, only higher level language (whistle) is supported.
 */
public class MappingFn<M extends Mappable> extends ErrorEnabledDoFn<M, MappingOutput> {
  // TODO(b/173141038): refactor the class for capturing performance metrics better.
  private static final Logger LOGGER = LoggerFactory.getLogger(MappingFn.class);
  public static final TupleTag<MappingOutput> MAPPING_TAG = new TupleTag<>("mapping");
  protected final Distribution transformMetrics =
      Metrics.distribution(MappingFn.class, "Transform");

  // Ensure the initialization only happens once. Ideally this should be handled by the library.
  private static final AtomicBoolean initialized = new AtomicBoolean();

  private final ValueProvider<String> mappingPath;
  private final ValueProvider<String> mappings;
  private final boolean enablePerformanceMetrics;

  protected TransformWrapper engine;

  // The config parameter should be the string representation of the whole mapping config, including
  // harmonization and libraries.
  protected MappingFn(
      ValueProvider<String> mappingPath,
      ValueProvider<String> mappings,
      Boolean enablePerformanceMetrics) {
    this.mappingPath = mappingPath;
    this.mappings = mappings;
    this.enablePerformanceMetrics = enablePerformanceMetrics;
  }

  public static MappingFn of(ValueProvider<String> mappingPath, Boolean enablePerformanceMetrics) {
    return new MappingFn(mappingPath, StaticValueProvider.of(""), enablePerformanceMetrics);
  }

  public static MappingFn of(String mappingPath, Boolean enablePerformanceMetrics) {
    return of(StaticValueProvider.of(mappingPath), enablePerformanceMetrics);
  }

  public static MappingFn of(
      ValueProvider<String> mappingPath,
      ValueProvider<String> mappings,
      Boolean enablePerformanceMetrics) {
    return new MappingFn(mappingPath, mappings, enablePerformanceMetrics);
  }

  public static MappingFn of(
      String mappings, String mappingPath, Boolean enablePerformanceMetrics) {
    return of(
        StaticValueProvider.of(mappingPath),
        StaticValueProvider.of(mappings),
        enablePerformanceMetrics);
  }

  @Setup
  public void initialize() {
    // Make sure the mapping configuration is only initialized once.
    synchronized (initialized) {
      if (!initialized.get()) {
        LOGGER.info("Initializing the mapping configurations.");
        engine = TransformWrapper.getInstance();
        try {
          // Mapping configurations are loaded from the `mappingPath` only if `mappings` is absent.
          String mappingsToUse = mappings.get();
          if (Strings.isNullOrEmpty(mappingsToUse)) {
            mappingsToUse = loadMapping(mappingPath.get());
          }
          engine.initializeWhistler(mappingsToUse);
        } catch (RuntimeException e) {
          LOGGER.error("Unable to initialize mapping configurations.", e);
          throw e; // Fail fast.
        }
        initialized.set(true);
      }
    }
  }

  private static String loadMapping(String mappingPath) {
    MappingConfigProvider provider = MappingConfigProviderFactory.createProvider(mappingPath);
    try {
      return new String(provider.getMappingConfig(true /* force */), StandardCharsets.UTF_8);
    } catch (IOException | NullPointerException e) {
      throw new RuntimeException("Unable to load mapping configurations.", e);
    }
  }

  // Runs a lambda and collects the metrics.
  protected static <T> T runAndReportMetrics(Distribution metrics, Supplier<T> supplier) {
    Instant start = Instant.now();
    T result = supplier.get();
    metrics.update(Instant.now().toEpochMilli() - start.toEpochMilli());
    return result;
  }

  @Override
  public void process(ProcessContext ctx) {
    M input = ctx.element();
    ctx.output(
        runAndReportMetrics(
            transformMetrics,
            () -> {
              String transformedData = engine.transform(input.getData());
              return (enablePerformanceMetrics)
                  ? new MappedFhirMessageWithSourceTime(
                      transformedData, input.getCreateTime().get())
                  : new MappedFhirMessageWithSourceTime(transformedData);
            }));
  }

  @Override
  protected List<String> getSources(M input) {
    String data_source = input.getId();
    try {
        data_source = new JSONObject(input.getData()).getString("__data_source__");
    } catch (Exception e) {
        // <TODO> write out to log
    }
    return Collections.singletonList(data_source);
  }

  @Override
  protected String extractUID(M input) {
    String data_source = input.getId();
    try {
        data_source = new JSONObject(input.getData()).getString("__data_source__");
    } catch (Exception e) {
        // <TODO> write out to log
    }
    return FileNameUtils.getBaseName(data_source);
  }
  
  @Override
  protected String getData(M input) {
    return input.getData();
  }
  
  @Override
  protected String getErrorFilePath() {
    return MethodCHFLogConfig.getErrorPath();
  }

  @Override
  public List<String> getErrorFiles(M input) {
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

  /*
  public void setErrorFiles(M input) {
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
  */

  /**
   * Multiple tests run from the same jvm could end up reusing the same mappings leading to
   * side-effects. This function is exposed give tests the ability to re-initialize mappings.
   *
   * <p>ONLY FOR TESTS.
   */
  @VisibleForTesting
  public static void resetInitialize() {
    initialized.set(false);
  }

  @Override
  protected String getFnClassName() {
    return getClass().getSimpleName();
  }
}
