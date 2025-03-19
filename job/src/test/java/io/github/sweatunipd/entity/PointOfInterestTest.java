package io.github.sweatunipd.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PointOfInterestTest {
  private PointOfInterest pointOfInterest;
  private PointOfInterest pointOfInterestReplica1;
  private PointOfInterest pointOfInterestReplica2;
  private GPSData gpsData;

  @BeforeEach
  public void setUp() throws InterruptedException {
    pointOfInterest = new PointOfInterest(78.5f, 78.5f, "IT101010101", "Test", "Test", "Test");
    pointOfInterestReplica1 =
        new PointOfInterest(78.5f, 78.5f, "IT101010101", "Test", "Test", "Test");
    pointOfInterestReplica2 = new PointOfInterest(80.5f, 80.5f, "IT101010101", "Test", "Test", "Test");
    gpsData = new GPSData(1, 78.5f, 78.5f);
  }

  @Test
  @DisplayName("Test if equals() see the same object equal to itself")
  void testEquals() {
    Assertions.assertEquals(pointOfInterest, pointOfInterestReplica1);
  }

  @Test
  @DisplayName("Test if equals() finds that two objects are different")
  void testNotEqualsSameObject() {
    Assertions.assertNotEquals(pointOfInterestReplica1, pointOfInterestReplica2);
  }

  @Test
  @DisplayName("Test not equals between object and null")
  void testObjNullInequality() {
    Assertions.assertNotEquals(null, pointOfInterestReplica1);
  }

  @Test
  @DisplayName("Test that the object isn't instance of another to be equal")
  void testNotEqualsDifferentObject() {
    Assertions.assertNotEquals((Object) gpsData, (Object) pointOfInterest);
  }

  @Test
  @DisplayName("Test hashcode")
  void testHashCode() {
    Assertions.assertEquals(pointOfInterest.hashCode(), pointOfInterestReplica1.hashCode());
  }
}
