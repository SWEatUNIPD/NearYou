package io.github.sweatunipd;

import io.github.sweatunipd.entity.GPSData;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GPSDataDeserializationSchema extends AbstractDeserializationSchema<GPSData> {
  private static final Logger LOG = LoggerFactory.getLogger(GPSDataDeserializationSchema.class);
  private transient ObjectMapper objectMapper;

  @Override
  public void open(InitializationContext context) {
    objectMapper = JsonMapper.builder().build();
  }

  @Override
  public GPSData deserialize(byte[] bytes) {
    try {
      return objectMapper.readValue(bytes, GPSData.class);
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      return null;
    }
  }
}
