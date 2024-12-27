package io.github.sweatunipd.NearYou;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import io.github.sweatunipd.NearYou.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Component
public class KafkaListeners {

  private ChatLanguageModel chatLanguageModel;
  private UserRepository userRepository;
  private SensorRepository sensorRepository;
  private RentRepository rentRepository;
  private LocationDataRepository locationDataRepository;
  private GenerationRepository generationRepository;
  private MerchantRepository merchantRepository;
  private PointOfInterestRepository pointOfInterestRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  public KafkaListeners(
      ChatLanguageModel chatLanguageModel,
      UserRepository userRepository,
      SensorRepository sensorRepository,
      RentRepository rentRepository,
      LocationDataRepository locationDataRepository,
      GenerationRepository generationRepository,
      MerchantRepository merchantRepository,
      PointOfInterestRepository pointOfInterestRepository) {
    this.chatLanguageModel = chatLanguageModel;
    this.userRepository = userRepository;
    this.sensorRepository = sensorRepository;
    this.rentRepository = rentRepository;
    this.locationDataRepository = locationDataRepository;
    this.generationRepository = generationRepository;
    this.merchantRepository = merchantRepository;
    this.pointOfInterestRepository = pointOfInterestRepository;
  }

  /**
   * groupId serve per distinguere gli ascoltatori di uno specifico topic, ma nel nostro caso è uno
   * solo Secondo la documentazione di spring, possiamo ascoltare i messagi sia tramite
   * `MessageListenerContainer`, sia tramite la annotation riportata qua sotto
   */
  // @KafkaListener(topics = "gps-data", groupId = "spring")
  void listener(String data) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNode = mapper.readTree(data);
    String test =
        chatLanguageModel.generate(
            "Write me a short name of a company (like Apple and others) [NOTHING ELSE; NO INTRODUCTION; NO ENDINGS; NO NOTHING]");

    DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss.SSS");
    String time = "2011-10-02 18:48:05.123";
  }
  ;
}
