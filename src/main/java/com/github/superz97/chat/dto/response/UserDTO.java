package com.github.superz97.chat.dto.response;

import com.github.superz97.chat.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String bio;
    private UserStatus status;
    private LocalDateTime lastSeen;
    private LocalDateTime createdAt;
    private Set<String> channelIds;
    private Set<String> groupIds;

}
