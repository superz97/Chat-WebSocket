package com.github.superz97.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicator {

    private String userId;

    private String username;

    private String channelId;

    private String groupId;

    private String recipientId; // For private messages

    @Builder.Default
    private boolean typing = false;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

}
