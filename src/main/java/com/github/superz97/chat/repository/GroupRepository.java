package com.github.superz97.chat.repository;

import com.github.superz97.chat.entity.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {

    List<Group> findByActiveTrue();

    @Query("{ 'memberIds': ?0, 'active': true }")
    List<Group> findByMemberId(String userId);

    @Query("{ 'creatorId': ?0, 'active': true }")
    List<Group> findByCreatorId(String creatorId);

    @Query("{ 'adminIds': ?0, 'active': true }")
    List<Group> findByAdminId(String adminId);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'active': true }")
    List<Group> searchByName(String name);

    @Query(value = "{ '_id': ?0, 'memberIds': ?1 }", exists = true)
    boolean isMember(String groupId, String userId);

    @Query(value = "{ '_id': ?0, 'adminIds': ?1 }", exists = true)
    boolean isAdmin(String groupId, String userId);

    @Query(value = "{ 'memberIds': ?0, 'active': true }", count = true)
    long countUserGroups(String userId);

}
