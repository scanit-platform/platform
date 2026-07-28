package com.scanit.receipt.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTests {

    @Mock
    private S3Client s3Client;

    private S3StorageService s3StorageService;

    @BeforeEach
    void setUp() {
        s3StorageService =
                new S3StorageService(
                        s3Client,
                        "test-bucket"
                );
    }

    @Test
    void shouldDeleteReceiptObjectFromS3() {
        s3StorageService.deleteReceipt(
                "https://test-bucket.s3.amazonaws.com/receipt-key"
        );

        ArgumentCaptor<DeleteObjectRequest> captor =
                ArgumentCaptor.forClass(
                        DeleteObjectRequest.class
                );

        verify(s3Client).deleteObject(
                captor.capture()
        );

        assertThat(captor.getValue().bucket())
                .isEqualTo("test-bucket");

        assertThat(captor.getValue().key())
                .isEqualTo("receipt-key");
    }
}