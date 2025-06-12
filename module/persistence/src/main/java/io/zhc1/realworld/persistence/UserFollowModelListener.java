package io.zhc1.realworld.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import io.zhc1.realworld.model.UserFollow;
import lombok.RequiredArgsConstructor;

@Profile("mongodb")
@Component
@RequiredArgsConstructor
public class UserFollowModelListener extends AbstractMongoEventListener<UserFollow> {

    private final SequenceGeneratorService sequenceGenerator;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<UserFollow> event) {
        if (event.getSource().getId() == null) {
            try {
                // Using reflection to set the ID to avoid changing the public API of the model
                java.lang.reflect.Field idField = UserFollow.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(event.getSource(), (int) sequenceGenerator.generateSequence(UserFollow.class.getName()));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to set user follow ID", e);
            }
        }
    }
} 