package io.github.sweatunipd.entity;

import java.sql.Timestamp;
import java.util.Objects;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

public class GPSData {
  private int rentId;
  private float latitude;
  private float longitude;
  private Timestamp timestamp;

  public GPSData(
      @JsonProperty("rentId") int rentId,
      @JsonProperty("latitude") float latitude,
      @JsonProperty("longitude") float longitude,
      @JsonProperty("timestamp") Timestamp timestamp) {
    this.rentId = rentId;
    this.latitude = latitude;
    this.longitude = longitude;
    this.timestamp = timestamp;
  }

  public GPSData() {}

  public Timestamp getTimestamp() {
    return timestamp;
  }

  public int getRentId() {
    return rentId;
  }

  public float getLatitude() {
    return latitude;
  }

  public float getLongitude() {
    return longitude;
  }

  public void setLatitude(float latitude) {
    this.latitude = latitude;
  }

  public void setLongitude(float longitude) {
    this.longitude = longitude;
  }

  public void setRentId(int rentId) {
    this.rentId = rentId;
  }

  public void setTimestamp(Timestamp timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof GPSData gpsData)) return false;
    return rentId == gpsData.rentId
        && Float.compare(latitude, gpsData.latitude) == 0
        && Float.compare(longitude, gpsData.longitude) == 0
        && Objects.equals(timestamp, gpsData.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, rentId, latitude, longitude);
  }
}
