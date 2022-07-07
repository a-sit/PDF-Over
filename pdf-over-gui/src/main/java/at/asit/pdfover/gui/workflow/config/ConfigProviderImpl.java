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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.asit.pdfover.commons.Profile;
import org.eclipse.swt.graphics.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.bku.mobile.MobileBKUs;
import at.asit.pdfover.gui.exceptions.InvalidEmblemFile;
import at.asit.pdfover.gui.exceptions.InvalidPortException;
import at.asit.pdfover.gui.utils.LocaleSerializer;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.signator.BKUs;
import at.asit.pdfover.signator.SignaturePosition;

// TODO: review which properties use the overlay in this file (also: remove unneeded setters/getters, maybe template impl for overlays?)

/**
 * Implementation of the configuration provider and manipulator
 */
public class ConfigProviderImpl {


	/** Default Mobile BKU type */
	public static final MobileBKUs DEFAULT_MOBILE_BKU_TYPE = MobileBKUs.A_TRUST;


	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(ConfigProviderImpl.class);

	/**
	 * An empty property entry
	 */
	private static final String STRING_EMPTY = "";

	private String configurationFile = Constants.DEFAULT_CONFIG_FILENAME;

	// The persistent configuration read from the config file
	private ConfigurationContainer configuration;

	// The configuration overlay built from the cmd line args
	private ConfigurationContainer configurationOverlay;

	/**
	 * Constructor
	 */
	public ConfigProviderImpl() {
		this.configuration = new ConfigurationContainer();
		this.configurationOverlay = new ConfigurationContainer();
	}

