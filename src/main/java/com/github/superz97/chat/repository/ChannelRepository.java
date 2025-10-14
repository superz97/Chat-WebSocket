package com.github.superz97.chat.repository;

import com.github.superz97.chat.entity.Channel;
import com.github.superz97.chat.enums.ChannelType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends MongoRepository<Channel, String> {

    Optional<Channel> findByName(String name);

    List<Channel> findByActiveTrue();

    @Query("{ 'type': 'PUBLIC', 'active': true }")
    List<Channel> findPublicChannels();

    @Query("{ 'memberIds': ?0, 'active': true }")
    List<Channel> findByMemberId(String userId);

    @Query("{ 'creatorId': ?0, 'active': true }")
    List<Channel> findByCreatorId(String creatorId);

    @Query("{ 'adminIds': ?0, 'active': true }")
    List<Channel> findByAdminId(String adminId);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'active': true }")
    List<Channel> searchByName(String name);

    @Query("{ 'type': ?0, 'active': true }")
    List<Channel> findByType(ChannelType type);

    boolean existsByName(String name);

    @Query(value = "{ '_id': ?0, 'memberIds': ?1 }", exists = true)
    boolean isMember(String channelId, String userId);

    @Query(value = "{ '_id': ?0, 'adminIds': ?1 }", exists = true)
    boolean isAdmin(String channelId, String userId);

}
