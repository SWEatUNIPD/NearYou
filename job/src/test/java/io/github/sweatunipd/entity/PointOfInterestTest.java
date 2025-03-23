package io.github.sweatunipd.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PointOfInterestTest {
  private PointOfInterest pointOfInterest;

  @BeforeEach
  public void setUp() throws InterruptedException {
    pointOfInterest = new PointOfInterest(78.5f, 78.5f, "IT101010101", "Test", "Test", "Test");
  }

  @Test
  @DisplayName("Test different latitutude")
  void testDifferentLat() {
    PointOfInterest pointOfInterestCopy = new PointOfInterest(80f, 78.5f, "IT101010101", "Test", "Test", "Test");
    Assertions.assertNotEquals(pointOfInterest, pointOfInterestCopy);
  }

  @Test
  @DisplayName("Test different longitude")
  void testDifferentLon() {
    PointOfInterest pointOfInterestCopy = new PointOfInterest(78.5f, 80f, "IT101010101", "Test", "Test", "Test");
    Assertions.assertNotEquals(pointOfInterest, pointOfInterestCopy);
  }

  @Test
  @DisplayName("Test different VAT")
  void testDifferentVat() {
    PointOfInterest pointOfInterestCopy = new PointOfInterest(78.5f, 78.5f, "IT101010100", "Test", "Test", "Test");
    Assertions.assertNotEquals(pointOfInterest, pointOfInterestCopy);
  }

  @Test
  @DisplayName("Test different name")
  void testDifferentName() {
    PointOfInterest pointOfInterestCopy = new PointOfInterest(78.5f, 78.5f, "IT101010101", "Test1", "Test", "Test");
    Assertions.assertNotEquals(pointOfInterest, pointOfInterestCopy);
  }

  @Test
  @DisplayName("Test different category")
  void testDifferentCategory() {
    PointOfInterest pointOfInterestCopy = new PointOfInterest(78.5f, 78.5f, "IT101010101", "Test", "Test1", "Test");
    Assertions.assertNotEquals(pointOfInterest, pointOfInterestCopy);
  }

  @Test
  @DisplayName("Test different offer")
  void testDifferentOffer() {
    PointOfInterest pointOfInterestCopy = new PointOfInterest(78.5f, 78.5f, "IT101010101", "Test", "Test", "Test1");
    Assertions.assertNotEquals(pointOfInterest, pointOfInterestCopy);
  }

  @Test
  @DisplayName("Test different obj")
  void testDifferentObj() {
    Object gpsData = new GPSData(1, 78.5f, 78.5f);
    Assertions.assertNotEquals(pointOfInterest, gpsData);
  }

  @Test
  @DisplayName("Test same POI")
  void testSamePOI() {
    PointOfInterest pointOfInterestCopy = new PointOfInterest(78.5f, 78.5f, "IT101010101", "Test", "Test", "Test");
    Assertions.assertEquals(pointOfInterest, pointOfInterestCopy);
  }

  @Test
  @DisplayName("Test hashcode")
  void testHashCode() {
    PointOfInterest pointOfInterestCopy=pointOfInterest;
    Assertions.assertEquals(pointOfInterest.hashCode(), pointOfInterestCopy.hashCode());
  }
}
