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
import java.util.Objects;

import at.asit.pdfover.commons.Profile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import at.asit.pdfover.commons.BKUs;
import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.bku.mobile.MobileBKUValidator;
import at.asit.pdfover.gui.exceptions.InvalidEmblemFile;
import at.asit.pdfover.gui.exceptions.InvalidPortException;
import lombok.NonNull;

/**
 * Implementation of the configuration container
 */
public class ConfigurationDataInMemory {

	/** the emblem size (in mm) for logo only signatures */
	public double logoOnlyTargetSize = Constants.DEFAULT_LOGO_ONLY_SIZE;

	/** the emblem File */
	protected String emblemFile = null;
	public String getEmblemPath() { return this.emblemFile; }
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
		this.mobileNumber = MobileBKUValidator.normalizeMobileNumber(number);
	}

	/** The mobile phone password */
	public String mobilePassword = null;

	public boolean rememberPassword = false;

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

	/** Holds the default BKU to use */
	public @NonNull BKUs defaultBKU = BKUs.NONE;

	/** Holds the output folder */
	public String outputFolder = null;

	/** Holds the signatureNote */
	public String signatureNote = null;

	/** Holds the locale */
	public Locale interfaceLocale = null;
	
	/** Holds the signature locale */
	public Locale signatureLocale = null;

	/** Holds the PDF/A compatibility setting */
	public boolean signaturePDFACompat = false;

	/** Holds the default signature position */
	public boolean autoPositionSignature = false;

	/** Keystore signing options */
	public enum KeyStorePassStorageType { MEMORY, DISK };
	public Boolean keystoreEnabled = null;
	public String keystoreFile = null;
	public String keystoreType = null;
	public String keystoreAlias = null;
	public KeyStorePassStorageType keystorePassStorageType = null;
	public String keystoreStorePass = null;
	public String keystoreKeyPass = null;

	/** Whether to automatically check for updates */
	public boolean updateCheck = true;

	/** Holds the main window size
	 * 
	 * @IMPORTANT this must always be valid and non-null, even if configuration failed to load for whatever reason (it is used by error handlers!)
	*/
	public @NonNull Point mainWindowSize = new Point(Constants.DEFAULT_MAINWINDOW_WIDTH, Constants.DEFAULT_MAINWINDOW_HEIGHT);

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
	public @NonNull Profile getSignatureProfile() {
		return Objects.requireNonNullElse(this.signatureProfile, Profile.SIGNATURBLOCK_SMALL);
	}
	public void setSignatureProfile(Profile profile) { this.signatureProfile = profile; }

	public @NonNull String saveFilePostFix = Constants.DEFAULT_POSTFIX;

	/** whether fido2 authentication should be selected by default */
	public boolean fido2ByDefault = false;

	public String lastOpenedDirectory = null;

}
