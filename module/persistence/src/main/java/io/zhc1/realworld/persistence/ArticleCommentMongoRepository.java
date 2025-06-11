package io.zhc1.realworld.persistence;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.zhc1.realworld.model.Article;
import io.zhc1.realworld.model.ArticleComment;

interface ArticleCommentMongoRepository extends MongoRepository<ArticleComment, Integer> {
    /**
     * Finds all comments for a given article, ordered by creation date in descending order.
     *
     * @param article The article for which to find comments.
     * @return A list of {@link ArticleComment}s, ordered by {@code createdAt} descending.
     */
    List<ArticleComment> findByArticleOrderByCreatedAtDesc(Article article);

    /**
     * Deletes all comments associated with a given article.
     *
     * @param article The article whose comments should be deleted.
     */
    void deleteByArticle(Article article);
}
