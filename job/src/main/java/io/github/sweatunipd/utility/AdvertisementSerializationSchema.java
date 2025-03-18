package io.github.sweatunipd.utility;

import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.entity.PointOfInterest;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author SWEatUNIPD
 */
public class AdvertisementSerializationSchema
        implements SerializationSchema<Tuple3<GPSData, PointOfInterest, String>> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Method that serializes a tuple containing the GPSData, the ID of the POI and the string
     * of the generated advertisement
     *
     * @param adv The advertisement to be serialized
     * @return serialization of the tuple
     */
    @Override
    public byte[] serialize(Tuple3<GPSData, PointOfInterest, String> adv) {
        try {
            ObjectNode node = MAPPER.createObjectNode();
            node.put("rent_id", adv.f0.toString());
            node.put("adv", adv.f2);
            return MAPPER.writeValueAsBytes(node);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize Tuple2 to JSON", e);
        }
    }
}