	/* load from disk */
	public void loadFromDisk() throws IOException {

		Properties config = new Properties();

		config.load(new FileInputStream(Constants.CONFIG_DIRECTORY + File.separator + getConfigurationFileName()));

		setDefaultEmblem(config.getProperty(Constants.CFG_EMBLEM));

		setDefaultMobileNumber(config.getProperty(Constants.CFG_MOBILE_NUMBER));

		setProxyHost(config.getProperty(Constants.CFG_PROXY_HOST));
		setProxyUser(config.getProperty(Constants.CFG_PROXY_USER));
		setProxyPass(config.getProperty(Constants.CFG_PROXY_PASS));

		setDefaultOutputFolder(config.getProperty(Constants.CFG_OUTPUT_FOLDER));

		String postFix = config.getProperty(Constants.CFG_POSTFIX);
		if (postFix == null)
			setSaveFilePostFix(Constants.DEFAULT_POSTFIX);
		else
			setSaveFilePostFix(postFix);

		String localeString = config.getProperty(Constants.CFG_LOCALE);

		Locale targetLocale = LocaleSerializer.parseFromString(localeString);
		if (targetLocale != null)
			setLocale(targetLocale);

		String signatureLocaleString = config.getProperty(Constants.CFG_SIGNATURE_LOCALE);

		Locale signatureTargetLocale = LocaleSerializer.parseFromString(signatureLocaleString);
		if (signatureTargetLocale != null)
			setSignatureLocale(signatureTargetLocale);

		String useMarker = config.getProperty(Constants.CFG_USE_MARKER);
		if (useMarker != null)
			setUseMarker(useMarker.equalsIgnoreCase(Constants.TRUE));

		String useSignatureFields = config.getProperty(Constants.CFG_USE_SIGNATURE_FIELDS);
		if (useSignatureFields != null)
			setUseSignatureFields(useSignatureFields.equalsIgnoreCase(Constants.TRUE));

		String enablePlaceholder = config.getProperty(Constants.CFG_ENABLE_PLACEHOLDER);
		if (enablePlaceholder != null)
			setEnablePlaceholderUsage(enablePlaceholder.equalsIgnoreCase(Constants.TRUE));

		String signatureProfile = config.getProperty(Constants.SIGNATURE_PROFILE);
		if (signatureProfile != null)
		{
			Profile profile = Profile.getProfile(signatureProfile);
			if (profile != null)
			{
				this.configuration.setSignatureProfile(profile);
				this.configurationOverlay.setSignatureProfile(profile);
			}
		}

		if (config.containsKey(Constants.CFG_SIGNATURE_NOTE))
			setSignatureNote(config.getProperty(Constants.CFG_SIGNATURE_NOTE));
		else
			setSignatureNote(Profile.getProfile(getSignatureProfile()).getDefaultSignatureBlockNote(getSignatureLocale()));

		String compat = config.getProperty(Constants.CFG_SIGNATURE_PDFA_COMPAT);
		if (compat != null)
			setSignaturePdfACompat(compat.equalsIgnoreCase(Constants.TRUE));

		String bkuUrl = config.getProperty(Constants.CFG_MOBILE_BKU_URL);
		if (bkuUrl != null && !bkuUrl.isEmpty())
			this.configuration.mobileBKUURL = bkuUrl;

		String bkuType = config
				.getProperty(Constants.CFG_MOBILE_BKU_TYPE);

		if (bkuType != null && !bkuType.isEmpty())
		{
			try
			{
				this.configuration.mobileBKUType = MobileBKUs.valueOf(bkuType.trim().toUpperCase());
			} catch (IllegalArgumentException e) {
				log.error("Invalid BKU type: " + bkuType);
				this.configuration.mobileBKUType = DEFAULT_MOBILE_BKU_TYPE;
			}
		}

		String useBase64 = config.getProperty(Constants.CFG_MOBILE_BKU_BASE64);
		if (useBase64 != null)
			this.configuration.mobileBKUBase64 = useBase64.equalsIgnoreCase(Constants.TRUE);

		String proxyPortString = config.getProperty(Constants.CFG_PROXY_PORT);
		if (proxyPortString != null && !proxyPortString.trim().isEmpty())
		{
			int port = Integer.parseInt(proxyPortString);

			if (port > 0 && port <= 0xFFFF)
				setProxyPort(port);
			else
				log.warn("Proxy port is out of range!: " + port);
		}

		// Set Default BKU
		String bkuString = config.getProperty(Constants.CFG_BKU);
		BKUs defaultBKU = BKUs.NONE;
		if (bkuString != null) {
			try {
				defaultBKU = BKUs.valueOf(bkuString);
			} catch (IllegalArgumentException ex) {
				log.error("Invalid BKU config value " + bkuString + " using none!");
				defaultBKU = BKUs.NONE;
			} catch (NullPointerException ex) {
				log.error("Invalid BKU config value " + bkuString + " using none!");
				defaultBKU = BKUs.NONE;
			}
		}
		setDefaultBKU(defaultBKU);

		// Set Signature placeholder transparency
		int transparency = Constants.DEFAULT_SIGNATURE_PLACEHOLDER_TRANSPARENCY;
		String trans = config.getProperty(Constants.CFG_SIGNATURE_PLACEHOLDER_TRANSPARENCY);
		if (trans != null) {
			try {
				transparency = Integer.parseInt(trans);
			} catch (NumberFormatException e) {
				log.debug("Couldn't parse placeholder transparency", e);
				// ignore parsing exception
			}
		}
		setPlaceholderTransparency(transparency);

		// Set MainWindow size
		int width = Constants.DEFAULT_MAINWINDOW_WIDTH;
		int height = Constants.DEFAULT_MAINWINDOW_HEIGHT;
		String size = config.getProperty(Constants.CFG_MAINWINDOW_SIZE);
		parse: {
			if (size == null)
				break parse;
			int pos = size.indexOf(',');
			if (pos <= 0)
				break parse;

			try {
				width = Integer.parseInt(size.substring(0, pos).trim());
				height = Integer.parseInt(size.substring(pos + 1).trim());
			} catch (NumberFormatException e) {
				log.debug("Couldn't parse main window size", e);
				// ignore parsing exception
			}
		}
		this.configuration.mainWindowSize = new Point(width, height);

		// Set Signature Position
		String signaturePosition = config.getProperty(Constants.CFG_SIGNATURE_POSITION);
		SignaturePosition position = null;
		if (signaturePosition != null && !signaturePosition.trim().isEmpty()) {
			signaturePosition = signaturePosition.trim().toLowerCase();

			Pattern pattern = Pattern.compile("(x=(\\d\\.?\\d?);y=(\\d\\.?\\d?);p=(\\d))|(auto)|(x=(\\d\\.?\\d?);y=(\\d\\.?\\d?))");
			Matcher matcher = pattern.matcher(signaturePosition);
			if (matcher.matches()) {
				if (matcher.groupCount() == 8) {
					if (matcher.group(1) != null) {
						// we have format: x=..;y=..;p=...
						try {
							// group 2 = x value
							float x = Float.parseFloat(matcher.group(2));

							// group 3 = y value
							float y = Float.parseFloat(matcher.group(3));

							// group 4 = p value
							int p = Integer.parseInt(matcher.group(3));

							position = new SignaturePosition(x, y, p);
						} catch (NumberFormatException ex) {
							log.error(
									"Signature Position read from config failed: Not a valid number", ex);
						}
					} else if (matcher.group(5) != null) {
						// we have format auto
						position = new SignaturePosition();
					} else if (matcher.group(6) != null) {
						// we have format x=...;y=...;
						// group 7 = x value
						float x = Float.parseFloat(matcher.group(7));

						// group 8 = y value
						float y = Float.parseFloat(matcher.group(8));

						position = new SignaturePosition(x, y);
					}
				} else {
					log.error("Signature Position read from config failed: wrong group Count!");
				}
			} else {
				log.error("Signature Position read from config failed: not matching string");
			}
		}
		setDefaultSignaturePosition(position);

		//Set keystore stuff
		String keystoreEnabled = config.getProperty(Constants.CFG_KEYSTORE_ENABLED);
		if (keystoreEnabled != null)
			setKeyStoreEnabled(keystoreEnabled.equalsIgnoreCase(Constants.TRUE));
		setKeyStoreFile(config.getProperty(Constants.CFG_KEYSTORE_FILE));
		setKeyStoreType(config.getProperty(Constants.CFG_KEYSTORE_TYPE));
		setKeyStoreAlias(config.getProperty(Constants.CFG_KEYSTORE_ALIAS));
		setKeyStoreStorePass(config.getProperty(Constants.CFG_KEYSTORE_STOREPASS));
		String keystoreKeyPass = config.getProperty(Constants.CFG_KEYSTORE_KEYPASS);
		setKeyStoreKeyPass(keystoreKeyPass);

		// Set update check
		String updateCheck = config.getProperty(Constants.CFG_UPDATE_CHECK);
		if (updateCheck != null)
			setUpdateCheck(!updateCheck.equalsIgnoreCase(Constants.FALSE));
		
		log.info("Successfully loaded config from: " + getConfigurationFileName());
	}

