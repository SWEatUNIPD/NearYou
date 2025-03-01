package io.github.sweatunipd.NearYou.infrastracture.adapters.input;

import io.github.sweatunipd.NearYou.application.ports.input.StorePositionUseCase;
import io.github.sweatunipd.NearYou.domain.model.GPSDataCmd;
import io.github.sweatunipd.NearYou.infrastracture.adapters.input.dto.GPSDataDto;
import org.jetbrains.annotations.NotNull;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GPSDataListener {
    private final StorePositionUseCase storePositionUseCase;

    public GPSDataListener(StorePositionUseCase storePositionUseCase) {
        this.storePositionUseCase = storePositionUseCase;
    }

    @KafkaListener(topics = "gps-data", groupId = "backend")
    void listener(@NotNull GPSDataDto dto) {
        storePositionUseCase.storePosition(new GPSDataCmd(dto.rentId(), dto.latitude(), dto.longitude()));
    }
}

