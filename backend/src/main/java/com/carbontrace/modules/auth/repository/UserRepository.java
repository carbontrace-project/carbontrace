package com.carbontrace.modules.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carbontrace.modules.auth.entity.User;

/**
 * Data access for {@code users}.
 *
 * <p>Email is the login identifier and is unique, so it is the only lookup key
 * besides the primary key.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
