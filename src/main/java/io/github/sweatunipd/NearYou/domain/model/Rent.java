package io.github.sweatunipd.NearYou.domain.model;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity(name = "rents")
public class Rent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "id")
    private Long id;

    @Column(nullable = false, name = "rentId")
    private int rentId;

    @Column(nullable = false, name = "latitude")
    private float latitude;

    @Column(nullable = false, name = "longitude")
    private float longitude;

    public Rent() {}

    public Rent(int rentId, float latitude, float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.rentId = rentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public int getRentId() {
        return rentId;
    }

    public void setRentId(int rentId) {
        this.rentId = rentId;
    }
}
