package io.github.sweatunipd.NearYou.entity;

import jakarta.persistence.*;

@Entity(name = "points_of_interest")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"latitude", "longitude"})})
public class PointOfInterest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "merchant_vat", referencedColumnName = "vat", nullable = false)
  private Merchant merchant;

  @Column(nullable = false)
  private double latitude;

  @Column(nullable = false)
  private double longitude;

  public PointOfInterest(Long id, double latitude, double longitude, Merchant merchant) {
    this.id = id;
    this.latitude = latitude;
    this.longitude = longitude;
    this.merchant = merchant;
  }

  public PointOfInterest() {}

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

  public Merchant getMerchant() {
    return merchant;
  }

  public void setMerchant(Merchant merchant) {
    this.merchant = merchant;
  }
}
