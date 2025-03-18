package io.github.sweatunipd.requests;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import io.github.sweatunipd.database.DataSourceSingleton;
import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.entity.PointOfInterest;
import java.sql.*;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvertisementGenerationRequest
    extends RichAsyncFunction<Tuple2<GPSData, PointOfInterest>, Tuple3<GPSData, Integer, String>> {

  private static final Logger LOG = LoggerFactory.getLogger(AdvertisementGenerationRequest.class);
  private transient ChatLanguageModel model;
  private transient Connection
      connection; // FIXME: perché il transient su una classe che non viene serializzata

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
    model =
        OpenAiChatModel.builder()
            .apiKey("demo")
            .modelName(OpenAiChatModelName.GPT_4_O_MINI)
            .build();
    connection = DataSourceSingleton.getConnection();
  }

  /** Method that closes the async request */
  @Override
  public void close() {
    try {
      connection.close();
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  /**
   * Method that generates the advertisement with the LLM
   *
   * TODO: fare JavaDoc
   */
  @Override
  public void asyncInvoke(
      Tuple2<GPSData, PointOfInterest> value,
      ResultFuture<Tuple3<GPSData, Integer, String>> resultFuture) {
    CompletableFuture.supplyAsync(
            () -> {
              try (PreparedStatement statement =
                  connection.prepareStatement(
                      "SELECT users.preferences FROM users JOIN rents ON rents.user_id = users.id WHERE rents.id=?")) {
                statement.setInt(1, value.f0.getRentId());
                try (ResultSet resultSet = statement.executeQuery()) {
                  if (resultSet.next()) {
                    SystemMessage systemMessage =
                        new SystemMessage(
                            "Genera un annuncio pubblicitario accattivante che abbia una lunghezza massima di 40 parole relativo a un esercizio commerciale. Verifica che l'utente sia effettivamente interessato al punto di interesse: nel caso contrario, non scrivere niente, nemmeno una lettera. Se l'utente non scrive di una preferenza in particolare che abbia a che fare con il punto di interesse, crea comunque l'annuncio. Controlla anche la categoria merceologica con quello che stai scrivendo per evitare di uscire fuori contesto. Le descrizioni rimangono tali, quindi non sono nomi: se vedi cose scritte in inglese, interpretale e traducile. Ti fornisco anche il nome per darti qualche dettaglio in più. Fai qualche ricerca online su Google e perlustra le recensioni per arricchire l'annuncio e/o capire se ha senso generare l'annuncio.");
                    UserMessage userMessage =
                        new UserMessage(
                            "Gli interessi dell'utente sono i seguenti: "
                                + resultSet.getString("preferences")
                                + ".\nL'offerta dell'esercizio commerciale è la seguente: "
                                + value.f1.getOffer()
                                + ".\nLa categoria dell'esercizio commerciale è: "
                                + value.f1.getCategory()
                                + ".\nIl nome dell'esercizio commerciale è: "
                                + value.f1.getName());
                    ChatResponse aiResponse = model.chat(systemMessage, userMessage);
                    return new Tuple3<>(value.f0, value.f1.getId(), aiResponse.aiMessage().text());
                  }
                }
              } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
              }
              return null;
            })
        .thenAccept(
            result -> {
              if (result != null) {
                resultFuture.complete(Collections.singleton(result));
              }else{
                resultFuture.complete(Collections.emptySet());
              }
            });
    }
}
