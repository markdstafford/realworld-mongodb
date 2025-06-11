package io.zhc1.realworld.persistence;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.zhc1.realworld.model.Article;
import io.zhc1.realworld.model.ArticleFavorite;
import io.zhc1.realworld.model.User;

interface ArticleFavoriteMongoRepository extends MongoRepository<ArticleFavorite, Integer> {
    /**
     * Deletes an article favorite by user and article.
     *
     * @param user    The user who favorited the article.
     * @param article The article that was favorited.
     */
    void deleteByUserAndArticle(User user, Article article);

    /**
     * Checks if an article favorite exists for a given user and article.
     *
     * @param user    The user.
     * @param article The article.
     * @return {@code true} if the favorite exists, {@code false} otherwise.
     */
    boolean existsByUserAndArticle(User user, Article article);

    /**
     * Counts the number of favorites for a given article.
     *
     * @param article The article.
     * @return The total number of favorites for the article.
     */
    int countByArticle(Article article);

    /**
     * Finds all articles favorited by a specific user.
     * This is useful for implementing the "favorited by" facet in article listings.
     *
     * @param user The user whose favorited articles are to be found.
     * @return A list of {@link ArticleFavorite} associations for the given user.
     */
    List<ArticleFavorite> findByUser(User user);

    /**
     * Deletes all favorites associated with a given article.
     * This is useful when an article is deleted.
     *
     * @param article The article whose favorites should be deleted.
     */
    void deleteByArticle(Article article);
}
