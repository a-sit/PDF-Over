package at.asit.pdfover.gui.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import at.asit.pdfover.commons.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates the position of signatures in PDF documents by comparing them with reference images.
 * This class provides functionality to:
 *  * - Capture and save reference images for comparison
 *  * - Compare signature positions between signed documents and reference images
 *  * - Handle ignored areas in signature blocks (e.g., date fields)
 *  *
 *  * The class supports different signature profiles (Amtssignatur, Base Logo, etc.)
 *  * and can perform both positive and negative comparison tests.
 *  * @author resekk, mtappler
 */
public class SignaturePositionValidator implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(SignaturePositionValidator.class);

    private static int DEFAULT_PAGE = 1;
    private static final float ZOOM = 4;
    private static final String UNIX_SEPARATOR = "/";
    private static final String PNG_FORMAT = "png";

    private static final String REF_IMAGE_IGNORED = "refImage_ignored." + PNG_FORMAT;
    private static final String SIG_PAGE_IMAGE_IGNORED = "sigPageImage_ignored." + PNG_FORMAT;
    private static final String DIFFERENCE_IMAGE = "difference." + PNG_FORMAT;

    private final boolean captureRefImage;
    private final String refFileDir;
    private final Profile currentProfile;
    private final boolean isNegativeTest;
    private final String pathOutputFile;

    /**
     * Map of profiles and belonging coordinates for the black triangle
     * which overwrites ignored areas such as the date.
     */
    private static final Map<Profile, String> IGNORED_AREAS = Map.of(
            Profile.AMTSSIGNATURBLOCK, "215,679,142,9",
            Profile.SIGNATURBLOCK_SMALL, "287,690,90,6"
    );

    public SignaturePositionValidator(boolean negative, boolean captureRefImage,
                                      Profile currentProfile, String outputFile ) {
        this.isNegativeTest = negative;
        this.captureRefImage = captureRefImage;
        this.currentProfile = currentProfile;
        this.pathOutputFile = outputFile;
        this.refFileDir = "src/test/resources";
    }

    /**
     * Verifies the signature position by comparing the signed PDF against a reference image.
     * Positive tests expect the images to match.
     * Negative tests expect the images to differ.
     */
    protected void verifySignaturePosition() throws IOException {
        String refImagePath = getReferencePath();
        logger.debug("Starting signature position verification with reference: {}", refImagePath);

        if (shouldSkipComparison()) return;

        BufferedImage signedImage = captureImage(pathOutputFile);
        BufferedImage referenceImage = loadReferenceImage(refImagePath);

        try {
            applyIgnoredAreas(signedImage, referenceImage);

            try (ImageComparisonResult result = ImageComparisonResult.compare(signedImage, referenceImage)) {
                saveComparisonResults(result, refImagePath);
                validateComparisonResult(result);
            }
        } finally {
            if (signedImage != null) signedImage.flush();
            if (referenceImage != null) referenceImage.flush();
        }
    }

    /**
     * Validates the result of an image comparison based on the test type.
     * For positive tests, the images should match exactly.
     * For negative tests, the images should have differences.
     *
     * @param result The ImageComparisonResult containing the comparison outcome
     */
    private void validateComparisonResult(ImageComparisonResult result) {
        String testType = isNegativeTest ? "negative" : "positive";
        assertTrue(isNegativeTest != result.isEqual(),
                String.format("Unexpected comparison result for %s test. Images %s match.",
                        testType, result.isEqual() ? "do" : "do not"));
    }

    /**
     * Applies ignored areas to signed image and reference image by clearing specified rectangular regions.
     * For each image a Graphics context is created.
     * Defined areas are cleared using the current profile's ignored area definitions.
     *
     * @param images One or more BufferedImages to apply ignored areas to
     */
    private void applyIgnoredAreas(BufferedImage... images) {
        List<Rectangle> areas = parseIgnoredAreas();
        int height = images[0].getHeight();

        for (BufferedImage image : images) {
            Graphics g = image.getGraphics();
            try {
                for (Rectangle area : areas) {
                    clearIgnoredArea(g, area, height);
                }
            } finally {
                if (g != null) {
                    g.dispose();
                }
            }
        }
    }

    /**
     * Parses the ignored areas defined for the current profile into a list of Rectangle objects.
     * Areas are defined in the {@link #IGNORED_AREAS} map as semicolon-separated strings,
     * each representing a rectangle in the format "x,y,width,height".
     *
     * @return List of Rectangle objects representing areas to ignore during comparison,
     *         or an empty list if no areas are defined for the current profile
     */
    private List<Rectangle> parseIgnoredAreas() {
        String areaDefinitions = IGNORED_AREAS.get(currentProfile);
        if (areaDefinitions == null || areaDefinitions.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(areaDefinitions.split(";"))
                .map(this::parseIgnoredArea)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * This parses one ignored area definition and returns a Rectangle-object
     * representing it. The definitions have the exact format
     * "<x>,<y>,<width>,<height>", with x and y specifying the coordinates of
     * the upper left corner of the area and width and height specifying the
     * size in pixels (width and height extends to the right and the bottom of
     * the image).
     *
     * @param coordinates
     *            an ignored area definition
     * @return a rectangle representing the area
     */
    private Rectangle parseIgnoredArea(String coordinates) {
        try {
            String[] parts = coordinates.split(",");
            if (parts.length != 4) {
                logger.warn("Invalid coordinate format: {}", coordinates);
                return null;
            }
            return new Rectangle(
                    Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3])
            );
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse coordinates: {}", coordinates, e);
            return null;
        }
    }

    /**
     * Clears a rectangular area in the image using the provided Graphics context.
     * Converts PDF coordinates to AWT coordinates and applies the zoom factor.
     * In PDF coordinates, (0,0) is at the bottom-left, while in AWT it's at the top-left.
     *
     * @param g The Graphics context to draw on
     * @param area The Rectangle defining the area to clear in PDF coordinates
     * @param imageHeight The total height of the image for coordinate conversion
     * @see #ZOOM The zoom factor applied to the coordinates
     */
    private void clearIgnoredArea(Graphics g, Rectangle area, int imageHeight) {
        int x = (int) (area.x * ZOOM);
        int y = imageHeight - (int) (area.y * ZOOM); // Convert PDF to AWT coordinates
        int width = (int) (area.width * ZOOM);
        int height = (int) (area.height * ZOOM);
        g.clearRect(x, y, width, height);
    }

    /**
     * Retrieves the full path to the reference image file based on the test type.
     *
     * @return The complete file path to the reference image
     */
    private String getReferencePath() {
        String refImageFileName = isNegativeTest ?
                ReferenceFile.getNegativeTestFileName() :
                ReferenceFile.getFileNameForProfile(currentProfile);
        Objects.requireNonNull(refImageFileName, "Reference image filename cannot be null");
        return buildPath(refFileDir, refImageFileName);
    }

    /**
     * Loads a reference image from the filesystem.
     *
     * @param refImageFilePath Path to the reference image
     * @return The loaded BufferedImage
     * @throws IOException if image loading fails
     * @throws AssertionError if the image couldn't be loaded
     */
    private BufferedImage loadReferenceImage(String refImageFilePath) throws IOException {
        BufferedImage refImage = ImageIO.read(new File(refImageFilePath));
        assertNotNull(refImage, "Could not load reference image");
        return refImage;
    }

    private String buildPath(String... parts) {
        return String.join(UNIX_SEPARATOR, parts);
    }

    /**
     * Determines whether the image comparison should be skipped based on test configuration.
     *
     * @return true if comparison should be skipped, false otherwise
     * @throws IOException if there's an error while capturing the reference image
     * @see #captureReferenceImage(String, String)
     */
    private boolean shouldSkipComparison() throws IOException {
        if (captureRefImage && isNegativeTest) {
            logger.debug("Skipping comparison: Negative test in capture mode");
            return true;
        }

        if (captureRefImage) {
            String refPath = buildPath(refFileDir, ReferenceFile.getFileNameForProfile(currentProfile));
            captureReferenceImage(refPath, pathOutputFile);
            logger.debug("Captured reference image: {}", refFileDir);
            return true;
        }

        return false;
    }

    /**
     * Saves a BufferedImage to the specified file path in PNG format.
     * Creates parent directories if they don't exist.
     *
     * @param image The BufferedImage to save
     * @param filePath The path where the image should be saved
     */
    private void saveImage(BufferedImage image, String filePath) throws IOException {
        Objects.requireNonNull(image, "Image cannot be null");
        Objects.requireNonNull(filePath, "File path cannot be null");

        File outputFile = new File(filePath);
        if (!outputFile.getParentFile().mkdirs() && !outputFile.getParentFile().exists()) {
            throw new IOException("Failed to create directory " + outputFile.getParentFile());
        }

        if (!ImageIO.write(image, PNG_FORMAT, outputFile)) {
            throw new IOException("Failed to write image to file: " + filePath);
        }
        image.flush();
    }

    /**
     * Saves the comparison results between the reference and signed images to the filesystem.
     * Creates a directory based on the reference image name and saves three images:
     * - Modified reference image with ignored areas
     * - Modified signed image with ignored areas
     * - Difference image highlighting pixel differences in red
     *
     * @param result The ImageComparisonResult containing the modified and difference images
     * @param refImagePath The path to the reference image, used to determine the output directory name
     */
    private void saveComparisonResults(ImageComparisonResult result, String refImagePath) throws IOException {
        String resultDir = createResultDirectory(refImagePath);

        Map<String, BufferedImage> imagesToSave = Map.of(
                REF_IMAGE_IGNORED, result.getModifiedReference(),
                SIG_PAGE_IMAGE_IGNORED, result.getModifiedSigned(),
                DIFFERENCE_IMAGE, result.getDifferenceImage()
        );

        for (Map.Entry<String, BufferedImage> entry : imagesToSave.entrySet()) {
            String outputPath = buildPath(resultDir, entry.getKey());
            try {
                saveImage(entry.getValue(), outputPath);
                logger.debug("Saved comparison result image: {}", outputPath);
            } catch (IOException e) {
                logger.error("Failed to save comparison result image: {}", outputPath, e);
                throw new IOException("Failed to save comparison result: " + outputPath, e);
            }
        }
    }

    /**
     * Creates a directory for storing comparison result files.
     * If the directory doesn't exist, it will be created along with any necessary parent directories.
     *
     * @param refImagePath The path to the reference image file used for comparison
     * @return The absolute path to the created directory as a String
     */
    private String createResultDirectory(String refImagePath) throws IOException {
        String dirName = extractResultDirectoryName(refImagePath);
        Path path = Paths.get(dirName);
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)){
            Files.createDirectories(path);
        }
        return dirName;
    }

    /**
     * Extracts the result directory name from the reference image path.
     *
     * @param refImagePath The full path to the reference image file
     * @return The constructed directory name as a String
     */
    private String extractResultDirectoryName(String refImagePath) {
        Objects.requireNonNull(refImagePath, "Reference image path cannot be null");

        String fileName = Paths.get(refImagePath).getFileName().toString();
        String baseName = fileName.substring(7, fileName.lastIndexOf('.'));

        return buildPath(refFileDir,
                isNegativeTest ? baseName + currentProfile : baseName
        );
    }

    /**
     * Captures and saves a reference image from a signed PDF document for signature comparison.
     *
     * @param refPath  the file path where the reference image will be saved
     * @param signedPath    the path to the signed PDF file from which to capture the image
     */
    private void captureReferenceImage(String refPath, String signedPath) throws IOException {
        BufferedImage image = captureImage(signedPath);
        saveImage(image, refPath);
    }

    /**
     * Captures and renders a specific page from a PDF document as a BufferedImage.
     * The page is rendered using the configured zoom factor ({@link #ZOOM}).
     * Note: Page numbers are 1-based, but PDFRenderer uses 0-based indexing internally.
     *
     * @param pdfPath path to the PDF file to be rendered
     * @return the captured page as a BufferedImage
     */
    private BufferedImage captureImage(String pdfPath) throws IOException {
        Objects.requireNonNull(pdfPath, "PDF path cannot be null");

        try (PDDocument pdf = PDDocument.load(new File(pdfPath))) {
            return new PDFRenderer(pdf).renderImage(DEFAULT_PAGE - 1, ZOOM);
        }
    }

    @Override
    public void close() throws Exception {

    }
}