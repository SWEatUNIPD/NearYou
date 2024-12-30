package io.github.sweatunipd.NearYou;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import io.github.sweatunipd.NearYou.entity.Generation.GenerationBuilder;
import io.github.sweatunipd.NearYou.entity.LocationData.LocationDataBuilder;
import io.github.sweatunipd.NearYou.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;

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
  private final ObjectMapper mapper = new ObjectMapper();

  @Autowired
  public KafkaListeners(
      ChatLanguageModel chatLanguageModel,
      UserRepository userRepository,
      SensorRepository sensorRepository,
      RentRepository rentRepository,
      LocationDataRepository locationDataRepository,
      GenerationRepository generationRepository,
      MerchantRepository merchantRepository,
      PointOfInterestRepository pointOfInterestRepository
  ) {
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
  @KafkaListener(topics = "gps-data", groupId = "spring")
  void listener(String data) throws JsonProcessingException {
    JsonNode jsonNode = mapper.readTree(data);
    String test = chatLanguageModel.generate("Write hello world");
    int userId = jsonNode.get("sensorId").asInt();
    Long rentId = jsonNode.get("rentId").asLong();
    float latitude = (float) jsonNode.get("latitude").asDouble();
    float longitude = (float) jsonNode.get("longitude").asDouble();
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    Long locationDataId =
        locationDataRepository
            .save(
                new LocationDataBuilder()
                    .setFetchTime(timestamp)
                    .setRent(
                        rentRepository
                            .findById(rentId)
                            .orElseThrow(() -> new RuntimeException("No rent found")))
                    .setLatitude(latitude)
                    .setLongitude(longitude)
                    .build())
            .getId();

    generationRepository.save(
        new GenerationBuilder()
            .setAdv(test)
            .setLocationData(
                locationDataRepository
                    .findById(locationDataId)
                    .orElseThrow(() -> new RuntimeException("No location data found")))
            .build());
  }
}
