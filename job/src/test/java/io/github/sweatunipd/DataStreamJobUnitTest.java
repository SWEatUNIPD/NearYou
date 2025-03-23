package io.github.sweatunipd;

import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.entity.PointOfInterest;
import io.github.sweatunipd.requests.AdvertisementGenerationRequest;
import io.github.sweatunipd.requests.NearestPOIRequest;
import io.github.sweatunipd.service.KafkaTopicService;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
public class DataStreamJobUnitTest {
    @Mock
    private StreamExecutionEnvironment env;
    @Mock
    private KafkaTopicService topicService;
    @Mock
    private DataStreamSource<GPSData> kafkaSource;
    @Mock
    private DataStreamSink<GPSData> gpsDataDataStreamSink;
    @Mock
    private SingleOutputStreamOperator<Tuple2<GPSData, PointOfInterest>> interestedPOI;
    @Mock
    private SingleOutputStreamOperator<Tuple3<GPSData, PointOfInterest, String>> generatedAdvertisement;

    private DataStreamJob dataStreamJob;

    @BeforeEach
    void setUp() {
        dataStreamJob = new DataStreamJob(env, topicService);
    }

    @Test
    @DisplayName("Job execution")
    void test() throws Exception {
        //Mock of topic creation
        Mockito.doNothing().when(topicService).createTopics("gps-data", "adv-data");

        //Mock of gps-data Kafka Source
        Mockito.when(env.fromSource(Mockito.<KafkaSource<GPSData>>any(), Mockito.any(), Mockito.anyString())).thenReturn(kafkaSource);
        Mockito.when(kafkaSource.filter(Mockito.any())).thenReturn(kafkaSource);
        Mockito.when(kafkaSource.addSink(Mockito.any())).thenReturn(gpsDataDataStreamSink);
        Mockito.when(gpsDataDataStreamSink.name(Mockito.anyString())).thenReturn(gpsDataDataStreamSink);

        try (MockedStatic<AsyncDataStream> mockedAsyncDataStream = Mockito.mockStatic(AsyncDataStream.class)) {
            mockedAsyncDataStream.when(() -> AsyncDataStream.unorderedWait(
                            Mockito.same(kafkaSource),
                            Mockito.any(NearestPOIRequest.class),
                            Mockito.anyLong(),
                            Mockito.eq(TimeUnit.MILLISECONDS),
                            Mockito.anyInt()))
                    .thenReturn(interestedPOI);

            mockedAsyncDataStream.when(() -> AsyncDataStream.unorderedWait(
                            Mockito.same(interestedPOI),
                            Mockito.any(AdvertisementGenerationRequest.class),
                            Mockito.anyLong(),
                            Mockito.eq(TimeUnit.MILLISECONDS),
                            Mockito.anyInt()))
                    .thenReturn(generatedAdvertisement);

            Mockito.when(generatedAdvertisement.filter(Mockito.any())).thenReturn(generatedAdvertisement);
            Mockito.when(generatedAdvertisement.sinkTo(Mockito.<KafkaSink<Tuple3<GPSData, PointOfInterest, String>>>any())).thenReturn(Mockito.mock());
            Mockito.when(generatedAdvertisement.addSink(Mockito.any())).thenReturn(Mockito.mock());
            Mockito.when(env.execute(Mockito.anyString())).thenReturn(Mockito.mock(JobExecutionResult.class));

            dataStreamJob.execute();
        }
    }
}