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
import at.asit.pdfover.signer.pdfas.PdfAs4SignatureParameter;

/**
 *
 */
public class SignaturePlaceholderCache {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(SignaturePlaceholderCache.class);

	private static void saveImage(BufferedImage image, String fileDir, String fileName, String fileExt) throws IOException {
		File file = new File(fileDir, fileName + "." + fileExt);
		ImageIO.write(image, fileExt, file); // ignore returned boolean
	}

	private static Image loadImage(String fileDir, String fileName, String fileExt) throws IOException {
		return ImageIO.read(new File(fileDir, fileName + "." + fileExt));
	}

	/**
	 * Get placeholder as AWT Image
	 * @param param SignatureParameter
	 * @return the placeholder AWT Image
	 */
	public static Image getPlaceholder(PdfAs4SignatureParameter param) {
		final String fileDir = Constants.CONFIG_DIRECTORY;
		final String imgFileName = Constants.PLACEHOLDER_CACHE_FILENAME;
		final String imgFileExt = "png";
		final String propFileName = Constants.PLACEHOLDER_CACHE_PROPS_FILENAME;

		final String sigLangProp = "LANG";
		final String sigEmblProp = "EMBL";
		final String sigEHshProp = "EHSH";
		final String sigPdfAProp = "PDFA";
		final String sigNoteProp = "NOTE";
		final String sigProfProp = "PROF";

		String sigLang = param.getSignatureLanguage();
		String sigEmbl = "";
		String sigEHsh = "";
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
		String sigNote = param.getProperty("SIG_NOTE");
		if (sigNote == null)
			sigNote = "";
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
				log.debug("Placeholder cache hit");
				return loadImage(fileDir, imgFileName, imgFileExt);
			}
			log.debug("Placeholder cache miss (" +
					sigLang + "|" + sigProps.getProperty(sigLangProp) + " - " +//
					sigEmbl + "|" + sigProps.getProperty(sigEmblProp) + " - " +
					sigEHsh + "|" + sigProps.getProperty(sigEHshProp) + " - " +
					sigNote + "|" + sigProps.getProperty(sigNoteProp) + " - " +
					sigPdfA + "|" + sigProps.getProperty(sigPdfAProp) + " - " +
					profile + "|" + sigProps.getProperty(sigProfProp) + ")");
		} catch (Exception e) {
			log.warn("Can't load signature Placeholder", e);
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
			log.error("Can't save signature Placeholder", e);
			return param.getPlaceholder();
		}
	}

	/**
	 * Get placeholder as SWT ImageData
	 * @param param SignatureParameter
	 * @return the placeholder SWT ImageData
	 */
	public static ImageData getSWTPlaceholder(PdfAs4SignatureParameter param) {
		return ImageConverter.convertToSWT((BufferedImage) getPlaceholder(param));
	}
}
