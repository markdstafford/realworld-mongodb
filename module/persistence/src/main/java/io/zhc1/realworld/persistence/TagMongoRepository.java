package io.zhc1.realworld.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.zhc1.realworld.model.Tag;

interface TagMongoRepository extends MongoRepository<Tag, String> {
    // No custom methods needed, similar to TagJpaRepository
}
