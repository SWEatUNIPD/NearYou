package io.github.sweatunipd;

import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.entity.PointOfInterest;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DataStreamJob {

  public static void main(String[] args) throws Exception {
    // Execution Environment Configuration
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // Kafka Source Configuration
    Properties props = new Properties();
    KafkaSource<GPSData> source =
        KafkaSource.<GPSData>builder()
            .setBootstrapServers("localhost:9094")
            .setProperties(props)
            .setTopics("gps-data")
            .setGroupId(UUID.randomUUID().toString())
            .setStartingOffsets(OffsetsInitializer.latest())
            .setValueOnlyDeserializer(new GPSDataDeserializationSchema())
            .build();

    // Kafka Entering Queue Data stream
    DataStreamSource<GPSData> kafka =
        env.fromSource(source, WatermarkStrategy.noWatermarks(), "GPS Data");

    // Sink of the transmitted positions in DB
    kafka
        .filter(Objects::nonNull)
        .addSink(
            JdbcSink.sink(
                "INSERT INTO positions (time_stamp, rent_id, latitude, longitude) VALUES (?, ?::UUID, ?, ?)",
                (statement, gpsData) -> {
                  if (gpsData != null) {
                    statement.setTimestamp(1, gpsData.getTimestamp());
                    statement.setString(2, gpsData.getRentId().toString());
                    statement.setFloat(3, gpsData.getLatitude());
                    statement.setFloat(4, gpsData.getLongitude());
                  }
                },
                JdbcExecutionOptions.builder()
                    .withBatchSize(1000)
                    .withBatchIntervalMs(100)
                    .withMaxRetries(5)
                    .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                    .withUrl("jdbc:postgresql://localhost:5432/admin")
                    .withDriverName("org.postgresql.Driver")
                    .withUsername("admin")
                    .withPassword("adminadminadmin")
                    .build()))
        .name("Sink GPS Data in Database");

    // Data Stream of the interested POI for every single position in range
    DataStream<Tuple2<UUID, PointOfInterest>> interestedPOI =
        AsyncDataStream.unorderedWait(
            kafka, new NearestPOIRequest(), 1000, TimeUnit.MILLISECONDS, 1000);

    // Data Stream of generated advertisements
    DataStream<Tuple3<UUID, Integer, String>> generatedAdvertisement =
        AsyncDataStream.unorderedWait(
            interestedPOI,
            new AdvertisementGenerationRequest(),
            30000,
            TimeUnit.MILLISECONDS,
            1000);

    // Configuration of the Kafka Sink
    KafkaSink<Tuple3<UUID, Integer, String>> kafkaSink =
        KafkaSink.<Tuple3<UUID, Integer, String>>builder()
            .setBootstrapServers("localhost:9094")
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder()
                    .setTopic("adv-data")
                    .setValueSerializationSchema(new AdvertisementSerializationSchema())
                    .build())
            .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
            .build();

    // Sink of the successfully generated advertisements to the users via Kafka exiting queue
    generatedAdvertisement.filter(adv-> !adv.f2.isEmpty()).sinkTo(kafkaSink);

    // Sink of the generated advertisement in DB
    generatedAdvertisement.addSink(
        JdbcSink.sink(
            "INSERT INTO advertisements VALUES (?::UUID, ?, ?)",
            (preparedStatement, advertisement) -> {
              preparedStatement.setString(1, advertisement.f0.toString());
              preparedStatement.setInt(2, advertisement.f1);
              preparedStatement.setString(3, advertisement.f2);
            },
            JdbcExecutionOptions.builder()
                .withBatchSize(1000)
                .withBatchIntervalMs(100)
                .withMaxRetries(5)
                .build(),
            new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                .withUrl("jdbc:postgresql://localhost:5432/admin")
                .withDriverName("org.postgresql.Driver")
                .withUsername("admin")
                .withPassword("adminadminadmin")
                .build()));

    // Flink Job Execution
    env.execute("Kafka to PostgreSQL");
  }
}
