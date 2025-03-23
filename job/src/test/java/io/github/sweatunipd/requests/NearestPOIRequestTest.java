package io.github.sweatunipd.requests;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.sweatunipd.database.DatabaseConnectionSingleton;
import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.entity.PointOfInterest;
import io.r2dbc.spi.*;
import java.util.List;
import java.util.function.BiFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class NearestPOIRequestTest {

  @Mock private ConnectionFactory connectionFactory;

  @Mock private Connection connection;

  @Mock private Statement statement;

  @Mock private Result result;

  @Mock private Row row;

  @Mock private ResultFuture<Tuple2<GPSData, PointOfInterest>> resultFuture;

  private NearestPOIRequest nearestPOIRequest;
  private GPSData gpsData;

  @BeforeEach
  void setUp() {
    nearestPOIRequest = new NearestPOIRequest();
    gpsData = new GPSData(1, 78.5f, 78.5f);
  }

  @Test
  void testReturnPOI() {
    try(MockedStatic<DatabaseConnectionSingleton> mockedStatic = Mockito.mockStatic(DatabaseConnectionSingleton.class)) {
      mockedStatic.when(DatabaseConnectionSingleton::getConnection).thenReturn(connectionFactory);
      Mockito.when(connectionFactory.create()).thenAnswer(invocation -> Mono.just(connection));
      Mockito.when(connection.createStatement(Mockito.anyString())).thenReturn(statement);
      Mockito.when(statement.bind(Mockito.anyString(), Mockito.any())).thenReturn(statement);
      Mockito.when(statement.execute()).thenAnswer(invocation -> Flux.just(result));
      Mockito.when(result.map(Mockito.<BiFunction<Row, RowMetadata, ?>>any())).thenAnswer(invocation -> Flux.just(row));
      Mockito.when(row.get("latitude", Float.class)).thenReturn(78.5f);
      Mockito.when(row.get("longitude", Float.class)).thenReturn(78.5f);
      Mockito.when(row.get("vat", String.class)).thenReturn("IT1234");
      Mockito.when(row.get("name", String.class)).thenReturn("Test");
      Mockito.when(row.get("category", String.class)).thenReturn("Test");
      Mockito.when(row.get("offer", String.class)).thenReturn("Test");

      nearestPOIRequest.asyncInvoke(gpsData, resultFuture);
    }
  }

  @Test
  void testNoPOI(){
    try(MockedStatic<DatabaseConnectionSingleton> mockedStatic = Mockito.mockStatic(DatabaseConnectionSingleton.class)) {
      mockedStatic.when(DatabaseConnectionSingleton::getConnection).thenReturn(connectionFactory);
      Mockito.when(connectionFactory.create()).thenAnswer(invocation -> Mono.just(connection));
      Mockito.when(connection.createStatement(Mockito.anyString())).thenReturn(statement);
      Mockito.when(statement.bind(Mockito.anyString(), Mockito.any())).thenReturn(statement);
      Mockito.when(statement.execute()).thenAnswer(invocation -> Flux.just(result));
      Mockito.when(result.map(Mockito.<BiFunction<Row, RowMetadata, ?>>any())).thenAnswer(invocation -> Flux.empty());

      nearestPOIRequest.asyncInvoke(gpsData, resultFuture);
    }
  }

  @Test
  void testOnErrorCase() {
    try(MockedStatic<DatabaseConnectionSingleton> mockedStatic = Mockito.mockStatic(DatabaseConnectionSingleton.class)) {
      mockedStatic.when(DatabaseConnectionSingleton::getConnection).thenReturn(connectionFactory);
      Mockito.when(connectionFactory.create()).thenAnswer(invocation -> Mono.just(connection));
      Mockito.when(connection.createStatement(Mockito.anyString()))
          .thenThrow(new RuntimeException("Test error"));

      // Logger
      Logger logger = (Logger) LoggerFactory.getLogger(NearestPOIRequest.class);
      ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
      listAppender.start();
      logger.addAppender(listAppender);

      nearestPOIRequest.asyncInvoke(gpsData, resultFuture);

      List<ILoggingEvent> logsList = listAppender.list;
      Assertions.assertEquals(1, logsList.size());
      Assertions.assertEquals("Test error", logsList.get(0).getMessage());
    }
  }
}
