package io.github.sweatunipd.requests;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import io.github.sweatunipd.database.DatabaseConnectionSingleton;
import io.github.sweatunipd.model.GPSData;
import io.github.sweatunipd.model.PointOfInterest;
import io.r2dbc.spi.Connection;
import java.util.Collections;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class AdvertisementGenerationRequest
    extends RichAsyncFunction<
        Tuple2<GPSData, PointOfInterest>, Tuple3<GPSData, PointOfInterest, String>> {

  private static final Logger LOG = LoggerFactory.getLogger(AdvertisementGenerationRequest.class);
  private ChatLanguageModel model;

  /**
   * Initialization method called before the trigger of the async operation
   *
   * @param openContext The context containing information about the context in which the function¯
   *     is opened.
   */
  @Override
  public void open(OpenContext openContext) {
    model =
        OpenAiChatModel.builder()
            .apiKey("demo") // FIXME
            .modelName(OpenAiChatModelName.GPT_4_O_MINI)
            .build();
  }

  /**
   * Method that triggers the async operation for each element of the stream
   *
   * @param value tuple containing the position emitted by the user and the interested point of
   *     interest
   * @param resultFuture Future of the result of the processing; tuple of three element that
   *     includes the position, the interested POI and the advertisement string/text
   */
  @Override
  public void asyncInvoke(
      Tuple2<GPSData, PointOfInterest> value,
      ResultFuture<Tuple3<GPSData, PointOfInterest, String>> resultFuture) {

    Mono.usingWhen(
            DatabaseConnectionSingleton.getConnection().create(),
            connection ->
                Mono.from(
                        connection
                            .createStatement(
                                "SELECT users.text_area FROM users JOIN rents ON rents.user_email = users.email WHERE rents.id=$1")
                            .bind("$1", value.f0.getRentId())
                            .execute())
                    .flatMap(result -> Mono.from(result.map((row, metadata) -> row))),
            Connection::close)
        .mapNotNull(row -> row.get("text_area", String.class))
        .defaultIfEmpty("")
        .flatMap(
            text_area -> {
              String userInterests = text_area.isEmpty() ? "" : text_area;
              SystemMessage systemMessage =
                  new SystemMessage(
                      "Genera un annuncio pubblicitario accattivante che abbia una lunghezza massima di 40 parole relativo a un esercizio commerciale. Verifica che l'utente sia effettivamente interessato al punto di interesse: nel caso contrario, non scrivere niente, nemmeno una lettera. Se l'utente non scrive di una preferenza in particolare che abbia a che fare con il punto di interesse, crea comunque l'annuncio. Controlla anche la categoria merceologica con quello che stai scrivendo per evitare di uscire fuori contesto. Le descrizioni rimangono tali, quindi non sono nomi: se vedi cose scritte in inglese, interpretale e traducile. Ti fornisco anche il nome per darti qualche dettaglio in più. Fai qualche ricerca online su Google e perlustra le recensioni per arricchire l'annuncio e/o capire se ha senso generare l'annuncio.");
              UserMessage userMessage =
                  new UserMessage(
                      "Gli interessi dell'utente sono i seguenti: "
                          + userInterests
                          + ".\nL'offerta dell'esercizio commerciale è la seguente: "
                          + value.f1.offer()
                          + ".\nLa categoria dell'esercizio commerciale è: "
                          + value.f1.category()
                          + ".\nIl nome dell'esercizio commerciale è: "
                          + value.f1.name());
              return Mono.fromCallable(() -> model.chat(systemMessage, userMessage))
                  .subscribeOn(Schedulers.boundedElastic()) // FIXME: check online
                  .map(ChatResponse::aiMessage)
                  .map(AiMessage::text);
            })
        .doOnSuccess(
            aiResponse ->
                resultFuture.complete(
                    Collections.singleton(new Tuple3<>(value.f0, value.f1, aiResponse))))
        .doOnError(
            throwable -> {
              LOG.error(throwable.getMessage());
              resultFuture.complete(Collections.emptySet());
            })
        .subscribe();
  }
}
