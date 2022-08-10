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

import at.asit.pdfover.commons.Profile;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.graphics.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.bku.mobile.MobileBKUs;
import at.asit.pdfover.gui.exceptions.InvalidEmblemFile;
import at.asit.pdfover.gui.exceptions.InvalidPortException;
import at.asit.pdfover.gui.utils.LocaleSerializer;
import at.asit.pdfover.gui.workflow.config.ConfigurationDataInMemory.KeyStorePassStorageType;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.signator.BKUs;

/**
 * Implementation of the configuration provider and manipulator
 */
public class ConfigurationManager {


	/** Default Mobile BKU type */
	public static final MobileBKUs DEFAULT_MOBILE_BKU_TYPE = MobileBKUs.A_TRUST;


	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(ConfigurationManager.class);

	/**
	 * An empty property entry
	 */
	private static final String STRING_EMPTY = "";

	private String configurationFile = Constants.DEFAULT_CONFIG_FILENAME;

	private boolean loaded = false;

	// The persistent configuration read from the config file
	private ConfigurationDataInMemory configuration;

	// The configuration overlay built from the cmd line args
	private ConfigurationDataInMemory configurationOverlay;

	/**
	 * Constructor
	 */
	public ConfigurationManager() {
		this.configuration = new ConfigurationDataInMemory();
		this.configurationOverlay = new ConfigurationDataInMemory();
	}

	static public void factoryResetPersistentConfig() {
		// tell logback to close all file handles
		((ch.qos.logback.classic.LoggerContext)LoggerFactory.getILoggerFactory()).stop();

		File configDirectory = new File(Constants.CONFIG_DIRECTORY);
		File backupDirectory = new File(Constants.CONFIG_BACKUP_DIRECTORY);
		
		// delete existing backup, if any
		FileUtils.deleteQuietly(backupDirectory);

		// attempt 1: try to move the old config directory to a backup location
		try {
			FileUtils.moveDirectory(
				configDirectory,
				backupDirectory
			);
		} catch (Exception e) {
			System.out.println("Failed move config directory to backup location:");
			e.printStackTrace();

			// attempt 2: try to simply force delete the config directory
			try {
				FileUtils.forceDelete(configDirectory);
			} catch (Exception e2) {
				System.out.println("Failed to delete config directory:");
				e2.printStackTrace();

				// attempt 3: try to schedule the config directory for force deletion on JVM exit
				try {
					FileUtils.forceDeleteOnExit(configDirectory);
				} catch (Exception e3) {
					System.out.println("Failed to schedule config directory for deletion:");
					e3.printStackTrace();
				}
			}
		}
	}

