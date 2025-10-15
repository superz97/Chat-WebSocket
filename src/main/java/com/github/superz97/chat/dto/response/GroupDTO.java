package com.github.superz97.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDTO {

    private String id;
    private String name;
    private String description;
    private String creatorId;
    private Set<String> memberIds;
    private Set<String> adminIds;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
    private Integer maxMembers;
    private int currentMemberCount;
    private GroupSettingsDTO settings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupSettingsDTO {
        private boolean allowMemberInvites;
        private boolean allowMemberMessages;
        private boolean onlyAdminsCanPost;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateGroupRequest {
        private String name;
        private String description;
        private Set<String> memberIds;
        private Integer maxMembers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateGroupRequest {
        private String name;
        private String description;
        private String avatarUrl;
        private Integer maxMembers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateGroupSettingsRequest {
        private Boolean allowMemberInvites;
        private Boolean allowMemberMessages;
        private Boolean onlyAdminsCanPost;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddMemberRequest {
        private String userId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemoveMemberRequest {
        private String userId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromoteToAdminRequest {
        private String userId;
    }

}
