package io.poc.authservice.repository;

import io.poc.authservice.entity.UserCred;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<UserCred, Long> {

}
