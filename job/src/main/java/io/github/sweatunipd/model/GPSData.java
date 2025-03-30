package io.github.sweatunipd.model;

import java.sql.Timestamp;
import java.util.Objects;

public record GPSData(int rentId, float latitude, float longitude, Timestamp timestamp) {
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
    return Objects.hash(rentId, latitude, longitude, timestamp);
  }
}
