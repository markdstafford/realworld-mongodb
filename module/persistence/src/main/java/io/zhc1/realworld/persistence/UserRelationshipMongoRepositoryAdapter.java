package io.zhc1.realworld.persistence;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import io.zhc1.realworld.model.User;
import io.zhc1.realworld.model.UserFollow;
import io.zhc1.realworld.model.UserRelationshipRepository;

@Profile("mongodb")
@Component("userRelationshipMongoRepositoryAdapter") // Explicit bean name
@RequiredArgsConstructor
class UserRelationshipMongoRepositoryAdapter implements UserRelationshipRepository {

    private final UserFollowMongoRepository userFollowMongoRepository;

    @Override
    public void save(UserFollow userFollow) {
        userFollowMongoRepository.save(userFollow);
    }

    @Override
    public List<UserFollow> findByFollower(User follower) {
        return userFollowMongoRepository.findByFollower(follower);
    }

    @Override
    @Transactional // Retain transactional behavior if applicable for MongoDB setup
    public void deleteBy(User follower, User following) {
        userFollowMongoRepository.deleteByFollowerAndFollowing(follower, following);
    }

    @Override
    public boolean existsBy(User follower, User following) {
        return userFollowMongoRepository.existsByFollowerAndFollowing(follower, following);
    }
}
