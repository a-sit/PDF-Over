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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import at.asit.pdfover.commons.Profile;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.graphics.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.BKUs;
import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.exceptions.InvalidEmblemFile;
import at.asit.pdfover.gui.exceptions.InvalidPortException;
import at.asit.pdfover.gui.utils.LocaleSerializer;
import at.asit.pdfover.gui.workflow.config.ConfigurationDataInMemory.KeyStorePassStorageType;
import at.asit.pdfover.commons.Messages;

import static at.asit.pdfover.commons.Constants.ISNOTNULL;

/**
 * Implementation of the configuration provider and manipulator
 */
public class ConfigurationManager {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(ConfigurationManager.class);

	private String configurationFile = Constants.DEFAULT_CONFIG_FILENAME;

	private boolean loaded = false;

	// The persistent configuration read from the config file
	private ConfigurationDataInMemory configuration;

	// The configuration overlay built from the cmd line args
	private ConfigurationDataInMemory configurationOverlay;

	// whether the configuration screen should crash on startup (for debugging purposes)
	public boolean crashOnConfig = false;

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

		{ /* for testing of error handlers */
			String crashProperty = diskConfig.getProperty("CRASH");
			if ("startup".equalsIgnoreCase(crashProperty))
				throw new RuntimeException("A robot must obey the orders given it by human beings except where such orders would conflict with the First Law.\n(CRASH=startup is set.)");
			else if ("config".equalsIgnoreCase(crashProperty))
				this.crashOnConfig = true;
			else if (crashProperty != null)
				log.warn("Unknown value '{}' for CRASH property -- you want 'startup' or 'config'.", crashProperty);
		}

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

