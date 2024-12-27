package io.github.sweatunipd.NearYou.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "merchants")
public class Merchant {
  @Id private String vat;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String activityName;

  public Merchant(String activityName, String email, String vat) {
    this.activityName = activityName;
    this.email = email;
    this.vat = vat;
  }

  public Merchant() {
  }

  public String getActivityName() {
    return activityName;
  }

  public void setActivityName(String activityName) {
    this.activityName = activityName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getVat() {
    return vat;
  }

  public void setVat(String vat) {
    this.vat = vat;
  }
}
