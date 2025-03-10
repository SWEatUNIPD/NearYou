package io.github.sweatunipd.entity;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

public class GPSData {
    private int rentId;
    private float latitude;
    private float longitude;

    public GPSData(@JsonProperty("latitude") float latitude, @JsonProperty("longitude") float longitude, @JsonProperty("trackerId") int rentId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.rentId = rentId;
    }

    public GPSData() {
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

    public int getRentId() {
        return rentId;
    }

    public void setRentId(int rentId) {
        this.rentId = rentId;
    }

    @Override
    public String toString() {
        return "GPSData{" +
                "latitude=" + latitude +
                ", rentId=" + rentId +
                ", longitude=" + longitude +
                '}';
    }
}
