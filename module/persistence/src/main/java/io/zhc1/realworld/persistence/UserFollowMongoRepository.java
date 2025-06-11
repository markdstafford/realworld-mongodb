package io.zhc1.realworld.persistence;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.zhc1.realworld.model.User;
import io.zhc1.realworld.model.UserFollow;

interface UserFollowMongoRepository extends MongoRepository<UserFollow, Integer> {
    /**
     * Finds all follow relationships where the given user is the follower.
     *
     * @param follower The user who is following others.
     * @return A list of {@link UserFollow} relationships.
     */
    List<UserFollow> findByFollower(User follower);

    /**
     * Deletes a follow relationship between a follower and a following user.
     *
     * @param follower  The user who is following.
     * @param following The user who is being followed.
     */
    void deleteByFollowerAndFollowing(User follower, User following);

    /**
     * Checks if a follow relationship exists between a follower and a following user.
     *
     * @param follower  The user who is following.
     * @param following The user who is being followed.
     * @return {@code true} if the follow relationship exists, {@code false} otherwise.
     */
    boolean existsByFollowerAndFollowing(User follower, User following);
}
