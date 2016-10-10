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
public class CachedFileNameEmblem implements Emblem {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(CachedFileNameEmblem.class);

	private String fileName = null;

	/**
	 * Constructor
	 * @param filename
	 */
	public CachedFileNameEmblem(String filename) {
		this.fileName = filename;
	}

	private String getFileHash(String filename) throws IOException {
		InputStream is = Files.newInputStream(Paths.get(this.fileName));
		return DigestUtils.md5Hex(is);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.Emblem#getFileName()
	 */
	@Override
	public String getFileName() {
		final String fileDir = System.getProperty("user.home") + File.separator + ".pdf-over"; //$NON-NLS-1$ //$NON-NLS-2$
		final String imgFileName = ".emblem"; //$NON-NLS-1$
		final String imgFileExt = "png"; //$NON-NLS-1$
		final String propFileName = ".emblem.properties"; //$NON-NLS-1$

		final String imgProp = "IMG"; //$NON-NLS-1$
		final String hshProp = "HSH"; //$NON-NLS-1$
		final int maxWidth  = 480;
		final int maxHeight = 600;

		String emblemImg = this.fileName;
		String emblemHsh = null;
		String cachedEmblemFileName = fileDir + File.separator + imgFileName + "." + imgFileExt; //$NON-NLS-1$

		if (emblemImg == null || !(new File(emblemImg).exists()))
			return null;

		Properties emblemProps = new Properties();
		// compare cache, try to load if match
		try {
			InputStream in = new FileInputStream(new File(fileDir, propFileName));
			emblemProps.load(in);
			if (emblemImg.equals(emblemProps.getProperty(imgProp))) {
				emblemHsh = getFileHash(emblemImg);
				if (emblemHsh.equals(emblemProps.getProperty(hshProp))) {
					log.debug("Emblem cache hit: " + cachedEmblemFileName); //$NON-NLS-1$
					return cachedEmblemFileName; //$NON-NLS-1$
				}
			}
			log.debug("Emblem cache miss");
		} catch (Exception e) {
			log.warn("Can't load emblem cache", e); //$NON-NLS-1$
		}

		try {
			// create new cache
			if (emblemHsh == null)
				emblemHsh = getFileHash(emblemImg);
			emblemProps.setProperty(imgProp, emblemImg);
			emblemProps.setProperty(hshProp, emblemHsh);
			BufferedImage img = ImageIO.read(new File(emblemImg));
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
			BufferedImage cacheImg = img;
			if (width != owidth || height == oheight) {
				//scale image
				log.debug("Scaling emblem: " + owidth + "x" + oheight + " to " + width + "x" + height); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				cacheImg = new BufferedImage(width, height, img.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : img.getType());
				Graphics2D g = cacheImg.createGraphics();
				g.drawImage(img, 0, 0, width, height, null);
				g.dispose();
			}

			File file = new File(fileDir, imgFileName + "." + imgFileExt); //$NON-NLS-1$
			ImageIO.write(cacheImg, imgFileExt, file); // ignore returned boolean
			OutputStream out = new FileOutputStream(new File(fileDir, propFileName));
			emblemProps.store(out, null);
		} catch (IOException e) {
			log.error("Can't save emblem cache", e); //$NON-NLS-1$
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
}
