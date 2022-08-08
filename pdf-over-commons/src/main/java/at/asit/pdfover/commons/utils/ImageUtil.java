package at.asit.pdfover.commons.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;

class EXIFRotation {
    private static final Logger log = LoggerFactory.getLogger(EXIFRotation.class);
    /**
     * rotate by this times Math.PI / 2
     */
    final int rotationInQuarters;
    /**
     * whether you should mirror (left-right) the image AFTER rotation
     */
    final boolean shouldMirrorLR;

    private EXIFRotation(int rotateQuarters, boolean mirrorLR) {
        this.rotationInQuarters = rotateQuarters;
        this.shouldMirrorLR = mirrorLR;
    }

    public static final EXIFRotation NONE = new EXIFRotation(0, false);

    private static final EXIFRotation[] rotationForIndex = {
        /* invalid (0) */ NONE,
        /* 1 */ NONE,
        /* 2 */ new EXIFRotation(0, true),
        /* 3 */ new EXIFRotation(2, false),
        /* 4 */ new EXIFRotation(2, true),
        /* 5 */ new EXIFRotation(1, true),
        /* 6 */ new EXIFRotation(1, false),
        /* 7 */ new EXIFRotation(3, true),
        /* 8 */ new EXIFRotation(3, false)
    };

    static EXIFRotation For(File file) throws IOException
    {
        try
        {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            if (metadata == null)
                return NONE;
            ExifIFD0Directory metaDir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (metaDir == null)
                return NONE;
            int orientation = metaDir.getInt(ExifDirectoryBase.TAG_ORIENTATION);
            if (rotationForIndex.length <= orientation)
            {
                log.warn("Invalid orientation {} in EXIF metadata for {}", orientation, file.getName());
                return NONE;
            }
            return rotationForIndex[orientation];
        } catch (ImageProcessingException | MetadataException e) {
            log.error("Failed to read EXIF metadata for {}", file.getName(), e);
            return NONE;
        }
    }
}

public final class ImageUtil {
    
    /**
     * ImageIO.read, except it honors EXIF rotation metadata
     * (which the default, for some reason, does not)
     */
    public static final BufferedImage readImageWithEXIFRotation(File input) throws IOException
    {
        if (input == null)
            throw new IllegalArgumentException("input == null");
        if (!input.canRead())
            throw new IllegalArgumentException("cannot read input");
        
        ImageInputStream stream = ImageIO.createImageInputStream(input);
        if (stream == null)
            throw new RuntimeException("Failed to create ImageInputStream for some reason?");
        
        Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
        if (!iter.hasNext())
        {
            stream.close();
            return null;
        }

        ImageReader reader = iter.next();
        boolean isJPEG = reader.getFormatName().equals("JPEG");
        ImageReadParam param = reader.getDefaultReadParam();
        reader.setInput(stream, true, false);
        BufferedImage image;
        try {
            image = reader.read(0, param);
        } finally {
            reader.dispose();
            stream.close();
        }

        if (!isJPEG)
            return image;

        EXIFRotation rotation = EXIFRotation.For(input);
        if (rotation.rotationInQuarters > 0)
        {
            boolean isSideways = ((rotation.rotationInQuarters % 2) == 1);
            int sourceWidth = image.getWidth();
            int sourceHeight = image.getHeight();
            int targetWidth = isSideways ? sourceHeight : sourceWidth;
            int targetHeight = isSideways ? sourceWidth : sourceHeight;
            
            BufferedImage result = new BufferedImage(targetWidth, targetHeight, image.getType());
            Graphics2D g = result.createGraphics();
            g.translate((targetWidth - sourceWidth)/2, (targetHeight - sourceHeight)/2);
            g.rotate(rotation.rotationInQuarters * Math.PI / 2, sourceWidth/2, sourceHeight/2);
            g.drawRenderedImage(image, null);
            g.dispose();
            image = result;
        }

        if (rotation.shouldMirrorLR)
        {
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage result = new BufferedImage(width, height, image.getType());
            Graphics2D g = result.createGraphics();
            g.drawImage(image, width, 0, -width, height, null);
            g.dispose();
            image = result;
        }
        return image;
    }

    public static java.awt.Image debugDisplayImage(java.awt.Image image) {
        JPanel panel = new JPanel();
        panel.add(new JLabel(new ImageIcon(image)));
        JOptionPane.showMessageDialog(null, new JScrollPane(panel));
        return image;
    }

    /**
	 * Convert AWT Image to SWT Image
	 *
	 * @param bufferedImage
	 * @return SWT Image data
	 */
	public static ImageData convertToSWT(final BufferedImage bufferedImage) {
		if (bufferedImage == null)
			return null;

		if ((bufferedImage.getWidth() <= 0) || (bufferedImage.getHeight() <= 0))
			return null;

		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel) bufferedImage
					.getColorModel();
			PaletteData palette = new PaletteData(colorModel.getRedMask(),
					colorModel.getGreenMask(), colorModel.getBlueMask());
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[3];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					int pixel = palette.getPixel(new RGB(pixelArray[0],
							pixelArray[1], pixelArray[2]));
					data.setPixel(x, y, pixel);
				}
			}
			return data;
		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
			IndexColorModel colorModel = (IndexColorModel) bufferedImage
					.getColorModel();
			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
						blues[i] & 0xFF);
			}
			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return data;
		} else if (bufferedImage.getColorModel() instanceof ComponentColorModel) {
		    ComponentColorModel colorModel = (ComponentColorModel)bufferedImage.getColorModel();

		    //ASSUMES: 3 BYTE BGR IMAGE TYPE

		    PaletteData palette = new PaletteData(0x0000FF, 0x00FF00,0xFF0000);
		    ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);

		    //This is valid because we are using a 3-byte Data model with no transparent pixels
		    data.transparentPixel = -1;

		    WritableRaster raster = bufferedImage.getRaster();
		    int[] pixelArray = new int[bufferedImage.getColorModel().getNumComponents()];
		    for (int y = 0; y < data.height; y++) {
		        for (int x = 0; x < data.width; x++) {
		            raster.getPixel(x, y, pixelArray);
		            int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
		            data.setPixel(x, y, pixel);
		        }
		    }
		    return data;
		}
		return null;
	}
}