	private void setProperty(Properties props, @Nonnull String key, @Nonnull String value) { props.setProperty(key, value); }
	private void setPropertyIfNotNull(Properties props, @Nonnull String key, @CheckForNull String value) { if (value != null) setProperty(props, key, value); }
	/* save to file */
	public void saveToDisk() throws IOException {
		String filename = this.getConfigurationFileName();
		File configFile = new File(Constants.CONFIG_DIRECTORY + File.separator + filename);

		Properties props = new Properties();
		props.clear();

		setProperty(props, Constants.CFG_BKU, ISNOTNULL(getDefaultBKUPersistent().name()));

		setPropertyIfNotNull(props, Constants.CFG_PROXY_HOST, getProxyHostPersistent());
		int proxyPort = getProxyPortPersistent();
		if (proxyPort != -1)
			setProperty(props, Constants.CFG_PROXY_PORT, ISNOTNULL(Integer.toString(proxyPort)));
		setPropertyIfNotNull(props, Constants.CFG_PROXY_USER, getProxyUserPersistent());
		setPropertyIfNotNull(props, Constants.CFG_PROXY_PASS, getProxyPassPersistent());

		setPropertyIfNotNull(props, Constants.CFG_EMBLEM, getDefaultEmblemPersistent());
		setProperty(props, Constants.CFG_LOGO_ONLY_SIZE, ISNOTNULL(Double.toString(getLogoOnlyTargetSize())));
		
		setPropertyIfNotNull(props, Constants.CFG_SIGNATURE_NOTE, getSignatureNote());
		setPropertyIfNotNull(props, Constants.CFG_MOBILE_NUMBER, getDefaultMobileNumberPersistent());
		if (getRememberMobilePassword())
			setProperty(props, Constants.CFG_MOBILE_PASSWORD_REMEMBER, Constants.TRUE);
		setPropertyIfNotNull(props, Constants.CFG_OUTPUT_FOLDER, getDefaultOutputFolderPersistent());
		setProperty(props, Constants.CFG_POSTFIX, getSaveFilePostFix());

		Point size = this.configuration.mainWindowSize;
		setProperty(props, Constants.CFG_MAINWINDOW_SIZE, size.x + "," + size.y);

		Locale configLocale = getInterfaceLocale();
		if(configLocale != null) {
			setProperty(props, Constants.CFG_LOCALE, LocaleSerializer.getParsableString(configLocale));
		}

		Locale signatureLocale = this.getSignatureLocale();
		if(signatureLocale != null) {
			setProperty(props, Constants.CFG_SIGNATURE_LOCALE, LocaleSerializer.getParsableString(signatureLocale));
		}

		if (getUseMarker())
			setProperty(props, Constants.CFG_USE_MARKER, Constants.TRUE);

		if (getUseSignatureFields()) {
			setProperty(props, Constants.CFG_USE_SIGNATURE_FIELDS, Constants.TRUE);
		}

		if (getEnablePlaceholderUsage()) {
			setProperty(props, Constants.CFG_ENABLE_PLACEHOLDER, Constants.TRUE);
		}

		if (getSignaturePdfACompat())
			setProperty(props, Constants.CFG_SIGNATURE_PDFA_COMPAT, Constants.TRUE);

		if (!getAutoPositionSignaturePersistent())
			setProperty(props, Constants.CFG_SIGNATURE_POSITION, "");
		else
			setProperty(props, Constants.CFG_SIGNATURE_POSITION, "auto");

		if (Constants.THEME != Constants.Themes.DEFAULT)
			setProperty(props, Constants.CFG_THEME, ISNOTNULL(Constants.THEME.name()));

		if (getKeyStoreEnabledPersistent())
			setProperty(props, Constants.CFG_KEYSTORE_ENABLED, Constants.TRUE);
		setPropertyIfNotNull(props, Constants.CFG_KEYSTORE_FILE, getKeyStoreFilePersistent());
		setPropertyIfNotNull(props, Constants.CFG_KEYSTORE_TYPE, getKeyStoreTypePersistent());
		setPropertyIfNotNull(props, Constants.CFG_KEYSTORE_ALIAS, getKeyStoreAliasPersistent());

		KeyStorePassStorageType keystorePassStorageType = getKeyStorePassStorageType();
		if (keystorePassStorageType == null)
			setProperty(props, Constants.CFG_KEYSTORE_PASSSTORETYPE, "none");
		else if (keystorePassStorageType == KeyStorePassStorageType.MEMORY)
			setProperty(props, Constants.CFG_KEYSTORE_PASSSTORETYPE, "memory");
		else if (keystorePassStorageType == KeyStorePassStorageType.DISK)
			setProperty(props, Constants.CFG_KEYSTORE_PASSSTORETYPE, "disk");

		if (keystorePassStorageType == KeyStorePassStorageType.DISK)
		{
			String keystoreStorePass = getKeyStoreStorePassPersistent();
			if (keystoreStorePass == null)
				keystoreStorePass = "";
			setProperty(props, Constants.CFG_KEYSTORE_STOREPASS, keystoreStorePass);
			String keystoreKeyPass = getKeyStoreKeyPassPersistent();
			if (keystoreKeyPass == null)
				keystoreKeyPass = "";
			setProperty(props, Constants.CFG_KEYSTORE_KEYPASS, keystoreKeyPass);
		}

		if (!getUpdateCheck())
			setProperty(props, Constants.CFG_UPDATE_CHECK, Constants.FALSE);

		setProperty(props, Constants.SIGNATURE_PROFILE, ISNOTNULL(getSignatureProfile().name()));


		FileOutputStream outputstream = new FileOutputStream(configFile, false);

		props.store(outputstream, "Configuration file was generated!");

		log.info("Configuration file saved to " + configFile.getAbsolutePath());
	}

	static private <T> T fallThroughOnNull(T one, T two) { return (one != null) ? one : two; }

	public void setConfigurationFileName(String configurationFile)
	{
		if (this.configurationFile.equals(configurationFile))
			return;
		if (this.loaded)
			throw new RuntimeException("Cannot change configuration file path after it has been loaded");
		this.configurationFile = configurationFile;
	}
	public String getConfigurationFileName() { return this.configurationFile; }

