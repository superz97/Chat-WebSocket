package com.github.superz97.chat.controller;

import com.github.superz97.chat.dto.api.ApiResponse;
import com.github.superz97.chat.dto.response.FileDTO;
import com.github.superz97.chat.entity.FileAttachment;
import com.github.superz97.chat.entity.User;
import com.github.superz97.chat.service.FileService;
import com.github.superz97.chat.service.MessageService;
import com.github.superz97.chat.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final UserService userService;
    private final MessageService messageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileDTO>> uploadFile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String messageId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        FileAttachment fileAttachment = fileService.uploadFile(file, user.getId(), messageId);

        // Add attachment to message if messageId provided
        if (messageId != null) {
            messageService.addAttachment(messageId, fileAttachment.getId());
        }

        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully",
                fileService.toDTO(fileAttachment)));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse<FileDTO>> getFileInfo(@PathVariable String fileId) {
        FileAttachment file = fileService.getFileById(fileId);
        return ResponseEntity.ok(ApiResponse.success(fileService.toDTO(file)));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileId,
            HttpServletRequest request) {
        Resource resource = fileService.loadFileAsResource(fileId);
        FileAttachment fileAttachment = fileService.getFileById(fileId);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            contentType = "application/octet-stream";
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileAttachment.getOriginalFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/message/{messageId}")
    public ResponseEntity<ApiResponse<List<FileDTO>>> getMessageFiles(
            @PathVariable String messageId) {
        List<FileAttachment> files = fileService.getFilesByMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success(fileService.toDTOList(files)));
    }

    @GetMapping("/my-uploads")
    public ResponseEntity<ApiResponse<List<FileDTO>>> getMyUploads(
            @AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        List<FileAttachment> files = fileService.getFilesByUploader(user.getId());
        return ResponseEntity.ok(ApiResponse.success(fileService.toDTOList(files)));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String fileId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        fileService.deleteFile(fileId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }

    @GetMapping("/storage-usage")
    public ResponseEntity<ApiResponse<Long>> getStorageUsage(
            @AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        long usage = fileService.calculateUserStorageUsage(user.getId());
        return ResponseEntity.ok(ApiResponse.success(usage));
    }

}
