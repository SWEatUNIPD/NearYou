package io.github.sweatunipd.entity;

import java.util.Objects;

public record PointOfInterest(
    float latitude,
    float longitude,
    String vat,
    String name,
    String category,
    String offer) {
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PointOfInterest that)) return false;
    return Float.compare(latitude, that.latitude) == 0
        && Float.compare(longitude, that.longitude) == 0
        && Objects.equals(name, that.name)
        && Objects.equals(offer, that.offer)
        && Objects.equals(category, that.category)
        && Objects.equals(vat, that.vat);
  }

  @Override
  public int hashCode() {
    return Objects.hash(latitude, longitude, vat, name, category, offer);
  }
}
