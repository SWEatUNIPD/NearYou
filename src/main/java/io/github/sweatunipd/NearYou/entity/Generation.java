package io.github.sweatunipd.NearYou.entity;

import jakarta.persistence.*;

@Entity(name = "generations")
public class Generation {
  @Id
  private long id;

  @OneToOne
  @JoinColumn(name = "location_data_id", referencedColumnName = "id", nullable = false, unique = true)
  private LocationData locationData;

  @Column(name = "advertisment")
  private String adv;

  public Generation(String adv, LocationData locationData) {
    this.adv = adv;
    this.locationData = locationData;
  }

  public Generation() {
  }

  public String getAdv() {
    return adv;
  }

  public void setAdv(String adv) {
    this.adv = adv;
  }

  public LocationData getLocationData() {
    return locationData;
  }

  public void setLocationData(LocationData locationData) {
    this.locationData = locationData;
  }
}
