package io.github.sweatunipd;

import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.entity.PointOfInterest;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple2;
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

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DataStreamJob {

  public static void main(String[] args) throws Exception {
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.enableCheckpointing(60000);
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

    DataStreamSource<GPSData> kafka =
        env.fromSource(source, WatermarkStrategy.noWatermarks(), "GPS Data - Kafka Queue");

    kafka
        .addSink(
            JdbcSink.sink(
                "INSERT INTO positions (rent_id, latitude, longitude) VALUES (?, ?, ?)",
                (statement, gpsData) -> {
                  statement.setInt(1, gpsData.getRentId());
                  statement.setFloat(2, gpsData.getLatitude());
                  statement.setFloat(3, gpsData.getLongitude());
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

    DataStream<Tuple2<Integer, PointOfInterest>> interestedPOI =
        AsyncDataStream.unorderedWait(
            kafka, new NearestPOIRequest(), 1000, TimeUnit.MILLISECONDS, 1000);

    DataStream<Tuple2<Integer, String>> generatedAdvertisement =
        AsyncDataStream.unorderedWait(
            interestedPOI,
            new AdvertisementGenerationRequest(),
            30000,
            TimeUnit.MILLISECONDS,
            1000);

    KafkaSink<Tuple2<Integer, String>> kafkaSink =
        KafkaSink.<Tuple2<Integer, String>>builder()
            .setBootstrapServers("localhost:9094")
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder()
                    .setTopic("adv-data")
                    .setValueSerializationSchema(new AdvertisementSerializationSchema())
                    .build())
            .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
            .build();

    generatedAdvertisement.sinkTo(kafkaSink);

    env.execute("Kafka to PostgreSQL");
  }
}
