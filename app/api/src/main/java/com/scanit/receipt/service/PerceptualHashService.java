package com.scanit.receipt.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class PerceptualHashService {

    /*
     * A 9 × 8 image produces exactly 64 comparisons:
     *
     * 8 horizontal comparisons per row
     * × 8 rows
     * = 64 hash bits.
     */
    private static final int HASH_WIDTH = 9;
    private static final int HASH_HEIGHT = 8;

    /*
     * A 64-bit hash is represented by 16 hexadecimal characters.
     */
    private static final int HASH_HEX_LENGTH = 16;

    /*
     * Two receipt images are treated as visually similar when
     * no more than five bits of their hashes are different.
     */
    private static final int MAX_HAMMING_DISTANCE = 5;

    public String calculateHash(MultipartFile file) {
        try {
            BufferedImage image = readImage(file);

            return calculateDifferenceHash(image);
        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Could not calculate receipt image hash",
                    exception
            );
        }
    }

    /**
     * Determines whether two perceptual hashes represent
     * visually similar receipt images.
     */
    public boolean isSimilar(
            String firstHash,
            String secondHash
    ) {
        int distance = calculateHammingDistance(
                firstHash,
                secondHash
        );

        return distance <= MAX_HAMMING_DISTANCE;
    }

    /**
     * Counts how many bits are different between two hashes.
     *
     * Package-private visibility allows the service test in
     * the same package to verify the calculated distance.
     */
    int calculateHammingDistance(
            String firstHash,
            String secondHash
    ) {
        if (!isValidHash(firstHash)
                || !isValidHash(secondHash)) {

            return Integer.MAX_VALUE;
        }

        try {
            long firstValue = Long.parseUnsignedLong(
                    firstHash,
                    16
            );

            long secondValue = Long.parseUnsignedLong(
                    secondHash,
                    16
            );

            long differentBits =
                    firstValue ^ secondValue;

            return Long.bitCount(differentBits);
        } catch (NumberFormatException exception) {
            return Integer.MAX_VALUE;
        }
    }

    private boolean isValidHash(String hash) {
        return hash != null
                && hash.length() == HASH_HEX_LENGTH
                && hash.matches("[0-9a-fA-F]{16}");
    }

    private BufferedImage readImage(
            MultipartFile file
    ) throws IOException {

        if ("application/pdf".equalsIgnoreCase(
                file.getContentType()
        )) {
            return readFirstPdfPage(file);
        }

        BufferedImage image = ImageIO.read(
                new ByteArrayInputStream(
                        file.getBytes()
                )
        );

        if (image == null) {
            throw new IllegalArgumentException(
                    "The uploaded file is not a readable image"
            );
        }

        return image;
    }

    private BufferedImage readFirstPdfPage(
            MultipartFile file
    ) throws IOException {

        try (PDDocument document =
                     Loader.loadPDF(file.getBytes())) {

            if (document.getNumberOfPages() == 0) {
                throw new IllegalArgumentException(
                        "The uploaded PDF has no pages"
                );
            }

            PDFRenderer renderer =
                    new PDFRenderer(document);

            return renderer.renderImageWithDPI(
                    0,
                    150
            );
        }
    }

    private String calculateDifferenceHash(
            BufferedImage source
    ) {
        BufferedImage resized = new BufferedImage(
                HASH_WIDTH,
                HASH_HEIGHT,
                BufferedImage.TYPE_BYTE_GRAY
        );

        Graphics2D graphics =
                resized.createGraphics();

        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            graphics.drawImage(
                    source,
                    0,
                    0,
                    HASH_WIDTH,
                    HASH_HEIGHT,
                    null
            );
        } finally {
            graphics.dispose();
        }

        long hash = 0L;
        int bitPosition = 0;

        for (int y = 0; y < HASH_HEIGHT; y++) {
            for (int x = 0;
                 x < HASH_WIDTH - 1;
                 x++) {

                int leftPixel = resized
                        .getRaster()
                        .getSample(x, y, 0);

                int rightPixel = resized
                        .getRaster()
                        .getSample(x + 1, y, 0);

                if (leftPixel > rightPixel) {
                    hash |= 1L << bitPosition;
                }

                bitPosition++;
            }
        }

        return String.format(
                "%016x",
                hash
        );
    }
}