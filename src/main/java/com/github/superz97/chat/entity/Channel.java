package com.github.superz97.chat.entity;

import com.github.superz97.chat.enums.ChannelType;
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
@Document(collection = "channels")
public class    Channel {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    private String creatorId;

    @Builder.Default
    private ChannelType type = ChannelType.PUBLIC;

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

}
