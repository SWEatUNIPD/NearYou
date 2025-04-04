package io.github.sweatunipd;

import io.github.sweatunipd.dto.GPSDataDto;
import io.github.sweatunipd.model.GPSData;
import io.github.sweatunipd.model.PointOfInterest;
import io.github.sweatunipd.requests.AdvertisementGenerationRequest;
import io.github.sweatunipd.requests.NearestPOIRequest;
import io.github.sweatunipd.service.KafkaTopicService;
import io.github.sweatunipd.utility.AdvertisementSerializationSchema;
import io.github.sweatunipd.utility.GPSDataDeserializationSchema;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestartStrategyOptions;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataStreamJob {

  private final StreamExecutionEnvironment env;
  private final KafkaTopicService topicService;

  public DataStreamJob(StreamExecutionEnvironment env, KafkaTopicService topicService) {
    this.env = env;
    this.topicService = topicService;
  }

  public void execute() throws Exception {
    // Topic creation
    topicService.createTopic("gps-data", 3, (short) 1);
    topicService.createTopic("adv-data", 3, (short) 1);

    // Stream source
    Properties props = new Properties();
    KafkaSource<GPSDataDto> source =
        KafkaSource.<GPSDataDto>builder()
            .setBootstrapServers(System.getProperty("kafka.bootstrap.servers", "kafka:9092"))
            .setProperties(props)
            .setTopics("gps-data")
            .setGroupId("nearyou-group")
            .setStartingOffsets(OffsetsInitializer.latest())
            .setValueOnlyDeserializer(new GPSDataDeserializationSchema())
            .build();
    DataStreamSource<GPSDataDto> kafkaSource =
        env.fromSource(source, WatermarkStrategy.noWatermarks(), "GPS Data");

    // Converting DTO in a formal entity
    DataStream<GPSData> gpsDataStream =
        kafkaSource
            .filter(Objects::nonNull)
            .map(
                gpsDataDto ->
                    new GPSData(
                        gpsDataDto.getRentId(),
                        gpsDataDto.getLatitude(),
                        gpsDataDto.getLongitude(),
                        new Timestamp(gpsDataDto.getTimestamp())));

    // JDBC Connection Option
    JdbcConnectionOptions jdbcConnectionOptions =
        new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
            .withUrl(System.getProperty("jdbc.url", "jdbc:postgresql://postgis:5432/admin"))
            .withDriverName("org.postgresql.Driver")
            .withUsername("admin") // DB username
            .withConnectionCheckTimeoutSeconds(10)
            .withPassword("adminadminadmin") // DB password
            .build();

    // Sink positions in DB
    gpsDataStream
        .addSink(
            JdbcSink.sink(
                "INSERT INTO positions (time_stamp, rent_id, latitude, longitude) VALUES (?, ?, ?, ?)",
                (statement, gpsData) -> {
                  statement.setTimestamp(1, gpsData.timestamp());
                  statement.setInt(2, gpsData.rentId());
                  statement.setFloat(3, gpsData.latitude());
                  statement.setFloat(4, gpsData.longitude());
                },
                JdbcExecutionOptions.builder()
                    .withBatchSize(1000)
                    .withBatchIntervalMs(100)
                    .withMaxRetries(5)
                    .build(),
                jdbcConnectionOptions))
        .name("Sink GPS Data in Database");

    // Data Stream of the interested POI for every single position in range
    DataStream<Tuple2<GPSData, PointOfInterest>> interestedPOI =
        AsyncDataStream.unorderedWait(
            gpsDataStream, new NearestPOIRequest(), 1000, TimeUnit.MILLISECONDS, 1000);

    // Data Stream of generated advertisements
    DataStream<Tuple3<GPSData, PointOfInterest, String>> generatedAdvertisement =
        AsyncDataStream.unorderedWait(
            interestedPOI,
            new AdvertisementGenerationRequest(),
            30000,
            TimeUnit.MILLISECONDS,
            1000);

    // Configuration of the Kafka Sink
    KafkaSink<Tuple3<GPSData, PointOfInterest, String>> kafkaSink =
        KafkaSink.<Tuple3<GPSData, PointOfInterest, String>>builder()
            .setBootstrapServers(System.getProperty("kafka.bootstrap.servers", "kafka:9092"))
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder()
                    .setTopic("adv-data")
                    .setValueSerializationSchema(new AdvertisementSerializationSchema())
                    .build())
            .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
            .build();

    // Sink of the successfully generated advertisements to the users via Kafka exiting queue
    generatedAdvertisement.filter(adv -> !adv.f2.isEmpty()).sinkTo(kafkaSink);

    // Sink of the generated advertisement in DB
    generatedAdvertisement.addSink(
        JdbcSink.sink(
            "INSERT INTO advertisements(latitude_poi, longitude_poi, position_time_stamp, position_rent_id, adv) VALUES (?, ?, ?, ?, ?)",
            (preparedStatement, advertisement) -> {
              preparedStatement.setFloat(1, advertisement.f1.latitude());
              preparedStatement.setFloat(2, advertisement.f1.longitude());
              preparedStatement.setTimestamp(3, advertisement.f0.timestamp());
              preparedStatement.setInt(4, advertisement.f0.rentId());
              preparedStatement.setString(5, advertisement.f2);
            },
            JdbcExecutionOptions.builder()
                .withBatchSize(1000)
                .withBatchIntervalMs(100)
                .withMaxRetries(5)
                .build(),
            jdbcConnectionOptions));

    // Job execution
    env.execute("NearYou - Smart Custom Advertising Platform");
  }

  public static final Logger LOG = LoggerFactory.getLogger(DataStreamJob.class);

  private static final Properties KAFKA_ADMIN_PROPS;
  private static final Configuration STREAM_EXECUTION_ENVIRONMENT_CONFIG;

  static {
    KAFKA_ADMIN_PROPS = new Properties();
    KAFKA_ADMIN_PROPS.put(
        AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
        System.getProperty("kafka.bootstrap.servers", "kafka:9092"));
    STREAM_EXECUTION_ENVIRONMENT_CONFIG = new Configuration();
    STREAM_EXECUTION_ENVIRONMENT_CONFIG.set(RestartStrategyOptions.RESTART_STRATEGY, "fixed-delay");
    STREAM_EXECUTION_ENVIRONMENT_CONFIG.set(
        RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS,
        3); // number of restart attempts
    STREAM_EXECUTION_ENVIRONMENT_CONFIG.set(
        RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY, Duration.ofSeconds(10));
  }

  public static void main(String[] args) throws Exception {
    LOG.info("Job starting - NearYou");

    try (Admin kafkaAdmin = AdminClient.create(KAFKA_ADMIN_PROPS)) {
      StreamExecutionEnvironment env =
          StreamExecutionEnvironment.getExecutionEnvironment(STREAM_EXECUTION_ENVIRONMENT_CONFIG);
      // generati il prima possibile, altrimenti possiamo farne a meno di mandare
      // l'annuncio?
      DataStreamJob job = new DataStreamJob(env, new KafkaTopicService(kafkaAdmin));
      job.execute();
    }
  }
}
