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
package at.asit.pdfover.gui.utils;

// Imports
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.imageio.ImageIO;

import at.asit.pdfover.commons.Profile;
import org.eclipse.swt.graphics.ImageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.signator.CachedFileNameEmblem;
import at.asit.pdfover.signator.Emblem;
import at.asit.pdfover.signator.SignatureParameter;

/**
 * 
 */
public class SignaturePlaceholderCache {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(SignaturePlaceholderCache.class);

	private static void saveImage(BufferedImage image, String fileDir, String fileName, String fileExt) throws IOException {
		File file = new File(fileDir, fileName + "." + fileExt); //$NON-NLS-1$
		ImageIO.write(image, fileExt, file); // ignore returned boolean
	}

	private static Image loadImage(String fileDir, String fileName, String fileExt) throws IOException {
		return ImageIO.read(new File(fileDir, fileName + "." + fileExt)); //$NON-NLS-1$
	}

	/**
	 * Get placeholder as AWT Image
	 * @param param SignatureParameter
	 * @return the placeholder AWT Image
	 */
	public static Image getPlaceholder(SignatureParameter param) {
		final String fileDir = Constants.CONFIG_DIRECTORY;
		final String imgFileName = Constants.PLACEHOLDER_CACHE_FILENAME;
		final String imgFileExt = "png"; //$NON-NLS-1$
		final String propFileName = Constants.PLACEHOLDER_CACHE_PROPS_FILENAME;

		final String sigLangProp = "LANG"; //$NON-NLS-1$
		final String sigEmblProp = "EMBL"; //$NON-NLS-1$
		final String sigEHshProp = "EHSH"; //$NON-NLS-1$
		final String sigPdfAProp = "PDFA"; //$NON-NLS-1$
		final String sigNoteProp = "NOTE"; //$NON-NLS-1$
		final String sigProfProp = "PROF"; //$NON-NLS-1$

		String sigLang = param.getSignatureLanguage();
		String sigEmbl = ""; //$NON-NLS-1$
		String sigEHsh = ""; //$NON-NLS-1$
		if (param.getEmblem() != null) {
			Emblem embl = param.getEmblem();
			if (embl instanceof CachedFileNameEmblem) {
				sigEmbl = ((CachedFileNameEmblem) embl).getOriginalFileName();
				sigEHsh = ((CachedFileNameEmblem) embl).getOriginalFileHash();
			} else {
				sigEmbl = embl.getFileName();
			}
		}
		String sigPdfA = param.getSignaturePdfACompat() ? Constants.TRUE : Constants.FALSE;
		String sigNote = param.getProperty("SIG_NOTE"); //$NON-NLS-1$
		if (sigNote == null)
			sigNote = ""; //$NON-NLS-1$
		String profile = param.getSignatureProfile();
		if (profile == null){
			// set default value
			profile = Profile.getDefaultProfile();
		}
		Properties sigProps = new Properties();
		// compare cache, try to load if match
		try {
			InputStream in = new FileInputStream(new File(fileDir, propFileName));
			sigProps.load(in);
			if (sigLang.equals(sigProps.getProperty(sigLangProp)) &&
			    sigEmbl.equals(sigProps.getProperty(sigEmblProp)) &&
			    sigEHsh.equals(sigProps.getProperty(sigEHshProp)) &&
			    sigNote.equals(sigProps.getProperty(sigNoteProp)) &&
			    sigPdfA.equals(sigProps.getProperty(sigPdfAProp)) &&
				profile.equals(sigProps.getProperty(sigProfProp))) {
				log.debug("Placeholder cache hit"); //$NON-NLS-1$
				return loadImage(fileDir, imgFileName, imgFileExt);
			}
			log.debug("Placeholder cache miss (" + //$NON-NLS-1$
					sigLang + "|" + sigProps.getProperty(sigLangProp) + " - " +//$NON-NLS-1$ //$NON-NLS-2$
					sigEmbl + "|" + sigProps.getProperty(sigEmblProp) + " - " + //$NON-NLS-1$ //$NON-NLS-2$
					sigEHsh + "|" + sigProps.getProperty(sigEHshProp) + " - " + //$NON-NLS-1$ //$NON-NLS-2$
					sigNote + "|" + sigProps.getProperty(sigNoteProp) + " - " + //$NON-NLS-1$ //$NON-NLS-2$
					sigPdfA + "|" + sigProps.getProperty(sigPdfAProp) + " - " + //$NON-NLS-1$ //$NON-NLS-2$
					profile + "|" + sigProps.getProperty(sigProfProp) + ")");
		} catch (Exception e) {
			log.warn("Can't load signature Placeholder", e); //$NON-NLS-1$
		}

		// create new cache
		try {
			sigProps.setProperty(sigLangProp, sigLang);
			sigProps.setProperty(sigEmblProp, sigEmbl);
			sigProps.setProperty(sigEHshProp, sigEHsh);
			sigProps.setProperty(sigNoteProp, sigNote);
			sigProps.setProperty(sigPdfAProp, sigPdfA);
			sigProps.setProperty(sigProfProp, profile);
			OutputStream out = new FileOutputStream(new File(fileDir, propFileName));
			sigProps.store(out, null);
			Image img = param.getPlaceholder();
			saveImage((BufferedImage) img, fileDir, imgFileName, imgFileExt);
			return img;
		} catch (IOException e) {
			log.error("Can't save signature Placeholder", e); //$NON-NLS-1$
			return param.getPlaceholder();
		}
	}

	/**
	 * Get placeholder as SWT ImageData
	 * @param param SignatureParameter
	 * @return the placeholder SWT ImageData
	 */
	public static ImageData getSWTPlaceholder(SignatureParameter param) {
		return ImageConverter.convertToSWT((BufferedImage) getPlaceholder(param));
	}
}
