package io.github.sweatunipd.requests.requests;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.sweatunipd.database.DataSourceSingleton;
import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.entity.PointOfInterest;
import io.github.sweatunipd.requests.NearestPOIRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class NearestPOIRequestTest {
  @Mock private Connection connection;
  @Mock private OpenContext openContext;
  @Mock private ResultFuture<Tuple2<GPSData, PointOfInterest>> resultFuture;
  @Mock private PreparedStatement preparedStatement;
  @Mock private ResultSet resultSet;

  private NearestPOIRequest nearestPOIRequest;

  @BeforeEach
  void setUp() {
    nearestPOIRequest = new NearestPOIRequest();
  }

  @Test
  @DisplayName("open() creates the connection to the database")
  public void openAsyncJobTest() throws SQLException {
    try (MockedStatic<DataSourceSingleton> dataSourceMockedStatic = Mockito.mockStatic(DataSourceSingleton.class)) {
      dataSourceMockedStatic.when(DataSourceSingleton::getConnection).thenReturn(connection);
      nearestPOIRequest.open(openContext);
      dataSourceMockedStatic.verify(DataSourceSingleton::getConnection, Mockito.times(1));
    }
  }

  @Test
  @DisplayName("open() tries to open the connection to the database and encounters an error")
  public void failOpenAsyncJobTest() {
    try (MockedStatic<DataSourceSingleton> dataSourceMockedStatic = Mockito.mockStatic(DataSourceSingleton.class)) {
      dataSourceMockedStatic
          .when(DataSourceSingleton::getConnection)
          .thenThrow(new SQLException("SQLException encountered in DB connection opening"));
      SQLException ex =
          Assertions.assertThrows(SQLException.class, () -> nearestPOIRequest.open(openContext));
      Assertions.assertEquals("SQLException encountered in DB connection opening", ex.getMessage());
      dataSourceMockedStatic.verify(DataSourceSingleton::getConnection, Mockito.times(1));
    }
  }

  @Test
  @DisplayName("close() closes the connection to the database")
  public void closeAsyncJobTest() throws SQLException {
    try (MockedStatic<DataSourceSingleton> dataSourceMockedStatic = Mockito.mockStatic(DataSourceSingleton.class)) {
      dataSourceMockedStatic.when(DataSourceSingleton::getConnection).thenReturn(connection);
      nearestPOIRequest.open(openContext);
      dataSourceMockedStatic.verify(DataSourceSingleton::getConnection, Mockito.times(1));
      Logger logger = (Logger) LoggerFactory.getLogger(NearestPOIRequest.class);
      ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
      listAppender.start();
      logger.addAppender(listAppender);

      nearestPOIRequest.close();

      List<ILoggingEvent> logsList = listAppender.list;
      Assertions.assertEquals(0, logsList.size());
      Mockito.verify(connection, Mockito.times(1)).close();
    }
  }

  @Test
  @DisplayName(
      "close() handles the exception thrown by the closure of the connection to the database")
  public void closeAsyncJobHandleExceptionTest() throws SQLException {
    try (MockedStatic<DataSourceSingleton> dataSourceMockedStatic = Mockito.mockStatic(DataSourceSingleton.class)) {
      dataSourceMockedStatic.when(DataSourceSingleton::getConnection).thenReturn(connection);
      nearestPOIRequest.open(openContext);

      Logger logger = (Logger) LoggerFactory.getLogger(NearestPOIRequest.class);
      ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
      listAppender.start();
      logger.addAppender(listAppender);

      Mockito.doThrow(new SQLException("Error encountered in DB connection closure"))
          .when(connection)
          .close();

      nearestPOIRequest.close();

      List<ILoggingEvent> logsList = listAppender.list;
      Assertions.assertEquals(Level.ERROR, logsList.get(0).getLevel());
      Assertions.assertEquals(
          "Error encountered in DB connection closure", logsList.get(0).getMessage());
    }
  }

  @Test
  @DisplayName("Check if asyncInvoke() returns a nearest POI")
  void testNearestPOIRequest() throws SQLException {
    try (MockedStatic<DataSourceSingleton> dataSourceMockedStatic = Mockito.mockStatic(DataSourceSingleton.class)) {
      dataSourceMockedStatic.when(DataSourceSingleton::getConnection).thenReturn(connection);
      nearestPOIRequest.open(openContext);
      GPSData gpsData = new GPSData(1, 78.5f, 78.5f);
      PointOfInterest pointOfInterest =
          new PointOfInterest(1, "IT101010101", "Test", 78.5f, 78.5f, "Test", "Test");

      Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
      Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
      Mockito.when(resultSet.next()).thenReturn(true);
      Mockito.when(resultSet.getInt("id")).thenReturn(pointOfInterest.getId());
      Mockito.when(resultSet.getString("merchant_vat"))
          .thenReturn(pointOfInterest.getMerchantVAT());
      Mockito.when(resultSet.getString("name")).thenReturn(pointOfInterest.getName());
      Mockito.when(resultSet.getFloat("latitude")).thenReturn(pointOfInterest.getLatitude());
      Mockito.when(resultSet.getFloat("longitude")).thenReturn(pointOfInterest.getLongitude());
      Mockito.when(resultSet.getString("category")).thenReturn(pointOfInterest.getCategory());
      Mockito.when(resultSet.getString("description")).thenReturn(pointOfInterest.getOffer());

      Tuple2<GPSData, PointOfInterest> tuple = new Tuple2<>(gpsData, pointOfInterest);

      nearestPOIRequest.asyncInvoke(gpsData, resultFuture);

      Mockito.verify(resultFuture, Mockito.timeout(3000)).complete(Collections.singleton(tuple));
    }
  }

  @Test
  @DisplayName("check if asyncInvoke() doesnt find a nearest POI")
  void testNoPOIOnRange() throws SQLException {
    try (MockedStatic<DataSourceSingleton> dataSourceMockedStatic = Mockito.mockStatic(DataSourceSingleton.class)) {
      dataSourceMockedStatic.when(DataSourceSingleton::getConnection).thenReturn(connection);
      nearestPOIRequest.open(openContext);
      GPSData gpsData = new GPSData(1, 78.5f, 78.5f);

      Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
      Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
      Mockito.when(resultSet.next()).thenReturn(false);

      nearestPOIRequest.asyncInvoke(gpsData, resultFuture);

      Mockito.verify(resultFuture, Mockito.timeout(3000)).complete(Collections.emptyList());
    }
  }

  @Test
  @DisplayName(
      "Check if there is some connection problems with the database while preparing the statement")
  void testFailPreparedStatement() throws SQLException {
    try (MockedStatic<DataSourceSingleton> dataSourceMockedStatic = Mockito.mockStatic(DataSourceSingleton.class)) {
      dataSourceMockedStatic.when(DataSourceSingleton::getConnection).thenReturn(connection);
      nearestPOIRequest.open(openContext);
      GPSData gpsData = new GPSData(1, 78.5f, 78.5f);

      Mockito.when(connection.prepareStatement(Mockito.anyString())).thenThrow(new SQLException("Error encountered while preparing the statement"));

      Logger logger = (Logger) LoggerFactory.getLogger(NearestPOIRequest.class);
      ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
      listAppender.start();
      logger.addAppender(listAppender);

      nearestPOIRequest.asyncInvoke(gpsData, resultFuture);

      Mockito.verify(resultFuture, Mockito.timeout(3000)).complete(Collections.emptyList());

      List<ILoggingEvent> logsList = listAppender.list;
      Assertions.assertEquals(Level.ERROR, logsList.get(0).getLevel());
      Assertions.assertEquals(
          "Error encountered while preparing the statement", logsList.get(0).getMessage());
    }
  }

  @Test
  @DisplayName(
      "Check if there is some connection problems with the database while executing the query")
  void testFailExecuteQuery() throws SQLException {
    try (MockedStatic<DataSourceSingleton> dataSourceMockedStatic = Mockito.mockStatic(DataSourceSingleton.class)) {
      dataSourceMockedStatic.when(DataSourceSingleton::getConnection).thenReturn(connection);
      nearestPOIRequest.open(openContext);
      GPSData gpsData = new GPSData(1, 78.5f, 78.5f);

      Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
      Mockito.when(preparedStatement.executeQuery())
          .thenThrow(new SQLException("Error encountered while executing the query"));

      Logger logger = (Logger) LoggerFactory.getLogger(NearestPOIRequest.class);
      ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
      listAppender.start();
      logger.addAppender(listAppender);

      nearestPOIRequest.asyncInvoke(gpsData, resultFuture);

      Mockito.verify(resultFuture, Mockito.timeout(3000)).complete(Collections.emptyList());

      List<ILoggingEvent> logsList = listAppender.list;
      Assertions.assertEquals(Level.ERROR, logsList.get(0).getLevel());
      Assertions.assertEquals(
          "Error encountered while executing the query", logsList.get(0).getMessage());
    }
  }
}