	/* load from disk */
	public void loadFromDisk() throws IOException {
		if (loaded)
			throw new RuntimeException("ConfigProvider double load?");

		Properties diskConfig = new Properties();

		diskConfig.load(new FileInputStream(Constants.CONFIG_DIRECTORY + File.separator + getConfigurationFileName()));

		setDefaultEmblemPersistent(diskConfig.getProperty(Constants.CFG_EMBLEM));
		try {
			String strProp = diskConfig.getProperty(Constants.CFG_LOGO_ONLY_SIZE);
			if (strProp != null)
				setLogoOnlyTargetSizePersistent(Double.parseDouble(strProp));
		} catch (NumberFormatException e) { log.info("Invalid value for CFG_LOGO_ONLY_SIZE ignored.", e); }

		setDefaultMobileNumberPersistent(diskConfig.getProperty(Constants.CFG_MOBILE_NUMBER));
		setRememberMobilePasswordPersistent(Constants.TRUE.equals(diskConfig.getProperty(Constants.CFG_MOBILE_PASSWORD_REMEMBER)));

		setProxyHostPersistent(diskConfig.getProperty(Constants.CFG_PROXY_HOST));
		setProxyUserPersistent(diskConfig.getProperty(Constants.CFG_PROXY_USER));
		setProxyPassPersistent(diskConfig.getProperty(Constants.CFG_PROXY_PASS));

		setDefaultOutputFolderPersistent(diskConfig.getProperty(Constants.CFG_OUTPUT_FOLDER));

		String postFix = diskConfig.getProperty(Constants.CFG_POSTFIX);
		if (postFix == null)
			setSaveFilePostFixPersistent(Constants.DEFAULT_POSTFIX);
		else
			setSaveFilePostFixPersistent(postFix);

		String localeString = diskConfig.getProperty(Constants.CFG_LOCALE);

		Locale targetLocale = LocaleSerializer.parseFromString(localeString);
		if (targetLocale != null)
			setInterfaceLocalePersistent(targetLocale);

		String signatureLocaleString = diskConfig.getProperty(Constants.CFG_SIGNATURE_LOCALE);

		Locale signatureTargetLocale = LocaleSerializer.parseFromString(signatureLocaleString);
		if (signatureTargetLocale != null)
			setSignatureLocalePersistent(signatureTargetLocale);

		String useMarker = diskConfig.getProperty(Constants.CFG_USE_MARKER);
		if (useMarker != null)
			setUseMarkerPersistent(useMarker.equalsIgnoreCase(Constants.TRUE));

		String useSignatureFields = diskConfig.getProperty(Constants.CFG_USE_SIGNATURE_FIELDS);
		if (useSignatureFields != null)
			setUseSignatureFieldsPersistent(useSignatureFields.equalsIgnoreCase(Constants.TRUE));

		String enablePlaceholder = diskConfig.getProperty(Constants.CFG_ENABLE_PLACEHOLDER);
		if (enablePlaceholder != null)
			setEnablePlaceholderUsagePersistent(enablePlaceholder.equalsIgnoreCase(Constants.TRUE));

		String signatureProfileName = diskConfig.getProperty(Constants.SIGNATURE_PROFILE);
		if (signatureProfileName != null)
			setSignatureProfilePersistent(Profile.getProfile(signatureProfileName));

		if (diskConfig.containsKey(Constants.CFG_SIGNATURE_NOTE))
			setSignatureNotePersistent(diskConfig.getProperty(Constants.CFG_SIGNATURE_NOTE));
		else
			setSignatureNotePersistent(getSignatureProfile().getDefaultSignatureBlockNote(getSignatureLocale()));

		String compat = diskConfig.getProperty(Constants.CFG_SIGNATURE_PDFA_COMPAT);
		if (compat != null)
			setSignaturePdfACompatPersistent(compat.equalsIgnoreCase(Constants.TRUE));

		String bkuUrl = diskConfig.getProperty(Constants.CFG_MOBILE_BKU_URL);
		if (bkuUrl != null && !bkuUrl.isEmpty())
			this.configuration.mobileBKUURL = bkuUrl;

		String bkuType = diskConfig.getProperty(Constants.CFG_MOBILE_BKU_TYPE);

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

		String useBase64 = diskConfig.getProperty(Constants.CFG_MOBILE_BKU_BASE64);
		if (useBase64 != null)
			this.configuration.mobileBKUBase64 = useBase64.equalsIgnoreCase(Constants.TRUE);

		String proxyPortString = diskConfig.getProperty(Constants.CFG_PROXY_PORT);
		if (proxyPortString != null && !proxyPortString.trim().isEmpty())
		{
			int port = Integer.parseInt(proxyPortString);

			if (port > 0 && port <= 0xFFFF)
				setProxyPortPersistent(port);
			else
				log.warn("Proxy port is out of range!: " + port);
		}

		// Set Default BKU
		String bkuString = diskConfig.getProperty(Constants.CFG_BKU);
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
		setDefaultBKUPersistent(defaultBKU);

		// Set MainWindow size
		int width = Constants.DEFAULT_MAINWINDOW_WIDTH;
		int height = Constants.DEFAULT_MAINWINDOW_HEIGHT;
		String size = diskConfig.getProperty(Constants.CFG_MAINWINDOW_SIZE);
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
		String signaturePositionStr = diskConfig.getProperty(Constants.CFG_SIGNATURE_POSITION);
		setAutoPositionSignaturePersistent(signaturePositionStr != null && signaturePositionStr.trim().equals("auto"));

		//Set keystore stuff
		String keystoreEnabled = diskConfig.getProperty(Constants.CFG_KEYSTORE_ENABLED);
		if (keystoreEnabled != null)
			setKeyStoreEnabledPersistent(keystoreEnabled.equalsIgnoreCase(Constants.TRUE));
		setKeyStoreFilePersistent(diskConfig.getProperty(Constants.CFG_KEYSTORE_FILE));
		setKeyStoreTypePersistent(diskConfig.getProperty(Constants.CFG_KEYSTORE_TYPE));
		setKeyStoreAliasPersistent(diskConfig.getProperty(Constants.CFG_KEYSTORE_ALIAS));
		setKeyStoreStorePassPersistent(diskConfig.getProperty(Constants.CFG_KEYSTORE_STOREPASS));
		setKeyStoreKeyPassPersistent(diskConfig.getProperty(Constants.CFG_KEYSTORE_KEYPASS));
		String storeTypeOnDisk = diskConfig.getProperty(Constants.CFG_KEYSTORE_PASSSTORETYPE);
		if (storeTypeOnDisk == null) /* auto-detect based on old config */
		{
			String oldKeyPass = getKeyStoreKeyPassPersistent();
			String oldStorePass = getKeyStoreStorePassPersistent();
			if ((oldKeyPass != null && !oldKeyPass.trim().isEmpty()) || (oldStorePass != null && !oldStorePass.trim().isEmpty()))  /* previously stored password exists */
				storeTypeOnDisk = "disk";
			else
				storeTypeOnDisk = "memory";
		}
		if ("disk".equals(storeTypeOnDisk))
			setKeyStorePassStorageTypePersistent(KeyStorePassStorageType.DISK);
		else if ("memory".equals(storeTypeOnDisk))
			setKeyStorePassStorageTypePersistent(KeyStorePassStorageType.MEMORY);
		else
			setKeyStorePassStorageTypePersistent(null);

		// Set update check
		String updateCheck = diskConfig.getProperty(Constants.CFG_UPDATE_CHECK);
		if (updateCheck != null)
			setUpdateCheckPersistent(!updateCheck.equalsIgnoreCase(Constants.FALSE));
		
		log.info("Successfully loaded config from: " + getConfigurationFileName());
		loaded = true;
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
		props.setProperty(Constants.CFG_LOGO_ONLY_SIZE, Double.toString(getLogoOnlyTargetSize()));
		props.setProperty(Constants.CFG_SIGNATURE_NOTE, getSignatureNote());
		props.setProperty(Constants.CFG_MOBILE_NUMBER, getDefaultMobileNumberPersistent());
		if (getRememberMobilePassword())
			props.setProperty(Constants.CFG_MOBILE_PASSWORD_REMEMBER, Constants.TRUE);
		props.setProperty(Constants.CFG_OUTPUT_FOLDER, getDefaultOutputFolderPersistent());
		props.setProperty(Constants.CFG_POSTFIX, getSaveFilePostFix());

		Point size = this.configuration.mainWindowSize;
		props.setProperty(Constants.CFG_MAINWINDOW_SIZE, size.x + "," + size.y);

		Locale configLocale = getInterfaceLocale();
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

		if (!getAutoPositionSignaturePersistent())
			props.setProperty(Constants.CFG_SIGNATURE_POSITION, "");
		else
			props.setProperty(Constants.CFG_SIGNATURE_POSITION, "auto");

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

		KeyStorePassStorageType keystorePassStorageType = getKeyStorePassStorageType();
		if (keystorePassStorageType == null)
			props.setProperty(Constants.CFG_KEYSTORE_PASSSTORETYPE, "none");
		else if (keystorePassStorageType == KeyStorePassStorageType.MEMORY)
			props.setProperty(Constants.CFG_KEYSTORE_PASSSTORETYPE, "memory");
		else if (keystorePassStorageType == KeyStorePassStorageType.DISK)
			props.setProperty(Constants.CFG_KEYSTORE_PASSSTORETYPE, "disk");

		if (keystorePassStorageType == KeyStorePassStorageType.DISK)
		{
			String keystoreStorePass = getKeyStoreStorePassPersistent();
			if (keystoreStorePass == null)
				keystoreStorePass = STRING_EMPTY;
			props.setProperty(Constants.CFG_KEYSTORE_STOREPASS, keystoreStorePass);
			String keystoreKeyPass = getKeyStoreKeyPassPersistent();
			if (keystoreKeyPass == null)
				keystoreKeyPass = STRING_EMPTY;
			props.setProperty(Constants.CFG_KEYSTORE_KEYPASS, keystoreKeyPass);
		}

		if (!getUpdateCheck())
			props.setProperty(Constants.CFG_UPDATE_CHECK, Constants.FALSE);

		props.setProperty(Constants.SIGNATURE_PROFILE, getSignatureProfile().name());


		FileOutputStream outputstream = new FileOutputStream(configFile, false);

		props.store(outputstream, "Configuration file was generated!");

		log.info("Configuration file saved to " + configFile.getAbsolutePath());
	}

