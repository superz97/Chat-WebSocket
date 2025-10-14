package com.github.superz97.chat.repository;

import com.github.superz97.chat.entity.User;
import com.github.superz97.chat.enums.UserStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);
    Optional<User> findByKeycloakId(String keycloakId);
    Optional<User> findByEmail(String email);
    List<User> findByStatus(UserStatus status);
    List<User> findByIdIn(Set<String> ids);

    @Query("{ 'username': { $regex: ?0, $options: 'i' } }")
    List<User> searchByUsername(String username);

    @Query("{ 'status': 'ONLINE' }")
    List<User> findOnlineUsers();

    @Query("{ 'channelIds': ?0 }")
    List<User> findByChannelId(String channelId);

    @Query("{ 'groupIds': ?0 }")
    List<User> findByGroupId(String groupId);

    @Query("{ 'lastSeen': { $gte: ?0 } }")
    List<User> findRecentlyActive(LocalDateTime since);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByKeycloakId(String keycloakId);

}
