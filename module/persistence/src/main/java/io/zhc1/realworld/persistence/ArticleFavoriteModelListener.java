package io.zhc1.realworld.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import io.zhc1.realworld.model.ArticleFavorite;
import lombok.RequiredArgsConstructor;

@Profile("mongodb")
@Component
@RequiredArgsConstructor
public class ArticleFavoriteModelListener extends AbstractMongoEventListener<ArticleFavorite> {

    private final SequenceGeneratorService sequenceGenerator;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<ArticleFavorite> event) {
        if (event.getSource().getId() == null) {
            try {
                // Using reflection to set the ID to avoid changing the public API of the model
                java.lang.reflect.Field idField = ArticleFavorite.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(event.getSource(), (int) sequenceGenerator.generateSequence(ArticleFavorite.class.getName()));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to set article favorite ID", e);
            }
        }
    }
} 