	/* save to file */
	public void saveToDisk() throws IOException {
		String filename = this.getConfigurationFileName();
		File configFile = new File(Constants.CONFIG_DIRECTORY + File.separator + filename);

		Properties props = new Properties();
		props.clear();

		props.setProperty(Constants.CFG_BKU, getDefaultBKUPersistent().toString());

		String proxyHost = getProxyHostPersistent();
		if (proxyHost != STRING_EMPTY)
			props.setProperty(Constants.CFG_PROXY_HOST, proxyHost);
		int proxyPort = getProxyPortPersistent();
		if (proxyPort != -1)
			props.setProperty(Constants.CFG_PROXY_PORT,Integer.toString(proxyPort));
		String proxyUser = getProxyUserPersistent();
		if (proxyUser != STRING_EMPTY)
			props.setProperty(Constants.CFG_PROXY_USER, proxyUser);
		String proxyPass = getProxyPassPersistent();
		if (proxyPass != STRING_EMPTY)
			props.setProperty(Constants.CFG_PROXY_PASS, proxyPass);

		props.setProperty(Constants.CFG_EMBLEM, getDefaultEmblemPersistent());
		props.setProperty(Constants.CFG_SIGNATURE_NOTE, getSignatureNote());
		props.setProperty(Constants.CFG_MOBILE_NUMBER, getDefaultMobileNumberPersistent());
		props.setProperty(Constants.CFG_OUTPUT_FOLDER, getDefaultOutputFolderPersistent());
		props.setProperty(Constants.CFG_POSTFIX, getSaveFilePostFix());
		props.setProperty(Constants.CFG_SIGNATURE_PLACEHOLDER_TRANSPARENCY,
				Integer.toString(getPlaceholderTransparency()));

		Point size = this.configuration.mainWindowSize;
		props.setProperty(Constants.CFG_MAINWINDOW_SIZE, size.x + "," + size.y);

		Locale configLocale = getLocale();
		if(configLocale != null) {
			props.setProperty(Constants.CFG_LOCALE, LocaleSerializer.getParsableString(configLocale));
		}

		Locale signatureLocale = this.getSignatureLocale();
		if(signatureLocale != null) {
			props.setProperty(Constants.CFG_SIGNATURE_LOCALE, LocaleSerializer.getParsableString(signatureLocale));
		}

		if (getUseMarker())
			props.setProperty(Constants.CFG_USE_MARKER, Constants.TRUE);

		if (getUseSignatureFields()) {
			props.setProperty(Constants.CFG_USE_SIGNATURE_FIELDS, Constants.TRUE);
		}

		if (getEnablePlaceholderUsage()) {
			props.setProperty(Constants.CFG_ENABLE_PLACEHOLDER, Constants.TRUE);
		}

		if (getSignaturePdfACompat())
			props.setProperty(Constants.CFG_SIGNATURE_PDFA_COMPAT, Constants.TRUE);

		SignaturePosition pos = getDefaultSignaturePositionPersistent();
		if (pos == null) {
			props.setProperty(Constants.CFG_SIGNATURE_POSITION, "");
		} else if (pos.useAutoPositioning()) {
			props.setProperty(Constants.CFG_SIGNATURE_POSITION, "auto");
		} else {
			props.setProperty(Constants.CFG_SIGNATURE_POSITION,
					String.format((Locale) null, "x=%f;y=%f;p=%d",
							pos.getX(), pos.getY(), pos.getPage()));
		}

		String mobileBKUURL = getMobileBKUURL();
		if (!mobileBKUURL.equals(Constants.DEFAULT_MOBILE_BKU_URL))
			props.setProperty(Constants.CFG_MOBILE_BKU_URL, mobileBKUURL);

		MobileBKUs mobileBKUType = getMobileBKUType();
		if (mobileBKUType != DEFAULT_MOBILE_BKU_TYPE)
			props.setProperty(Constants.CFG_MOBILE_BKU_TYPE, mobileBKUType.toString());

		if (getMobileBKUBase64())
			props.setProperty(Constants.CFG_MOBILE_BKU_BASE64, Constants.TRUE);

		if (Constants.THEME != Constants.Themes.DEFAULT)
			props.setProperty(Constants.CFG_THEME, Constants.THEME.name());

		if (getKeyStoreEnabledPersistent())
			props.setProperty(Constants.CFG_KEYSTORE_ENABLED, Constants.TRUE);
		String keystoreFile = getKeyStoreFilePersistent();
		if (keystoreFile != STRING_EMPTY)
			props.setProperty(Constants.CFG_KEYSTORE_FILE, keystoreFile);
		String keystoreType = getKeyStoreTypePersistent();
		if (keystoreType != STRING_EMPTY)
			props.setProperty(Constants.CFG_KEYSTORE_TYPE, keystoreType);
		String keystoreAlias = getKeyStoreAliasPersistent();
		if (keystoreAlias != STRING_EMPTY)
			props.setProperty(Constants.CFG_KEYSTORE_ALIAS, keystoreAlias);
		String keystoreStorePass = getKeyStoreStorePassPersistent();
		if (keystoreStorePass != STRING_EMPTY)
			props.setProperty(Constants.CFG_KEYSTORE_STOREPASS, keystoreStorePass);
		String keystoreKeyPass = getKeyStoreKeyPassPersistent();
		if (keystoreKeyPass != STRING_EMPTY)
			props.setProperty(Constants.CFG_KEYSTORE_KEYPASS, keystoreKeyPass);

		if (!getUpdateCheck())
			props.setProperty(Constants.CFG_UPDATE_CHECK, Constants.FALSE);

		props.setProperty(Constants.SIGNATURE_PROFILE, getSignatureProfile());


		FileOutputStream outputstream = new FileOutputStream(configFile, false);

		props.store(outputstream, "Configuration file was generated!");

		log.info("Configuration file saved to " + configFile.getAbsolutePath());
	}

