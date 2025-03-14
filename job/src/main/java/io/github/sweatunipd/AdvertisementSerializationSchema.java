package io.github.sweatunipd;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.UUID;

public class AdvertisementSerializationSchema
    implements SerializationSchema<Tuple3<UUID, Integer, String>> {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public byte[] serialize(Tuple3<UUID, Integer, String> integerStringTuple2) {
    try {
      ObjectNode node = MAPPER.createObjectNode();
      node.put("rent_id", integerStringTuple2.f0.toString());
      node.put("adv", integerStringTuple2.f1);
      return MAPPER.writeValueAsBytes(node);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize Tuple2 to JSON", e);
    }
  }
}
