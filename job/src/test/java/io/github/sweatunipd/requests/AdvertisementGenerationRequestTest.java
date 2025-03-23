package io.github.sweatunipd.requests;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import io.github.sweatunipd.database.DatabaseConnectionSingleton;
import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.entity.PointOfInterest;
import io.r2dbc.spi.*;

import java.util.List;
import java.util.function.BiFunction;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
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
class AdvertisementGenerationRequestTest {

  @Mock private ConnectionFactory connectionFactory;

  @Mock private Connection connection;

  @Mock private Statement statement;

  @Mock private Result result;

  @Mock private Row row;

  @Mock private ResultFuture<Tuple3<GPSData, PointOfInterest, String>> resultFuture;

  @Mock private OpenAiChatModel.OpenAiChatModelBuilder modelBuilder;

  @Mock private OpenAiChatModel model;

  @Mock private ChatResponse chatResponse;

  @Mock private AiMessage aiMessage;

  @Mock private OpenContext openContext;

  private AdvertisementGenerationRequest advertisementGenerationRequest;
  private Tuple2<GPSData, PointOfInterest> tuple;

  @BeforeEach
  void setUp() {
    advertisementGenerationRequest = new AdvertisementGenerationRequest();
    tuple =
        new Tuple2<>(
            new GPSData(1, 45.0f, 11.0f),
            new PointOfInterest(78.5f, 78.5f, "IT1234", "Test", "Test", "Test"));

    // OpenAI model
    try (MockedStatic<OpenAiChatModel> mockedStaticModel =
        Mockito.mockStatic(OpenAiChatModel.class)) {
      mockedStaticModel.when(OpenAiChatModel::builder).thenReturn(modelBuilder);
      Mockito.when(modelBuilder.apiKey(Mockito.anyString())).thenReturn(modelBuilder);
      Mockito.when(modelBuilder.modelName(Mockito.<OpenAiChatModelName>any()))
          .thenReturn(modelBuilder);
      Mockito.when(modelBuilder.build()).thenReturn(model);

      advertisementGenerationRequest.open(openContext);
    }
  }

  @Test
  void testReturnPOI() {
    try (MockedStatic<DatabaseConnectionSingleton> mockedStatic =
        Mockito.mockStatic(DatabaseConnectionSingleton.class); ) {
      // Connection mock
      mockedStatic.when(DatabaseConnectionSingleton::getConnection).thenReturn(connectionFactory);

      // Reactive stream mock
      Mockito.when(connectionFactory.create()).thenAnswer(invocation -> Mono.just(connection));
      Mockito.when(connection.createStatement(Mockito.anyString())).thenReturn(statement);
      Mockito.when(statement.bind(Mockito.anyString(), Mockito.any())).thenReturn(statement);
      Mockito.when(statement.execute()).thenAnswer(invocation -> Mono.just(result));
      Mockito.when(result.map(Mockito.<BiFunction<Row, RowMetadata, ?>>any()))
          .thenAnswer(invocation -> Flux.just(row));
      Mockito.when(row.get("text_area", String.class)).thenReturn("test");
      Mockito.when(model.chat(Mockito.any(), Mockito.any())).thenReturn(chatResponse);
      Mockito.when(chatResponse.aiMessage()).thenReturn(aiMessage);
      Mockito.when(aiMessage.text()).thenReturn("test");

      // Method invocation
      advertisementGenerationRequest.asyncInvoke(tuple, resultFuture);
    }
  }

  @Test
  void testNoUserInterest(){
    try(MockedStatic<DatabaseConnectionSingleton> mockedStatic = Mockito.mockStatic(DatabaseConnectionSingleton.class)) {
      mockedStatic.when(DatabaseConnectionSingleton::getConnection).thenReturn(connectionFactory);
      Mockito.when(connectionFactory.create()).thenAnswer(invocation -> Mono.just(connection));
      Mockito.when(connection.createStatement(Mockito.anyString())).thenReturn(statement);
      Mockito.when(statement.bind(Mockito.anyString(), Mockito.any())).thenReturn(statement);
      Mockito.when(statement.execute()).thenAnswer(invocation -> Flux.empty());

      advertisementGenerationRequest.asyncInvoke(tuple, resultFuture);
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
      Logger logger = (Logger) LoggerFactory.getLogger(AdvertisementGenerationRequest.class);
      ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
      listAppender.start();
      logger.addAppender(listAppender);

      advertisementGenerationRequest.asyncInvoke(tuple, resultFuture);

      List<ILoggingEvent> logsList = listAppender.list;
      Assertions.assertEquals(1, logsList.size());
      Assertions.assertEquals("Test error", logsList.get(0).getMessage());
    }
  }
}
