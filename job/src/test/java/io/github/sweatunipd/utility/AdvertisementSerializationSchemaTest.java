package io.github.sweatunipd.utility;
import io.github.sweatunipd.entity.GPSData;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;



public class AdvertisementSerializationSchemaTest {

    private AdvertisementSerializationSchema serializer;
    private static final ObjectMapper TEST_MAPPER = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        serializer = new AdvertisementSerializationSchema();
    }

    @Test
    public void testSerialize() throws Exception {
        GPSData gpsData = new GPSData(1, 45.0f, 12.0f);
        Tuple3<GPSData, Integer, String> adv = new Tuple3<>(gpsData, 123, "Test Advertisement");

        byte[] serializedData = serializer.serialize(adv);

        ObjectNode node = (ObjectNode) TEST_MAPPER.readTree(serializedData);
        assertEquals(gpsData.toString(), node.get("rent_id").asText());
        assertEquals(123, node.get("adv").asInt());
    }

    @Test
    public void testSerializeWithNullValues() {
        Tuple3<GPSData, Integer, String> adv = new Tuple3<>(null, null, null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            serializer.serialize(adv);
        });

        String expectedMessage = "Failed to serialize Tuple3 to JSON";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}