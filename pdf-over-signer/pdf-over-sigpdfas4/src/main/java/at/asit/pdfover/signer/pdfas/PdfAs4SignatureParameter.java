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
package at.asit.pdfover.signer.pdfas;

//Imports
import iaik.x509.X509Certificate;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.signator.SignatureDimension;
import at.asit.pdfover.signator.SignatureParameter;
import at.asit.pdfover.signator.SignaturePosition;
import at.gv.egiz.pdfas.lib.api.Configuration;
import at.gv.egiz.pdfas.lib.api.PdfAs;
import at.gv.egiz.pdfas.lib.api.PdfAsFactory;
import at.gv.egiz.pdfas.lib.api.sign.SignParameter;

/**
 * Implementation of SignatureParameter for PDF-AS 4 Library
 */
public class PdfAs4SignatureParameter extends SignatureParameter {
	/** The profile ID for the German signature block */
	private static final String PROFILE_ID_DE = "SIGNATURBLOCK_SMALL_DE_PDFA";
	/** The profile ID for the German signature block if a signature note is set */
	private static final String PROFILE_ID_DE_NOTE = "SIGNATURBLOCK_SMALL_DE_NOTE_PDFA";
	/** The profile ID for the English signature block */
	private static final String PROFILE_ID_EN = "SIGNATURBLOCK_SMALL_EN_PDFA";
	/** The profile ID for the English signature block if a signature note is set */
	private static final String PROFILE_ID_EN_NOTE = "SIGNATURBLOCK_SMALL_EN_NOTE_PDFA";

	private HashMap<String, String> genericProperties = new HashMap<String, String>();

	private int sig_w = 229;
	private int sig_h = 77;

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(PdfAs4SignatureParameter.class);

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.SignatureParameter#getPlaceholderDimension()
	 */
	@Override
	public SignatureDimension getPlaceholderDimension() {
		return new SignatureDimension(this.sig_w, this.sig_h);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.SignatureParameter#getPlaceholder()
	 */
	@Override
	public Image getPlaceholder() {
		String sigProfile = getPdfAsSignatureProfileId();
		String sigEmblem = (getEmblem() == null ? null : getEmblem().getFileName());
		String sigNote = getProperty("SIG_NOTE");

		try {
			X509Certificate cert = new X509Certificate(PdfAs4SignatureParameter.class.getResourceAsStream("/qualified.cer"));
			PdfAs pdfas = PdfAs4Helper.getPdfAs();
			Configuration conf = pdfas.getConfiguration();
			if (sigEmblem != null && !sigEmblem.trim().equals("")) {
				conf.setValue("sig_obj." + sigProfile + ".value.SIG_LABEL", sigEmblem);
			}
			if (sigNote != null) {
				conf.setValue("sig_obj." + sigProfile + ".value.SIG_NOTE", sigNote);
			}
			SignParameter param = PdfAsFactory
					.createSignParameter(conf, null, null);
			param.setSignatureProfileId(sigProfile);
			Image img = pdfas.generateVisibleSignaturePreview(param, cert, 72*4);
			this.sig_w = img.getWidth(null)/4;
			this.sig_h = img.getHeight(null)/4;

			return img;
		} catch (Exception e) {
			log.error("Failed to get signature placeholder", e);
			return new BufferedImage(getPlaceholderDimension().getWidth(),
					getPlaceholderDimension().getHeight(),
					BufferedImage.TYPE_INT_RGB);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.SignatureParameter#setProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public void setProperty(String key, String value) {
		this.genericProperties.put(key, value);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.SignatureParameter#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String key) {
		return this.genericProperties.get(key);
	}

	/**
	 * Gets the Signature Position String for PDF-AS
	 * 
	 * @return Signature Position String
	 */
	public String getPdfAsSignaturePosition() {
		SignaturePosition in_pos = getSignaturePosition();
		String out_pos;

		if (!in_pos.useAutoPositioning()) {
			if (in_pos.getPage() < 1) {
				out_pos = String.format(
						(Locale) null,
						"p:new;x:%f;y:%f",  in_pos.getX(),
						in_pos.getY());
			} else {
				out_pos = String.format(
						(Locale) null,
						"p:%d;x:%f;y:%f", in_pos.getPage(), in_pos.getX(),
						in_pos.getY());
			}
		} else {
			out_pos = "p:auto;x:auto;y:auto";
		}

		return out_pos;
	}

	/**
	 * Get the Signature Profile ID for this set of parameters
	 * @return the Signature Profile ID
	 */
	public String getPdfAsSignatureProfileId() {
		String lang = getSignatureLanguage();
		boolean useNote = (getProperty("SIG_NOTE") != null);

		if (lang != null && lang.equals("en"))
			return useNote ? PROFILE_ID_EN_NOTE : PROFILE_ID_EN;

		return useNote ? PROFILE_ID_DE_NOTE : PROFILE_ID_DE;
	}
}