	// TODO review this
	public void setConfigurationFileName(String configurationFile) { this.configurationFile = configurationFile; }
	public String getConfigurationFileName() { return this.configurationFile; }

	public void setDefaultBKU(BKUs bku) {
		this.configuration.defaultBKU = bku;
	}

	public void setDefaultBKUOverlay(BKUs bku) {
		this.configurationOverlay.defaultBKU = bku;
	}

	public BKUs getDefaultBKU() {
		BKUs bku = this.configurationOverlay.defaultBKU;
		if (bku == BKUs.NONE)
			bku = getDefaultBKUPersistent();
		return bku;
	}

	public BKUs getDefaultBKUPersistent() {
		return this.configuration.defaultBKU;
	}

	public void setDefaultSignaturePosition(SignaturePosition signaturePosition) {
		this.configuration.defaultSignaturePosition = signaturePosition;
	}

	public void setDefaultSignaturePositionOverlay(SignaturePosition signaturePosition) {
		this.configurationOverlay.defaultSignaturePosition = signaturePosition;
	}

	public SignaturePosition getDefaultSignaturePosition() {
		SignaturePosition position = this.configurationOverlay.defaultSignaturePosition;
		if (position == null)
			position = getDefaultSignaturePositionPersistent();
		return position;
	}

	public SignaturePosition getDefaultSignaturePositionPersistent() {
		return this.configuration.defaultSignaturePosition;
	}

