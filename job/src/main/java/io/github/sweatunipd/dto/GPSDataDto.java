package io.github.sweatunipd.dto;

import java.util.Objects;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

public class GPSDataDto {
  private int rentId;
  private float latitude;
  private float longitude;
  private long timestamp;

  public GPSDataDto() {}

  public int getRentId() {
    return rentId;
  }

  public float getLatitude() {
    return latitude;
  }

  public float getLongitude() {
    return longitude;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setRentId(int rentId) {
    this.rentId = rentId;
  }

  public void setLatitude(float latitude) {
    this.latitude = latitude;
  }

  public void setLongitude(float longitude) {
    this.longitude = longitude;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof GPSDataDto gpsDataDto)) return false;
    return rentId == gpsDataDto.rentId
        && Float.compare(latitude, gpsDataDto.latitude) == 0
        && Float.compare(longitude, gpsDataDto.longitude) == 0
        && timestamp == gpsDataDto.timestamp;
  }

  @Override
  public int hashCode() {
    return Objects.hash(rentId, latitude, longitude, timestamp);
  }
}
