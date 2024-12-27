package io.github.sweatunipd.NearYou.repository;

import io.github.sweatunipd.NearYou.entity.LocationData;
import io.github.sweatunipd.NearYou.entity.PointOfInterest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointOfInterestRepository extends JpaRepository<PointOfInterest, Long> {}