	public void setConfigurationFileName(String configurationFile)
	{
		if (this.configurationFile.equals(configurationFile))
			return;
		if (this.loaded)
			throw new RuntimeException("Cannot change configuration file path after it has been loaded");
		this.configurationFile = configurationFile;
	}
	public String getConfigurationFileName() { return this.configurationFile; }

	public void setDefaultBKUPersistent(BKUs bku) {
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

	public void setAutoPositionSignaturePersistent(boolean state) {
		this.configuration.autoPositionSignature = state;
	}

	public void setAutoPositionSignatureOverlay() {
		this.configurationOverlay.autoPositionSignature = true;
	}

	public boolean getAutoPositionSignature() {
		return this.configurationOverlay.autoPositionSignature || getAutoPositionSignaturePersistent();
	}

	public boolean getAutoPositionSignaturePersistent() {
		return this.configuration.autoPositionSignature;
	}

	public void setDefaultMobileNumberPersistent(String number) {
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

	public void setDefaultMobilePasswordOverlay(String password) {
		if (password == null || password.trim().isEmpty()) {
			this.configurationOverlay.mobilePassword = STRING_EMPTY;
		} else {
			this.configurationOverlay.mobilePassword = password;
		}
	}

	public String getDefaultMobilePassword() {
		/* this does not exist as a permanent config variable */
		return this.configurationOverlay.mobilePassword;
	}

	public boolean getRememberMobilePassword() {
		return this.configuration.rememberPassword;
	}

	public void setRememberMobilePasswordPersistent(boolean state) {
		this.configuration.rememberPassword = state;
	}

	public void setDefaultEmblemPersistent(String emblem) {
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

	public String getDefaultEmblemPath() {
		String emblem = this.configurationOverlay.getEmblemPath();
		if (emblem == null)
			emblem = getDefaultEmblemPersistent();
		return emblem;
	}

	public String getDefaultEmblemPersistent() {
		String emblem = this.configuration.getEmblemPath();
		if (emblem == null)
			emblem = STRING_EMPTY;
		return emblem;
	}

	public void setLogoOnlyTargetSizePersistent(double v) {
		this.configuration.logoOnlyTargetSize = v;
	}

	public double getLogoOnlyTargetSize() {
		return this.configuration.logoOnlyTargetSize;
	}

	public void setProxyHostPersistent(String host) {
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

	public void setProxyPortPersistent(int port) {
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

	public void setProxyUserPersistent(String user) {
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

	public void setProxyPassPersistent(String pass) {
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

	public void setDefaultOutputFolderPersistent(String outputFolder) {
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

	public void setSignatureNotePersistent(String note) {
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

	public void setInterfaceLocalePersistent(Locale locale) {
		if(locale == null) {
			this.configuration.interfaceLocale = Messages.getDefaultLocale();
		} else {
			this.configuration.interfaceLocale = locale;
			Locale.setDefault(locale);
			Messages.setLocale(locale);
		}
	}

	public Locale getInterfaceLocale() {
		Locale locale = this.configuration.interfaceLocale;
		if (locale == null)
			locale = Messages.getDefaultLocale();
		return locale;
	}

	public void setSignatureLocalePersistent(Locale locale) {
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

	public void setSignaturePdfACompatPersistent(boolean compat) {
		this.configuration.signaturePDFACompat = compat;
	}

	public boolean getSignaturePdfACompat() {
		return this.configuration.signaturePDFACompat;
	}

	public void setKeyStoreEnabledPersistent(Boolean enabled) {
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

	public void setKeyStoreFilePersistent(String file) {
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

	public void setKeyStoreTypePersistent(String type) {
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

	public void setKeyStoreAliasPersistent(String alias) {
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

	public void setKeyStorePassStorageTypePersistent(KeyStorePassStorageType type) {
		this.configuration.keystorePassStorageType = type;
	}

	public KeyStorePassStorageType getKeyStorePassStorageType() {
		return this.configuration.keystorePassStorageType;
	}

	public void setKeyStoreStorePassPersistent(String storePass) {
		this.configuration.keystoreStorePass = storePass;
	}

	public void setKeyStoreStorePassOverlay(String storePass) {
		this.configurationOverlay.keystoreStorePass = storePass;
	}

	public String getKeyStoreStorePass() {
		String storePass = this.configurationOverlay.keystoreStorePass;
		if (storePass != null)
			return storePass;
		if (getKeyStorePassStorageType() != KeyStorePassStorageType.DISK)
			return null;
		return getKeyStoreStorePassPersistent();
	}

	public String getKeyStoreStorePassPersistent() {
		return this.configuration.keystoreStorePass;
	}

	public void setKeyStoreKeyPassPersistent(String keyPass) {
		this.configuration.keystoreKeyPass = keyPass;
	}

	public void setKeyStoreKeyPassOverlay(String keyPass) {
		this.configurationOverlay.keystoreKeyPass = keyPass;
	}

	public String getKeyStoreKeyPass() {
		String keyPass = this.configurationOverlay.keystoreKeyPass;
		if (keyPass != null)
			return keyPass;
		if (getKeyStorePassStorageType() != KeyStorePassStorageType.DISK)
			return null;
		return getKeyStoreKeyPassPersistent();
	}

	public String getKeyStoreKeyPassPersistent() {
		return this.configuration.keystoreKeyPass;
	}

	public void setUpdateCheckPersistent(boolean checkUpdate) {
		this.configuration.updateCheck = checkUpdate;
	}

	public boolean getUpdateCheck() {
		return this.configuration.updateCheck;
	}

	public void setMainWindowSizePersistent(Point size) {
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

	public boolean getUseSignatureFields() {
		return this.configuration.getUseSignatureFields();
	}

	public void setUseSignatureFieldsPersistent(boolean useFields) {
		this.configuration.setUseSignatureFields(useFields);
		if (useFields) setUseMarkerPersistent(false);
	}

	public boolean getUseMarker() {
		return this.configuration.getUseMarker();
	}

	public void setUseMarkerPersistent(boolean useMarker) {
		this.configuration.setUseMarker(useMarker);
		if (useMarker) setUseSignatureFieldsPersistent(false);
	}

    public void setSaveFilePostFixPersistent(String postFix) {
        this.configuration.saveFilePostFix = postFix;
    }

	public String getSaveFilePostFix(){
		return this.configuration.saveFilePostFix;
	}

	public Profile getSignatureProfile() {
		return this.configuration.getSignatureProfile();
	}

	public void setSignatureProfilePersistent(Profile profile) {
		this.configuration.setSignatureProfile(profile);
	}

	public void setEnablePlaceholderUsagePersistent(boolean bool) {
		this.configuration.enabledPlaceholderUsage = bool;
	}

	public boolean getEnablePlaceholderUsage() {
		return this.configuration.enabledPlaceholderUsage;
	}



}
