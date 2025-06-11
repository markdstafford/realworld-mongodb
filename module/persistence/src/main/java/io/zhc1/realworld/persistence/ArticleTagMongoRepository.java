package io.zhc1.realworld.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.zhc1.realworld.model.Article;
import io.zhc1.realworld.model.ArticleTag;
import io.zhc1.realworld.model.Tag;

interface ArticleTagMongoRepository extends MongoRepository<ArticleTag, Integer> {

    /**
     * Finds all article-tag associations for a given article.
     * @param article The article to find tags for.
     * @return A list of ArticleTag associations.
     */
    List<ArticleTag> findByArticle(Article article);

    /**
     * Finds all article-tag associations for a given tag.
     * @param tag The tag to find articles for.
     * @return A list of ArticleTag associations.
     */
    List<ArticleTag> findByTag(Tag tag);

    /**
     * Deletes all article-tag associations for a given article.
     * This is useful when an article is deleted to clean up the join documents.
     * @param article The article whose tag associations should be deleted.
     * @return The number of deleted associations.
     */
    long deleteByArticle(Article article);

    /**
     * Finds a specific article-tag association.
     * @param article The article.
     * @param tag The tag.
     * @return An Optional containing the ArticleTag if found, otherwise empty.
     */
    Optional<ArticleTag> findByArticleAndTag(Article article, Tag tag);

    /**
     * Checks if a specific article-tag association exists.
     * @param article The article.
     * @param tag The tag.
     * @return true if the association exists, false otherwise.
     */
    boolean existsByArticleAndTag(Article article, Tag tag);
}
