package io.github.sweatunipd.NearYou;

import io.github.sweatunipd.NearYou.entity.Gender;
import io.github.sweatunipd.NearYou.entity.Merchant.MerchantBuilder;
import io.github.sweatunipd.NearYou.entity.PointOfInterest.PointOfInterestBuilder;
import io.github.sweatunipd.NearYou.entity.Rent.RentBuilder;
import io.github.sweatunipd.NearYou.entity.Sensor.SensorBuilder;
import io.github.sweatunipd.NearYou.entity.User.UserBuilder;
import io.github.sweatunipd.NearYou.repository.*;
import java.sql.Timestamp;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NearYouApplication {

  private final MerchantRepository merchantRepository;

  public NearYouApplication(MerchantRepository merchantRepository) {
    this.merchantRepository = merchantRepository;
  }

  public static void main(String[] args) {
    SpringApplication.run(NearYouApplication.class, args);
  }

  @Bean
  CommandLineRunner commandLineRunner(
      UserRepository userRepository,
      SensorRepository sensorRepository,
      RentRepository rentRepository,
      MerchantRepository merchantRepository,
      PointOfInterestRepository poiRepository) {

    return args -> {
      addUser(userRepository);
      addSensor(sensorRepository);
      addRent(userRepository, sensorRepository, rentRepository);
      addPOI(merchantRepository, poiRepository);
    };
  }

  public void addUser(UserRepository userRepository) {
    userRepository.save(
        new UserBuilder()
            .setEmail("merjakla03@gmail.com")
            .setName("Klaudio")
            .setSurname("Merja")
            .setAge(21)
            .setGender(Gender.MALE)
            .build());
  }

  public void addSensor(SensorRepository sensorRepository) {
    sensorRepository.save(new SensorBuilder().build());
  }

  public void addRent(
      UserRepository userRepository,
      SensorRepository sensorRepository,
      RentRepository rentRepository) {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    rentRepository.save(
        new RentBuilder()
            .setStartTime(timestamp)
            .setUser(
                userRepository
                    .findById("merjakla03@gmail.com")
                    .orElseThrow(() -> new IllegalArgumentException("Problem 1")))
            .setSensor(
                sensorRepository
                    .findById(1)
                    .orElseThrow(() -> new IllegalArgumentException("Problem 2")))
            .build());
  }

  public void addPOI(
      MerchantRepository merchantRepository, PointOfInterestRepository poiRepository) {
    merchantRepository.save(
        new MerchantBuilder()
            .setEmail("info@acme.com")
            .setVat("IT010101")
            .setActivityName("ACME")
            .build());
    poiRepository.save(
        new PointOfInterestBuilder()
            .setLatitude(45.38631f)
            .setLongitude(11.86328f)
            .setMerchant(
                merchantRepository.findById("IT010101").orElseThrow(IllegalArgumentException::new))
            .build());
  }
}
