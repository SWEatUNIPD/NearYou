package io.github.sweatunipd.utility;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.sweatunipd.dto.GPSDataDto;

import java.util.List;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class GPSDataDeserializationSchemaTest {
  @Mock private DeserializationSchema.InitializationContext initializationContext;

  private GPSDataDeserializationSchema gpsDataDeserializationSchema;

  @BeforeEach
  void setUp() {
    gpsDataDeserializationSchema = new GPSDataDeserializationSchema();
  }

  @Test
  @DisplayName("Test if the deserialization is done successfully")
  void testInvokeMethodOpen() {
    String jsonGPSData = "{\"rentId\":1,\"latitude\":78.5,\"longitude\":78.5,\"timestamp\":1742897857667}";
    byte[] gpsDataBytes = jsonGPSData.getBytes();

    gpsDataDeserializationSchema.open(initializationContext);

    GPSDataDto data = gpsDataDeserializationSchema.deserialize(gpsDataBytes);

    Assertions.assertEquals(1, data.getRentId());
    Assertions.assertEquals(78.5f, data.getLatitude());
    Assertions.assertEquals(78.5f, data.getLongitude());
  }

  @Test
  @DisplayName("Test if deserialization fails because of wrong data coming from the client")
  void testInvokeMethodFail() {
    String jsonWrongFormat = "{\"trackerId\":1,\"latitude\":78.5,\"longitude\":78.5,}";
    byte[] bytes = jsonWrongFormat.getBytes();

    gpsDataDeserializationSchema.open(initializationContext);

    Logger logger = (Logger) LoggerFactory.getLogger(GPSDataDeserializationSchema.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    gpsDataDeserializationSchema.deserialize(bytes);

    List<ILoggingEvent> logsList = listAppender.list;
    Assertions.assertEquals(Level.ERROR, logsList.get(0).getLevel());
  }
}