	public void setPlaceholderTransparency(int transparency) {
		this.configuration.placeholderTransparency = transparency;
	}

	public int getPlaceholderTransparency() {
		return this.configuration.placeholderTransparency;
	}

	public void setDefaultMobileNumber(String number) {
		if (number == null || number.trim().isEmpty()) {
			this.configuration.setMobileNumber(STRING_EMPTY);
		} else {
			this.configuration.setMobileNumber(number);
		}
	}

	public void setDefaultMobileNumberOverlay(String number) {
		if (number == null || number.trim().isEmpty()) {
			this.configurationOverlay.setMobileNumber(STRING_EMPTY);
		} else {
			this.configurationOverlay.setMobileNumber(number);
		}
	}

	public String getDefaultMobileNumber() {
		String number = this.configurationOverlay.getMobileNumber();
		if (number == null)
			number = getDefaultMobileNumberPersistent();
		return number;
	}

	public String getDefaultMobileNumberPersistent() {
		String number = this.configuration.getMobileNumber();
		if (number == null)
			number = STRING_EMPTY;
		return number;
	}

	public void setDefaultMobilePassword(String password) {
		if (password == null || password.trim().isEmpty()) {
			this.configuration.mobilePassword = STRING_EMPTY;
		} else {
			this.configuration.mobilePassword = password;
		}
	}

	public void setDefaultMobilePasswordOverlay(String password) {
		if (password == null || password.trim().isEmpty()) {
			this.configurationOverlay.mobilePassword = STRING_EMPTY;
		} else {
			this.configurationOverlay.mobilePassword = password;
		}
	}

	public String getDefaultMobilePassword() {
		String password = this.configurationOverlay.mobilePassword;
		if (password == null)
			password = getDefaultMobilePasswordPersistent();
		return password;
	}

	public String getDefaultMobilePasswordPersistent() {
		String password = this.configuration.mobilePassword;
		if (password == null)
			password = STRING_EMPTY;
		return password;
	}

	public void setDefaultEmblem(String emblem) {
		try {
			if (emblem == null || emblem.trim().isEmpty()) {
				this.configuration.setEmblem(STRING_EMPTY);
			} else {
				this.configuration.setEmblem(emblem);
			}
		} catch (InvalidEmblemFile e) {
			log.error("Error setting emblem file", e);
			try {
				this.configuration.setEmblem(STRING_EMPTY);
			} catch (InvalidEmblemFile e1) {
				// Ignore
			}
		}
	}

	public void setDefaultEmblemOverlay(String emblem) {
		try {
			if (emblem == null || emblem.trim().isEmpty()) {
				this.configurationOverlay.setEmblem(STRING_EMPTY);
			} else {
				this.configurationOverlay.setEmblem(emblem);
			}
		} catch (InvalidEmblemFile e) {
			log.error("Error setting emblem file", e);
			try {
				this.configurationOverlay.setEmblem(STRING_EMPTY);
			} catch (InvalidEmblemFile e1) {
				// Ignore
			}
		}
	}

	public String getDefaultEmblem() {
		String emblem = this.configurationOverlay.getEmblem();
		if (emblem == null)
			emblem = getDefaultEmblemPersistent();
		return emblem;
	}

	public String getDefaultEmblemPersistent() {
		String emblem = this.configuration.getEmblem();
		if (emblem == null)
			emblem = STRING_EMPTY;
		return emblem;
	}

