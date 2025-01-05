package io.github.sweatunipd.NearYou.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity(name = "rents")
@Table(
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"user_email", "sensor_id", "start_time"})
    })
public class Rent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_email", referencedColumnName = "email", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sensor_id", referencedColumnName = "id", nullable = false)
  private Sensor sensor;

  @Column(name = "start_time", nullable = false)
  private Timestamp startTime;

  @Column(name = "end_time")
  private Timestamp endTime;

  protected Rent() {}

  private Rent(RentBuilder builder) {
    this.user = builder.user;
    this.sensor = builder.sensor;
    this.startTime = builder.startTime;
    this.endTime = builder.endTime;
  }

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public Sensor getSensor() {
    return sensor;
  }

  public Timestamp getStartTime() {
    return startTime;
  }

  public Timestamp getEndTime() {
    return endTime;
  }

  public static class RentBuilder {
    private User user;
    private Sensor sensor;
    private Timestamp startTime;
    private Timestamp endTime;

    public RentBuilder setUser(final User user) {
      this.user = user;
      return this;
    }

    public RentBuilder setSensor(final Sensor sensor) {
      this.sensor = sensor;
      return this;
    }

    public RentBuilder setStartTime(final Timestamp startTime) {
      this.startTime = startTime;
      return this;
    }

    public RentBuilder setEndTime(final Timestamp endTime) {
      this.endTime = endTime;
      return this;
    }

    public Rent build() {
      return new Rent(this);
    }
  }
}
