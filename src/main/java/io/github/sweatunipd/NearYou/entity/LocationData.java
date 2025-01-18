package io.github.sweatunipd.NearYou.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity(name = "location_datas")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"fetch_time", "rent_id"})})
public class LocationData {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, name = "fetch_time")
  private Timestamp fetchTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rent_id", referencedColumnName = "id", nullable = false)
  private Rent rent;

  @Column(nullable = false)
  private float latitude;

  @Column(nullable = false)
  private float longitude;

  private LocationData(LocationDataBuilder builder) {
    this.fetchTime = builder.fetchTime;
    this.rent = builder.rent;
    this.latitude = builder.latitude;
    this.longitude = builder.longitude;
  }

  protected LocationData() {}

  public Timestamp getFetchTime() {
    return fetchTime;
  }

  public Long getId() {
    return id;
  }

  public float getLatitude() {
    return latitude;
  }

  public float getLongitude() {
    return longitude;
  }

  public Rent getRent() {
    return rent;
  }

  public static class LocationDataBuilder {
    private Timestamp fetchTime;
    private Rent rent;
    private float latitude;
    private float longitude;

    public LocationDataBuilder setFetchTime(Timestamp fetchTime) {
      this.fetchTime = fetchTime;
      return this;
    }

    public LocationDataBuilder setRent(Rent rent) {
      this.rent = rent;
      return this;
    }

    public LocationDataBuilder setLatitude(float latitude) {
      this.latitude = latitude;
      return this;
    }

    public LocationDataBuilder setLongitude(float longitude) {
      this.longitude = longitude;
      return this;
    }

    public LocationData build() {
      return new LocationData(this);
    }
  }
}
