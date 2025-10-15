package com.github.superz97.chat.controller;

import com.github.superz97.chat.dto.api.ApiResponse;
import com.github.superz97.chat.dto.response.MessageDTO;
import com.github.superz97.chat.entity.Message;
import com.github.superz97.chat.entity.User;
import com.github.superz97.chat.service.MessageService;
import com.github.superz97.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<MessageDTO>> sendMessage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody MessageDTO.SendMessageRequest request) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        Message message = messageService.sendMessage(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Message sent successfully",
                messageService.toDTO(message)));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageDTO>> getMessage(@PathVariable String messageId) {
        Message message = messageService.getMessageById(messageId);
        return ResponseEntity.ok(ApiResponse.success(messageService.toDTO(message)));
    }

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> getChannelMessages(
            @PathVariable String channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<MessageDTO> messages = messageService.getChannelMessages(channelId, page, size)
                .map(messageService::toDTO);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> getGroupMessages(
            @PathVariable String groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<MessageDTO> messages = messageService.getGroupMessages(groupId, page, size)
                .map(messageService::toDTO);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/private/{userId}")
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> getPrivateMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        String keycloakId = jwt.getSubject();
        User currentUser = userService.getUserByKeycloakId(keycloakId);

        Page<MessageDTO> messages = messageService.getPrivateMessages(
                        currentUser.getId(), userId, page, size)
                .map(messageService::toDTO);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageDTO>> editMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String messageId,
            @RequestBody MessageDTO.EditMessageRequest request) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        Message message = messageService.editMessage(messageId, user.getId(), request.getContent());
        return ResponseEntity.ok(ApiResponse.success("Message updated successfully",
                messageService.toDTO(message)));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String messageId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        messageService.deleteMessage(messageId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Message deleted successfully", null));
    }

    @PostMapping("/{messageId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String messageId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        messageService.markAsRead(messageId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Message marked as read", null));
    }

    @PostMapping("/channel/{channelId}/read")
    public ResponseEntity<ApiResponse<Void>> markChannelMessagesAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String channelId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        messageService.markChannelMessagesAsRead(channelId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Messages marked as read", null));
    }

    @PostMapping("/group/{groupId}/read")
    public ResponseEntity<ApiResponse<Void>> markGroupMessagesAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String groupId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        messageService.markGroupMessagesAsRead(groupId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Messages marked as read", null));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        long count = messageService.getUnreadPrivateMessageCount(user.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/channel/{channelId}/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadChannelCount(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String channelId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        long count = messageService.getUnreadChannelMessageCount(channelId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/group/{groupId}/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadGroupCount(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String groupId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        long count = messageService.getUnreadGroupMessageCount(groupId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/channel/{channelId}/search")
    public ResponseEntity<ApiResponse<List<MessageDTO>>> searchInChannel(
            @PathVariable String channelId,
            @RequestParam String query) {
        List<MessageDTO> messages = messageService.searchInChannel(channelId, query).stream()
                .map(messageService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/group/{groupId}/search")
    public ResponseEntity<ApiResponse<List<MessageDTO>>> searchInGroup(
            @PathVariable String groupId,
            @RequestParam String query) {
        List<MessageDTO> messages = messageService.searchInGroup(groupId, query).stream()
                .map(messageService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/{messageId}/replies")
    public ResponseEntity<ApiResponse<List<MessageDTO>>> getReplies(@PathVariable String messageId) {
        List<MessageDTO> replies = messageService.getReplies(messageId).stream()
                .map(messageService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(replies));
    }

}
