package io.zhc1.realworld.persistence;

import org.springframework.context.annotation.Profile; // Added import
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import io.zhc1.realworld.model.Article;
import io.zhc1.realworld.model.ArticleFavorite;
import io.zhc1.realworld.model.ArticleFavoriteRepository;
import io.zhc1.realworld.model.User;

@Profile("h2") // Added annotation
@Repository
@RequiredArgsConstructor
class ArticleFavoriteJpaRepositoryAdapter implements ArticleFavoriteRepository { // Changed class name
    private final ArticleFavoriteJpaRepository articleFavoriteJpaRepository;

    @Override
    public void save(ArticleFavorite articleFavorite) {
        articleFavoriteJpaRepository.save(articleFavorite);
    }

    @Override
    @Transactional
    public void deleteBy(User user, Article article) {
        articleFavoriteJpaRepository.deleteByUserAndArticle(user, article);
    }

    @Override
    public boolean existsBy(User user, Article article) {
        return articleFavoriteJpaRepository.existsByUserAndArticle(user, article);
    }
}
