package io.github.sweatunipd.NearYou;

import io.github.sweatunipd.NearYou.entity.Gender;
import io.github.sweatunipd.NearYou.entity.Rent.RentBuilder;
import io.github.sweatunipd.NearYou.entity.Sensor.SensorBuilder;
import io.github.sweatunipd.NearYou.entity.User.UserBuilder;
import io.github.sweatunipd.NearYou.repository.RentRepository;
import io.github.sweatunipd.NearYou.repository.SensorRepository;
import io.github.sweatunipd.NearYou.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.sql.Timestamp;

@SpringBootApplication
public class NearYouApplication {

  public static void main(String[] args) {
    SpringApplication.run(NearYouApplication.class, args);
  }

  @Bean
  CommandLineRunner commandLineRunner(
      UserRepository userRepository,
      SensorRepository sensorRepository,
      RentRepository rentRepository) {

    return args -> {
      addUser(userRepository);
      addSensor(sensorRepository);
      addRent(userRepository, sensorRepository, rentRepository);
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
    rentRepository.save(new RentBuilder()
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
}
