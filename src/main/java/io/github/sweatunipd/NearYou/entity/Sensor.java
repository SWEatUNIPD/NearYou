package io.github.sweatunipd.NearYou.entity;

import jakarta.persistence.*;

@Entity(name = "sensors")
public class Sensor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  protected Sensor(){}

  private Sensor(SensorBuilder sensorBuilder) {
  }

  public int getId() {
    return id;
  }

  public static class SensorBuilder {
    public Sensor build() {
      return new Sensor(this);
    }
  }
}
