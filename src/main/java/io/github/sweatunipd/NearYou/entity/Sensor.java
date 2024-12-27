package io.github.sweatunipd.NearYou.entity;

import jakarta.persistence.*;

@Entity(name = "sensors")
public class Sensor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  public Sensor(Long id) {
    this.id = id;
  }

  public Sensor() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
