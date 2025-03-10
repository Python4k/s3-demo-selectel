package com.python4k.s3demoselectel.service.implementation;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.python4k.s3demoselectel.service.AwsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
public class AwsServiceImplementation implements AwsService {

    private final AmazonS3 s3Client;

    @Override
    public void uploadFile(String bucketName, String keyName, Long contentLength, String contentType, InputStream value) throws AmazonClientException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        metadata.setContentType(contentType);

        s3Client.putObject(bucketName, keyName, value, metadata);
        log.info("Uploaded file {} to {}", keyName, bucketName);
    }

    @Override
    public ByteArrayOutputStream downloadFile(String bucketName, String keyName) throws IOException, AmazonClientException {
        S3Object s3Object = s3Client.getObject(bucketName, keyName);
        InputStream inputStream = s3Object.getObjectContent();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int len;
        byte[] buffer = new byte[4096];
        while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        log.info("Downloaded file {} from {}", keyName, bucketName);
        return outputStream;
    }

    @Override
    public List<String> listFiles(String bucketName) throws AmazonClientException {
        List<String> keys = new ArrayList<>();
        ObjectListing objectListing = s3Client.listObjects(bucketName);

        while (true) {
            List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();
            if (objectSummaries.isEmpty()) {
                break;
            }

            objectSummaries.stream()
                    .map(S3ObjectSummary::getKey)
                    .filter(key -> !key.endsWith("/"))
                    .forEach(keys::add);

            objectListing = s3Client.listNextBatchOfObjects(objectListing);
        }
        log.info("Files found in bucket ({}): {}", bucketName, keys);
        return keys;
    }

    @Override
    public void deleteFile(String bucketName, String keyName) throws AmazonClientException {
        s3Client.deleteObject(bucketName, keyName);
        log.info("Deleted file {} from bucket ({})", keyName, bucketName);
    }
}
