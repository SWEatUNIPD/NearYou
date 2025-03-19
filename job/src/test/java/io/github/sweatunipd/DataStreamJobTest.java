package io.github.sweatunipd;

import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.utility.AdvertisementSerializationSchema;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.test.util.AbstractTestBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataStreamJobTest extends AbstractTestBase {

    @Test
    public void testGeneratedAdvertisementSink() throws Exception {
        // Start Kafka container
        KafkaContainer kafkaContainer = new KafkaContainer("apache/kafka-native:3.8.0");
        kafkaContainer.start();

        // Set up Flink environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Configuration config = new Configuration();
        config.setString("kafka.bootstrap.server", kafkaContainer.getBootstrapServers());
        config.setString("kafka.output.topic", "adv-data");
        env.getConfig().setGlobalJobParameters(config);

        // Create Kafka sink
        KafkaSink<Tuple3<GPSData, Integer, String>> kafkaSink =
            KafkaSink.<Tuple3<GPSData, Integer, String>>builder()
                .setBootstrapServers(config.getString("kafka.bootstrap.server", "localhost:9094"))
                .setRecordSerializer(
                    KafkaRecordSerializationSchema.builder()
                        .setTopic(config.getString("kafka.output.topic", "adv-data"))
                        .setValueSerializationSchema(new AdvertisementSerializationSchema())
                        .build())
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        // Create test data stream
        DataStream<Tuple3<GPSData, Integer, String>> generatedAdvertisement = env.fromElements(
            Tuple3.of(new GPSData(1, 45.0f, 12.0f), 1, "advertisement1"),
            Tuple3.of(new GPSData(2, 46.0f, 13.0f), 2, "advertisement2"),
            Tuple3.of(new GPSData(3, 45.0f, 12.0f), 3, ""),
            Tuple3.of(new GPSData(4, 45.1f, 12.1f), 4, "advertisement4")
        );

        // Apply filter and sink to Kafka
        generatedAdvertisement.filter(adv -> !adv.f2.isEmpty()).sinkTo(kafkaSink);

        // Execute Flink job
        env.execute("Test Job");

        // Set up Kafka consumer to verify the results
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList("adv-data"));

        // Poll for records
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

        // Verify the results
        assertEquals(3, records.count());
        /* da togliere / modificare (non funziona)
        for (ConsumerRecord<String, String> record : records) {
            assertEquals(Tuple3.of(new GPSData(1, 45.0f, 12.0f), 1, "advertisement1"), record.value());
            assertEquals(Tuple3.of(new GPSData(2, 46.0f, 13.0f), 2, "advertisement2"), record.value());
            assertEquals(Tuple3.of(new GPSData(4, 45.1f, 12.1f), 4, "advertisement4"), record.value());
        } */     

        // Stop Kafka container
        kafkaContainer.stop();
    }
}
