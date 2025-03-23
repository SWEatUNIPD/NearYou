package io.github.sweatunipd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.sql.*;
import java.util.Properties;

import io.github.sweatunipd.service.KafkaTopicService;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import javax.xml.crypto.Data;

@Testcontainers
public class DataStreamJobIntegrationTest {

  private static final Logger LOG = LoggerFactory.getLogger(DataStreamJobIntegrationTest.class);

  @Container
  public static final KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

@Container
  public static final PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>(
              DockerImageName.parse("postgis/postgis:17-3.5").asCompatibleSubstituteFor("postgres"))
          .withDatabaseName("admin")
          .withUsername("admin")
          .withPassword("adminadminadmin")
                  .withInitScript("create.sql");

  private MiniClusterWithClientResource miniCluster;

  @BeforeEach
  public void setUp() throws Exception {
    Configuration configuration = new Configuration();
    ConfigOption<String> javaOpts = ConfigOptions.key("env.java.opts").stringType().noDefaultValue();
    configuration.set(javaOpts, "--add-opens java.base/java.util=ALL-UNNAMED");

    miniCluster =
        new MiniClusterWithClientResource(
            new MiniClusterResourceConfiguration.Builder()
                .setNumberTaskManagers(1)
                .setNumberSlotsPerTaskManager(1)
                .build());
    miniCluster.before();
    System.setProperty("kafka.bootstrap.servers", kafka.getBootstrapServers());
    System.setProperty("jdbc.url", postgres.getJdbcUrl());
    System.setProperty("postgres.hostname", postgres.getHost());
    System.setProperty("postgres.port", String.valueOf(postgres.getFirstMappedPort()));
    System.setProperty("postgres.username", postgres.getUsername());
    System.setProperty("postgres.password", postgres.getPassword());
    System.setProperty("postgres.dbname", postgres.getDatabaseName());
    kafka.waitingFor(Wait.forListeningPort());
    postgres.waitingFor(Wait.forListeningPort());
    kafka.start();
    postgres.start();
  }

  @AfterEach
  public void tearDown() {
    miniCluster.after();
  }

  @Test
  @DisplayName("Test if Postgres works")
  public void testPostgres() {
    Properties dbProps = new Properties();
    dbProps.put("user", "admin");
    dbProps.put("password", "adminadminadmin");
    try (Connection connection =
            DriverManager.getConnection(
                System.getProperty("jdbc.url", "jdbc:postgresql://postgis:5432/admin"), dbProps);
        PreparedStatement stmt =
            connection.prepareStatement("select count(*) from points_of_interest");
        ResultSet resultSet = stmt.executeQuery()) {
      int count = -1;
      if (resultSet.next()) {
        count = resultSet.getInt(1);
      }
      Assertions.assertTrue(count > 0);
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  @Test
  @DisplayName("Flink Processing Test")
  void testFlink() throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    Properties adminProps = new Properties();
    adminProps.put(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
            System.getProperty("kafka.bootstrap.servers", "kafka:9092"));
    KafkaTopicService topicService = new KafkaTopicService(Admin.create(adminProps));
    DataStreamJob dataStreamJob = new DataStreamJob(env, topicService);
    Thread flinkJob = new Thread(() -> {
        try {
          dataStreamJob.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    });
    flinkJob.start();

    Thread.sleep(10000);

    Thread kafkaProducer = new Thread(() -> {
      Properties producerProps = new Properties();
      producerProps.put(
              ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, System.getProperty("kafka.bootstrap.servers"));
      producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
      producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
      try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("trackerId", 1);
        node.put("latitude", 78.5f);
        node.put("longitude", 78.5f);

        String jsonStringify = objectMapper.writeValueAsString(node);

        ProducerRecord<String, String> record =
                new ProducerRecord<>(
                        "gps-data", jsonStringify);
        producer.send(record);
      } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
      }
    });
    kafkaProducer.start();


    Thread.sleep(20000);

    Properties dbProps = new Properties();
    dbProps.put("user", "admin");
    dbProps.put("password", "adminadminadmin");
    try (Connection connection =
            DriverManager.getConnection(
                System.getProperty("jdbc.url", "jdbc:postgresql://postgis:5432/admin"), dbProps);
        PreparedStatement stmt = connection.prepareStatement("select count(*) from positions");
        ResultSet resultSet = stmt.executeQuery();
         PreparedStatement stmt2 = connection.prepareStatement("select count(*) from advertisements");
         ResultSet resultSet2 = stmt2.executeQuery()) {
      int countPositions = -1;
      if (resultSet.next()) {
        countPositions = resultSet.getInt(1);
      }
      int countAdvertisements = -1;
      if (resultSet2.next()) {
        countAdvertisements = resultSet2.getInt(1);
      }
      Assertions.assertEquals(1, countPositions);
      Assertions.assertEquals(1, countAdvertisements);
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
    }
  }
}
