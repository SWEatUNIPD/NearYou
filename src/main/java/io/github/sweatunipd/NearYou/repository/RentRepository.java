package io.github.sweatunipd.NearYou.repository;

import io.github.sweatunipd.NearYou.entity.Rent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentRepository extends JpaRepository<Rent, Long> {
}
