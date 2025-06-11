package io.zhc1.realworld.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import io.zhc1.realworld.model.Article;
import io.zhc1.realworld.model.ArticleFavorite;
import io.zhc1.realworld.model.ArticleFavoriteRepository;
import io.zhc1.realworld.model.User;

@Profile("mongodb")
@Component("articleFavoriteMongoRepositoryAdapter") // Explicit bean name to avoid conflicts
@RequiredArgsConstructor
class ArticleFavoriteMongoRepositoryAdapter implements ArticleFavoriteRepository {

    private final ArticleFavoriteMongoRepository articleFavoriteMongoRepository;

    @Override
    public void save(ArticleFavorite articleFavorite) {
        articleFavoriteMongoRepository.save(articleFavorite);
    }

    @Override
    @Transactional // Retain transactional behavior if applicable for MongoDB setup
    public void deleteBy(User user, Article article) {
        articleFavoriteMongoRepository.deleteByUserAndArticle(user, article);
    }

    @Override
    public boolean existsBy(User user, Article article) {
        return articleFavoriteMongoRepository.existsByUserAndArticle(user, article);
    }
}
