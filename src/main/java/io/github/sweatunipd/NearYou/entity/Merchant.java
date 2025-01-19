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

  public Merchant(MerchantBuilder builder) {
    this.activityName = builder.activityName;
    this.email = builder.email;
    this.vat = builder.vat;
  }

  protected Merchant() {}

  public String getActivityName() {
    return activityName;
  }

  public String getEmail() {
    return email;
  }

  public String getVat() {
    return vat;
  }

  public static class MerchantBuilder {
    private String vat;
    private String email;
    private String activityName;

    public MerchantBuilder setVat(String vat) {
      this.vat = vat;
      return this;
    }

    public MerchantBuilder setEmail(String email) {
      this.email = email;
      return this;
    }

    public MerchantBuilder setActivityName(String activityName) {
      this.activityName = activityName;
      return this;
    }

    public Merchant build() {
      return new Merchant(this);
    }
  }
}
