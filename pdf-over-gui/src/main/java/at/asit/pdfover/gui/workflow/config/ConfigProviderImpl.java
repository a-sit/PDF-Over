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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

/**
 * Implementation of the configuration provider and manipulator
 */
public class ConfigProviderImpl implements ConfigProvider, ConfigManipulator,
		ConfigOverlayManipulator, PersistentConfigProvider {


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
	private static final String STRING_EMPTY = ""; //

	private String configurationFile = Constants.DEFAULT_CONFIG_FILENAME;

	// The persistent configuration read from the config file
	private ConfigurationContainer configuration;

	// The configuration overlay built from the cmd line args
	private ConfigurationContainer configurationOverlay;

	/**
	 * Constructor
	 */
	public ConfigProviderImpl() {
		this.configuration = new ConfigurationContainerImpl();
		this.configurationOverlay = new ConfigurationContainerImpl();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * at.asit.pdfover.gui.workflow.ConfigProvider#loadConfiguration(java.io
	 * .InputStream)
	 */
	@Override
	public void loadConfiguration(InputStream configSource) throws IOException {

		Properties config = new Properties();

		config.load(configSource);

		setDefaultEmblem(config.getProperty(Constants.CFG_EMBLEM));

		setDefaultMobileNumber(config.getProperty(Constants.CFG_MOBILE_NUMBER));

		setSignatureNote(config.getProperty(Constants.CFG_SIGNATURE_NOTE));

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

		String compat = config.getProperty(Constants.CFG_SIGNATURE_PDFA_COMPAT);
		if (compat != null)
			setSignaturePdfACompat(compat.equalsIgnoreCase(Constants.TRUE));

		String bkuUrl = config.getProperty(Constants.CFG_MOBILE_BKU_URL);
		if (bkuUrl != null && !bkuUrl.isEmpty())
			this.configuration.setMobileBKUURL(bkuUrl);

		String bkuType = config
				.getProperty(Constants.CFG_MOBILE_BKU_TYPE);

		if (bkuType != null && !bkuType.isEmpty())
		{
			try
			{
				this.configuration.setMobileBKUType(MobileBKUs.valueOf(
						bkuType.trim().toUpperCase()));
			} catch (IllegalArgumentException e) {
				log.error("Invalid BKU type: " + bkuType); //
				this.configuration.setMobileBKUType(DEFAULT_MOBILE_BKU_TYPE);
			}
		}

		String useBase64 = config.getProperty(Constants.CFG_MOBILE_BKU_BASE64);
		if (useBase64 != null)
			this.configuration.setMobileBKUBase64(useBase64.equalsIgnoreCase(Constants.TRUE));

		String proxyPortString = config.getProperty(Constants.CFG_PROXY_PORT);
		if (proxyPortString != null && !proxyPortString.trim().isEmpty())
		{
			int port = Integer.parseInt(proxyPortString);

			if (port > 0 && port <= 0xFFFF)
				setProxyPort(port);
			else
				log.warn("Proxy port is out of range!: " + port); //
		}

		// Set Default BKU
		String bkuString = config.getProperty(Constants.CFG_BKU);
		BKUs defaultBKU = BKUs.NONE;
		if (bkuString != null) {
			try {
				defaultBKU = BKUs.valueOf(bkuString);
			} catch (IllegalArgumentException ex) {
				log.error("Invalid BKU config value " + bkuString + " using none!"); // //
				defaultBKU = BKUs.NONE;
			} catch (NullPointerException ex) {
				log.error("Invalid BKU config value " + bkuString + " using none!"); // //
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
				log.debug("Couldn't parse placeholder transparency", e); //
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
				log.debug("Couldn't parse main window size", e); //
				// ignore parsing exception
			}
		}
		this.configuration.setMainWindowSize(new Point(width, height));

		// Set Signature Position
		String signaturePosition = config.getProperty(Constants.CFG_SIGNATURE_POSITION);
		SignaturePosition position = null;
		if (signaturePosition != null && !signaturePosition.trim().isEmpty()) {
			signaturePosition = signaturePosition.trim().toLowerCase();

			Pattern pattern = Pattern.compile(SIGN_POS_REGEX);
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
									"Signature Position read from config failed: Not a valid number", ex); //
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
					log.error("Signature Position read from config failed: wrong group Count!"); //
				}
			} else {
				log.error("Signature Position read from config failed: not matching string"); //
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
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * at.asit.pdfover.gui.workflow.ConfigManipulator#saveCurrentConfiguration()
	 */
	@Override
	public void saveCurrentConfiguration() throws IOException {
		String filename = this.getConfigurationFile();

		File configFile = new File(this.getConfigurationDirectory()
				+ File.separator + filename);

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

		Point size = this.configuration.getMainWindowSize();
		props.setProperty(Constants.CFG_MAINWINDOW_SIZE, size.x + "," + size.y); //

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
			props.setProperty(Constants.CFG_SIGNATURE_POSITION, ""); //
		} else if (pos.useAutoPositioning()) {
			props.setProperty(Constants.CFG_SIGNATURE_POSITION, "auto"); //
		} else {
			props.setProperty(Constants.CFG_SIGNATURE_POSITION,
					String.format((Locale) null, "x=%f;y=%f;p=%d", //
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

		props.store(outputstream, "Configuration file was generated!"); //

		log.info("Configuration file saved to " + configFile.getAbsolutePath()); //
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * at.asit.pdfover.gui.workflow.ConfigProvider#getConfigurationDirectory()
	 */
	@Override
	public String getConfigurationDirectory() {
		return Constants.CONFIG_DIRECTORY;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * at.asit.pdfover.gui.workflow.ConfigManipulator#setConfigurationFile(java
	 * .lang.String)
	 */
	@Override
	public void setConfigurationFile(String configurationFile) {
		this.configurationFile = configurationFile;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getConfigurationFile()
	 */
	@Override
	public String getConfigurationFile() {
		return this.configurationFile;
	}

	/**
	 * Sets the default bku type
	 *
	 * @param bku
	 *            the bku type
	 */
	@Override
	public void setDefaultBKU(BKUs bku) {
		this.configuration.setDefaultBKU(bku);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigOverlayManipulator#setDefaultBKUOverlay(at.asit.pdfover.signator.BKUs)
	 */
	@Override
	public void setDefaultBKUOverlay(BKUs bku) {
		this.configurationOverlay.setDefaultBKU(bku);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultBKU()
	 */
	@Override
	public BKUs getDefaultBKU() {
		BKUs bku = this.configurationOverlay.getDefaultBKU();
		if (bku == BKUs.NONE)
			bku = getDefaultBKUPersistent();
		return bku;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.PersistentConfigProvider#getDefaultBKUPersistent()
	 */
	@Override
	public BKUs getDefaultBKUPersistent() {
		return this.configuration.getDefaultBKU();
	}

	/**
	 * Sets the default signature position
	 *
	 * @param signaturePosition
	 *            the default signature position
	 */
	@Override
	public void setDefaultSignaturePosition(SignaturePosition signaturePosition) {
		this.configuration.setDefaultSignaturePosition(signaturePosition);
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigOverlayManipulator#setDefaultSignaturePositionOverlay(at.asit.pdfover.signator.SignaturePosition)
	 */
	@Override
	public void setDefaultSignaturePositionOverlay(SignaturePosition signaturePosition) {
		this.configurationOverlay.setDefaultSignaturePosition(signaturePosition);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultSignaturePosition()
	 */
	@Override
	public SignaturePosition getDefaultSignaturePosition() {
		SignaturePosition position = this.configurationOverlay.getDefaultSignaturePosition();
		if (position == null)
			position = getDefaultSignaturePositionPersistent();
		return position;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.PersistentConfigProvider#getDefaultSignaturePositionPersistent()
	 */
	@Override
	public SignaturePosition getDefaultSignaturePositionPersistent() {
		return this.configuration.getDefaultSignaturePosition();
	}

	/**
	 * Sets the signature placeholder transparency
	 *
	 * @param transparency
	 *            the signature placeholder transparency
	 */
	@Override
	public void setPlaceholderTransparency(int transparency) {
		this.configuration.setPlaceholderTransparency(transparency);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * at.asit.pdfover.gui.workflow.ConfigProvider#getPlaceholderTransparency()
	 */
	@Override
	public int getPlaceholderTransparency() {
		return this.configuration.getPlaceholderTransparency();
	}

	/**
	 * Sets the default mobile number
	 *
	 * @param number
	 *            the default mobile number
	 */
	@Override
	public void setDefaultMobileNumber(String number) {
		if (number == null || number.trim().isEmpty()) {
			this.configuration.setMobileNumber(STRING_EMPTY);
		} else {
			this.configuration.setMobileNumber(number);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigOverlayManipulator#setDefaultMobileNumberOverlay(java.lang.String)
	 */
	@Override
	public void setDefaultMobileNumberOverlay(String number) {
		if (number == null || number.trim().isEmpty()) {
			this.configurationOverlay.setMobileNumber(STRING_EMPTY);
		} else {
			this.configurationOverlay.setMobileNumber(number);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultMobileNumber()
	 */
	@Override
	public String getDefaultMobileNumber() {
		String number = this.configurationOverlay.getMobileNumber();
		if (number == null)
			number = getDefaultMobileNumberPersistent();
		return number;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.PersistentConfigProvider#getDefaultMobileNumberPersistent()
	 */
	@Override
	public String getDefaultMobileNumberPersistent() {
		String number = this.configuration.getMobileNumber();
		if (number == null)
			number = STRING_EMPTY;
		return number;
	}

	/**
	 * Sets the default mobile password
	 *
	 * @param password
	 *            the default password
	 */
	@Override
	public void setDefaultMobilePassword(String password) {
		if (password == null || password.trim().isEmpty()) {
			this.configuration.setMobilePassword(STRING_EMPTY);
		} else {
			this.configuration.setMobilePassword(password);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigOverlayManipulator#setDefaultMobilePasswordOverlay(java.lang.String)
	 */
	@Override
	public void setDefaultMobilePasswordOverlay(String password) {
		if (password == null || password.trim().isEmpty()) {
			this.configurationOverlay.setMobilePassword(STRING_EMPTY);
		} else {
			this.configurationOverlay.setMobilePassword(password);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultPassword()
	 */
	@Override
	public String getDefaultMobilePassword() {
		String password = this.configurationOverlay.getMobilePassword();
		if (password == null)
			password = getDefaultMobilePasswordPersistent();
		return password;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.PersistentConfigProvider#getDefaultMobilePasswordPersistent()
	 */
	@Override
	public String getDefaultMobilePasswordPersistent() {
		String password = this.configuration.getMobilePassword();
		if (password == null)
			password = STRING_EMPTY;
		return password;
	}

	/**
	 * Sets the default emblem
	 *
	 * @param emblem
	 *            the default emblem
	 */
	@Override
	public void setDefaultEmblem(String emblem) {
		try {
			if (emblem == null || emblem.trim().isEmpty()) {
				this.configuration.setEmblem(STRING_EMPTY);
			} else {
				this.configuration.setEmblem(emblem);
			}
		} catch (InvalidEmblemFile e) {
			log.error("Error setting emblem file", e); //
			try {
				this.configuration.setEmblem(STRING_EMPTY);
			} catch (InvalidEmblemFile e1) {
				// Ignore
			}
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigOverlayManipulator#setDefaultEmblemOverlay(java.lang.String)
	 */
	@Override
	public void setDefaultEmblemOverlay(String emblem) {
		try {
			if (emblem == null || emblem.trim().isEmpty()) {
				this.configurationOverlay.setEmblem(STRING_EMPTY);
			} else {
				this.configurationOverlay.setEmblem(emblem);
			}
		} catch (InvalidEmblemFile e) {
			log.error("Error setting emblem file", e); //
			try {
				this.configurationOverlay.setEmblem(STRING_EMPTY);
			} catch (InvalidEmblemFile e1) {
				// Ignore
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultEmblem()
	 */
	@Override
	public String getDefaultEmblem() {
		String emblem = this.configurationOverlay.getEmblem();
		if (emblem == null)
			emblem = getDefaultEmblemPersistent();
		return emblem;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.PersistentConfigProvider#getDefaultEmblemPersistent()
	 */
	@Override
	public String getDefaultEmblemPersistent() {
		String emblem = this.configuration.getEmblem();
		if (emblem == null)
			emblem = STRING_EMPTY;
		return emblem;
	}

	/*@Override
	public String getDownloadURL() {
		return Constants.CERTIFICATE_DOWNLOAD_XML_URL;
	}*/

	/**
	 * Sets the proxy host
	 *
	 * @param host
	 *            the proxy host
	 */
	@Override
	public void setProxyHost(String host) {
		if (host == null || host.trim().isEmpty()) {
			this.configuration.setProxyHost(STRING_EMPTY);
		} else {
			this.configuration.setProxyHost(host);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigOverlayManipulator#setProxyHostOverlay(java.lang.String)
	 */
	@Override
	public void setProxyHostOverlay(String host) {
		if (host == null || host.trim().isEmpty()) {
			this.configurationOverlay.setProxyHost(STRING_EMPTY);
		} else {
			this.configurationOverlay.setProxyHost(host);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getProxyHost()
	 */
	@Override
	public String getProxyHost() {
		String host = this.configurationOverlay.getProxyHost();
		if (host == null)
			host = getProxyHostPersistent();
		return host;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.PersistentConfigProvider#getProxyHostPersistent()
	 */
	@Override
	public String getProxyHostPersistent() {
		String host = this.configuration.getProxyHost();
		if (host == null)
			host = STRING_EMPTY;
		return host;
	}

	/**
	 * Sets the proxy port
	 *
	 * @param port
	 *            the proxy port
	 */
	@Override
	public void setProxyPort(int port) {
		try {
			this.configuration.setProxyPort(port);
		} catch (InvalidPortException e) {
			log.error("Error setting proxy port" , e); //
			// ignore
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigOverlayManipulator#setProxyPortOverlay(int)
	 */
	@Override
	public void setProxyPortOverlay(int port) {
		try {
			this.configurationOverlay.setProxyPort(port);
		} catch (InvalidPortException e) {
			log.error("Error setting proxy port" , e); //
			// ignore
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getProxyPort()
	 */
	@Override
	public int getProxyPort() {
		int port = this.configurationOverlay.getProxyPort();
		if (port == -1)
			port = getProxyPortPersistent();
		return port;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.PersistentConfigProvider#getProxyPortPersistent()
	 */
	@Override
	public int getProxyPortPersistent() {
		return this.configuration.getProxyPort();
	}

	/**
	 * Sets the proxy username
	 *
	 * @param user
	 *            the proxy username
	 */
	@Override
	public void setProxyUser(String user) {
		if (user == null || user.trim().isEmpty()) {
			this.configuration.setProxyUser(STRING_EMPTY);
		} else {
			this.configuration.setProxyUser(user);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigOverlayManipulator#setProxyUserOverlay(java.lang.String)
	 */
	@Override
	public void setProxyUserOverlay(String user) {
		if (user == null || user.trim().isEmpty()) {
			this.configurationOverlay.setProxyUser(STRING_EMPTY);
		} else {
			this.configurationOverlay.setProxyUser(user);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigProvider#getProxyUser()
	 */
	@Override
	public String getProxyUser() {
		String user = this.configurationOverlay.getProxyUser();
		if (user == null)
			user = getProxyUserPersistent();
		return user;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.PersistentConfigProvider#getProxyUserPersistent()
	 */
	@Override
	public String getProxyUserPersistent() {
		String user = this.configuration.getProxyUser();
		if (user == null)
			user = STRING_EMPTY;
		return user;
	}

	/**
	 * Sets the proxy password
	 *
	 * @param pass
	 *            the proxy password
	 */
	@Override
	public void setProxyPass(String pass) {
		if (pass == null || pass.trim().isEmpty()) {
			this.configuration.setProxyPass(STRING_EMPTY);
		} else {
			this.configuration.setProxyPass(pass);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigOverlayManipulator#setProxyPassOverlay(java.lang.String)
	 */
	@Override
	public void setProxyPassOverlay(String pass) {
		if (pass == null || pass.trim().isEmpty()) {
			this.configurationOverlay.setProxyPass(STRING_EMPTY);
		} else {
			this.configurationOverlay.setProxyPass(pass);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigProvider#getProxyPass()
	 */
	@Override
	public String getProxyPass() {
		String pass = this.configurationOverlay.getProxyPass();
		if (pass == null)
			pass = getProxyPassPersistent();
		return pass;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.PersistentConfigProvider#getProxyPassPersistent()
	 */
	@Override
	public String getProxyPassPersistent() {
		String pass = this.configuration.getProxyPass();
		if (pass == null)
			pass = STRING_EMPTY;
		return pass;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * at.asit.pdfover.gui.workflow.ConfigManipulator#setDefaultOutputFolder
	 * (java.lang.String)
	 */
	@Override
	public void setDefaultOutputFolder(String outputFolder) {
		if (outputFolder == null || outputFolder.trim().isEmpty()) {
			this.configuration.setOutputFolder(STRING_EMPTY);
		} else {
			this.configuration.setOutputFolder(outputFolder);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigOverlayManipulator#setDefaultOutputFolderOverlay(java.lang.String)
	 */
	@Override
	public void setDefaultOutputFolderOverlay(String outputFolder) {
		if (outputFolder == null || outputFolder.trim().isEmpty()) {
			this.configurationOverlay.setOutputFolder(STRING_EMPTY);
		} else {
			this.configurationOverlay.setOutputFolder(outputFolder);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultOutputFolder()
	 */
	@Override
	public String getDefaultOutputFolder() {
		String outputFolder = this.configurationOverlay.getOutputFolder();
		if (outputFolder == null)
			outputFolder = getDefaultOutputFolderPersistent();
		return outputFolder;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.PersistentConfigProvider#getDefaultOutputFolderPersistent()
	 */
	@Override
	public String getDefaultOutputFolderPersistent() {
		String outputFolder = this.configuration.getOutputFolder();
		if (outputFolder == null)
			outputFolder = STRING_EMPTY;
		return outputFolder;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getMobileBKUURL()
	 */
	@Override
	public String getMobileBKUURL() {
		return this.configuration.getMobileBKUURL();
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getMobileBKUType()
	 */
	@Override
	public MobileBKUs getMobileBKUType() {
		return this.configuration.getMobileBKUType();
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigProvider#getMobileBKUBase64()
	 */
	@Override
	public boolean getMobileBKUBase64() {
		return this.configuration.getMobileBKUBase64();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * at.asit.pdfover.gui.workflow.ConfigManipulator#setSignatureNote(java.
	 * lang.String)
	 */
	@Override
	public void setSignatureNote(String note) {
		if (note == null || note.trim().isEmpty()) {
			this.configuration.setSignatureNote(STRING_EMPTY);
		} else {
			this.configuration.setSignatureNote(note);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getSignatureNote()
	 */
	@Override
	public String getSignatureNote() {
		String note = this.configuration.getSignatureNote();
		if (note == null)
			note = STRING_EMPTY;
		return note;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigManipulator#setLocale(java.util.Locale)
	 */
	@Override
	public void setLocale(Locale locale) {
		if(locale == null) {
			this.configuration.setLocale(Messages.getDefaultLocale());
		} else {
			this.configuration.setLocale(locale);
			Locale.setDefault(locale);
			Messages.setLocale(locale);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getConfigLocale()
	 */
	@Override
	public Locale getLocale() {
		Locale locale = this.configuration.getLocale();
		if (locale == null)
			locale = Messages.getDefaultLocale();
		return locale;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigManipulator#setSignatureLocale(java.util.Locale)
	 */
	@Override
	public void setSignatureLocale(Locale locale) {
		if(locale == null) {
			this.configuration.setSignatureLocale(Messages.getDefaultLocale());
		} else {
			this.configuration.setSignatureLocale(locale);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getSignatureLocale()
	 */
	@Override
	public Locale getSignatureLocale() {
		Locale locale = this.configuration.getSignatureLocale();
		if (locale == null)
			locale = Messages.getDefaultLocale();
		return locale;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigManipulator#setSignaturePdfACompat(boolean)
	 */
	@Override
	public void setSignaturePdfACompat(boolean compat) {
		this.configuration.setSignaturePdfACompat(compat);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigProvider#getSignaturePdfACompat()
	 */
	@Override
	public boolean getSignaturePdfACompat() {
		return this.configuration.getSignaturePdfACompat();
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigManipulator#setKeyStoreEnabled(boolean)
	 */
	@Override
	public void setKeyStoreEnabled(Boolean enabled) {
		this.configuration.setKeyStoreEnabled(enabled);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigOverlayManipulator#setKeyStoreEnabledOverlay(boolean)
	 */
	@Override
	public void setKeyStoreEnabledOverlay(Boolean enabled) {
		this.configurationOverlay.setKeyStoreEnabled(enabled);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigProvider#getKeyStoreEnabled()
	 */
	@Override
	public Boolean getKeyStoreEnabled() {
		Boolean enabled = this.configurationOverlay.getKeyStoreEnabled();
		if (enabled == null)
			enabled = getKeyStoreEnabledPersistent();
		return enabled;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.PersistentConfigProvider#getKeyStoreEnabledPersistent()
	 */
	@Override
	public Boolean getKeyStoreEnabledPersistent() {
		Boolean enabled = this.configuration.getKeyStoreEnabled();
		if (enabled == null)
			enabled = false;
		return enabled;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigManipulator#setKeyStoreFile(java.lang.String)
	 */
	@Override
	public void setKeyStoreFile(String file) {
		if (file == null || file.trim().isEmpty()) {
			this.configuration.setKeyStoreFile(STRING_EMPTY);
		} else {
			this.configuration.setKeyStoreFile(file);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigOverlayManipulator#setKeyStoreFileOverlay(java.lang.String)
	 */
	@Override
	public void setKeyStoreFileOverlay(String file) {
		if (file == null || file.trim().isEmpty()) {
			this.configurationOverlay.setKeyStoreFile(STRING_EMPTY);
		} else {
			this.configurationOverlay.setKeyStoreFile(file);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigProvider#getKeyStoreFile()
	 */
	@Override
	public String getKeyStoreFile() {
		String file = this.configurationOverlay.getKeyStoreFile();
		if (file == null)
			file = getKeyStoreFilePersistent();
		return file;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.PersistentConfigProvider#getKeyStoreFilePersistent()
	 */
	@Override
	public String getKeyStoreFilePersistent() {
		String file = this.configuration.getKeyStoreFile();
		if (file == null)
			file = STRING_EMPTY;
		return file;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigManipulator#setKeyStoreType(java.lang.String)
	 */
	@Override
	public void setKeyStoreType(String type) {
		if (type == null || type.trim().isEmpty()) {
			this.configuration.setKeyStoreType(STRING_EMPTY);
		} else {
			this.configuration.setKeyStoreType(type);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigOverlayManipulator#setKeyStoreTypeOverlay(java.lang.String)
	 */
	@Override
	public void setKeyStoreTypeOverlay(String type) {
		if (type == null || type.trim().isEmpty()) {
			this.configurationOverlay.setKeyStoreType(STRING_EMPTY);
		} else {
			this.configurationOverlay.setKeyStoreType(type);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigProvider#getKeyStoreType()
	 */
	@Override
	public String getKeyStoreType() {
		String type = this.configurationOverlay.getKeyStoreType();
		if (type == null)
			type = getKeyStoreTypePersistent();
		return type;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.PersistentConfigProvider#getKeyStoreTypePersistent()
	 */
	@Override
	public String getKeyStoreTypePersistent() {
		String type = this.configuration.getKeyStoreType();
		if (type == null)
			type = STRING_EMPTY;
		return type;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigManipulator#setKeyStoreAlias(java.lang.String)
	 */
	@Override
	public void setKeyStoreAlias(String alias) {
		if (alias == null || alias.trim().isEmpty()) {
			this.configuration.setKeyStoreAlias(STRING_EMPTY);
		} else {
			this.configuration.setKeyStoreAlias(alias);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigOverlayManipulator#setKeyStoreAliasOverlay(java.lang.String)
	 */
	@Override
	public void setKeyStoreAliasOverlay(String alias) {
		if (alias == null || alias.trim().isEmpty()) {
			this.configurationOverlay.setKeyStoreAlias(STRING_EMPTY);
		} else {
			this.configurationOverlay.setKeyStoreAlias(alias);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigProvider#getKeyStoreAlias()
	 */
	@Override
	public String getKeyStoreAlias() {
		String alias = this.configurationOverlay.getKeyStoreAlias();
		if (alias == null)
			alias = getKeyStoreAliasPersistent();
		return alias;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.PersistentConfigProvider#getKeyStoreAliasPersistent()
	 */
	@Override
	public String getKeyStoreAliasPersistent() {
		String alias = this.configuration.getKeyStoreAlias();
		if (alias == null)
			alias = STRING_EMPTY;
		return alias;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigManipulator#setKeyStoreStorePass(java.lang.String)
	 */
	@Override
	public void setKeyStoreStorePass(String storePass) {
		if (storePass == null || storePass.trim().isEmpty()) {
			this.configuration.setKeyStoreStorePass(STRING_EMPTY);
		} else {
			this.configuration.setKeyStoreStorePass(storePass);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigOverlayManipulator#setKeyStoreStorePassOverlay(java.lang.String)
	 */
	@Override
	public void setKeyStoreStorePassOverlay(String storePass) {
		if (storePass == null || storePass.trim().isEmpty()) {
			this.configurationOverlay.setKeyStoreStorePass(STRING_EMPTY);
		} else {
			this.configurationOverlay.setKeyStoreStorePass(storePass);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigProvider#getKeyStoreStorePass()
	 */
	@Override
	public String getKeyStoreStorePass() {
		String storePass = this.configurationOverlay.getKeyStoreStorePass();
		if (storePass != null)
			return storePass;
		return getKeyStoreStorePassPersistent();
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.PersistentConfigProvider#getKeyStoreStorePassPersistent()
	 */
	@Override
	public String getKeyStoreStorePassPersistent() {
		String storePass = this.configuration.getKeyStoreStorePass();
		if (storePass == null)
			storePass = STRING_EMPTY;
		return storePass;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigManipulator#setKeyStoreKeyPass(java.lang.String)
	 */
	@Override
	public void setKeyStoreKeyPass(String keyPass) {
		if (keyPass == null || keyPass.trim().isEmpty()) {
			this.configuration.setKeyStoreKeyPass(STRING_EMPTY);
		} else {
			this.configuration.setKeyStoreKeyPass(keyPass);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigOverlayManipulator#setKeyStoreKeyPassOverlay(java.lang.String)
	 */
	@Override
	public void setKeyStoreKeyPassOverlay(String keyPass) {
		if (keyPass == null || keyPass.trim().isEmpty()) {
			this.configurationOverlay.setKeyStoreKeyPass(STRING_EMPTY);
		} else {
			this.configurationOverlay.setKeyStoreKeyPass(keyPass);
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigProvider#getKeyStoreKeyPass()
	 */
	@Override
	public String getKeyStoreKeyPass() {
		String keyPass = this.configurationOverlay.getKeyStoreKeyPass();
		if (keyPass != null)
			return keyPass;
		return getKeyStoreKeyPassPersistent();
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.PersistentConfigProvider#getKeyStoreKeyPassPersistent()
	 */
	@Override
	public String getKeyStoreKeyPassPersistent() {
		String keyPass = this.configuration.getKeyStoreKeyPass();
		if (keyPass == null)
			keyPass = STRING_EMPTY;
		return keyPass;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigManipulator#setUpdateCheck(boolean)
	 */
	@Override
	public void setUpdateCheck(boolean checkUpdate) {
		this.configuration.setUpdateCheck(checkUpdate);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigProvider#getUpdateCheck()
	 */
	@Override
	public boolean getUpdateCheck() {
		return this.configuration.getUpdateCheck();
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigManipulator#setMainWindowSize(org.eclipse.swt.graphics.Point)
	 */
	@Override
	public void setMainWindowSize(Point size) {
		this.configuration.setMainWindowSize(size);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getMainWindowSize()
	 */
	@Override
	public Point getMainWindowSize() {
		return this.configuration.getMainWindowSize();
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigProvider#getSkipFinish()
	 */
	@Override
	public boolean getSkipFinish() {
		return this.configurationOverlay.getSkipFinish();
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.ConfigOverlayManipulator#setSkipFinishOverlay(boolean)
	 */
	@Override
	public void setSkipFinishOverlay(boolean skipFinish) {
		this.configurationOverlay.setSkipFinish(skipFinish);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.workflow.config.ConfigProvider#getUseMarker()
	 */
	@Override
	public boolean getUseMarker() {
		return this.configurationOverlay.getUseMarker();
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.config.PersistentConfigProvider#getUseSignatureFields()
	 */
	@Override
	public boolean getUseSignatureFields() {
		return this.configurationOverlay.getUseSignatureFields();
	}




	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.workflow.config.ConfigManipulator#setUseMarker(
	 * boolean)
	 */
	@Override
	public void setUseMarker(boolean useMarker) {
		this.configurationOverlay.setUseMarker(useMarker);
		if (useMarker) setUseSignatureFields(false);
	}

	@Override
	public void setUseSignatureFields(boolean useFields) {
		this.configurationOverlay.setUseSignatureFields(useFields);
		if (useFields) setUseMarker(false);
	}

	@Override
	public void setSignatureProfile(String profile) {
		this.configurationOverlay.setSignatureProfile(Profile.getProfile(profile));
	}

    @Override
    public void setSaveFilePostFix(String postFix) {
        this.configurationOverlay.setSaveFilePostFix(postFix);
    }

    @Override
	public String getSaveFilePostFix(){
		return this.configurationOverlay.getSaveFilePostFix();
	}

    @Override
	public String getSignatureProfile() {
		return this.configurationOverlay.getSignatureProfile().name();
	}


	@Override
	public void setEnablePlaceholderUsage(boolean bool) {
		this.configurationOverlay.setEnablePlaceholderUsage(bool);
	}

	@Override
	public boolean getEnablePlaceholderUsage() {
		return this.configurationOverlay.getEnablePlaceholderUsage();
	}



}
