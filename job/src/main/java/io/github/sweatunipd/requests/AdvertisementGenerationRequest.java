package io.github.sweatunipd.requests;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.entity.PointOfInterest;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class AdvertisementGenerationRequest
    extends RichAsyncFunction<Tuple2<GPSData, PointOfInterest>, Tuple3<GPSData, Integer, String>> {

  private static final Logger LOG = LoggerFactory.getLogger(AdvertisementGenerationRequest.class);
  private transient ChatLanguageModel model;
  private transient Connection
      connection; // FIXME: perché il transient su una classe che non viene serializzata

  // (https://nightlies.apache.org/flink/flink-docs-master/docs/dev/datastream/operators/asyncio/)

  /**
   * Initialization method called before the trigger of the async operation
   *
   * @param openContext The context containing information about the context in which the function¯
   *     is opened.
   * @throws SQLException exception that is thrown when the system can't establish a connection with
   *     the DB
   */
  @Override
  public void open(OpenContext openContext) throws SQLException {
    Map<String, String> config = getRuntimeContext().getGlobalJobParameters();
    model =
        OpenAiChatModel.builder()
            .apiKey(config.getOrDefault("langchain.openai.api.key", "demo"))
            .modelName(config.getOrDefault("langchain.openai.model.name", "gpt-4o-mini"))
            .build();
    Properties props = new Properties();
    props.setProperty("user", config.getOrDefault("postgres.username", "admin"));
    props.setProperty("password", config.getOrDefault("postgres.password", "adminadminadmin"));
    connection =
        DriverManager.getConnection(
            config.getOrDefault(
                "postgres.jdbc.connection.url", "jdbc:postgresql://localhost:5432/admin"),
            props);
  }

  /** Method that closes the async request */
  @Override
  public void close() {
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  /**
   * Method that triggers the async operation for each element of the stream
   *
   * @param interestedPOI tuple containing the POJO representing the GPS data and the POJO
   *     representing the point of interest
   * @param resultFuture Future of result of the processing; tuple containing the POJO of the GPS
   *     Data, the ID of the point of interest and the string containing the result of the LLM's
   *     generation
   */
  @Override
  public void asyncInvoke(
      Tuple2<GPSData, PointOfInterest> interestedPOI,
      ResultFuture<Tuple3<GPSData, Integer, String>> resultFuture) {
    CompletableFuture.supplyAsync(
            () -> {
              try (PreparedStatement statement =
                  connection.prepareStatement(
                      "SELECT users.preferences FROM users JOIN rents ON rents.user_id = users.id WHERE rents.id=?"); ) {
                statement.setInt(1, interestedPOI.f0.getRentId());
                try (ResultSet resultSet = statement.executeQuery()) {
                  if (resultSet.next()) {
                    SystemMessage systemMessage =
                        new SystemMessage(
                            "Genera un annuncio pubblicitario accattivante che abbia una lunghezza massima di 40 parole relativo a un esercizio commerciale. Verifica che l'utente sia effettivamente interessato al punto di interesse: nel caso contrario, non scrivere niente, nemmeno una lettera. Se le preferenze fornite dall'utente non esprimono nulla a riguardo di una tipologia di punto di interesse, genera comunque l'annuncio per coinvolgerlo.");
                    UserMessage userMessage =
                        new UserMessage(
                            "Gli interessi dell'utente sono i seguenti: "
                                + resultSet.getString("preferences")
                                + ".\nL'offerta dell'esercizio commerciale è la seguente: "
                                + interestedPOI.f1.getOffer()
                                + ".");
                    ChatResponse aiResponse = model.chat(systemMessage, userMessage);
                    return new Tuple3<>(
                        interestedPOI.f0,
                        interestedPOI.f1.getId(),
                        aiResponse.aiMessage().text());
                  }
                }
              } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
                return null;
              }
              return null;
            })
        .thenAccept(
            result -> {
              if (result != null) {
                resultFuture.complete(Collections.singletonList(result));
              } else {
                resultFuture.complete(Collections.emptyList());
              }
            });
  }
}
