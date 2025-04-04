package io.github.sweatunipd.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

public class GPSDataTest {
    private GPSData gpsData;

    @BeforeEach
    void setUp() {
        gpsData=new GPSData(1, 78.5f, 78.5f, new Timestamp(System.currentTimeMillis()));
    }

    //Equals test
    @Test
    @DisplayName("Test if equals() see the same object equal to itself")
    void testEquals() {
        GPSData gpsDataCopy=gpsData;
        Assertions.assertEquals(gpsData, gpsDataCopy);
    }

    @Test
    @DisplayName("Test different rentId")
    void testDifferentRentId() {
        GPSData gpsDataCopy=new GPSData(2, 78.5f, 78.5f, gpsData.timestamp());
        Assertions.assertNotEquals(gpsData, gpsDataCopy);
    }

    @Test
    @DisplayName("Test different latitude")
    void testDifferentLat() {
        GPSData gpsDataCopy=new GPSData(1, 80f, 78.5f, gpsData.timestamp());
        Assertions.assertNotEquals(gpsData, gpsDataCopy);
    }

    @Test
    @DisplayName("Test different longitude")
    void testDifferentLon() {
        GPSData gpsDataCopy=new GPSData(1, 78.5f, 80f, gpsData.timestamp());
        Assertions.assertNotEquals(gpsData, gpsDataCopy);
    }

    @Test
    @DisplayName("Test different timestamp")
    void testDifferentTimestamp() throws InterruptedException {
        Thread.sleep(200);
        GPSData gpsDataCopy=new GPSData(1, 78.5f, 78.5f, new Timestamp(System.currentTimeMillis()));
        Assertions.assertNotEquals(gpsData, gpsDataCopy);
    }

    @Test
    @DisplayName("Test if the object compared are different")
    void testDifferentObject() {
        Object pointOfInterest=new PointOfInterest(78.5f, 78.5f, "IT101010101", "Test", "Test", "Test");
        Assertions.assertNotEquals(gpsData, pointOfInterest);
    }

    @Test
    @DisplayName("Test hashcode")
    void testHashCode() {
        GPSData gpsDataCopy=gpsData;
        Assertions.assertEquals(gpsData.hashCode(), gpsDataCopy.hashCode());
    }
}
