package io.zhc1.realworld.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import io.zhc1.realworld.model.PasswordEncoder;
import io.zhc1.realworld.model.User;
import io.zhc1.realworld.model.UserRepository;

@Profile("mongodb")
@Component("userMongoRepositoryAdapter") // Explicit bean name to avoid conflict if both profiles are active in tests
@RequiredArgsConstructor
class UserMongoRepositoryAdapter implements UserRepository {

    private final UserMongoRepository userMongoRepository;

    @Override
    public User save(User user) {
        return userMongoRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userMongoRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userMongoRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userMongoRepository.findByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMongoRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMongoRepository.existsByUsername(username);
    }

    @Override
    public boolean existsBy(String email, String username) {
        return userMongoRepository.existsByEmailOrUsername(email, username);
    }

    @Override
    @Transactional // MongoDB transactions require a replica set configuration
    public User updateUserDetails(
            UUID userId,
            PasswordEncoder passwordEncoder,
            String email,
            String username,
            String password,
            String bio,
            String imageUrl) {
        return this.findById(userId)
                .map(user -> {
                    if (!user.equalsEmail(email) && this.existsByEmail(email)) {
                        throw new IllegalArgumentException("email is already exists.");
                    }

                    if (!user.equalsUsername(username) && this.existsByUsername(username)) {
                        throw new IllegalArgumentException("username is already exists.");
                    }

                    user.setEmail(email);
                    user.setUsername(username);
                    user.encryptPassword(passwordEncoder, password);
                    user.setBio(bio);
                    user.setImageUrl(imageUrl);
                    return userMongoRepository.save(user);
                })
                .orElseThrow(() -> new IllegalArgumentException("user not found."));
    }
}
