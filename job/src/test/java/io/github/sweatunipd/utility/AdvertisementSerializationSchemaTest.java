package io.github.sweatunipd.utility;

import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.entity.PointOfInterest;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AdvertisementSerializationSchemaTest {

  private GPSData gpsData;
  private PointOfInterest pointOfInterest;
  private String advertisement;
  private AdvertisementSerializationSchema advertisementSerializationSchema;

  @BeforeEach
  void setUp() {
    gpsData = new GPSData(1, 78.5f, 78.5f);
    pointOfInterest =
        new PointOfInterest(78.5f, 78.5f, "IT101010101", "TestShop", "Tester", "Testing a shop");
    advertisement = "Lorem ipsum";
    advertisementSerializationSchema = new AdvertisementSerializationSchema();
  }

  @Test
  @DisplayName("Test consistency")
  void consistencyTest() throws Exception {
    Tuple3<GPSData, PointOfInterest, String> result =
        new Tuple3<>(gpsData, pointOfInterest, advertisement);

    byte[] bytes = advertisementSerializationSchema.serialize(result);

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode = objectMapper.readTree(bytes);

    Assertions.assertEquals(advertisement, jsonNode.get("adv").asText());
    Assertions.assertEquals(gpsData.getRentId(), jsonNode.get("rent_id").asInt());
  }

  @Test
  @DisplayName(
      "Check if is thrown a NullPointerException while trying to invoke a method from a null object")
  void testNullAttributesOnTuple() {
    Tuple3<GPSData, PointOfInterest, String> adv = new Tuple3<>(null, null, null);
    Assertions.assertThrows(
        NullPointerException.class, () -> advertisementSerializationSchema.serialize(adv));
  }
}
