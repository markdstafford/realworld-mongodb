package io.zhc1.realworld.persistence;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import io.zhc1.realworld.config.CacheName;
import io.zhc1.realworld.model.Tag;
import io.zhc1.realworld.model.TagRepository;

@Profile("mongodb")
@Component("tagMongoRepositoryAdapter") // Explicit bean name to avoid conflicts
@RequiredArgsConstructor
class TagMongoRepositoryAdapter implements TagRepository {

    private final TagMongoRepository tagMongoRepository;

    @Override
    @Cacheable(value = CacheName.ALL_TAGS)
    public List<Tag> findAll() {
        return tagMongoRepository.findAll();
    }
}
