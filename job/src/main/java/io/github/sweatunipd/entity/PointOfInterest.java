package io.github.sweatunipd.entity;

import java.util.Objects;

public class PointOfInterest {
  private int id;
  private String merchantVAT;
  private String name;
  private float latitude;
  private float longitude;
  private String category;
  private String offer;

  public PointOfInterest(
      int id,
      String merchantVAT,
      String name,
      float latitude,
      float longitude,
      String category,
      String offer) {
    this.id = id;
    this.merchantVAT = merchantVAT;
    this.name = name;
    this.latitude = latitude;
    this.longitude = longitude;
    this.category = category;
    this.offer = offer;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  public String getMerchantVAT() {
    return merchantVAT;
  }

  public void setMerchantVAT(String merchantVAT) {
    this.merchantVAT = merchantVAT;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getOffer() {
    return offer;
  }

  public void setOffer(String offer) {
    this.offer = offer;
  }

  @Override
  public String toString() {
    return "PointOfInterest{"
        + "category='"
        + category
        + '\''
        + ", id="
        + id
        + ", merchantVAT='"
        + merchantVAT
        + '\''
        + ", name='"
        + name
        + '\''
        + ", latitude="
        + latitude
        + ", longitude="
        + longitude
        + ", offer='"
        + offer
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PointOfInterest that)) return false;
    return id == that.id
        && Float.compare(latitude, that.latitude) == 0
        && Float.compare(longitude, that.longitude) == 0
        && Objects.equals(merchantVAT, that.merchantVAT)
        && Objects.equals(name, that.name)
        && Objects.equals(category, that.category)
        && Objects.equals(offer, that.offer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, merchantVAT, name, latitude, longitude, category, offer);
  }
}
