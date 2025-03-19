package io.github.sweatunipd.entity;

import java.sql.Timestamp;
import java.util.Objects;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

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

  public int getRentId() {
    return rentId;
  }

  public float getLatitude() {
    return latitude;
  }

  public float getLongitude() {
    return longitude;
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
