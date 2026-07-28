package com.scanit.receipt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class S3StorageService {

    private static final long MAX_FILE_SIZE_BYTES =
            10L * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "application/pdf"
            );

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageService(
            S3Client s3Client,
            @Value(
                    "${spring.cloud.aws.s3.receipts-bucket}"
            ) String bucketName
    ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public String upload(MultipartFile file) {
        validateReceipt(file);

        String originalFilename =
                getOriginalFilename(file);

        String key =
                UUID.randomUUID().toString();

        try {
            PutObjectRequest request =
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(
                                    file.getContentType()
                            )
                            .metadata(
                                    Map.of(
                                            "original-filename",
                                            originalFilename
                                    )
                            )
                            .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(
                            file.getBytes()
                    )
            );
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Receipt upload failed. Please try again.",
                    exception
            );
        }

        return String.format(
                "https://%s.s3.amazonaws.com/%s",
                bucketName,
                key
        );
    }

    public byte[] downloadReceipt(
            String imageUrl
    ) {
        String key = extractKey(imageUrl);

        GetObjectRequest request =
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

        ResponseBytes<GetObjectResponse> objectAsBytes =
                s3Client.getObjectAsBytes(request);

        return objectAsBytes.asByteArray();
    }

    public void deleteReceipt(
            String imageUrl
    ) {
        String key = extractKey(imageUrl);

        DeleteObjectRequest request =
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

        s3Client.deleteObject(request);
    }

    private String extractKey(
            String imageUrl
    ) {
        if (imageUrl == null
                || imageUrl.isBlank()) {

            throw new IllegalArgumentException(
                    "Receipt image URL is required"
            );
        }

        int separatorIndex =
                imageUrl.lastIndexOf("/");

        if (separatorIndex < 0
                || separatorIndex
                == imageUrl.length() - 1) {

            throw new IllegalArgumentException(
                    "Receipt image URL is invalid"
            );
        }

        return imageUrl.substring(
                separatorIndex + 1
        );
    }

    private void validateReceipt(
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a receipt file."
            );
        }

        if (file.getSize()
                > MAX_FILE_SIZE_BYTES) {

            throw new IllegalArgumentException(
                    "The file is too large. Maximum size is 10 MB."
            );
        }

        String contentType =
                file.getContentType();

        if (contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(
                contentType
        )) {

            throw new IllegalArgumentException(
                    "Invalid file format. Please upload a JPG, PNG or PDF."
            );
        }
    }

    private String getOriginalFilename(
            MultipartFile file
    ) {
        String originalFilename =
                file.getOriginalFilename();

        if (originalFilename == null
                || originalFilename.isBlank()) {

            return "receipt";
        }

        return Paths.get(originalFilename)
                .getFileName()
                .toString();
    }
}