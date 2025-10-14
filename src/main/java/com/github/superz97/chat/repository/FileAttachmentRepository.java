package com.github.superz97.chat.repository;

import com.github.superz97.chat.entity.FileAttachment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileAttachmentRepository extends MongoRepository<FileAttachment, String> {

    List<FileAttachment> findByMessageId(String messageId);

    List<FileAttachment> findByUploaderId(String uploaderId);

    @Query("{ 'uploaderId': ?0, 'uploadedAt': { $gte: ?1, $lte: ?2 } }")
    List<FileAttachment> findByUploaderAndDateRange(String uploaderId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("{ 'fileType': ?0 }")
    List<FileAttachment> findByFileType(FileAttachment.FileType fileType);

    @Query("{ 'uploaderId': ?0, 'fileType': ?1 }")
    List<FileAttachment> findByUploaderAndFileType(String uploaderId, FileAttachment.FileType fileType);

    @Query("{ 'originalFileName': { $regex: ?0, $options: 'i' } }")
    List<FileAttachment> searchByFileName(String fileName);

    @Query(value = "{ 'uploaderId': ?0 }", count = true)
    long countByUploader(String uploaderId);

    @Query("{ 'uploaderId': ?0 }")
    List<FileAttachment> findAllByUploader(String uploaderId);

    // Find files larger than specified size (in bytes)
    @Query("{ 'fileSize': { $gt: ?0 } }")
    List<FileAttachment> findLargeFiles(Long minSize);

    // Calculate total storage used by user
    @Query(value = "{ 'uploaderId': ?0 }", fields = "{ 'fileSize': 1 }")
    List<FileAttachment> findFileSizesByUploader(String uploaderId);

}