	public void setProxyHost(String host) {
		if (host == null || host.trim().isEmpty()) {
			this.configuration.proxyHost = STRING_EMPTY;
		} else {
			this.configuration.proxyHost = host;
		}
	}

	public void setProxyHostOverlay(String host) {
		if (host == null || host.trim().isEmpty()) {
			this.configurationOverlay.proxyHost = STRING_EMPTY;
		} else {
			this.configurationOverlay.proxyHost = host;
		}
	}

	public String getProxyHost() {
		String host = this.configurationOverlay.proxyHost;
		if (host == null)
			host = getProxyHostPersistent();
		return host;
	}

	public String getProxyHostPersistent() {
		String host = this.configuration.proxyHost;
		if (host == null)
			host = STRING_EMPTY;
		return host;
	}

	public void setProxyPort(int port) {
		try {
			this.configuration.setProxyPort(port);
		} catch (InvalidPortException e) {
			log.error("Error setting proxy port" , e);
			// ignore
		}
	}

	public void setProxyPortOverlay(int port) {
		try {
			this.configurationOverlay.setProxyPort(port);
		} catch (InvalidPortException e) {
			log.error("Error setting proxy port" , e);
			// ignore
		}
	}

	public int getProxyPort() {
		int port = this.configurationOverlay.getProxyPort();
		if (port == -1)
			port = getProxyPortPersistent();
		return port;
	}

	public int getProxyPortPersistent() {
		return this.configuration.getProxyPort();
	}

	public void setProxyUser(String user) {
		if (user == null || user.trim().isEmpty()) {
			this.configuration.proxyUser = STRING_EMPTY;
		} else {
			this.configuration.proxyUser = user;
		}
	}

	public void setProxyUserOverlay(String user) {
		if (user == null || user.trim().isEmpty()) {
			this.configurationOverlay.proxyUser = STRING_EMPTY;
		} else {
			this.configurationOverlay.proxyUser = user;
		}
	}

	public String getProxyUser() {
		String user = this.configurationOverlay.proxyUser;
		if (user == null)
			user = getProxyUserPersistent();
		return user;
	}

	public String getProxyUserPersistent() {
		String user = this.configuration.proxyUser;
		if (user == null)
			user = STRING_EMPTY;
		return user;
	}

	public void setProxyPass(String pass) {
		if (pass == null || pass.trim().isEmpty()) {
			this.configuration.proxyPass = STRING_EMPTY;
		} else {
			this.configuration.proxyPass = pass;
		}
	}

	public void setProxyPassOverlay(String pass) {
		if (pass == null || pass.trim().isEmpty()) {
			this.configurationOverlay.proxyPass = STRING_EMPTY;
		} else {
			this.configurationOverlay.proxyPass = pass;
		}
	}

	public String getProxyPass() {
		String pass = this.configurationOverlay.proxyPass;
		if (pass == null)
			pass = getProxyPassPersistent();
		return pass;
	}

	public String getProxyPassPersistent() {
		String pass = this.configuration.proxyPass;
		if (pass == null)
			pass = STRING_EMPTY;
		return pass;
	}

	public void setDefaultOutputFolder(String outputFolder) {
		if (outputFolder == null || outputFolder.trim().isEmpty()) {
			this.configuration.outputFolder = STRING_EMPTY;
		} else {
			this.configuration.outputFolder = outputFolder;
		}
	}

	public void setDefaultOutputFolderOverlay(String outputFolder) {
		if (outputFolder == null || outputFolder.trim().isEmpty()) {
			this.configurationOverlay.outputFolder = STRING_EMPTY;
		} else {
			this.configurationOverlay.outputFolder = outputFolder;
		}
	}

	public String getDefaultOutputFolder() {
		String outputFolder = this.configurationOverlay.outputFolder;
		if (outputFolder == null)
			outputFolder = getDefaultOutputFolderPersistent();
		return outputFolder;
	}

	public String getDefaultOutputFolderPersistent() {
		String outputFolder = this.configuration.outputFolder;
		if (outputFolder == null)
			outputFolder = STRING_EMPTY;
		return outputFolder;
	}

	public String getMobileBKUURL() {
		return this.configuration.mobileBKUURL;
	}

	public MobileBKUs getMobileBKUType() {
		return this.configuration.mobileBKUType;
	}

