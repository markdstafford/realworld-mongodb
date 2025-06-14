package io.zhc1.realworld.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.mongodb.core.mapping.Document;

@Entity
@Document(collection = "tags") // Added for MongoDB
@Getter
@Table(name = "tag")
@SuppressWarnings("JpaDataSourceORMInspection")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {
    @Id
    @org.springframework.data.annotation.Id
    @Column(length = 20)
    private String name;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Tag(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is null or blank.");
        }

        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Tag other && Objects.equals(this.getName(), other.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getName());
    }
}
