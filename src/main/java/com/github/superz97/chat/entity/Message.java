package com.github.superz97.chat.entity;

import com.github.superz97.chat.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
@CompoundIndex(name = "channel_timestamp", def = "{'channelId': 1, 'timestamp': -1}")
@CompoundIndex(name = "group_timestamp", def = "{'groupId': 1, 'timestamp': -1}")
public class Message {

    @Id
    private String id;

    @Indexed
    private String senderId;

    private String senderUsername;

    private String recipientId; // For private messages

    private String channelId; // For channel messages

    private String groupId; // For group messages

    private MessageType type;

    private String content;

    @Builder.Default
    private List<String> attachmentIds = new ArrayList<>();

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private LocalDateTime editedAt;

    @Builder.Default
    private boolean edited = false;

    @Builder.Default
    private boolean deleted = false;

    @Builder.Default
    private Set<String> readBy = new HashSet<>();

    private String replyToMessageId; // For threaded conversations

}
