package io.github.sweatunipd.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GPSDataTest {
    private GPSData gpsData1;
    private GPSData gpsDataReplica1;
    private GPSData gpsDataReplica2;
    private PointOfInterest pointOfInterest;

    @BeforeEach
    public void setUp() throws InterruptedException {
        gpsData1=new GPSData(1, 78.5f, 78.5f);
        gpsDataReplica1=gpsData1;
        Thread.sleep(200);
        gpsDataReplica2=new GPSData(1, 78.5f, 78.5f);
        pointOfInterest=new PointOfInterest(78.5f, 78.5f, "IT101010101", "Test", "Test", "Test");
    }

    @Test
    @DisplayName("Test if equals() see the same object equal to itself")
    void testEquals() {
        Assertions.assertEquals(gpsData1, gpsDataReplica1);
    }

    @Test
    @DisplayName("Test if two object with the same parameters are not equal because created in different time")
    void testTwoObjectsWithSameParameters() {
        Assertions.assertNotEquals(gpsData1, gpsDataReplica2);
    }

    @Test
    @DisplayName("Test that the object isn't instance of another to be equal")
    void testNotEquals() {

        Assertions.assertNotEquals((Object) gpsData1, (Object) pointOfInterest);
    }

    @Test
    @DisplayName("Test hashcode")
    void testHashCode() {
        Assertions.assertEquals(gpsData1.hashCode(), gpsDataReplica1.hashCode());
    }
}
