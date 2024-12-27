package io.github.sweatunipd.NearYou;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NearYouApplication {

  public static void main(String[] args) {
    SpringApplication.run(NearYouApplication.class, args);
  }

  @Bean
  CommandLineRunner commandLineRunner() {
    return args -> {
    };
  }
}
