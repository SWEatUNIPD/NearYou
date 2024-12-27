package io.github.sweatunipd.NearYou.entity;

import jakarta.persistence.*;

@Entity(name = "users")
public class User {
  @Id
  private String email;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String surname;

  @Column(nullable = false)
  @Enumerated(EnumType.ORDINAL)
  private Gender gender;

  @Column(nullable = false)
  private int age;

  public User(int age, String email, Gender gender, String name, String surname) {
    this.age = age;
    this.email = email;
    this.gender = gender;
    this.name = name;
    this.surname = surname;
  }

  public User() {
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }
}
