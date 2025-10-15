package com.github.superz97.chat.dto.response;

import com.github.superz97.chat.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    private String id;
    private String senderId;
    private String senderUsername;
    private String recipientId;
    private String channelId;
    private String groupId;
    private MessageType type;
    private String content;
    private List<String> attachmentIds;
    private LocalDateTime timestamp;
    private LocalDateTime editedAt;
    private boolean edited;
    private boolean deleted;
    private Set<String> readBy;
    private String replyToMessageId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendMessageRequest {
        private String recipientId;
        private String channelId;
        private String groupId;
        private String content;
        private MessageType type;
        private String replyToMessageId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EditMessageRequest {
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageSearchRequest {
        private String searchTerm;
        private String channelId;
        private String groupId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageHistoryRequest {
        private String channelId;
        private String groupId;
        private String recipientId;
        private int page;
        private int size;
    }

}
