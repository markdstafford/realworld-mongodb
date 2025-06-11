package io.zhc1.realworld.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.zhc1.realworld.model.User;

interface UserMongoRepository extends MongoRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // Spring Data MongoDB can derive queries for combined fields in existsBy methods
    // e.g. existsByEmailOrUsername(String email, String username)
    boolean existsByEmailOrUsername(String email, String username);
}
