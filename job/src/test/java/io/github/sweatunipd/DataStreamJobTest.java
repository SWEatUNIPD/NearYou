package io.github.sweatunipd;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class DataStreamJobTest {
//  @Container
//  public static final KafkaContainer kafka =
//      new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));
//
//  @Container
//  public static final PostgreSQLContainer<?> postgres =
//      new PostgreSQLContainer<>(
//              DockerImageName.parse("postgis/postgis:17-3.5").asCompatibleSubstituteFor("postgres"))
//          .withDatabaseName("admin")
//          .withUsername("admin")
//          .withPassword("adminadminadmin")
//          .withInitScript("create.sql");
//
//  @BeforeAll
//  public static void setUp() {
//    System.setProperty("kafka.bootstrap.servers", kafka.getBootstrapServers());
//    System.setProperty("jdbc.url", postgres.getJdbcUrl());
//    kafka.start();
//    postgres.start();
//  }
//
//  @Test
//  void test() throws Exception {
//    DataStreamJob.main(null);
//  }
}
