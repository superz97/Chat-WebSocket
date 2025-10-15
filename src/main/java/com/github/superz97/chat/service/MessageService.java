package com.github.superz97.chat.service;

import com.github.superz97.chat.dto.response.MessageDTO;
import com.github.superz97.chat.entity.Message;
import com.github.superz97.chat.entity.User;
import com.github.superz97.chat.enums.MessageType;
import com.github.superz97.chat.exception.ForbiddenException;
import com.github.superz97.chat.exception.ResourceNotFoundException;
import com.github.superz97.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserService userService;

    @Transactional
    public Message sendMessage(String senderId, MessageDTO.SendMessageRequest request) {
        log.info("Sending message from user: {}", senderId);

        User sender = userService.getUserById(senderId);

        // Check if user is blocked
        if (request.getRecipientId() != null) {
            if (userService.isUserBlocked(request.getRecipientId(), senderId)) {
                throw new ForbiddenException("You are blocked by this user");
            }
        }

        Message message = Message.builder()
                .senderId(senderId)
                .senderUsername(sender.getUsername())
                .recipientId(request.getRecipientId())
                .channelId(request.getChannelId())
                .groupId(request.getGroupId())
                .type(request.getType() != null ? request.getType() : MessageType.TEXT)
                .content(request.getContent())
                .replyToMessageId(request.getReplyToMessageId())
                .timestamp(LocalDateTime.now())
                .build();

        return messageRepository.save(message);
    }

    public Message getMessageById(String messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", messageId));
    }

    public Page<Message> getChannelMessages(String channelId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return messageRepository.findByChannelIdAndDeletedFalseOrderByTimestampDesc(channelId, pageable);
    }

    public Page<Message> getGroupMessages(String groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return messageRepository.findByGroupIdAndDeletedFalseOrderByTimestampDesc(groupId, pageable);
    }

    public Page<Message> getPrivateMessages(String userId1, String userId2, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return messageRepository.findPrivateMessagesBetweenUsers(userId1, userId2, pageable);
    }

    @Transactional
    public Message editMessage(String messageId, String userId, String newContent) {
        log.info("Editing message: {} by user: {}", messageId, userId);

        Message message = getMessageById(messageId);

        if (!message.getSenderId().equals(userId)) {
            throw new ForbiddenException("You can only edit your own messages");
        }

        message.setContent(newContent);
        message.setEdited(true);
        message.setEditedAt(LocalDateTime.now());

        return messageRepository.save(message);
    }

    @Transactional
    public void deleteMessage(String messageId, String userId) {
        log.info("Deleting message: {} by user: {}", messageId, userId);

        Message message = getMessageById(messageId);

        if (!message.getSenderId().equals(userId)) {
            throw new ForbiddenException("You can only delete your own messages");
        }

        message.setDeleted(true);
        messageRepository.save(message);
    }

    @Transactional
    public void markAsRead(String messageId, String userId) {
        Message message = getMessageById(messageId);
        message.getReadBy().add(userId);
        messageRepository.save(message);
    }

    @Transactional
    public void markChannelMessagesAsRead(String channelId, String userId) {
        List<Message> unreadMessages = messageRepository.findUnreadChannelMessages(channelId, userId);
        unreadMessages.forEach(message -> message.getReadBy().add(userId));
        messageRepository.saveAll(unreadMessages);
    }

    @Transactional
    public void markGroupMessagesAsRead(String groupId, String userId) {
        List<Message> unreadMessages = messageRepository.findUnreadGroupMessages(groupId, userId);
        unreadMessages.forEach(message -> message.getReadBy().add(userId));
        messageRepository.saveAll(unreadMessages);
    }

    @Transactional
    public void markPrivateMessagesAsRead(String senderId, String recipientId) {
        List<Message> unreadMessages = messageRepository.findUnreadPrivateMessages(recipientId);
        unreadMessages.stream()
                .filter(m -> m.getSenderId().equals(senderId))
                .forEach(message -> message.getReadBy().add(recipientId));
        messageRepository.saveAll(unreadMessages);
    }

    public long getUnreadPrivateMessageCount(String userId) {
        return messageRepository.countUnreadPrivateMessages(userId);
    }

    public long getUnreadChannelMessageCount(String channelId, String userId) {
        return messageRepository.countUnreadChannelMessages(channelId, userId);
    }

    public long getUnreadGroupMessageCount(String groupId, String userId) {
        return messageRepository.countUnreadGroupMessages(groupId, userId);
    }

    public List<Message> searchInChannel(String channelId, String searchTerm) {
        return messageRepository.searchInChannel(channelId, searchTerm);
    }

    public List<Message> searchInGroup(String groupId, String searchTerm) {
        return messageRepository.searchInGroup(groupId, searchTerm);
    }

    public List<Message> getReplies(String messageId) {
        return messageRepository.findByReplyToMessageIdAndDeletedFalse(messageId);
    }

    @Transactional
    public void addAttachment(String messageId, String attachmentId) {
        Message message = getMessageById(messageId);
        message.getAttachmentIds().add(attachmentId);
        messageRepository.save(message);
    }

    public MessageDTO toDTO(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .senderUsername(message.getSenderUsername())
                .recipientId(message.getRecipientId())
                .channelId(message.getChannelId())
                .groupId(message.getGroupId())
                .type(message.getType())
                .content(message.getContent())
                .attachmentIds(message.getAttachmentIds())
                .timestamp(message.getTimestamp())
                .editedAt(message.getEditedAt())
                .edited(message.isEdited())
                .deleted(message.isDeleted())
                .readBy(message.getReadBy())
                .replyToMessageId(message.getReplyToMessageId())
                .build();
    }

}
