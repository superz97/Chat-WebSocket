package com.github.superz97.chat.entity;

import com.github.superz97.chat.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String email;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String bio;

    @Builder.Default
    private UserStatus status;

    private LocalDateTime lastSeen;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private Set<String> channelIds = new HashSet<>();

    @Builder.Default
    private Set<String> groupIds = new HashSet<>();

    @Builder.Default
    private Set<String> blockedUserIds = new HashSet<>();

    @Builder.Default
    private boolean active = true;

}
