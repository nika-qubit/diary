package com.google.cloud.healthcare.etl.model.mapping;

import org.apache.beam.sdk.coders.CustomCoder;
import org.apache.beam.sdk.coders.InstantCoder;
import org.apache.beam.sdk.coders.NullableCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.joda.time.Instant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HclsApiCDAr2MappableMessageCoder extends CustomCoder<HclsApiCDAr2MappableMessage> {

    private static final NullableCoder<String> STRING_CODER = NullableCoder.of(StringUtf8Coder.of());
    private static final NullableCoder<Instant> INSTANT_CODER = NullableCoder.of(InstantCoder.of());

    public static HclsApiCDAr2MappableMessageCoder of() {
        return new HclsApiCDAr2MappableMessageCoder();
    }

    @Override
    public void encode(HclsApiCDAr2MappableMessage value, OutputStream outStream) throws IOException {
        STRING_CODER.encode(value.getId(), outStream);
        STRING_CODER.encode(value.getData(), outStream);
        INSTANT_CODER.encode(value.getCreateTime().orElse(null), outStream);
    }

    @Override
    public HclsApiCDAr2MappableMessage decode(InputStream inStream) throws IOException {
        String id = STRING_CODER.decode(inStream);
        String data = STRING_CODER.decode(inStream);
        Instant createTime = INSTANT_CODER.decode(inStream);
        return new HclsApiCDAr2MappableMessage(id, data, createTime);
    }
}
