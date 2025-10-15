package com.github.superz97.chat.entity;

import com.github.superz97.chat.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "file_attachments")
public class FileAttachment {

    @Id
    private String id;

    private String originalFileName;

    private String storedFileName;

    private String filePath;

    private String contentType;

    private Long fileSize;

    @Indexed
    private String uploaderId;

    private String uploaderUsername;

    @Indexed
    private String messageId;

    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();

    private String thumbnailPath; // For images/videos

    @Builder.Default
    private FileType fileType = FileType.OTHER;

    public static FileType determineFileType(String contentType) {
        if (contentType == null) return FileType.OTHER;

        if (contentType.startsWith("image/")) return FileType.IMAGE;
        if (contentType.startsWith("video/")) return FileType.VIDEO;
        if (contentType.startsWith("audio/")) return FileType.AUDIO;
        if (contentType.contains("pdf") ||
                contentType.contains("document") ||
                contentType.contains("text") ||
                contentType.contains("spreadsheet")) return FileType.DOCUMENT;

        return FileType.OTHER;
    }

}
