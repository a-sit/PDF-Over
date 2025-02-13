package at.asit.pdfover.gui.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import at.asit.pdfover.commons.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignaturePositionTestProvider {
    /**
     * Page where signature block is located.
     */
    private static int pageNo = 1;
    /**
     * Flag to create reference pictures for comparison.
     * @param true
     *         reference pic is created, compare test is skipped
     */
    private boolean captureRefImage;
    /**
     * Zoom value which is used for signed and reference document.
     */
    private static final float ZOOM = 4;
    /**
     * Directory for reference files.
     */
    private String refFileDir = "src/test/resources";

    private static final String UNIX_SEPARATOR = "/";

    /**
     *Current set signature profile.
     */
    private Profile currentProfile = null;

    public final String REF_FILE_AMTSSIGNATUR = "refFileAmtssignatur.png";
    public final String REF_FILE_SIGNATURBLOCK_SMALL = "refFileSignaturblockSmallNote.png";
    public final String REF_FILE_BASE_LOGO = "refFileBaseLogo.png";
    public final String REF_FILE_INVISIBLE = "refFileInvisible.png";
    public final String REF_FILE_TEST_NEGATIVE = "refFileTestNegative.png";

    private static final Logger logger = LoggerFactory
            .getLogger(SignaturePositionTestProvider.class);

    /**
     * Map of profiles and belonging reference files which 
     * are used to compare the signed document with.
     */
    private Map<Profile,String> refFileNames = Map.of(
        Profile.AMTSSIGNATURBLOCK, REF_FILE_AMTSSIGNATUR,
        Profile.SIGNATURBLOCK_SMALL, REF_FILE_SIGNATURBLOCK_SMALL,
        Profile.BASE_LOGO, REF_FILE_BASE_LOGO,
        Profile.INVISIBLE, REF_FILE_INVISIBLE
        );

    /**
     * Map of profiles and belonging coordinates for the black triangle
     * which overwrites ignored areas such as the date.
     */
    private static Map<Profile,String> ignoredAreas;
    static {
        ignoredAreas = new HashMap<Profile,String>();
        ignoredAreas.put(Profile.AMTSSIGNATURBLOCK, "215,679,142,9");
        ignoredAreas.put(Profile.SIGNATURBLOCK_SMALL, "287,690,90,6");
    }

    public boolean isCaptureRefImage() {
        return captureRefImage;
    }

    public int getPageNo() {
        return pageNo;
    }

    protected String getRefImageFileName(Profile currentProfile) {
        String refFileName = null;
        if (currentProfile != null) {
            refFileName = refFileNames.get(currentProfile);
        }
        return refFileName;
    }

    public List<Rectangle> getIgnoredAreas(Profile profile) {
        String ignored = ignoredAreas.get(profile);
        List<Rectangle> ignoredRectangles = new ArrayList<Rectangle>();
        if (ignored != null) {
            String[] ignoredAreaStringSplit = ignored.split(";");
            for (String ignoredAreaString : ignoredAreaStringSplit) {
                Rectangle ignoredRectangle = parseIgnoredArea(ignoredAreaString);
                if (ignoredRectangle != null) {
                    ignoredRectangles.add(ignoredRectangle);
                }
            }
        }
        return ignoredRectangles;
    }

    /**
     * Pixel-by-pixel comparison of signed pdf picture and reference document picture.
     * 
     * @param currentProfile
     * @param negative
     *         flag to use reference file for negative test cases
     * @param outputFile
     *         signed pdf
     * @throws IOException
     * @throws InterruptedException
     * @author mtappler, modified by kresek
     */
    protected void checkSignaturePosition(Profile currentProfile, boolean negative, String outputFile, boolean captureRefImage) throws IOException {
        this.captureRefImage = captureRefImage;
        this.currentProfile = currentProfile;
        String refImageFileName = null;
        if (negative)
            refImageFileName = REF_FILE_TEST_NEGATIVE;
        else
            refImageFileName = getRefImageFileName(currentProfile);

        assertNotNull(refImageFileName);

        String refImageFilePath = refFileDir.concat(UNIX_SEPARATOR).concat(refImageFileName);
        
        if (isCaptureRefImage() && refImageFileName.equals(REF_FILE_TEST_NEGATIVE)) {
        	return;
        } else if (isCaptureRefImage()) {	
            captureReferenceImage(refImageFilePath, outputFile);
            return;
        }

        BufferedImage sigPageImage = captureImage(outputFile, pageNo);
        assertNotNull(sigPageImage, "Could not get image of page");

        BufferedImage refImage = ImageIO.read(new File(refImageFilePath));
        assertNotNull(refImage, "Could not get reference image");

        assertEquals(refImage.getWidth(), sigPageImage.getWidth(), "Width of image differs from reference");
        assertEquals(refImage.getHeight(), sigPageImage.getHeight(), "Height of image differs from reference");

        Graphics sigPageGraphics = sigPageImage.getGraphics();
        Graphics refImageGraphics = refImage.createGraphics();
        int imageHeight = sigPageImage.getHeight();
        ignoreAreas(sigPageGraphics, imageHeight);
        ignoreAreas(refImageGraphics, imageHeight);

        String nameResultDir = refImageFileName.substring(0, refImageFileName.lastIndexOf('.'));
        nameResultDir = nameResultDir.substring(7);
        
        if(negative) {
        	nameResultDir = nameResultDir.concat(currentProfile.toString());
        }

        String pathRefImageIgnored = refFileDir
                .concat(UNIX_SEPARATOR)
                .concat(nameResultDir)
                .concat(UNIX_SEPARATOR)
                .concat("refImage_ignored.png");

        Path path = Paths.get(pathRefImageIgnored);
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)){
            Files.createDirectories(path);
        }
        ImageIO.write(refImage, "png", new File(pathRefImageIgnored));

        String pathSigPageImageIgnored = refFileDir
                .concat(UNIX_SEPARATOR)
                .concat(nameResultDir)
                .concat(UNIX_SEPARATOR)
                .concat("sigPageImage_ignored.png");
        ImageIO.write(sigPageImage, "png", new File(pathSigPageImageIgnored));

        boolean same = true;
        BufferedImage differenceImage = new BufferedImage(refImage.getWidth(),
                refImage.getHeight(), refImage.getType());
        Graphics differenceGraphics = differenceImage.createGraphics();
        differenceGraphics.setColor(Color.WHITE);
        differenceGraphics.fillRect(0, 0, differenceImage.getWidth(),
                differenceImage.getHeight());
        for (int x = 0; x < refImage.getWidth(); x++) {
            for (int y = 0; y < refImage.getHeight(); y++) {
                boolean samePixel = refImage.getRGB(x, y) == sigPageImage
                        .getRGB(x, y);
                if (!samePixel) {
                    same = false;
                    differenceImage.setRGB(x, y, Color.RED.getRGB());
                }
            }
        }

        String diffImage =  refFileDir
                .concat(UNIX_SEPARATOR)
                .concat(nameResultDir)
                .concat(UNIX_SEPARATOR)
                .concat("difference.png");
        ImageIO.write(differenceImage, "png", new File(diffImage));

        differenceGraphics.dispose();
        differenceImage.flush();
        sigPageGraphics.dispose();
        sigPageImage.flush();
        refImageGraphics.dispose();
        refImage.flush();

        if (negative) {
            assertFalse(same, "Images must not be the same for profile: " + currentProfile+ ", negative");
            logger.info("Test passed: Images are not the same for profile: " + currentProfile+ ", negative");
        } else {
            assertTrue(same, "Images must be the same for profile: " + currentProfile);
            logger.info("Test passed: Images are the same for profile: " + currentProfile);
        }


    }

   /**
     * This parses one ignored area definition and returns a Rectangle-object
     * representing it. The definitions have the exact format
     * "<x>,<y>,<width>,<height>", with x and y specifying the coordinates of
     * the upper left corner of the area and width and height specifying the
     * size in pixels (width and height extends to the right and the bottom of
     * the image).
     * 
     * @param ignoredAreaString
     *            an ignored area definition
     * @return a rectangle representing the area
     * 
     * @author mtappler
     */
    private Rectangle parseIgnoredArea(String ignoredAreaString) {
        String[] ignoredAreaStringSplit = ignoredAreaString.split(",");
        if (ignoredAreaStringSplit.length != 4)
            return null;
        int x = Integer.parseInt(ignoredAreaStringSplit[0]);
        int y = Integer.parseInt(ignoredAreaStringSplit[1]);
        int width = Integer.parseInt(ignoredAreaStringSplit[2]);
        int height = Integer.parseInt(ignoredAreaStringSplit[3]);
        return new Rectangle(x, y, width, height);
    }

    /**
     * Helper method, which "clears" all areas specified by
     * <code>ignoredAreas</code>. "Cleared" are colored in the background color
     * of the image (black).
     * 
     * @param graphics
     *            Graphics object corresponding to an image
     * @param imageHeight
     *            the height of the image
     * @author mtappler            
     */
    protected void ignoreAreas(Graphics graphics, int imageHeight) {
        for (Rectangle r : getIgnoredAreas(currentProfile) ) {
            int effectiveX = (int) (r.x * ZOOM);
            // in awt 0 is at top, in PDF-AS 0 is at bottom
            int effectiveY = imageHeight - (int) (r.y * ZOOM);
            int effectiveWidth = (int) (r.width * ZOOM);
            int effectiveHeight = (int) (r.height * ZOOM);
            graphics.clearRect(effectiveX, effectiveY, effectiveWidth,
                    effectiveHeight);
        }
    }

    /**
     * Helper method, which captures a reference image, i.e. an image of the
     * page with page number specified by <code>sigPageNumber</code> of the
     * PDF-file after signing. The captured file is saved, as well as a modified
     * version of it. The modified version contains black rectangles for the
     * ignored areas.
     *
     *            a test info object, which is used to store the location of the
     *            reference image, as well as the location of the reference
     *            image with ignored areas
     * @throws IOException
     * @throws InterruptedException 
     * 
     * @author mtappler
     */
    private void captureReferenceImage(String refImageFile, String outputFile) throws IOException {
        BufferedImage image = captureImage(outputFile, pageNo);
        ImageIO.write(image, "png", new File(refImageFile));
        Graphics refImageGraphics = image.createGraphics();
        ignoreAreas(refImageGraphics, image.getHeight());
        refImageGraphics.dispose();
        image.flush();	
    }

    /**
     * This method captures an image of a page of a PDF document.
     * 
     * @param fileName
     *            the name of the PDF file
     * @param pageNumber
     *            the page number which should be captured
     * @return the captured image
     * @throws InterruptedException 
     * 
     * @author mtappler
     */
    private BufferedImage captureImage(String fileName, int pageNumber) {
        try (PDDocument signedPdf = PDDocument.load(new File(fileName))) {
            PDFRenderer renderer = new PDFRenderer(signedPdf);
            return renderer.renderImage(pageNumber - 1, ZOOM);
        } catch (IOException e) {
            fail(String
            .format("Not possible to capture page %d of file %s, because of %s.",
                    pageNumber, fileName, e.getMessage()));
            return null;
        }
    }

}
