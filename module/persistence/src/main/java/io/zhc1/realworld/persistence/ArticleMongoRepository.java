package io.zhc1.realworld.persistence;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import io.zhc1.realworld.model.Article;
import io.zhc1.realworld.model.User;

interface ArticleMongoRepository extends MongoRepository<Article, Integer> {

    Optional<Article> findBySlug(String slug);

    Page<Article> findByAuthorInOrderByCreatedAtDesc(Collection<User> authors, Pageable pageable);

    boolean existsByTitle(String title);

    // Derived query to find articles by a specific tag name.
    // Assumes Article.articleTags.tag.name path.
    Page<Article> findByArticleTagsTagName(String tagName, Pageable pageable);

    // Derived query to find articles by author's username.
    // Assumes Article.author.username path.
    Page<Article> findByAuthorUsername(String username, Pageable pageable);

    // For global feed, ordered by creation date descending.
    Page<Article> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Note: More complex queries, previously handled by JpaSpecificationExecutor,
    // will be implemented in the ArticleMongoRepositoryAdapter using MongoTemplate.
}
