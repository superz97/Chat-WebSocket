package com.github.superz97.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "groups")
public class Group {

    @Id
    private String id;

    private String name;

    private String description;

    private String creatorId;

    @Builder.Default
    private Set<String> memberIds = new HashSet<>();

    @Builder.Default
    private Set<String> adminIds = new HashSet<>();

    private String avatarUrl;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @Builder.Default
    private boolean active = true;

    private Integer maxMembers;

    @Builder.Default
    private GroupSettings settings = new GroupSettings();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupSettings {
        @Builder.Default
        private boolean allowMemberInvites = true;

        @Builder.Default
        private boolean allowMemberMessages = true;

        @Builder.Default
        private boolean onlyAdminsCanPost = false;
    }

}
