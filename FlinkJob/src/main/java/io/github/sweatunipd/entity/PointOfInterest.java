package io.github.sweatunipd.entity;

import java.sql.Time;

public class PointOfInterest {
  private int id;
  private String merchantVAT;
  private String name;
  private Time startTime;
  private Time endTime;
  private float latitude;
  private float longitude;

  /*FIXME: il costruttore non è stato messo privato per poter essere utilizzato in caso di serializzazione/deserializzazione da confermare*/
  public PointOfInterest(
      int id,
      String merchantVAT,
      String name,
      Time startTime,
      Time endTime,
      float latitude,
      float longitude) {
    this.id = id;
    this.merchantVAT = merchantVAT;
    this.name = name;
    this.startTime = startTime;
    this.endTime = endTime;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Time getEndTime() {
    return endTime;
  }

  public void setEndTime(Time endTime) {
    this.endTime = endTime;
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

  public Time getStartTime() {
    return startTime;
  }

  public void setStartTime(Time startTime) {
    this.startTime = startTime;
  }

  @Override
  public String toString() {
    return "PointOfInterest{" +
            "endTime=" + endTime +
            ", id=" + id +
            ", merchantVAT='" + merchantVAT + '\'' +
            ", name='" + name + '\'' +
            ", startTime=" + startTime +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            '}';
  }

  public static class PointOfInterestBuilder {
    private int id;
    private String merchantVAT;
    private String name;
    private Time startTime;
    private Time endTime;
    private float latitude;
    private float longitude;

    public PointOfInterest build() {
      return new PointOfInterest(id, merchantVAT, name, startTime, endTime, latitude, longitude);
    }

    public PointOfInterestBuilder id(int id) {
      this.id = id;
      return this;
    }

    public PointOfInterestBuilder setMerchantVAT(String merchantVAT) {
      this.merchantVAT = merchantVAT;
      return this;
    }

    public PointOfInterestBuilder setName(String name) {
      this.name = name;
      return this;
    }

    public PointOfInterestBuilder setStartTime(Time startTime) {
      this.startTime = startTime;
      return this;
    }

    public PointOfInterestBuilder setEndTime(Time endTime) {
      this.endTime = endTime;
      return this;
    }

    public PointOfInterestBuilder setLatitude(float latitude) {
      this.latitude = latitude;
      return this;
    }

    public PointOfInterestBuilder setLongitude(float longitude) {
      this.longitude = longitude;
      return this;
    }
  }
}
