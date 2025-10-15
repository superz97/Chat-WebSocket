package com.github.superz97.chat.controller;

import com.github.superz97.chat.dto.response.MessageDTO;
import com.github.superz97.chat.dto.response.TypingIndicatorDTO;
import com.github.superz97.chat.dto.response.WebSocketMessageDTO;
import com.github.superz97.chat.entity.Message;
import com.github.superz97.chat.entity.User;
import com.github.superz97.chat.enums.WebSocketMessageType;
import com.github.superz97.chat.service.MessageService;
import com.github.superz97.chat.service.UserService;
import com.github.superz97.chat.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final MessageService messageService;
    private final UserService userService;
    private final WebSocketService webSocketService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageDTO.SendMessageRequest request,
                            Principal principal) {
        try {
            String username = principal.getName();
            User user = userService.getUserByUsername(username);

            Message message = messageService.sendMessage(user.getId(), request);
            MessageDTO messageDTO = messageService.toDTO(message);

            WebSocketMessageDTO wsMessage = WebSocketMessageDTO.typed(
                    WebSocketMessageType.MESSAGE,
                    messageDTO
            );

            // Route message to appropriate destination
            if (message.getChannelId() != null) {
                webSocketService.sendToChannel(message.getChannelId(), wsMessage);
            } else if (message.getGroupId() != null) {
                webSocketService.sendToGroup(message.getGroupId(), wsMessage);
            } else if (message.getRecipientId() != null) {
                webSocketService.sendToUser(message.getRecipientId(), wsMessage);
                webSocketService.sendToUser(message.getSenderId(), wsMessage);
            }

        } catch (Exception e) {
            log.error("Error sending message via WebSocket", e);
        }
    }

    @MessageMapping("/chat.typing.channel.{channelId}")
    public void sendTypingToChannel(@DestinationVariable String channelId,
                                    @Payload TypingIndicatorDTO typingIndicator,
                                    Principal principal) {
        try {
            String username = principal.getName();
            User user = userService.getUserByUsername(username);

            typingIndicator.setUserId(user.getId());
            typingIndicator.setUsername(user.getUsername());
            typingIndicator.setChannelId(channelId);

            webSocketService.broadcastTypingToChannel(channelId, typingIndicator);

        } catch (Exception e) {
            log.error("Error sending typing indicator to channel", e);
        }
    }

    @MessageMapping("/chat.typing.group.{groupId}")
    public void sendTypingToGroup(@DestinationVariable String groupId,
                                  @Payload TypingIndicatorDTO typingIndicator,
                                  Principal principal) {
        try {
            String username = principal.getName();
            User user = userService.getUserByUsername(username);

            typingIndicator.setUserId(user.getId());
            typingIndicator.setUsername(user.getUsername());
            typingIndicator.setGroupId(groupId);

            webSocketService.broadcastTypingToGroup(groupId, typingIndicator);

        } catch (Exception e) {
            log.error("Error sending typing indicator to group", e);
        }
    }

    @MessageMapping("/chat.typing.user.{userId}")
    public void sendTypingToUser(@DestinationVariable String userId,
                                 @Payload TypingIndicatorDTO typingIndicator,
                                 Principal principal) {
        try {
            String username = principal.getName();
            User user = userService.getUserByUsername(username);

            typingIndicator.setUserId(user.getId());
            typingIndicator.setUsername(user.getUsername());
            typingIndicator.setRecipientId(userId);

            webSocketService.sendTypingToUser(userId, typingIndicator);

        } catch (Exception e) {
            log.error("Error sending typing indicator to user", e);
        }
    }

    @MessageMapping("/chat.message.read")
    public void markMessageAsRead(@Payload String messageId, Principal principal) {
        try {
            String username = principal.getName();
            User user = userService.getUserByUsername(username);

            messageService.markAsRead(messageId, user.getId());

            Message message = messageService.getMessageById(messageId);
            WebSocketMessageDTO wsMessage = WebSocketMessageDTO.typed(
                    WebSocketMessageType.MESSAGE_READ,
                    messageService.toDTO(message)
            );

            // Notify sender
            webSocketService.sendToUser(message.getSenderId(), wsMessage);

        } catch (Exception e) {
            log.error("Error marking message as read", e);
        }
    }

}
