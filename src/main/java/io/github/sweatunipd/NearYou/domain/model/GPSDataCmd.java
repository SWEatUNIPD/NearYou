package io.github.sweatunipd.NearYou.domain.model;

public class GPSDataCmd {
    private int rentId;
    private float latitude;
    private float longitude;

    public GPSDataCmd(int rentId, float latitude, float longitude) {
        this.rentId = rentId;
        this.latitude = latitude;
        this.longitude = longitude;
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

}
