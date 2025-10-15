package com.github.superz97.chat.dto.response;

import com.github.superz97.chat.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {

    private String id;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String uploaderId;
    private String uploaderUsername;
    private String messageId;
    private LocalDateTime uploadedAt;
    private FileType fileType;
    private String downloadUrl;
    private String thumbnailUrl;

}
