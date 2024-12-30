package io.github.sweatunipd.NearYou.repository;

import io.github.sweatunipd.NearYou.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Integer> {}
