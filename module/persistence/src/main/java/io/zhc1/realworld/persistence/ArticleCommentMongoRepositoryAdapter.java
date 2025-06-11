package io.zhc1.realworld.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import io.zhc1.realworld.model.Article;
import io.zhc1.realworld.model.ArticleComment;
import io.zhc1.realworld.model.ArticleCommentRepository;

@Profile("mongodb")
@Component("articleCommentMongoRepositoryAdapter") // Explicit bean name to avoid conflicts
@RequiredArgsConstructor
class ArticleCommentMongoRepositoryAdapter implements ArticleCommentRepository {

    private final ArticleCommentMongoRepository articleCommentMongoRepository;

    @Override
    public ArticleComment save(ArticleComment articleComment) {
        return articleCommentMongoRepository.save(articleComment);
    }

    @Override
    public Optional<ArticleComment> findById(int commentId) {
        return articleCommentMongoRepository.findById(commentId);
    }

    @Override
    public List<ArticleComment> findByArticle(Article article) {
        // This method in ArticleCommentMongoRepository already orders by createdAt desc
        return articleCommentMongoRepository.findByArticleOrderByCreatedAtDesc(article);
    }

    @Override
    @Transactional // Retain transactional behavior if applicable for MongoDB setup
    public void delete(ArticleComment articleComment) {
        articleCommentMongoRepository.delete(articleComment);
    }
}