	public boolean getMobileBKUBase64() {
		return this.configuration.mobileBKUBase64;
	}

	public void setSignatureNote(String note) {
		if (note == null || note.trim().isEmpty()) {
			this.configuration.signatureNote = STRING_EMPTY;
		} else {
			this.configuration.signatureNote = note;
		}
	}

	public String getSignatureNote() {
		String note = this.configuration.signatureNote;
		if (note == null)
			note = STRING_EMPTY;
		return note;
	}

	public void setLocale(Locale locale) {
		if(locale == null) {
			this.configuration.locale = Messages.getDefaultLocale();
		} else {
			this.configuration.locale = locale;
			Locale.setDefault(locale);
			Messages.setLocale(locale);
		}
	}

	public Locale getLocale() {
		Locale locale = this.configuration.locale;
		if (locale == null)
			locale = Messages.getDefaultLocale();
		return locale;
	}

	public void setSignatureLocale(Locale locale) {
		if(locale == null) {
			this.configuration.signatureLocale = Messages.getDefaultLocale();
		} else {
			this.configuration.signatureLocale = locale;
		}
	}

	public Locale getSignatureLocale() {
		Locale locale = this.configuration.signatureLocale;
		if (locale == null)
			locale = Messages.getDefaultLocale();
		return locale;
	}

	public void setSignaturePdfACompat(boolean compat) {
		this.configuration.signaturePDFACompat = compat;
	}

	public boolean getSignaturePdfACompat() {
		return this.configuration.signaturePDFACompat;
	}

	public void setKeyStoreEnabled(Boolean enabled) {
		this.configuration.keystoreEnabled = enabled;
	}

	public void setKeyStoreEnabledOverlay(Boolean enabled) {
		this.configurationOverlay.keystoreEnabled = enabled;
	}

	public Boolean getKeyStoreEnabled() {
		Boolean enabled = this.configurationOverlay.keystoreEnabled;
		if (enabled == null)
			enabled = getKeyStoreEnabledPersistent();
		return enabled;
	}

	public Boolean getKeyStoreEnabledPersistent() {
		Boolean enabled = this.configuration.keystoreEnabled;
		if (enabled == null)
			enabled = false;
		return enabled;
	}

	public void setKeyStoreFile(String file) {
		if (file == null || file.trim().isEmpty()) {
			this.configuration.keystoreFile = STRING_EMPTY;
		} else {
			this.configuration.keystoreFile = file;
		}
	}

	public void setKeyStoreFileOverlay(String file) {
		if (file == null || file.trim().isEmpty()) {
			this.configurationOverlay.keystoreFile = STRING_EMPTY;
		} else {
			this.configurationOverlay.keystoreFile = file;
		}
	}

	public String getKeyStoreFile() {
		String file = this.configurationOverlay.keystoreFile;
		if (file == null)
			file = getKeyStoreFilePersistent();
		return file;
	}

	public String getKeyStoreFilePersistent() {
		String file = this.configuration.keystoreFile;
		if (file == null)
			file = STRING_EMPTY;
		return file;
	}

	public void setKeyStoreType(String type) {
		if (type == null || type.trim().isEmpty()) {
			this.configuration.keystoreType = STRING_EMPTY;
		} else {
			this.configuration.keystoreType = type;
		}
	}

	public void setKeyStoreTypeOverlay(String type) {
		if (type == null || type.trim().isEmpty()) {
			this.configurationOverlay.keystoreType = STRING_EMPTY;
		} else {
			this.configurationOverlay.keystoreType = type;
		}
	}

	public String getKeyStoreType() {
		String type = this.configurationOverlay.keystoreType;
		if (type == null)
			type = getKeyStoreTypePersistent();
		return type;
	}

	public String getKeyStoreTypePersistent() {
		String type = this.configuration.keystoreType;
		if (type == null)
			type = STRING_EMPTY;
		return type;
	}

	public void setKeyStoreAlias(String alias) {
		if (alias == null || alias.trim().isEmpty()) {
			this.configuration.keystoreAlias = STRING_EMPTY;
		} else {
			this.configuration.keystoreAlias = alias;
		}
	}

	public void setKeyStoreAliasOverlay(String alias) {
		if (alias == null || alias.trim().isEmpty()) {
			this.configurationOverlay.keystoreAlias = STRING_EMPTY;
		} else {
			this.configurationOverlay.keystoreAlias = alias;
		}
	}

