package at.asit.pdfover.gui.tests;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Holds the results of comparing two images, including the modified images
 * and difference image.
 */
@Getter
public class ImageComparisonResult implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ImageComparisonResult.class);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color DIFFERENCE_COLOR = Color.RED;

    private final BufferedImage modifiedSigned;
    private final BufferedImage modifiedReference;
    private final BufferedImage differenceImage;
    private final boolean equal;

    /**
     * Creates a new comparison result by comparing two images.
     *
     * @param signedImage the modified signed image
     * @param referenceImage the modified reference image
     */
    public ImageComparisonResult(BufferedImage signedImage, BufferedImage referenceImage, BufferedImage differenceImage, boolean areEqual) {
        this.modifiedSigned = Objects.requireNonNull(signedImage, "Signed image cannot be null");
        this.modifiedReference = Objects.requireNonNull(referenceImage, "Reference image cannot be null");
        this.differenceImage = Objects.requireNonNull(differenceImage, "Difference image cannot be null");
        this.equal = areEqual;
    }

    /**
     * Creates a new blank image for visualizing differences between compared images.
     * The image is initialized with a white background ({@link #BACKGROUND_COLOR})
     * and will be used to mark pixel differences in red ({@link #DIFFERENCE_COLOR}).
     *
     * @param width The width of the image in pixels
     * @param height The height of the image in pixels
     * @return A new BufferedImage initialized with a white background
     */
    private static BufferedImage createDifferenceImage(int width, int height) {
        BufferedImage diff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = diff.createGraphics();
        try {
            g2d.setColor(BACKGROUND_COLOR);
            g2d.fillRect(0, 0, width, height);
            return diff;
        } finally {
            g2d.dispose();
        }
    }

    /**
     * Performs pixel-by-pixel comparison of two images and marks differences.
     *
     * @param signed The signed image to compare
     * @param reference The reference image to compare against
     * @param difference The image where differences will be marked in {@link #DIFFERENCE_COLOR}
     * @return true if all pixels match exactly, false if any differences are found
     */
    private static boolean comparePixels(BufferedImage signed, BufferedImage reference, BufferedImage difference) {
        boolean match = true;
        for (int x = 0; x < signed.getWidth(); x++) {
            for (int y = 0; y < signed.getHeight(); y++) {
                if (signed.getRGB(x, y) != reference.getRGB(x, y)) {
                    match = false;
                    difference.setRGB(x, y, Color.RED.getRGB());
                }
            }
        }
        return match;
    }

    /**
     * Validates that the dimensions of the signed image match the reference image exactly.
     * Throws AssertionError if either width or height differs between the images.
     *
     * @param signed The signed image to validate
     * @param reference The reference image to compare against
     */
    private static void validateImageDimensions(BufferedImage signed, BufferedImage reference) {
        Objects.requireNonNull(signed, "Signed image cannot be null");
        Objects.requireNonNull(reference, "Reference image cannot be null");

        assertEquals(reference.getWidth(), signed.getWidth(),
                "Width of image differs from reference");
        assertEquals(reference.getHeight(), signed.getHeight(),
                "Height of image differs from reference");
    }

    /**
     * Creates a new comparison result by comparing two images.
     */
    public static ImageComparisonResult compare(BufferedImage signedImage, BufferedImage referenceImage) {
        validateImageDimensions(signedImage, referenceImage);

        BufferedImage differenceImage = createDifferenceImage(signedImage.getWidth(), signedImage.getHeight());
        boolean areEqual = comparePixels(signedImage, referenceImage, differenceImage);

        return new ImageComparisonResult(signedImage, referenceImage, differenceImage, areEqual);
    }

    @Override
    public void close() {
        try {
            dispose();
        } catch (Exception e) {
            logger.warn("Error disposing image resources", e);
        }
    }

    public void dispose() {
        modifiedSigned.flush();
        modifiedReference.flush();
        differenceImage.flush();
    }
}