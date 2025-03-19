package io.github.sweatunipd.utility;

import io.github.sweatunipd.entity.GPSData;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;

public class AdvertisementSerializationSchema
        implements SerializationSchema<Tuple3<GPSData, Integer, String>> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Method that serializes a tuple containing the GPSData, the ID of the POI and the string
     * of the generated advertisement
     *
     * @param adv The advertisement to be serialized
     * @return serialization of the tuple
     */
    @Override
    public byte[] serialize(Tuple3<GPSData, Integer, String> adv) {
        try {
            ObjectNode node = MAPPER.createObjectNode();
            node.put("rent_id", adv.f0.toString());
            node.put("adv", adv.f1);
            return MAPPER.writeValueAsBytes(node);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize Tuple3 to JSON", e);
        }
    }
}
