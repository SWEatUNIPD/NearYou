package io.github.sweatunipd.NearYou.entity;

import jakarta.persistence.*;

@Entity(name = "generations")
public class Generation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "location_data_id",
      referencedColumnName = "id",
      nullable = false,
      unique = true)
  private LocationData locationData;

  @Column(name = "advertisment")
  private String adv;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "point_of_interest_id", referencedColumnName = "id")
  private PointOfInterest pointOfInterest;

  private Generation(GenerationBuilder builder) {
    this.adv = builder.adv;
    this.locationData = builder.locationData;
    this.pointOfInterest = builder.pointOfInterest;
  }

  protected Generation() {}

  public String getAdv() {
    return adv;
  }

  public LocationData getLocationData() {
    return locationData;
  }

  public PointOfInterest getPointOfInterest() {
    return pointOfInterest;
  }

  public static class GenerationBuilder {
    private String adv;
    private LocationData locationData;
    private PointOfInterest pointOfInterest;

    public GenerationBuilder setAdv(String adv) {
      this.adv = adv;
      return this;
    }

    public GenerationBuilder setLocationData(LocationData locationData) {
      this.locationData = locationData;
      return this;
    }

    public GenerationBuilder setPointOfInterest(PointOfInterest pointOfInterest) {
      this.pointOfInterest = pointOfInterest;
      return this;
    }

    public Generation build() {
      return new Generation(this);
    }
  }
}