	public void setDefaultBKUPersistent(@Nonnull BKUs bku) {
		this.configuration.defaultBKU = bku;
	}

	public void setDefaultBKUOverlay(@Nonnull BKUs bku) {
		this.configurationOverlay.defaultBKU = bku;
	}

	public @Nonnull BKUs getDefaultBKU() {
		BKUs bku = this.configurationOverlay.defaultBKU;
		if (bku == BKUs.NONE)
			bku = getDefaultBKUPersistent();
		return bku;
	}

	public @Nonnull BKUs getDefaultBKUPersistent() {
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
			this.configuration.setMobileNumber(null);
		} else {
			this.configuration.setMobileNumber(number);
		}
	}

	public void setDefaultMobileNumberOverlay(String number) {
		if (number == null || number.trim().isEmpty()) {
			this.configurationOverlay.setMobileNumber(null);
		} else {
			this.configurationOverlay.setMobileNumber(number);
		}
	}

	public @CheckForNull String getDefaultMobileNumber() {
		return fallThroughOnNull(this.configurationOverlay.getMobileNumber(), getDefaultMobileNumberPersistent());
	}

	public @CheckForNull String getDefaultMobileNumberPersistent() {
		return this.configuration.getMobileNumber();
	}

	public void setDefaultMobilePasswordOverlay(String password) {
		if (password == null || password.trim().isEmpty()) {
			this.configurationOverlay.mobilePassword = null;
		} else {
			this.configurationOverlay.mobilePassword = password;
		}
	}

	public @CheckForNull String getDefaultMobilePassword() {
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
				this.configuration.setEmblem(null);
			} else {
				this.configuration.setEmblem(emblem);
			}
		} catch (InvalidEmblemFile e) {
			log.error("Error setting emblem file", e);
			try {
				this.configuration.setEmblem(null);
			} catch (InvalidEmblemFile e1) {
				// Ignore
			}
		}
	}

	public void setDefaultEmblemOverlay(String emblem) {
		try {
			if (emblem == null || emblem.trim().isEmpty()) {
				this.configurationOverlay.setEmblem(null);
			} else {
				this.configurationOverlay.setEmblem(emblem);
			}
		} catch (InvalidEmblemFile e) {
			log.error("Error setting emblem file", e);
			try {
				this.configurationOverlay.setEmblem(null);
			} catch (InvalidEmblemFile e1) {
				// Ignore
			}
		}
	}

	public @CheckForNull String getDefaultEmblemPath() {
		return fallThroughOnNull(this.configurationOverlay.getEmblemPath(), getDefaultEmblemPersistent());
	}

	public @CheckForNull String getDefaultEmblemPersistent() {
		return this.configuration.getEmblemPath();
	}

	public void setLogoOnlyTargetSizePersistent(double v) {
		this.configuration.logoOnlyTargetSize = v;
	}

	public double getLogoOnlyTargetSize() {
		return this.configuration.logoOnlyTargetSize;
	}

	public void setProxyHostPersistent(String host) {
		if (host == null || host.trim().isEmpty()) {
			this.configuration.proxyHost = null;
		} else {
			this.configuration.proxyHost = host;
		}
	}

	public void setProxyHostOverlay(String host) {
		if (host == null || host.trim().isEmpty()) {
			this.configurationOverlay.proxyHost = null;
		} else {
			this.configurationOverlay.proxyHost = host;
		}
	}

	public @CheckForNull String getProxyHost() {
		return fallThroughOnNull(this.configurationOverlay.proxyHost, getProxyHostPersistent());
	}

	public @CheckForNull String getProxyHostPersistent() {
		return this.configuration.proxyHost;
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
		if (port == -1) // TODO -1 is a terrible, no good, very bad hack
			port = getProxyPortPersistent();
		return port;
	}

	public int getProxyPortPersistent() {
		return this.configuration.getProxyPort();
	}

	public void setProxyUserPersistent(String user) {
		if (user == null || user.trim().isEmpty()) {
			this.configuration.proxyUser = null;
		} else {
			this.configuration.proxyUser = user;
		}
	}

	public void setProxyUserOverlay(String user) {
		if (user == null || user.trim().isEmpty()) {
			this.configurationOverlay.proxyUser = null;
		} else {
			this.configurationOverlay.proxyUser = user;
		}
	}

	public @CheckForNull String getProxyUser() {
		return fallThroughOnNull(this.configurationOverlay.proxyUser, getProxyUserPersistent());
	}

	public @CheckForNull String getProxyUserPersistent() {
		return this.configuration.proxyUser;
	}

	public void setProxyPassPersistent(String pass) {
		if (pass == null || pass.trim().isEmpty()) {
			this.configuration.proxyPass = null;
		} else {
			this.configuration.proxyPass = pass;
		}
	}

	public void setProxyPassOverlay(String pass) {
		if (pass == null || pass.trim().isEmpty()) {
			this.configurationOverlay.proxyPass = null;
		} else {
			this.configurationOverlay.proxyPass = pass;
		}
	}

	public @CheckForNull String getProxyPass() {
		return fallThroughOnNull(this.configurationOverlay.proxyPass, getProxyPassPersistent());
	}

	public @CheckForNull String getProxyPassPersistent() {
		return this.configuration.proxyPass;
	}

	public void setDefaultOutputFolderPersistent(String outputFolder) {
		if (outputFolder == null || outputFolder.trim().isEmpty()) {
			this.configuration.outputFolder = null;
		} else {
			this.configuration.outputFolder = outputFolder;
		}
	}

	public void setDefaultOutputFolderOverlay(String outputFolder) {
		if (outputFolder == null || outputFolder.trim().isEmpty()) {
			this.configurationOverlay.outputFolder = null;
		} else {
			this.configurationOverlay.outputFolder = outputFolder;
		}
	}

	public @CheckForNull String getDefaultOutputFolder() {
		return fallThroughOnNull(this.configurationOverlay.outputFolder, getDefaultOutputFolderPersistent());
	}

	public @CheckForNull String getDefaultOutputFolderPersistent() {
		return this.configuration.outputFolder;
	}

	public void setSignatureNotePersistent(String note) {
		if (note == null || note.trim().isEmpty()) {
			this.configuration.signatureNote = null;
		} else {
			this.configuration.signatureNote = note;
		}
	}

	public @CheckForNull String getSignatureNote() {
		return this.configuration.signatureNote;
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

	public @Nonnull Locale getInterfaceLocale() {
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

	public @Nonnull Locale getSignatureLocale() {
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

	public boolean getKeyStoreEnabled() {
		return ISNOTNULL(fallThroughOnNull(this.configurationOverlay.keystoreEnabled, getKeyStoreEnabledPersistent()));
	}

	public boolean getKeyStoreEnabledPersistent() {
		return ISNOTNULL(fallThroughOnNull(this.configuration.keystoreEnabled, Boolean.FALSE));
	}

	public void setKeyStoreFilePersistent(String file) {
		if (file == null || file.trim().isEmpty()) {
			this.configuration.keystoreFile = null;
		} else {
			this.configuration.keystoreFile = file;
		}
	}

	public void setKeyStoreFileOverlay(String file) {
		if (file == null || file.trim().isEmpty()) {
			this.configurationOverlay.keystoreFile = null;
		} else {
			this.configurationOverlay.keystoreFile = file;
		}
	}

	public @CheckForNull String getKeyStoreFile() {
		return fallThroughOnNull(this.configurationOverlay.keystoreFile, getKeyStoreFilePersistent());
	}

	public @CheckForNull String getKeyStoreFilePersistent() {
		return this.configuration.keystoreFile;
	}

	public void setKeyStoreTypePersistent(String type) {
		if (type == null || type.trim().isEmpty()) {
			this.configuration.keystoreType = null;
		} else {
			this.configuration.keystoreType = type;
		}
	}

	public void setKeyStoreTypeOverlay(String type) {
		if (type == null || type.trim().isEmpty()) {
			this.configurationOverlay.keystoreType = null;
		} else {
			this.configurationOverlay.keystoreType = type;
		}
	}

	public @CheckForNull String getKeyStoreType() {
		return fallThroughOnNull(this.configurationOverlay.keystoreType, getKeyStoreTypePersistent());
	}

	public @CheckForNull String getKeyStoreTypePersistent() {
		return this.configuration.keystoreType;
	}

	public void setKeyStoreAliasPersistent(String alias) {
		if (alias == null || alias.trim().isEmpty()) {
			this.configuration.keystoreAlias = null;
		} else {
			this.configuration.keystoreAlias = alias;
		}
	}

	public void setKeyStoreAliasOverlay(String alias) {
		if (alias == null || alias.trim().isEmpty()) {
			this.configurationOverlay.keystoreAlias = null;
		} else {
			this.configurationOverlay.keystoreAlias = alias;
		}
	}

	public @CheckForNull String getKeyStoreAlias() {
		return fallThroughOnNull(this.configurationOverlay.keystoreAlias, getKeyStoreAliasPersistent());
	}

	public @CheckForNull String getKeyStoreAliasPersistent() {
		return this.configuration.keystoreAlias;
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

	public @CheckForNull String getKeyStoreStorePass() {
		String storePass = this.configurationOverlay.keystoreStorePass;
		if (storePass != null)
			return storePass;
		if (getKeyStorePassStorageType() != KeyStorePassStorageType.DISK)
			return null;
		return getKeyStoreStorePassPersistent();
	}

	public @CheckForNull String getKeyStoreStorePassPersistent() {
		return this.configuration.keystoreStorePass;
	}

	public void setKeyStoreKeyPassPersistent(String keyPass) {
		this.configuration.keystoreKeyPass = keyPass;
	}

	public void setKeyStoreKeyPassOverlay(String keyPass) {
		this.configurationOverlay.keystoreKeyPass = keyPass;
	}

	public @CheckForNull String getKeyStoreKeyPass() {
		String keyPass = this.configurationOverlay.keystoreKeyPass;
		if (keyPass != null)
			return keyPass;
		if (getKeyStorePassStorageType() != KeyStorePassStorageType.DISK)
			return null;
		return getKeyStoreKeyPassPersistent();
	}

	public @CheckForNull String getKeyStoreKeyPassPersistent() {
		return this.configuration.keystoreKeyPass;
	}

	public void setUpdateCheckPersistent(boolean checkUpdate) {
		this.configuration.updateCheck = checkUpdate;
	}

	public boolean getUpdateCheck() {
		return this.configuration.updateCheck;
	}

	public void setMainWindowSizePersistent(@Nonnull Point size) {
		this.configuration.mainWindowSize = size;
	}

	public @Nonnull Point getMainWindowSize() {
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

    public void setSaveFilePostFixPersistent(@Nonnull String postFix) {
        this.configuration.saveFilePostFix = postFix;
    }

	public @Nonnull String getSaveFilePostFix(){
		return this.configuration.saveFilePostFix;
	}

	public @Nonnull Profile getSignatureProfile() {
		return ISNOTNULL(fallThroughOnNull(this.configuration.signatureProfile, Profile.SIGNATURBLOCK_SMALL));
	}

	public void setSignatureProfilePersistent(Profile profile) {
		this.configuration.signatureProfile = profile;
	}

	public void setEnablePlaceholderUsagePersistent(boolean bool) {
		this.configuration.enabledPlaceholderUsage = bool;
	}

	public boolean getEnablePlaceholderUsage() {
		return this.configuration.enabledPlaceholderUsage;
	}



}
