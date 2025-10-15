package com.github.superz97.chat.service;

import com.github.superz97.chat.dto.response.NotificationDTO;
import com.github.superz97.chat.dto.response.TypingIndicatorDTO;
import com.github.superz97.chat.dto.response.WebSocketMessageDTO;
import com.github.superz97.chat.enums.WebSocketMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    // Send message to a specific channel
    public void sendToChannel(String channelId, WebSocketMessageDTO message) {
        log.debug("Sending WebSocket message to channel: {}", channelId);
        messagingTemplate.convertAndSend("/topic/channel/" + channelId, message);
    }

    // Send message to a specific group
    public void sendToGroup(String groupId, WebSocketMessageDTO message) {
        log.debug("Sending WebSocket message to group: {}", groupId);
        messagingTemplate.convertAndSend("/topic/group/" + groupId, message);
    }

    // Send private message to a specific user
    public void sendToUser(String userId, WebSocketMessageDTO message) {
        log.debug("Sending WebSocket message to user: {}", userId);
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/messages",
                message
        );
    }

    // Send notification to a specific user
    public void sendNotificationToUser(String userId, NotificationDTO notification) {
        log.debug("Sending notification to user: {}", userId);
        WebSocketMessageDTO message = WebSocketMessageDTO.typed(
                WebSocketMessageType.NOTIFICATION,
                notification
        );
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                message
        );
    }

    // Broadcast typing indicator to channel
    public void broadcastTypingToChannel(String channelId, TypingIndicatorDTO typingIndicator) {
        log.debug("Broadcasting typing indicator to channel: {}", channelId);
        WebSocketMessageDTO message = WebSocketMessageDTO.typed(
                typingIndicator.isTyping()
                        ? WebSocketMessageType.TYPING_START
                        : WebSocketMessageType.TYPING_STOP,
                typingIndicator
        );
        messagingTemplate.convertAndSend("/topic/channel/" + channelId + "/typing", message);
    }

    // Broadcast typing indicator to group
    public void broadcastTypingToGroup(String groupId, TypingIndicatorDTO typingIndicator) {
        log.debug("Broadcasting typing indicator to group: {}", groupId);
        WebSocketMessageDTO message = WebSocketMessageDTO.typed(
                typingIndicator.isTyping()
                        ? WebSocketMessageType.TYPING_START
                        : WebSocketMessageType.TYPING_STOP,
                typingIndicator
        );
        messagingTemplate.convertAndSend("/topic/group/" + groupId + "/typing", message);
    }

    // Send typing indicator to specific user (for private chat)
    public void sendTypingToUser(String userId, TypingIndicatorDTO typingIndicator) {
        log.debug("Sending typing indicator to user: {}", userId);
        WebSocketMessageDTO message = WebSocketMessageDTO.typed(
                typingIndicator.isTyping()
                        ? WebSocketMessageType.TYPING_START
                        : WebSocketMessageType.TYPING_STOP,
                typingIndicator
        );
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/typing",
                message
        );
    }

    // Broadcast user status change
    public void broadcastUserStatusChange(String userId, String status) {
        log.debug("Broadcasting user status change: {} - {}", userId, status);
        WebSocketMessageDTO message = WebSocketMessageDTO.typed(
                WebSocketMessageType.USER_STATUS_CHANGE,
                new UserStatusChangePayload(userId, status)
        );
        messagingTemplate.convertAndSend("/topic/user-status", message);
    }

    // Broadcast user online status
    public void broadcastUserOnline(String userId, String username) {
        log.debug("Broadcasting user online: {}", userId);
        WebSocketMessageDTO message = WebSocketMessageDTO.typed(
                WebSocketMessageType.USER_ONLINE,
                new UserOnlinePayload(userId, username)
        );
        messagingTemplate.convertAndSend("/topic/user-status", message);
    }

    // Broadcast user offline status
    public void broadcastUserOffline(String userId, String username) {
        log.debug("Broadcasting user offline: {}", userId);
        WebSocketMessageDTO message = WebSocketMessageDTO.typed(
                WebSocketMessageType.USER_OFFLINE,
                new UserOnlinePayload(userId, username)
        );
        messagingTemplate.convertAndSend("/topic/user-status", message);
    }

    // Broadcast channel event to all members
    public void broadcastChannelEvent(String channelId, WebSocketMessageType eventType, Object payload) {
        log.debug("Broadcasting channel event: {} to channel: {}", eventType, channelId);
        WebSocketMessageDTO message = WebSocketMessageDTO.typed(eventType, payload);
        messagingTemplate.convertAndSend("/topic/channel/" + channelId, message);
    }

    // Broadcast group event to all members
    public void broadcastGroupEvent(String groupId, WebSocketMessageType eventType, Object payload) {
        log.debug("Broadcasting group event: {} to group: {}", eventType, groupId);
        WebSocketMessageDTO message = WebSocketMessageDTO.typed(eventType, payload);
        messagingTemplate.convertAndSend("/topic/group/" + groupId, message);
    }

    // Helper payload classes
    public record UserStatusChangePayload(String userId, String status) {}
    public record UserOnlinePayload(String userId, String username) {}

}
