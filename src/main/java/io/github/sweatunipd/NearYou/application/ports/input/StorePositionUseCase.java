package io.github.sweatunipd.NearYou.application.ports.input;

import io.github.sweatunipd.NearYou.domain.model.GPSDataCmd;

import java.util.List;

public interface StorePositionUseCase {
    public void storePosition(GPSDataCmd data);
}
