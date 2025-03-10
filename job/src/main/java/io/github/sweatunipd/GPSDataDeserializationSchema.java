package io.github.sweatunipd;

import io.github.sweatunipd.entity.GPSData;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;

public class GPSDataDeserializationSchema extends AbstractDeserializationSchema<GPSData> {
    private transient ObjectMapper objectMapper;

    @Override
    public void open(InitializationContext context) throws Exception {
        objectMapper = JsonMapper.builder().build();
    }

    @Override
    public GPSData deserialize(byte[] bytes) throws IOException {
        return objectMapper.readValue(bytes, GPSData.class);
    }
}
