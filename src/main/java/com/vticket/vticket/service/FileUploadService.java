package com.vticket.vticket.service;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    private final Logger logger = Logger.getLogger(FileUploadService.class);

    private static final String UPLOAD_DIR = "uploads/profile_img";

    public String uploadFileImg(MultipartFile file) throws IOException {
        long startTime = System.currentTimeMillis();
        if (file.isEmpty()) {
            logger.info("Attempted to upload an empty file.");
            return "/";
        }

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") ||
                contentType.equals("image/png") || contentType.equals("image/gif"))) {
            logger.info("Unsupported file type: " + contentType);
            return null;
        }

        if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
            logger.info("File size exceeds limit: " + file.getSize());
            return null;
        }

        Path uploadPath = Paths.get(UPLOAD_DIR);
        // Ensure the upload directory exists
        if (!Files.exists(uploadPath)) {
            logger.info("Creating upload directory: " + uploadPath);
            Files.createDirectories(uploadPath);
        }

        // Generate a random filename
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String randomFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(randomFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("File uploaded successfully: " + filePath + " in " + (System.currentTimeMillis() - startTime) + "ms");

        return "/" + UPLOAD_DIR + "/" + randomFilename;
    }

}
