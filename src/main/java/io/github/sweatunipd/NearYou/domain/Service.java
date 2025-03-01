package io.github.sweatunipd.NearYou.domain;

import io.github.sweatunipd.NearYou.application.ports.input.StorePositionUseCase;
import io.github.sweatunipd.NearYou.domain.model.GPSDataCmd;
import io.github.sweatunipd.NearYou.domain.model.Rent;
import io.github.sweatunipd.NearYou.domain.model.RentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class Service implements StorePositionUseCase {

    private final RentRepository rentRepository;

    @Autowired
    public Service(RentRepository rentRepository) {
        this.rentRepository = rentRepository;
    }

    public void storePosition(GPSDataCmd data) {
        rentRepository.save(new Rent(data.getRentId(), data.getLatitude(), data.getLongitude()));
    }
}
