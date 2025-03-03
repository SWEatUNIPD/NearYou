package io.github.sweatunipd.NearYou.domain;

import dev.langchain4j.model.chat.ChatLanguageModel;
import io.github.sweatunipd.NearYou.application.ports.input.CreateRentUseCase;
import io.github.sweatunipd.NearYou.application.ports.input.StorePositionUseCase;
import io.github.sweatunipd.NearYou.domain.model.GPSDataCmd;
import io.github.sweatunipd.NearYou.domain.model.Rent;
import io.github.sweatunipd.NearYou.domain.model.RentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Service implements StorePositionUseCase, CreateRentUseCase {

    private final RentRepository rentRepository;
    private final ChatLanguageModel chatLanguageModel;

    @Autowired
    public Service(RentRepository rentRepository, ChatLanguageModel chatLanguageModel) {
        this.rentRepository = rentRepository;
        this.chatLanguageModel = chatLanguageModel;
    }

    public void storePosition(List<GPSDataCmd> cmds) {
        List<Rent> rents= new ArrayList<>();
        for (GPSDataCmd cmd : cmds) {
            rents.add(new Rent(cmd.getRentId(), cmd.getLatitude(), cmd.getLongitude()));
            CompletableFuture.runAsync(() -> {
                System.out.println(chatLanguageModel.generate("Scrivi un inserzione pubblicitaria di circa 40 parole relativo a MediaWorld (ovvero MediaMarkt in italia)"));
            });
        }
        rentRepository.saveAll(rents);
    }

    public int createRent(int id) {
        return 1; //TODO:
    }
}
