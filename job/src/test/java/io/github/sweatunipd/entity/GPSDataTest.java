package io.github.sweatunipd.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GPSDataTest {
    private GPSData gpsData;

    @BeforeEach
    void setUp() throws InterruptedException {
        gpsData=new GPSData(1, 78.5f, 78.5f);
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
        GPSData gpsDataCopy=new GPSData(2, 78.5f, 78.5f);
        Assertions.assertNotEquals(gpsData, gpsDataCopy);
    }

    @Test
    @DisplayName("Test different latitude")
    void testDifferentLat() {
        GPSData gpsDataCopy=new GPSData(1, 80f, 78.5f);
        Assertions.assertNotEquals(gpsData, gpsDataCopy);
    }

    @Test
    @DisplayName("Test different longitude")
    void testDifferentLon() {
        GPSData gpsDataCopy=new GPSData(1, 78.5f, 80f);
        Assertions.assertNotEquals(gpsData, gpsDataCopy);
    }

    @Test
    @DisplayName("Test different timestamp")
    void testDifferentTimestamp() throws InterruptedException {
        Thread.sleep(200);
        GPSData gpsDataCopy=new GPSData(1, 78.5f, 78.5f);
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

    @Test
    void testSetters(){
        GPSData gpsDataCopy=new GPSData();
        gpsDataCopy.setRentId(1);
        gpsDataCopy.setLatitude(78.5f);
        gpsDataCopy.setLongitude(78.5f);
        gpsDataCopy.setTimestamp(gpsData.getTimestamp());
        Assertions.assertEquals(gpsData,gpsDataCopy);
    }
}
