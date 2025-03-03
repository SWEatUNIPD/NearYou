package io.github.sweatunipd.NearYou.infrastracture.adapters.input.event;

import io.github.sweatunipd.NearYou.application.ports.input.StorePositionUseCase;
import io.github.sweatunipd.NearYou.domain.model.GPSDataCmd;
import io.github.sweatunipd.NearYou.infrastracture.adapters.input.dto.GPSDataDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GPSDataListener {
    private final StorePositionUseCase storePositionUseCase;

    public GPSDataListener(StorePositionUseCase storePositionUseCase) {
        this.storePositionUseCase = storePositionUseCase;
    }

    @KafkaListener(topics = "gps-data", groupId = "backend")
    void listener(List<GPSDataDto> dtos) {
        List<GPSDataCmd> cmds = dtos.stream().map(dto -> new GPSDataCmd(dto.rentId(), dto.latitude(), dto.longitude())).toList();
        storePositionUseCase.storePosition(cmds);
    }
}

