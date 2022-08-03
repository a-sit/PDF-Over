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

import at.asit.pdfover.Util;
// Imports
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Emblem {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(Emblem.class);

	private final String fileDir = System.getProperty("user.home") + File.separator + ".pdf-over";
	private final String imgFileName = ".emblem";
	private final String imgFileExt = "png";
	private final String propFileName = ".emblem.properties";

	private final String imgProp = "IMG";
	private final String hshProp = "HSH";
	private final int maxWidth  = 480;
	private final int maxHeight = 600;

	private String fileName = null;

	/**
	 * Constructor
	 * @param filename
	 */
	public Emblem(String filename) {
		this.fileName = filename;
	}

	private String getFileHash(String filename) throws IOException {
		InputStream is = Files.newInputStream(Paths.get(this.fileName));
		return DigestUtils.md5Hex(is);
	}


	private static BufferedImage scaleImage(BufferedImage img, int maxWidth, int maxHeight) {
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
		if (width != owidth || height == oheight) {
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
	public String getFileName() {
		String emblemImg = this.fileName;
		String emblemHsh = null;
		String cachedEmblemFileName = this.fileDir + File.separator + this.imgFileName + "." + this.imgFileExt;

		if (emblemImg == null || !(new File(emblemImg).exists()))
			return null;

		Properties emblemProps = new Properties();
		// compare cache, try to load if match
		try {
			InputStream in = new FileInputStream(new File(this.fileDir, this.propFileName));
			emblemProps.load(in);
			if (emblemImg.equals(emblemProps.getProperty(this.imgProp))) {
				emblemHsh = getFileHash(emblemImg);
				if (emblemHsh.equals(emblemProps.getProperty(this.hshProp))) {
					log.debug("Emblem cache hit: " + cachedEmblemFileName);
					return cachedEmblemFileName;
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
			emblemProps.setProperty(this.imgProp, emblemImg);
			emblemProps.setProperty(this.hshProp, emblemHsh);
			File imgFile = new File(emblemImg);

			img = scaleImage(img, this.maxWidth, this.maxHeight);
			BufferedImage img = Util.readImageWithEXIFRotation(imgFile);

			File file = new File(this.fileDir, this.imgFileName + "." + this.imgFileExt);
			ImageIO.write(img, this.imgFileExt, file); // ignore returned boolean
			OutputStream out = new FileOutputStream(new File(this.fileDir, this.propFileName));
			emblemProps.store(out, null);
		} catch (IOException e) {
			log.error("Can't save emblem cache", e);
			return this.fileName;
		}
		return cachedEmblemFileName;
	}

	/**
	 * Return the original filename
	 * @return the original filename
	 */
	public String getOriginalFileName() {
		return this.fileName;
	}

	/**
	 * Return the original filename
	 * @return the original filename
	 */
	public String getOriginalFileHash() {
		if (this.fileName == null || !(new File(this.fileName).exists()))
			return "";
		try {
			return getFileHash(this.fileName);
		} catch (IOException e) {
			log.debug("Error getting file hash", e);
			return "";
		}
	}
}
