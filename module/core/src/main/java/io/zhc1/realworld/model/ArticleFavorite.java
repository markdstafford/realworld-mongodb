package io.zhc1.realworld.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Entity
@Document(collection = "article_favorites") // Added for MongoDB
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "article_favorite",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "article_id"})})
public class ArticleFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @DBRef // Added for MongoDB
    private User user;

    @ManyToOne
    @JoinColumn(name = "article_id")
    @DBRef // Added for MongoDB
    private Article article;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ArticleFavorite(User user, Article article) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("user is null or unknown user.");
        }
        if (article == null || article.getId() == null) {
            throw new IllegalArgumentException("article is null or unknown article.");
        }

        this.user = user;
        this.article = article;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ArticleFavorite other
                && Objects.equals(this.getId(), other.getId())
                && Objects.equals(this.getUser(), other.getUser())
                && Objects.equals(this.getArticle(), other.getArticle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), this.getUser(), this.getArticle());
    }
}
