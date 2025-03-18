package io.github.sweatunipd.utility;
import io.github.sweatunipd.entity.GPSData;
import org.apache.flink.api.common.serialization.DeserializationSchema.InitializationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class GPSDataDeserializationSchemaTest {

  private GPSDataDeserializationSchema deserializationSchema;

  @BeforeEach
  public void setUp() throws Exception {
    deserializationSchema = new GPSDataDeserializationSchema();
    InitializationContext mockContext = Mockito.mock(InitializationContext.class);
    deserializationSchema.open(mockContext);  
  }

  @Test
  public void testDeserialize() throws IOException {
    byte[] bytes = "{\"latitude\": 45.0, \"rentId\": 1, \"longitude\": 12.0}".getBytes();

    GPSData result = deserializationSchema.deserialize(bytes);

    assertNotNull(result);
    assertEquals(45.0, result.getLatitude());
    assertEquals(12.0, result.getLongitude());
    assertEquals(1, result.getRentId());
    
  }

  @Test
  public void testDeserializeWithInvalidField() throws IOException {
    
    byte[] bytes = "{\"latitude\": 45.0, \"rentId\": invalid, \"longitude\": 12.0}".getBytes();

    GPSData result = deserializationSchema.deserialize(bytes);

    assertNull(result);
  }

/*
  @Test
  public void testDeserializeWithMIssingField() throws IOException {
    
    byte[] bytes = "{\"latitude\":45.0,\"longitude\":12.0}".getBytes();

    GPSData result = deserializationSchema.deserialize(bytes);

    assertNull(result);
  }
*/

  @Test
  public void testDeserializeWithEmptyData() throws IOException {
    
    byte[] bytes = new byte[0];

    GPSData result = deserializationSchema.deserialize(bytes);

    assertNull(result);
  }
}