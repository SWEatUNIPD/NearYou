package io.github.sweatunipd.NearYou.entity;

import jakarta.persistence.*;

@Entity(name = "users")
public class User {
  @Id private String email;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String surname;

  @Column(nullable = false)
  @Enumerated(EnumType.ORDINAL)
  private Gender gender;

  @Column(nullable = false)
  private int age;

  protected User() {} // Thx Intellij

  private User(UserBuilder builder) {
    this.email = builder.email;
    this.name = builder.name;
    this.surname = builder.surname;
    this.gender = builder.gender;
    this.age = builder.age;
  }

  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }

  public String getSurname() {
    return surname;
  }

  public Gender getGender() {
    return gender;
  }

  public int getAge() {
    return age;
  }

  public static class UserBuilder {
    private String email;
    private String name;
    private String surname;
    private Gender gender;
    private int age;

    public UserBuilder setEmail(final String email) {
      this.email = email;
      return this;
    }

    public UserBuilder setName(final String name) {
      this.name = name;
      return this;
    }

    public UserBuilder setSurname(final String surname) {
      this.surname = surname;
      return this;
    }

    public UserBuilder setGender(final Gender gender) {
      this.gender = gender;
      return this;
    }

    public UserBuilder setAge(final int age) {
      this.age = age;
      return this;
    }

    public User build() {
      return new User(this);
    }
  }
}
