package com.github.superz97.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicatorDTO {

    private String userId;
    private String username;
    private String channelId;
    private String groupId;
    private String recipientId;
    private boolean typing;
    private LocalDateTime timestamp;

}
