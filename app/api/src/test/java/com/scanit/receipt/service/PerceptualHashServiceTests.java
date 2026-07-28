package com.scanit.receipt.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class PerceptualHashServiceTests {

    private final PerceptualHashService service =
            new PerceptualHashService();

    @Test
    void shouldCalculateStableHashForSameImage()
            throws IOException {

        byte[] imageBytes = createTestPng();

        MockMultipartFile firstFile =
                new MockMultipartFile(
                        "file",
                        "receipt.png",
                        "image/png",
                        imageBytes
                );

        MockMultipartFile secondFile =
                new MockMultipartFile(
                        "file",
                        "receipt-copy.png",
                        "image/png",
                        imageBytes
                );

        String firstHash =
                service.calculateHash(firstFile);

        String secondHash =
                service.calculateHash(secondFile);

        assertThat(firstHash).hasSize(16);
        assertThat(secondHash)
                .isEqualTo(firstHash);
    }

    @Test
    void shouldTreatFiveChangedBitsAsSimilar() {
        assertThat(service.isSimilar(
                "0000000000000000",
                "000000000000001f"
        )).isTrue();
    }

    @Test
    void shouldRejectMoreThanFiveChangedBits() {
        assertThat(service.isSimilar(
                "0000000000000000",
                "000000000000003f"
        )).isFalse();
    }

    @Test
    void shouldRejectInvalidHash() {
        assertThat(service.isSimilar(
                "not-a-hash",
                "0000000000000000"
        )).isFalse();
    }

    private byte[] createTestPng()
            throws IOException {

        BufferedImage image = new BufferedImage(
                32,
                32,
                BufferedImage.TYPE_INT_RGB
        );

        for (int y = 0;
             y < image.getHeight();
             y++) {

            for (int x = 0;
                 x < image.getWidth();
                 x++) {

                int grey = x * 255
                        / (image.getWidth() - 1);

                int rgb = grey << 16
                        | grey << 8
                        | grey;

                image.setRGB(x, y, rgb);
            }
        }

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        ImageIO.write(
                image,
                "png",
                output
        );

        return output.toByteArray();
    }
}