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
package com.google.cloud.healthcare.etl.model.mapping;

import java.util.Optional;
import javax.annotation.Nullable;
//import org.apache.beam.sdk.io.gcp.healthcare.HL7v2Message;
import org.joda.time.Instant;

/**
 * Represents an HL7v2 message from the HCLS API for mapping. The ID is the resource name of the
 * HL7v2 message in the original HL7v2 store. This class doesn't wrap the original but keeps useful fields only to keep memory usage low.
 */
public class HclsApiCDAr2MappableMessage implements Mappable {

    private final String name;
    private final String schematizedData;
    @Nullable
    private Instant createTime;

    public HclsApiCDAr2MappableMessage(String name, String schematizedData) {
        this.name = name;
        this.schematizedData = schematizedData;
    }

    public HclsApiCDAr2MappableMessage(String name, String schematizedData, String createTime) {
        this.name = name;
        this.schematizedData = schematizedData;
        this.createTime = Instant.parse(createTime);
    }

    public HclsApiCDAr2MappableMessage(String name, String schematizedData, Instant createTime) {
        this.name = name;
        this.schematizedData = schematizedData;
        this.createTime = createTime;
    }

    public static HclsApiCDAr2MappableMessage fromNoName(String message) {
        return new HclsApiCDAr2MappableMessage("name", message);
    }


    public static HclsApiCDAr2MappableMessage from(String message) {
        return new HclsApiCDAr2MappableMessage("name", message);
    }

    /* https://www.joda.org/joda-time/key_instant.html */
    public static HclsApiCDAr2MappableMessage fromWithCreateTime(String message) {
        return new HclsApiCDAr2MappableMessage(
                "name", message, Instant.parse(null));
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getData() {
        return schematizedData;
    }

    @Override
    public Optional<Instant> getCreateTime() {
        return Optional.ofNullable(createTime);
    }
}
