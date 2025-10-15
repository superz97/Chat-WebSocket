package com.github.superz97.chat.dto.response;

import com.github.superz97.chat.enums.ChannelType;
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
public class ChannelDTO {

    private String id;
    private String name;
    private String description;
    private String creatorId;
    private ChannelType type;
    private Set<String> memberIds;
    private Set<String> adminIds;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
    private Integer maxMembers;
    private int currentMemberCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateChannelRequest {
        private String name;
        private String description;
        private ChannelType type;
        private Integer maxMembers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateChannelRequest {
        private String name;
        private String description;
        private String avatarUrl;
        private Integer maxMembers;
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
