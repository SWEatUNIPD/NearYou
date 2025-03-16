package io.github.sweatunipd.entity;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

public class GPSData {
  private Timestamp timestamp;
  private int rentId;
  private float latitude;
  private float longitude;

  public GPSData(
      @JsonProperty("trackerId") int rentId,
      @JsonProperty("latitude") float latitude,
      @JsonProperty("longitude") float longitude) {
    this.timestamp = new Timestamp(System.currentTimeMillis());
    this.rentId = rentId;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public Timestamp getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Timestamp timestamp) {
    this.timestamp = timestamp;
  }

  public int getRentId() {
    return rentId;
  }

  public void setRentId(int rentId) {
    this.rentId = rentId;
  }

  public float getLatitude() {
    return latitude;
  }

  public void setLatitude(float latitude) {
    this.latitude = latitude;
  }

  public float getLongitude() {
    return longitude;
  }

  public void setLongitude(float longitude) {
    this.longitude = longitude;
  }

  @Override
  public String toString() {
    return "GPSData{"
        + "latitude="
        + latitude
        + ", rentId="
        + rentId
        + ", longitude="
        + longitude
        + '}';
  }
}
