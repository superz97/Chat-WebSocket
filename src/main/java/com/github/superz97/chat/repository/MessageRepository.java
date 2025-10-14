package com.github.superz97.chat.repository;

import com.github.superz97.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    // Channel messages
    Page<Message> findByChannelIdAndDeletedFalseOrderByTimestampDesc(String channelId, Pageable pageable);
    List<Message> findByChannelIdAndTimestampAfterAndDeletedFalse(String channelId, LocalDateTime since);

    // Group messages
    Page<Message> findByGroupIdAndDeletedFalseOrderByTimestampDesc(String groupId, Pageable pageable);
    List<Message> findByGroupIdAndTimestampAfterAndDeletedFalse(String groupId, LocalDateTime since);

    // Private messages between two users
    @Query("{ $or: [ " +
            "{ 'senderId': ?0, 'recipientId': ?1 }, " +
            "{ 'senderId': ?1, 'recipientId': ?0 } " +
            "], 'deleted': false }")
    Page<Message> findPrivateMessagesBetweenUsers(String userId1, String userId2, Pageable pageable);

    @Query("{ 'channelId': ?0, 'content': { $regex: ?1, $options: 'i' }, 'deleted': false }")
    List<Message> searchInChannel(String channelId, String searchTerm);

    @Query("{ 'groupId': ?0, 'content': { $regex: ?1, $options: 'i' }, 'deleted': false }")
    List<Message> searchInGroup(String groupId, String searchTerm);

    // Unread messages
    @Query("{ 'recipientId': ?0, 'readBy': { $nin: [?0] }, 'deleted': false }")
    List<Message> findUnreadPrivateMessages(String userId);

    @Query("{ 'channelId': ?0, 'readBy': { $nin: [?1] }, 'deleted': false }")
    List<Message> findUnreadChannelMessages(String channelId, String userId);

    @Query("{ 'groupId': ?0, 'readBy': { $nin: [?1] }, 'deleted': false }")
    List<Message> findUnreadGroupMessages(String groupId, String userId);

    // Count unread messages
    @Query(value = "{ 'recipientId': ?0, 'readBy': { $nin: [?0] }, 'deleted': false }", count = true)
    long countUnreadPrivateMessages(String userId);

    @Query(value = "{ 'channelId': ?0, 'readBy': { $nin: [?1] }, 'deleted': false }", count = true)
    long countUnreadChannelMessages(String channelId, String userId);

    @Query(value = "{ 'groupId': ?0, 'readBy': { $nin: [?1] }, 'deleted': false }", count = true)
    long countUnreadGroupMessages(String groupId, String userId);

    // Messages by sender
    List<Message> findBySenderIdAndDeletedFalse(String senderId);

    // Threaded messages (replies)
    List<Message> findByReplyToMessageIdAndDeletedFalse(String replyToMessageId);

}