	public String getKeyStoreAlias() {
		String alias = this.configurationOverlay.keystoreAlias;
		if (alias == null)
			alias = getKeyStoreAliasPersistent();
		return alias;
	}

	public String getKeyStoreAliasPersistent() {
		String alias = this.configuration.keystoreAlias;
		if (alias == null)
			alias = STRING_EMPTY;
		return alias;
	}

	public void setKeyStoreStorePass(String storePass) {
		if (storePass == null || storePass.trim().isEmpty()) {
			this.configuration.keystoreStorePass = STRING_EMPTY;
		} else {
			this.configuration.keystoreStorePass = storePass;
		}
	}

	public void setKeyStoreStorePassOverlay(String storePass) {
		if (storePass == null || storePass.trim().isEmpty()) {
			this.configurationOverlay.keystoreStorePass = STRING_EMPTY;
		} else {
			this.configurationOverlay.keystoreStorePass = storePass;
		}
	}

	public String getKeyStoreStorePass() {
		String storePass = this.configurationOverlay.keystoreStorePass;
		if (storePass != null)
			return storePass;
		return getKeyStoreStorePassPersistent();
	}

	public String getKeyStoreStorePassPersistent() {
		String storePass = this.configuration.keystoreStorePass;
		if (storePass == null)
			storePass = STRING_EMPTY;
		return storePass;
	}

	public void setKeyStoreKeyPass(String keyPass) {
		if (keyPass == null || keyPass.trim().isEmpty()) {
			this.configuration.keystoreKeyPass = STRING_EMPTY;
		} else {
			this.configuration.keystoreKeyPass = keyPass;
		}
	}

	public void setKeyStoreKeyPassOverlay(String keyPass) {
		if (keyPass == null || keyPass.trim().isEmpty()) {
			this.configurationOverlay.keystoreKeyPass = STRING_EMPTY;
		} else {
			this.configurationOverlay.keystoreKeyPass = keyPass;
		}
	}

	public String getKeyStoreKeyPass() {
		String keyPass = this.configurationOverlay.keystoreKeyPass;
		if (keyPass != null)
			return keyPass;
		return getKeyStoreKeyPassPersistent();
	}

	public String getKeyStoreKeyPassPersistent() {
		String keyPass = this.configuration.keystoreKeyPass;
		if (keyPass == null)
			keyPass = STRING_EMPTY;
		return keyPass;
	}

	public void setUpdateCheck(boolean checkUpdate) {
		this.configuration.updateCheck = checkUpdate;
	}

	public boolean getUpdateCheck() {
		return this.configuration.updateCheck;
	}

	public void setMainWindowSize(Point size) {
		this.configuration.mainWindowSize = size;
	}

	public Point getMainWindowSize() {
		return this.configuration.mainWindowSize;
	}

	public boolean getSkipFinish() {
		return this.configurationOverlay.skipFinish;
	}

	public void setSkipFinishOverlay(boolean skipFinish) {
		this.configurationOverlay.skipFinish = skipFinish;
	}

	public boolean getUseMarker() {
		return this.configurationOverlay.getUseMarker();
	}

	public boolean getUseSignatureFields() {
		return this.configurationOverlay.getUseSignatureFields();
	}

	public void setUseMarker(boolean useMarker) {
		this.configurationOverlay.setUseMarker(useMarker);
		if (useMarker) setUseSignatureFields(false);
	}

	public void setUseSignatureFields(boolean useFields) {
		this.configurationOverlay.setUseSignatureFields(useFields);
		if (useFields) setUseMarker(false);
	}

	public void setSignatureProfile(String profile) {
		this.configurationOverlay.setSignatureProfile(Profile.getProfile(profile));
	}

    public void setSaveFilePostFix(String postFix) {
        this.configurationOverlay.saveFilePostFix = postFix;
    }

	public String getSaveFilePostFix(){
		return this.configurationOverlay.saveFilePostFix;
	}

	public String getSignatureProfile() {
		return this.configurationOverlay.getSignatureProfile().name();
	}

	public void setEnablePlaceholderUsage(boolean bool) {
		this.configurationOverlay.enabledPlaceholderUsage = bool;
	}

	public boolean getEnablePlaceholderUsage() {
		return this.configurationOverlay.enabledPlaceholderUsage;
	}



}
