package com.github.superz97.chat.service;

import com.github.superz97.chat.config.FileStorageConfig;
import com.github.superz97.chat.dto.response.FileDTO;
import com.github.superz97.chat.entity.FileAttachment;
import com.github.superz97.chat.entity.User;
import com.github.superz97.chat.enums.FileType;
import com.github.superz97.chat.exception.FileStorageException;
import com.github.superz97.chat.exception.ResourceNotFoundException;
import com.github.superz97.chat.repository.FileAttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final FileAttachmentRepository fileAttachmentRepository;
    private final FileStorageConfig fileStorageConfig;
    private final UserService userService;

    @Transactional
    public FileAttachment uploadFile(MultipartFile file, String uploaderId, String messageId) {
        log.info("Uploading file: {} by user: {}", file.getOriginalFilename(), uploaderId);

        User uploader = userService.getUserById(uploaderId);

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (originalFileName.contains("..")) {
                throw new FileStorageException("Invalid file path: " + originalFileName);
            }

            String fileExtension = getFileExtension(originalFileName);
            String storedFileName = UUID.randomUUID().toString() + fileExtension;

            FileType fileType = FileAttachment.determineFileType(file.getContentType());
            String subDirectory = getSubDirectoryForFileType(fileType);

            Path targetLocation = fileStorageConfig.getSubDirectory(subDirectory)
                    .resolve(storedFileName);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            FileAttachment fileAttachment = FileAttachment.builder()
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .filePath(subDirectory + "/" + storedFileName)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .uploaderId(uploaderId)
                    .uploaderUsername(uploader.getUsername())
                    .messageId(messageId)
                    .fileType(fileType)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            return fileAttachmentRepository.save(fileAttachment);

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName, ex);
        }
    }

    public FileAttachment getFileById(String fileId) {
        return fileAttachmentRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));
    }

    public Resource loadFileAsResource(String fileId) {
        try {
            FileAttachment fileAttachment = getFileById(fileId);
            Path filePath = fileStorageConfig.getFileStorageLocation()
                    .resolve(fileAttachment.getFilePath())
                    .normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found: " + fileAttachment.getOriginalFileName());
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found", ex);
        }
    }

    public List<FileAttachment> getFilesByMessage(String messageId) {
        return fileAttachmentRepository.findByMessageId(messageId);
    }

    public List<FileAttachment> getFilesByUploader(String uploaderId) {
        return fileAttachmentRepository.findByUploaderId(uploaderId);
    }

    @Transactional
    public void deleteFile(String fileId, String userId) {
        log.info("Deleting file: {} by user: {}", fileId, userId);

        FileAttachment fileAttachment = getFileById(fileId);

        // Only uploader can delete
        if (!fileAttachment.getUploaderId().equals(userId)) {
            throw new FileStorageException("You can only delete your own files");
        }

        try {
            Path filePath = fileStorageConfig.getFileStorageLocation()
                    .resolve(fileAttachment.getFilePath())
                    .normalize();

            Files.deleteIfExists(filePath);
            fileAttachmentRepository.delete(fileAttachment);

        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file", ex);
        }
    }

    public long calculateUserStorageUsage(String userId) {
        List<FileAttachment> files = fileAttachmentRepository.findFileSizesByUploader(userId);
        return files.stream()
                .mapToLong(FileAttachment::getFileSize)
                .sum();
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex);
    }

    private String getSubDirectoryForFileType(FileType fileType) {
        return switch (fileType) {
            case IMAGE -> "images";
            case VIDEO -> "videos";
            case AUDIO -> "audio";
            case DOCUMENT -> "documents";
            case OTHER -> "others";
        };
    }

    public FileDTO toDTO(FileAttachment fileAttachment) {
        return FileDTO.builder()
                .id(fileAttachment.getId())
                .originalFileName(fileAttachment.getOriginalFileName())
                .contentType(fileAttachment.getContentType())
                .fileSize(fileAttachment.getFileSize())
                .uploaderId(fileAttachment.getUploaderId())
                .uploaderUsername(fileAttachment.getUploaderUsername())
                .messageId(fileAttachment.getMessageId())
                .uploadedAt(fileAttachment.getUploadedAt())
                .fileType(fileAttachment.getFileType())
                .downloadUrl("/api/files/" + fileAttachment.getId() + "/download")
                .build();
    }

    public List<FileDTO> toDTOList(List<FileAttachment> files) {
        return files.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

}
