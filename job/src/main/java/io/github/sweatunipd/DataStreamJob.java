package io.github.sweatunipd;

import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.entity.PointOfInterest;
import io.github.sweatunipd.requests.AdvertisementGenerationRequest;
import io.github.sweatunipd.requests.NearestPOIRequest;
import io.github.sweatunipd.service.KafkaTopicService;
import io.github.sweatunipd.utility.AdvertisementSerializationSchema;
import io.github.sweatunipd.utility.GPSDataDeserializationSchema;
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
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DataStreamJob {

    private final StreamExecutionEnvironment env;
    private final KafkaTopicService topicService;
    private JobClient jobClient;

    public DataStreamJob(StreamExecutionEnvironment env, KafkaTopicService topicService) {
        this.env = env;
        this.topicService = topicService;
    }

    public void execute() throws Exception {
        //Topic creation
        topicService.createTopics("gps-data", "adv-data"); //FIXME: atm it has single replication factor and one partition

        //Stream source
        Properties props = new Properties();
        props.put("auto.offset.reset", "latest");
        KafkaSource<GPSData> source = KafkaSource.<GPSData>builder()
                .setBootstrapServers(System.getProperty("kafka.bootstrap.servers", "kafka:9092"))
                .setProperties(props)
                .setTopics("gps-data")
                .setGroupId("nearyou-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new GPSDataDeserializationSchema())
                .build();
        DataStreamSource<GPSData> kafkaSource =
                env.fromSource(source, WatermarkStrategy.noWatermarks(), "GPS Data");
        kafkaSource.map((gpsData) -> {
            System.out.println(gpsData);
            return gpsData;
        });

        // JDBC Connection Option
        JdbcConnectionOptions jdbcConnectionOptions = new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                .withUrl(System.getProperty("jdbc.url", "jdbc:postgresql://postgis:5432/admin"))
                .withDriverName("org.postgresql.Driver")
                .withUsername("admin") // DB username
                .withPassword("adminadminadmin") // DB password
                .build();

        //Sink positions in DB
        kafkaSource
                .filter(Objects::nonNull)
                .addSink(
                        JdbcSink.sink(
                                "INSERT INTO positions (time_stamp, rent_id, latitude, longitude) VALUES (?, ?, ?, ?)",
                                (statement, gpsData) -> {
                                    if (gpsData != null) {
                                        statement.setTimestamp(1, gpsData.getTimestamp());
                                        statement.setInt(2, gpsData.getRentId());
                                        statement.setFloat(3, gpsData.getLatitude());
                                        statement.setFloat(4, gpsData.getLongitude());
                                    }
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
                        kafkaSource, new NearestPOIRequest(), 1000, TimeUnit.MILLISECONDS, 1000);

        // Data Stream of generated advertisements
        DataStream<Tuple3<GPSData, PointOfInterest, String>> generatedAdvertisement =
                AsyncDataStream.unorderedWait(
                        interestedPOI,
                        new AdvertisementGenerationRequest(),
                        30000,
                        TimeUnit.MILLISECONDS,
                        1000);

        // Configuration of the Kafka Sink
        KafkaSink<Tuple3<GPSData, PointOfInterest, String>> kafkaSink = KafkaSink.<Tuple3<GPSData, PointOfInterest, String>>builder()
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
                            preparedStatement.setTimestamp(3, advertisement.f0.getTimestamp());
                            preparedStatement.setInt(4, advertisement.f0.getRentId());
                            preparedStatement.setString(5, advertisement.f2);
                        },
                        JdbcExecutionOptions.builder()
                                .withBatchSize(1000)
                                .withBatchIntervalMs(100)
                                .withMaxRetries(5)
                                .build(),
                        jdbcConnectionOptions));

        //Job execution
        jobClient=env.executeAsync("NearYou - Smart Custom Advertising Platform");
    }

    public void stopExecution() throws ExecutionException, InterruptedException {
        jobClient.cancel().get();
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
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(STREAM_EXECUTION_ENVIRONMENT_CONFIG);
            env.enableCheckpointing(30000); //FIXME: siamo sicuri serva un checkpointing per dei dati che devono essere generati il prima possibile, altrimenti possiamo farne a meno di mandare l'annuncio?
            DataStreamJob job = new DataStreamJob(env, new KafkaTopicService(kafkaAdmin));
            job.execute();
        }
    }
}
