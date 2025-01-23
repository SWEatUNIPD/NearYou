package io.github.sweatunipd.NearYou.entity;

import jakarta.persistence.*;
import java.awt.*;

@Entity(name = "points_of_interest")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"latitude", "longitude"})})
public class PointOfInterest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "merchant_vat", referencedColumnName = "vat", nullable = false)
  private Merchant merchant;

  @Column(nullable = false)
  private float latitude;

  @Column(nullable = false)
  private float longitude;

  private PointOfInterest(PointOfInterestBuilder builder) {
    this.latitude = builder.latitude;
    this.longitude = builder.longitude;
    this.merchant = builder.merchant;
  }

  protected PointOfInterest() {}

  public int getId() {
    return id;
  }

  public float getLatitude() {
    return latitude;
  }

  public float getLongitude() {
    return longitude;
  }

  public Merchant getMerchant() {
    return merchant;
  }

  public static class PointOfInterestBuilder {
    private Merchant merchant;
    private float latitude;
    private float longitude;

    public PointOfInterestBuilder setMerchant(Merchant merchant) {
      this.merchant = merchant;
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

    public PointOfInterest build() {
      return new PointOfInterest(this);
    }
  }
}
