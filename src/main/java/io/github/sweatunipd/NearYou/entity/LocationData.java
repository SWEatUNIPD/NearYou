package io.github.sweatunipd.NearYou.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity(name = "location_datas")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"fetch_time", "rent_id"})})
public class LocationData implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, name = "fetch_time")
  private Timestamp fetchTime;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "rent_id", referencedColumnName = "id", nullable = false)
  private Rent rent;

  @Column(nullable = false)
  private double latitude;

  @Column(nullable = false)
  private double longitude;

  public LocationData(Timestamp fetchTime, Long id, double latitude, double longitude, Rent rent) {
    this.fetchTime = fetchTime;
    this.id = id;
    this.latitude = latitude;
    this.longitude = longitude;
    this.rent = rent;
  }

  public LocationData() {
  }

  public Timestamp getFetchTime() {
    return fetchTime;
  }

  public void setFetchTime(Timestamp fetchTime) {
    this.fetchTime = fetchTime;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public Rent getRent() {
    return rent;
  }

  public void setRent(Rent rent) {
    this.rent = rent;
  }
}
