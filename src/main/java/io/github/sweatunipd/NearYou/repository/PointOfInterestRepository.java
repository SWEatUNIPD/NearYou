package io.github.sweatunipd.NearYou.repository;

import io.github.sweatunipd.NearYou.entity.PointOfInterest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PointOfInterestRepository extends JpaRepository<PointOfInterest, Integer> {
  @Query(
      value =
          "SELECT * FROM points_of_interest AS p WHERE ST_DWithin(ST_Transform(ST_SetSRID(ST_MakePoint(:latitude,:longitude),4326), 3857), ST_Transform(ST_SetSRID(ST_MakePoint(p.latitude,p.longitude),4326), 3857), :radius) ORDER BY ST_Distance(ST_SetSRID(ST_MakePoint(:latitude,:longitude),4326), ST_SetSRID(ST_MakePoint(p.latitude,p.longitude),4326)) LIMIT 1",
      nativeQuery = true)
  public Optional<PointOfInterest> nearbyPointOfInterest(
      @Param("latitude") float latitude,
      @Param("longitude") float longitude,
      @Param("radius") double radius);
}
