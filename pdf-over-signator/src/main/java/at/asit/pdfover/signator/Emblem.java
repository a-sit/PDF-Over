/*
 * Copyright 2012 by A-SIT, Secure Information Technology Center Austria
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package at.asit.pdfover.signator;

// Imports
import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.utils.ImageUtil;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO all of this caching business is a bit of a mess
 */
public class Emblem {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(Emblem.class);

	private static final String CACHE_DIR = Constants.CONFIG_DIRECTORY;
	private static final String CACHE_IMG_FILENAME = ".emblem.png";
	private static final String CACHE_IMG_FORMAT = "png";
	private static final String CACHE_PROPS_FILENAME = ".emblem.properties";

	private static final String PROPKEY_ORIG_PATH = "IMG";
	private static final String PROPKEY_ORIG_DIGEST = "HSH";
	private static final int MAX_EMBLEM_WIDTH  = 480;
	private static final int MAX_EMBLEM_HEIGHT = 600;

	private String originalFileName = null;
	private String originalFileHash = null;
	private Image image = null; /* image data, if we have it */

	private void lazyLoadImage() {
		if (this.image != null) return;

		String filename = getCachedFileName();
		if (this.image != null) return; /* getCachedFileName may have re-generated the cache and populated this.image */

		try {
			this.image = ImageUtil.readImageWithEXIFRotation(new File(filename));
		} catch (IOException e) {
			log.warn("Failed to load Emblem image");
		}
	}

	public int getWidth() { if (image == null) lazyLoadImage(); return (image != null) ? image.getWidth(null) : 0; }
	public int getHeight() { if (image == null) lazyLoadImage(); return (image != null) ? image.getHeight(null) : 0; }

	/**
	 * Constructor
	 * @param filename
	 */
	public Emblem(String filename) {
		this.originalFileName = filename;
	}

	private String getFileHash(String filename) throws IOException {
		InputStream is = Files.newInputStream(Path.of(filename));
		return DigestUtils.md5Hex(is);
	}

	private static BufferedImage reduceImageSizeIfNecessary(BufferedImage img, int maxWidth, int maxHeight) {
		int oheight = img.getHeight();
		int owidth = img.getWidth();

		double ratio = (double)owidth/(double)oheight;

		int height = oheight;
		int width = owidth;
		if (height > maxHeight) {
			height = maxHeight;
			width = (int) (maxHeight * ratio);
		}
		if (width > maxWidth) {
			width = maxWidth;
			height = (int) (maxWidth / ratio);
		}
		BufferedImage result = img;
		if (width != owidth || height != oheight) {
			//scale image
			log.debug("Scaling emblem: " + owidth + "x" + oheight + " to " + width + "x" + height);
			result = new BufferedImage(width, height, img.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : img.getType());
			Graphics2D g = result.createGraphics();
			g.drawImage(img, 0, 0, width, height, null);
			g.dispose();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.Emblem#getFileName()
	 */
	public String getCachedFileName() {
		String emblemImg = this.originalFileName;
		String emblemHsh = null;
		String cachedEmblemFileName = CACHE_DIR + File.separator + CACHE_IMG_FILENAME;

		if (emblemImg == null || !(new File(emblemImg).exists()))
			return null;

		Properties emblemProps = new Properties();
		// compare cache, try to load if match
		try {
			File cacheProps = new File(CACHE_DIR, CACHE_PROPS_FILENAME);
			if (cacheProps.exists()) {
				InputStream in = new FileInputStream(cacheProps);
				emblemProps.load(in);
				if (emblemImg.equals(emblemProps.getProperty(PROPKEY_ORIG_PATH))) {
					emblemHsh = getFileHash(emblemImg);
					if (emblemHsh.equals(emblemProps.getProperty(PROPKEY_ORIG_DIGEST))) {
						log.debug("Emblem cache hit: " + cachedEmblemFileName);
						return cachedEmblemFileName;
					}
				}
			}
			log.debug("Emblem cache miss");
		} catch (Exception e) {
			log.warn("Can't load emblem cache", e);
		}

		try {
			// create new cache
			if (emblemHsh == null)
				emblemHsh = getFileHash(emblemImg);
			emblemProps.setProperty(PROPKEY_ORIG_PATH, emblemImg);
			emblemProps.setProperty(PROPKEY_ORIG_DIGEST, emblemHsh);
			File imgFile = new File(emblemImg);

			BufferedImage img = ImageUtil.readImageWithEXIFRotation(imgFile);
			img = reduceImageSizeIfNecessary(img, MAX_EMBLEM_WIDTH, MAX_EMBLEM_HEIGHT);

			File file = new File(CACHE_DIR, CACHE_IMG_FILENAME);
			ImageIO.write(img, CACHE_IMG_FORMAT, file); // ignore returned boolean
			this.image = img;
			OutputStream out = new FileOutputStream(new File(CACHE_DIR, CACHE_PROPS_FILENAME));
			emblemProps.store(out, null);
		} catch (IOException e) {
			log.error("Can't save emblem cache", e);
			return this.originalFileName;
		}
		return cachedEmblemFileName;
	}

	/**
	 * Return the original filename
	 * @return the original filename
	 */
	public String getOriginalFileName() {
		return this.originalFileName;
	}

	/**
	 * Return the original filename
	 * @return the original filename
	 */
	public String getOriginalFileHash() {
		if (this.originalFileHash == null) {
			if (this.originalFileName == null || !(new File(this.originalFileName).exists())) {
				this.originalFileHash = "";
			} else try {
				this.originalFileHash = getFileHash(this.originalFileName);
			} catch (IOException e) {
				log.debug("Error getting file hash", e);
				this.originalFileHash = "";
			}
		}
		return this.originalFileHash;
	}
}
