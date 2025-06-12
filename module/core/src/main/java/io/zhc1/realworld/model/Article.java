package io.zhc1.realworld.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Entity
@Document(collection = "articles") // Added for MongoDB
@Getter
@SuppressWarnings("JpaDataSourceORMInspection")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article {
    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    @DBRef // Added for MongoDB
    private User author;

    @Column(length = 50, unique = true, nullable = false)
    private String slug;

    @Column(length = 50, unique = true, nullable = false)
    private String title;

    @Column(length = 50, nullable = false)
    private String description;

    @Column(length = 1_000, nullable = false)
    private String content;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @DBRef // Added for MongoDB, implies ArticleTag will be a separate document collection
    private Set<ArticleTag> articleTags = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt = LocalDateTime.now();

    private static String titleToSlug(String title) {
        return title.toLowerCase().replaceAll("\\s+", "-");
    }

    public Article(User author, String title, String description, String content) {
        if (author == null || author.getId() == null) {
            throw new IllegalArgumentException("author is null or unknown user.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is null or blank.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description is null or blank.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is null or blank.");
        }

        this.author = author;
        this.slug = Article.titleToSlug(title);
        this.title = title;
        this.description = description;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isNotAuthor(User author) {
        return !this.author.equals(author);
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is null or blank.");
        }

        this.title = title;
        this.slug = Article.titleToSlug(this.title);
        this.updatedAt = LocalDateTime.now();
    }

    public void setDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description is null or blank.");
        }

        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public void setContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is null or blank.");
        }

        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void addTag(ArticleTag tag) {
        articleTags.add(tag);
        tag.setArticle(this);
    }

    public void setArticleTags(Set<ArticleTag> articleTags) {
        this.articleTags = articleTags;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Article other && Objects.equals(this.getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}
