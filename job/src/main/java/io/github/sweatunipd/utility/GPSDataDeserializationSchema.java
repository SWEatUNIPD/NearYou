package io.github.sweatunipd.utility;

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

  /**
   * Method that initializes the ObjectMapper in order to perform the deserialization
   *
   * @param context Contextual information that can be used during initialization.
   */
  @Override
  public void open(InitializationContext context) {
    objectMapper = JsonMapper.builder().build();
  }

  /**
   * Method that deserialize the data in a GPSData pojo for the upcoming stream from the client
   *
   * @param bytes serialized message
   * @return GPSData as a product of the deserialization of the message
   */
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
