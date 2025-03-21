package io.github.sweatunipd;

import io.github.sweatunipd.database.DataSourceSingleton;
import io.github.sweatunipd.service.KafkaTopicService;
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.concurrent.*;

@Testcontainers
public class DataStreamJobIntegrationTest {
    private static MiniClusterWithClientResource cluster;

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

    @BeforeAll
    public static void setUp() {
        cluster = new MiniClusterWithClientResource(new MiniClusterResourceConfiguration.Builder().setNumberSlotsPerTaskManager(1).setNumberTaskManagers(1).withHaLeadershipControl().build());
        System.setProperty("kafka.bootstrap.servers", kafka.getBootstrapServers());
        System.setProperty("jdbc.url", postgres.getJdbcUrl());
        kafka.start();
        postgres.start();
    }

    @Test
    @DisplayName("Integration test")
    void test() throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties adminProps = new Properties();
        adminProps.put(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                System.getProperty("kafka.bootstrap.servers", "kafka:9092"));
        KafkaTopicService topicService = new KafkaTopicService(Admin.create(adminProps));
        DataStreamJob dataStreamJob = new DataStreamJob(env, topicService);
        dataStreamJob.execute();

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, System.getProperty("kafka.bootstrap.servers", "kafka:9092"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        try(KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(props)){
            String topic = "gps-data";
            String value = "{\"trackerId\":1,\"latitude\":45.420387,\"longitude\":11.818664}";

            ProducerRecord<String, String> record = new ProducerRecord<>(topic, value);
            kafkaProducer.send(record);
        }

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.schedule(()->{
            try {
                dataStreamJob.stopExecution();
                Connection connection = DataSourceSingleton.getConnection();
                PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM positions");
                ResultSet resultSet = stmt.executeQuery();
                int count = -1;
                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                }
                Assertions.assertEquals(1, count);

                stmt=connection.prepareStatement("SELECT COUNT(*) FROM advertisements");
                resultSet = stmt.executeQuery();
                count = -1;
                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                }
                Assertions.assertEquals(1, count);
            }catch(Exception e){
                e.printStackTrace();
            }
        }, 10000, TimeUnit.MILLISECONDS);
    }
}
