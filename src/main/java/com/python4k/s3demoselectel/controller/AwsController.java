package com.python4k.s3demoselectel.controller;

import com.amazonaws.AmazonClientException;
import com.python4k.s3demoselectel.enumeration.FileType;
import com.python4k.s3demoselectel.service.AwsService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
@Slf4j
public class AwsController {
    private final AwsService awsService;

    @GetMapping("/{bucketName}")
    public ResponseEntity<?> listFiles(@PathVariable String bucketName) {
        try {
            val files = awsService.listFiles(bucketName);
            return ResponseEntity.ok(files);
        } catch (AmazonClientException e){
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{bucketName}/download/{fileName}")
    @SneakyThrows
    public ResponseEntity<?> downloadFile(@PathVariable String bucketName, @PathVariable String fileName) {
        val body = awsService.downloadFile(bucketName, fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(FileType.fromFilename(fileName))
                .body(body.toByteArray());
    }

    @PostMapping("/{bucketName}/upload")
    @SneakyThrows(IOException.class)
    public ResponseEntity<?> uploadFile(@PathVariable("bucketName") String bucketName, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String contentType = file.getContentType();
        long fileSize = file.getSize();
        InputStream inputStream = file.getInputStream();

        awsService.uploadFile(bucketName, filename, fileSize, contentType, inputStream);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{bucketName}/{fileName}")
    public ResponseEntity<?> deleteFile(@PathVariable String bucketName, @PathVariable String fileName) {
        awsService.deleteFile(bucketName, fileName);
        return ResponseEntity.ok().build();
    }

}
