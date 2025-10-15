package com.github.superz97.chat.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Getter
public class FileStorageConfig {

    @Value("${chat.file-upload.directory}")
    private String uploadDirectory;

    @Value("${chat.file-upload.max-size}")
    private long maxFileSize;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDirectory).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);

            // Create subdirectories for different file types
            Files.createDirectories(this.fileStorageLocation.resolve("images"));
            Files.createDirectories(this.fileStorageLocation.resolve("videos"));
            Files.createDirectories(this.fileStorageLocation.resolve("documents"));
            Files.createDirectories(this.fileStorageLocation.resolve("audio"));
            Files.createDirectories(this.fileStorageLocation.resolve("others"));
            Files.createDirectories(this.fileStorageLocation.resolve("thumbnails"));

        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }

    public Path getSubDirectory(String subDir) {
        return fileStorageLocation.resolve(subDir);
    }

}
