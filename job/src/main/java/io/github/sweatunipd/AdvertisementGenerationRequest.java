package io.github.sweatunipd;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import io.github.sweatunipd.entity.PointOfInterest;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class AdvertisementGenerationRequest
    extends RichAsyncFunction<Tuple2<Integer, PointOfInterest>, Tuple2<Integer, String>> {

  public transient ChatLanguageModel model;

  @Override
  public void asyncInvoke(
      Tuple2<Integer, PointOfInterest> integerPointOfInterestTuple2,
      ResultFuture<Tuple2<Integer, String>> resultFuture) {
    CompletableFuture.<Tuple2<Integer, String>>supplyAsync(
            () -> {
              try {
                if (integerPointOfInterestTuple2.f1 != null) {
                  return new Tuple2<>(
                      integerPointOfInterestTuple2.f0,
                      model.chat(
                          "Genera un annuncio di 40 parole relativo al negozio di elettronica Mediaworld"));
                }
                return null;
              } catch (Exception e) {
                return null;
              }
            })
        .thenAccept(
            result -> {
              if (result == null) {
                resultFuture.complete(Collections.emptyList());
              } else {
                resultFuture.complete(Collections.singletonList(result));
              }
            });
  }

  @Override
  public void open(OpenContext openContext) {
    model =
        OpenAiChatModel.builder()
            .apiKey("demo")
            .modelName(OpenAiChatModelName.GPT_4_O_MINI)
            .build();
  }
}
