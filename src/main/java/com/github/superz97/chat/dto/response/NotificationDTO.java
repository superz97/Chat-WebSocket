package com.github.superz97.chat.dto.response;

import com.github.superz97.chat.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private String id;
    private NotificationType type;
    private String title;
    private String message;
    private String senderId;
    private String senderUsername;
    private String channelId;
    private String groupId;
    private String messageId;
    private LocalDateTime timestamp;
    private boolean read;

}
