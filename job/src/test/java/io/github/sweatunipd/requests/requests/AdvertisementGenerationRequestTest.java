package io.github.sweatunipd.requests.requests;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import io.github.sweatunipd.database.DataSourceSingleton;
import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.entity.PointOfInterest;
import io.github.sweatunipd.requests.AdvertisementGenerationRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
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
public class AdvertisementGenerationRequestTest {
  @Mock private Connection connection;
  @Mock private OpenContext openContext;
  @Mock OpenAiChatModel openAiChatModel;
  @Mock private OpenAiChatModel.OpenAiChatModelBuilder openAiChatModelBuilder;
  @Mock private PreparedStatement preparedStatement;
  @Mock private ResultFuture<Tuple3<GPSData, PointOfInterest, String>> resultFuture;
  @Mock private ResultSet resultSet;
  @Mock private ChatResponse chatResponse;
  @Mock private AiMessage aiMessage;

  private AdvertisementGenerationRequest advertisementGenerationRequest;

  @BeforeEach
  void setUp() {
    advertisementGenerationRequest = new AdvertisementGenerationRequest();
  }

  @Test
  @DisplayName("open() creates the connection to the database")
  public void openAsyncJobTest() {
    try (MockedStatic<DataSourceSingleton> dataSourceMockedStatic = Mockito.mockStatic(DataSourceSingleton.class);
         MockedStatic<OpenAiChatModel> openAiChatModelMockedStatic =
            Mockito.mockStatic(OpenAiChatModel.class)) {
      // Langchain mocking
      openAiChatModelMockedStatic.when(OpenAiChatModel::builder).thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.apiKey(Mockito.anyString()))
          .thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.modelName(Mockito.any(OpenAiChatModelName.class)))
          .thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.build()).thenReturn(openAiChatModel);
      // Database mocking
      dataSourceMockedStatic.when(DataSourceSingleton::getConnection).thenReturn(connection);

      // Function execution
      Assertions.assertDoesNotThrow(() -> advertisementGenerationRequest.open(openContext));

      // Tests
      openAiChatModelMockedStatic.verify(OpenAiChatModel::builder, Mockito.times(1));
      Mockito.verify(openAiChatModelBuilder, Mockito.times(1)).build();
      dataSourceMockedStatic.verify(DataSourceSingleton::getConnection, Mockito.times(1));
    }
  }

  @Test
  @DisplayName("open() tries to open the connection to the database and encounters an error")
  public void failOpenAsyncJobTest() {
    try (MockedStatic<DataSourceSingleton> dataSourceMockedStatic = Mockito.mockStatic(DataSourceSingleton.class);
         MockedStatic<OpenAiChatModel> openAiChatModelMockedStatic =
            Mockito.mockStatic(OpenAiChatModel.class)) {
      // Langchain mocking
      openAiChatModelMockedStatic.when(OpenAiChatModel::builder).thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.apiKey(Mockito.anyString()))
          .thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.modelName(Mockito.any(OpenAiChatModelName.class)))
          .thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.build()).thenReturn(openAiChatModel);
      // Database mocking
      dataSourceMockedStatic
          .when(DataSourceSingleton::getConnection)
          .thenThrow(new SQLException("SQLException encountered in DB connection opening"));

      // Function execution with Exception store
      SQLException ex =
          Assertions.assertThrows(
              SQLException.class, () -> advertisementGenerationRequest.open(openContext));

      // Tests
      Assertions.assertEquals("SQLException encountered in DB connection opening", ex.getMessage());

      openAiChatModelMockedStatic.verify(OpenAiChatModel::builder, Mockito.times(1));
      Mockito.verify(openAiChatModelBuilder, Mockito.times(1)).build();
      dataSourceMockedStatic.verify(DataSourceSingleton::getConnection, Mockito.times(1));
    }
  }

  @Test
  @DisplayName("close() closes the connection to the database")
  public void closeAsyncJobTest() throws SQLException {
    try (MockedStatic<DataSourceSingleton> dataSourceMockedStatic = Mockito.mockStatic(DataSourceSingleton.class);
         MockedStatic<OpenAiChatModel> openAiChatModelMockedStatic =
            Mockito.mockStatic(OpenAiChatModel.class)) {
      // Langchain mocking
      openAiChatModelMockedStatic.when(OpenAiChatModel::builder).thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.apiKey(Mockito.anyString()))
          .thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.modelName(Mockito.any(OpenAiChatModelName.class)))
          .thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.build()).thenReturn(openAiChatModel);
      // Database mocking
      dataSourceMockedStatic.when(DataSourceSingleton::getConnection).thenReturn(connection);

      // open() execution
      advertisementGenerationRequest.open(openContext);

      // Logger
      Logger logger = (Logger) LoggerFactory.getLogger(AdvertisementGenerationRequest.class);
      ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
      listAppender.start();
      logger.addAppender(listAppender);

      // close() execution
      advertisementGenerationRequest.close();

      // Tests
      List<ILoggingEvent> logsList = listAppender.list;
      Assertions.assertTrue(logsList.isEmpty());
    }
  }

  @Test
  @DisplayName(
      "close() handles the exception thrown by the closure of the connection to the database")
  public void closeAsyncJobHandleExceptionTest() throws SQLException {
    try (MockedStatic<DataSourceSingleton> dataSourceMockedStatic = Mockito.mockStatic(DataSourceSingleton.class);
         MockedStatic<OpenAiChatModel> openAiChatModelMockedStatic =
            Mockito.mockStatic(OpenAiChatModel.class)) {
      // Langchain mocking
      openAiChatModelMockedStatic.when(OpenAiChatModel::builder).thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.apiKey(Mockito.anyString()))
          .thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.modelName(Mockito.any(OpenAiChatModelName.class)))
          .thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.build()).thenReturn(openAiChatModel);
      // Database mocking
      dataSourceMockedStatic.when(DataSourceSingleton::getConnection).thenReturn(connection);
      Mockito.doThrow(new SQLException("Error encountered when closing connection"))
          .when(connection)
          .close();

      // open() execution
      advertisementGenerationRequest.open(openContext);

      // Logger
      Logger logger = (Logger) LoggerFactory.getLogger(AdvertisementGenerationRequest.class);
      ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
      listAppender.start();
      logger.addAppender(listAppender);

      // close() execution
      advertisementGenerationRequest.close();

      // Tests
      List<ILoggingEvent> logsList = listAppender.list;
      Assertions.assertFalse(logsList.isEmpty());
    }
  }

  @Test
  @DisplayName(
      "Test the generation of an advertisement (if the client is considered not interested, will follow the same flow as tested in this test)")
  public void testAdvertisementGeneration() throws SQLException {
    try (MockedStatic<DataSourceSingleton> dataSourceMockedStatic = Mockito.mockStatic(DataSourceSingleton.class);
         MockedStatic<OpenAiChatModel> openAiChatModelMockedStatic =
            Mockito.mockStatic(OpenAiChatModel.class)) {
      // Langchain mocking
      openAiChatModelMockedStatic.when(OpenAiChatModel::builder).thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.apiKey(Mockito.anyString()))
          .thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.modelName(Mockito.any(OpenAiChatModelName.class)))
          .thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.build()).thenReturn(openAiChatModel);
      Mockito.when(
              openAiChatModel.chat(
                  Mockito.any(SystemMessage.class), Mockito.any(UserMessage.class)))
          .thenReturn(chatResponse);
      Mockito.when(chatResponse.aiMessage()).thenReturn(aiMessage);
      Mockito.when(aiMessage.text()).thenReturn("Test advertisement");

      // Database mocking
      dataSourceMockedStatic.when(DataSourceSingleton::getConnection).thenReturn(connection);
      Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
      Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
      Mockito.when(resultSet.next()).thenReturn(true);

      // open() execution
      advertisementGenerationRequest.open(openContext);

      // Creation of GPSData and PointOfInterest and the input tuple
      GPSData gpsData = new GPSData(1, 78.5f, 78.5f);
      PointOfInterest pointOfInterest =
          new PointOfInterest(78,78.5f, "IT101010101", "Test", "Test", "Test");
      Tuple2<GPSData, PointOfInterest> tuple = new Tuple2<>(gpsData, pointOfInterest);

      // asyncInvoke() execution
      Tuple3<GPSData, PointOfInterest, String> output =
          new Tuple3<>(gpsData, pointOfInterest, "Test advertisement");
      advertisementGenerationRequest.asyncInvoke(tuple, resultFuture);

      // Tests
      Mockito.verify(resultFuture, Mockito.timeout(30000)).complete(Collections.singleton(output));
    }
  }

  @Test
  @DisplayName("Test if the user is not found in the database")
  public void testUserNotFound() throws SQLException {
    try (MockedStatic<DataSourceSingleton> dataSourceMockedStatic = Mockito.mockStatic(DataSourceSingleton.class);
         MockedStatic<OpenAiChatModel> openAiChatModelMockedStatic =
            Mockito.mockStatic(OpenAiChatModel.class)) {
      // Langchain mocking
      openAiChatModelMockedStatic.when(OpenAiChatModel::builder).thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.apiKey(Mockito.anyString()))
          .thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.modelName(Mockito.any(OpenAiChatModelName.class)))
          .thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.build()).thenReturn(openAiChatModel);

      // Database mocking
      dataSourceMockedStatic.when(DataSourceSingleton::getConnection).thenReturn(connection);
      Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
      Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
      Mockito.when(resultSet.next()).thenReturn(false);

      // open() execution
      advertisementGenerationRequest.open(openContext);

      // Creation of GPSData and PointOfInterest and the input tuple
      GPSData gpsData = new GPSData(1, 78.5f, 78.5f);
      PointOfInterest pointOfInterest = new PointOfInterest(78.5f, 78.5f, "IT101010101", "Test", "Test", "Test");
      Tuple2<GPSData, PointOfInterest> tuple = new Tuple2<>(gpsData, pointOfInterest);

      // asyncInvoke() execution
      advertisementGenerationRequest.asyncInvoke(tuple, resultFuture);

      // Tests
      Mockito.verify(resultFuture, Mockito.timeout(30000)).complete(Collections.emptySet());
    }
  }

  @Test
  @DisplayName(
      "Check if there is come connection problems with the database while preparing the statement")
  public void testFailPreparedStatement() throws SQLException {
    try (MockedStatic<DataSourceSingleton> dataSourceMockedStatic = Mockito.mockStatic(DataSourceSingleton.class);
         MockedStatic<OpenAiChatModel> openAiChatModelMockedStatic =
            Mockito.mockStatic(OpenAiChatModel.class)) {
      // Langchain mocking
      openAiChatModelMockedStatic.when(OpenAiChatModel::builder).thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.apiKey(Mockito.anyString()))
          .thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.modelName(Mockito.any(OpenAiChatModelName.class)))
          .thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.build()).thenReturn(openAiChatModel);

      // Database mocking
      dataSourceMockedStatic.when(DataSourceSingleton::getConnection).thenReturn(connection);
      Mockito.when(connection.prepareStatement(Mockito.anyString())).thenThrow(new SQLException("Error occurred while preparing the statement"));

      // open() execution
      advertisementGenerationRequest.open(openContext);

      // Creation of GPSData and PointOfInterest and the input tuple
      GPSData gpsData = new GPSData(1, 78.5f, 78.5f);
      PointOfInterest pointOfInterest = new PointOfInterest(78.5f, 78.5f, "IT101010101", "Test", "Test", "Test");
      Tuple2<GPSData, PointOfInterest> tuple = new Tuple2<>(gpsData, pointOfInterest);

      // Logger
      Logger logger = (Logger) LoggerFactory.getLogger(AdvertisementGenerationRequest.class);
      ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
      listAppender.start();
      logger.addAppender(listAppender);

      // asyncInvoke() execution
      advertisementGenerationRequest.asyncInvoke(tuple, resultFuture);

      // Tests
      Mockito.verify(resultFuture, Mockito.timeout(30000)).complete(Collections.emptySet());
      List<ILoggingEvent> logsList = listAppender.list;
      Assertions.assertEquals(Level.ERROR, logsList.get(0).getLevel());
      Assertions.assertEquals("Error occurred while preparing the statement", logsList.get(0).getMessage());
    }
  }

  @Test
  @DisplayName(
          "Check if there is come connection problems with the database while executing the query")
  public void testFailQueryExecution() throws SQLException {
    try (MockedStatic<DataSourceSingleton> dataSourceMockedStatic = Mockito.mockStatic(DataSourceSingleton.class);
         MockedStatic<OpenAiChatModel> openAiChatModelMockedStatic =
                 Mockito.mockStatic(OpenAiChatModel.class)) {
      // Langchain mocking
      openAiChatModelMockedStatic.when(OpenAiChatModel::builder).thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.apiKey(Mockito.anyString()))
              .thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.modelName(Mockito.any(OpenAiChatModelName.class)))
              .thenReturn(openAiChatModelBuilder);
      Mockito.when(openAiChatModelBuilder.build()).thenReturn(openAiChatModel);

      // Database mocking
      dataSourceMockedStatic.when(DataSourceSingleton::getConnection).thenReturn(connection);
      Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
      Mockito.when(preparedStatement.executeQuery()).thenThrow(new SQLException("Error occurred while executing the query"));

      // open() execution
      advertisementGenerationRequest.open(openContext);

      // Creation of GPSData and PointOfInterest and the input tuple
      GPSData gpsData = new GPSData(1, 78.5f, 78.5f);
      PointOfInterest pointOfInterest = new PointOfInterest(78.5f, 78.5f, "IT101010101", "Test", "Test", "Test");
      Tuple2<GPSData, PointOfInterest> tuple = new Tuple2<>(gpsData, pointOfInterest);

      // Logger
      Logger logger = (Logger) LoggerFactory.getLogger(AdvertisementGenerationRequest.class);
      ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
      listAppender.start();
      logger.addAppender(listAppender);

      // asyncInvoke() execution
      advertisementGenerationRequest.asyncInvoke(tuple, resultFuture);

      // Tests
      Mockito.verify(resultFuture, Mockito.timeout(30000)).complete(Collections.emptySet());
      List<ILoggingEvent> logsList = listAppender.list;
      Assertions.assertEquals(Level.ERROR, logsList.get(0).getLevel());
      Assertions.assertEquals("Error occurred while executing the query", logsList.get(0).getMessage());
    }
  }
}
