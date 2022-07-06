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
package at.asit.pdfover.gui.workflow.config;

// Imports
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

import at.asit.pdfover.commons.Profile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.bku.mobile.MobileBKUHelper;
import at.asit.pdfover.gui.bku.mobile.MobileBKUs;
import at.asit.pdfover.gui.exceptions.InvalidEmblemFile;
import at.asit.pdfover.gui.exceptions.InvalidPortException;
import at.asit.pdfover.signator.BKUs;
import at.asit.pdfover.signator.SignaturePosition;

/**
 * Implementation of the configuration container
 */
public class ConfigurationContainer {
	/**
	 * SLF4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ConfigurationContainer.class);

	/** the emblem File */
	protected String emblemFile = null;
	public String getEmblem() { return this.emblemFile; }
	public void setEmblem(String emblemFile) throws InvalidEmblemFile {
		if (emblemFile == null || emblemFile.trim().isEmpty()) {
			// Ok to set no file ...
		} else {
			File imageFile = new File(emblemFile);
			if (!imageFile.exists()) {
				throw new InvalidEmblemFile(imageFile,
						new FileNotFoundException(emblemFile));
			}

			try {
				Image img = new Image(Display.getDefault(), new ImageData(
						emblemFile));

				img.dispose();
			} catch (Exception ex) {
				throw new InvalidEmblemFile(imageFile, ex);
			}
		}

		this.emblemFile = emblemFile;
	}

	/** The mobile phone number */
	protected String mobileNumber = null;
	public String getMobileNumber() { return this.mobileNumber; }
	public void setMobileNumber(String number) {
		if(number == null || number.trim().isEmpty()) {
			this.mobileNumber = null;
			return;
		}
		this.mobileNumber = MobileBKUHelper.normalizeMobileNumber(number);
	}

	/** The mobile phone password */
	public String mobilePassword = null;

	/** Holds the proxy host */
	public String proxyHost = null;

	/** Holds the proxy port number */
	protected int proxyPort = -1;
	public int getProxyPort() { return this.proxyPort; }
	public void setProxyPort(int port) throws InvalidPortException {
		if(port > 0 && port <= 0xFFFF) {
			this.proxyPort = port;
			return;
		}
		if(port == -1) {
			this.proxyPort = -1;
			return;
		}
		throw new InvalidPortException(port);
	}

	/** Holds the proxy username */
	public String proxyUser = null;

	/** Holds the proxy password */
	public String proxyPass = null;

	/** Holds the transparency of the signature placeholder */
	public int placeholderTransparency = Constants.DEFAULT_SIGNATURE_PLACEHOLDER_TRANSPARENCY;

	/** Holds the default BKU to use */
	public BKUs defaultBKU = BKUs.NONE;

	/** Holds the output folder */
	public String outputFolder = null;

	/** Holds the signatureNote */
	public String signatureNote = null;

	/** Holds the locale */
	public Locale locale = null;
	
	/** Holds the signature locale */
	public Locale signatureLocale = null;

	/** Holds the PDF/A compatibility setting */
	public boolean signaturePDFACompat = false;

	/** Holds the mobile BKU URL */
	public String mobileBKUURL = Constants.DEFAULT_MOBILE_BKU_URL;

	/** Holds the mobile BKU type */
	public MobileBKUs mobileBKUType = ConfigProviderImpl.DEFAULT_MOBILE_BKU_TYPE;

	/** Holds the mobile BKU BASE64 setting */
	protected boolean mobileBKUBase64 = false;

	/** Holds the default signature position */
	public SignaturePosition defaultSignaturePosition = null;

	/** Keystore signing options */
	public Boolean keystoreEnabled = null;
	public String keystoreFile = null;
	public String keystoreType = null;
	public String keystoreAlias = null;
	public String keystoreStorePass = null;
	public String keystoreKeyPass = null;

	/** Whether to automatically check for updates */
	public boolean updateCheck = true;

	/** Holds the main window size */
	public Point mainWindowSize = null;

	/** Whether to skip the output state */
	public boolean skipFinish = false;

	/** Whether to use an existing signature marker. */
	protected boolean useMarker = false;
	public boolean getUseMarker() { return this.useMarker; }
	public void setUseMarker(boolean useMarker) {
		this.useMarker = useMarker;
		if (useMarker) setUseSignatureFields(false);
	}

	/** Either QR-Code or signature fields as marker */
	protected boolean useSignatureFields = false;
	public boolean getUseSignatureFields() { return this.useSignatureFields; }
	public void setUseSignatureFields(boolean useFields) {
		this.useSignatureFields  = useFields;
		if (useFields) setUseMarker(false);
	}

	/** describes if the placeholder search is enabled */
	public boolean enabledPlaceholderUsage = false;

	/**	The Signature Profile */
	protected Profile signatureProfile = null;
	public Profile getSignatureProfile() {
		if (this.signatureProfile == null) {
			this.signatureProfile = Profile.SIGNATURBLOCK_SMALL;
		}
		return this.signatureProfile;
	}
	public void setSignatureProfile(Profile profile) { this.signatureProfile = profile; }

	public String saveFilePostFix = "_signed";

}
