package io.github.sweatunipd.NearYou.repository;

import io.github.sweatunipd.NearYou.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

//<V, T> dove T è il tipo dell'id
public interface UserRepository extends JpaRepository<User, String> {
}
