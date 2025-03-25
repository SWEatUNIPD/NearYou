package io.github.sweatunipd.dto;

import io.github.sweatunipd.model.GPSData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

public class GPSDataDtoTest {

  private GPSDataDto gpsDataDto;

  @BeforeEach
  void setUp() {
    gpsDataDto = new GPSDataDto();
    gpsDataDto.setRentId(1);
    gpsDataDto.setLatitude(78.5f);
    gpsDataDto.setLongitude(78.5f);
    gpsDataDto.setTimestamp(1L);
  }

  @Test
  void testGetRentId() {
    Assertions.assertEquals(1, gpsDataDto.getRentId());
  }

  @Test
  void testGetLatitude() {
    Assertions.assertEquals(78.5f, gpsDataDto.getLatitude());
  }

  @Test
  void testGetLongitude() {
    Assertions.assertEquals(78.5f, gpsDataDto.getLongitude());
  }

  @Test void testGetTimestamp() {
    Assertions.assertEquals(1L, gpsDataDto.getTimestamp());
  }

  @Test
  void testEquality(){
    GPSDataDto gpsDataDtoCopy = gpsDataDto;
    Assertions.assertEquals(gpsDataDto, gpsDataDtoCopy);
  }

  @Test
  void testDifferentObj(){
    Object gpsData = new GPSData(1, 78.5f, 78.5f, new Timestamp(System.currentTimeMillis()));
    Assertions.assertNotEquals(gpsDataDto, gpsData);
  }

  @Test
  void testDifferentRentId(){
    GPSDataDto gpsDataDtoCopy = new GPSDataDto();
    gpsDataDtoCopy.setRentId(2);
    gpsDataDtoCopy.setLatitude(78.5f);
    gpsDataDtoCopy.setLongitude(78.5f);
    gpsDataDtoCopy.setTimestamp(1L);
    Assertions.assertNotEquals(gpsDataDto, gpsDataDtoCopy);
  }

  @Test
  void testDifferentLatitude(){
    GPSDataDto gpsDataDtoCopy = new GPSDataDto();
    gpsDataDtoCopy.setRentId(1);
    gpsDataDtoCopy.setLatitude(80f);
    gpsDataDtoCopy.setLongitude(78.5f);
    gpsDataDtoCopy.setTimestamp(1L);
    Assertions.assertNotEquals(gpsDataDto, gpsDataDtoCopy);
  }

  @Test
  void testDifferentLongitude(){
    GPSDataDto gpsDataDtoCopy = new GPSDataDto();
    gpsDataDtoCopy.setRentId(1);
    gpsDataDtoCopy.setLatitude(78.5f);
    gpsDataDtoCopy.setLongitude(80f);
    gpsDataDtoCopy.setTimestamp(1L);
    Assertions.assertNotEquals(gpsDataDto, gpsDataDtoCopy);
  }

  @Test
  void testDifferentTimestamp(){
    GPSDataDto gpsDataDtoCopy = new GPSDataDto();
    gpsDataDtoCopy.setRentId(1);
    gpsDataDtoCopy.setLatitude(78.5f);
    gpsDataDtoCopy.setLongitude(78.5f);
    gpsDataDtoCopy.setTimestamp(System.currentTimeMillis());
    Assertions.assertNotEquals(gpsDataDto, gpsDataDtoCopy);
  }

  @Test
  void testHashCode(){
    GPSDataDto gpsDataDtoCopy = gpsDataDto;
    Assertions.assertEquals(gpsDataDto.hashCode(), gpsDataDtoCopy.hashCode());
  }
}
