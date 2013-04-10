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
package at.asit.pdfover.gui.workflow;

// Imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.signator.BKUs;
import at.asit.pdfover.signator.SignaturePosition;

/**
 * 
 */
public class ConfigProviderImpl implements ConfigProvider, ConfigManipulator {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(ConfigProviderImpl.class);

	private BKUs defaultBKU = BKUs.NONE;

	/**
	 * Gets the Default Mobile URL
	 */
	public static final String DEFAULT_MOBILE_URL = "https://www.a-trust.at/mobile/https-security-layer-request/default.aspx"; //$NON-NLS-1$
	
	
	private SignaturePosition defaultSignaturePosition = null;

	/**
	 * An Emtpy property entry
	 */
	public static final String STRING_EMPTY = ""; //$NON-NLS-1$

	private String defaultMobileNumber = STRING_EMPTY;

	private String defaultPassword = STRING_EMPTY;

	private String emblem = STRING_EMPTY;

	private String proxyHost = STRING_EMPTY;

	private String configurationFile = ConfigManipulator.DEFAULT_CONFIG_FILE;

	private int proxyPort = -1;
	
	private String mobileBKU = DEFAULT_MOBILE_URL;

	private String outputFolder = STRING_EMPTY;

	/**
	 * Sets the default bku type
	 * 
	 * @param bku
	 *            the bku type
	 */
	@Override
	public void setDefaultBKU(BKUs bku) {
		this.defaultBKU = bku;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultBKU()
	 */
	@Override
	public BKUs getDefaultBKU() {
		return this.defaultBKU;
	}

	/**
	 * Sets the default signature position
	 * 
	 * @param signaturePosition
	 *            the default signature position
	 */
	@Override
	public void setDefaultSignaturePosition(SignaturePosition signaturePosition) {
		this.defaultSignaturePosition = signaturePosition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultSignaturePosition()
	 */
	@Override
	public SignaturePosition getDefaultSignaturePosition() {
		return this.defaultSignaturePosition;
	}

	/**
	 * Sets the default mobile number
	 * 
	 * @param number
	 *            the default mobile number
	 */
	@Override
	public void setDefaultMobileNumber(String number) {
		if (number == null || number.trim().equals("")) { //$NON-NLS-1$
			this.defaultMobileNumber = STRING_EMPTY;
		} else {
			this.defaultMobileNumber = number;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultMobileNumber()
	 */
	@Override
	public String getDefaultMobileNumber() {
		return this.defaultMobileNumber;
	}

	/**
	 * Sets the default password
	 * 
	 * @param password
	 *            the default password
	 */
	@Override
	public void setDefaultPassword(String password) {
		if (password == null || password.trim().equals("")) { //$NON-NLS-1$
			this.defaultPassword = STRING_EMPTY;
		} else {
			this.defaultPassword = password;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultPassword()
	 */
	@Override
	public String getDefaultPassword() {
		return this.defaultPassword;
	}

	/**
	 * Sets the default emblem
	 * 
	 * @param emblem
	 *            the default emblem
	 */
	@Override
	public void setDefaultEmblem(String emblem) {
		if (emblem == null || emblem.trim().equals("")) { //$NON-NLS-1$
			this.emblem = STRING_EMPTY;
		} else {
			this.emblem = emblem;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultEmblem()
	 */
	@Override
	public String getDefaultEmblem() {
		return this.emblem;
	}

	/**
	 * Sets the proxy host
	 * 
	 * @param host
	 *            the proxy host
	 */
	@Override
	public void setProxyHost(String host) {
		if (host == null || host.trim().equals("")) { //$NON-NLS-1$
			this.proxyHost = STRING_EMPTY;
		} else {
			this.proxyHost = host;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getProxyHost()
	 */
	@Override
	public String getProxyHost() {
		return this.proxyHost;
	}

	/**
	 * Sets the proxy port
	 * 
	 * @param port
	 *            the proxy port
	 */
	@Override
	public void setProxyPort(int port) {
		this.proxyPort = port;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getProxyPort()
	 */
	@Override
	public int getProxyPort() {
		return this.proxyPort;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.workflow.ConfigManipulator#saveCurrentConfiguration()
	 */
	@Override
	public void saveCurrentConfiguration() throws IOException {
		String filename = this.getConfigurationFile();

		File configFile = new File(filename);

		Properties props = new Properties();
		props.clear();

		props.setProperty(BKU_CONFIG, this.getDefaultBKU().toString());
		props.setProperty(PROXY_HOST_CONFIG, this.getProxyHost());
		props.setProperty(PROXY_PORT_CONFIG,
				Integer.toString(this.getProxyPort()));
		props.setProperty(EMBLEM_CONFIG, this.getDefaultEmblem());
		props.setProperty(MOBILE_NUMBER_CONFIG, this.getDefaultMobileNumber());
		props.setProperty(OUTPUT_FOLDER_CONFIG, this.getDefaultOutputFolder());

		SignaturePosition pos = this.getDefaultSignaturePosition();

		if (pos == null) {
			props.setProperty(SIGNATURE_POSITION_CONFIG, ""); //$NON-NLS-1$
		} else if (pos.useAutoPositioning()) {
			props.setProperty(SIGNATURE_POSITION_CONFIG, "auto"); //$NON-NLS-1$
		} else {
			props.setProperty(SIGNATURE_POSITION_CONFIG,
					String.format("x=%f;y=%f;p=%d", //$NON-NLS-1$
							pos.getX(), pos.getY(), pos.getPage()));
		}

		FileOutputStream outputstream = new FileOutputStream(configFile, false);

		props.store(outputstream, "Configuration file was generated!"); //$NON-NLS-1$

		log.info("Configuration file saved to " + configFile.getAbsolutePath()); //$NON-NLS-1$

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
		this.outputFolder = outputFolder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultOutputFolder()
	 */
	@Override
	public String getDefaultOutputFolder() {
		return this.outputFolder;
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
		
		// Set Emblem
		this.setDefaultEmblem(config
				.getProperty(ConfigManipulator.EMBLEM_CONFIG));

		// Set Mobile Phone Number
		this.setDefaultMobileNumber(config
				.getProperty(ConfigManipulator.MOBILE_NUMBER_CONFIG));

		// Set Proxy Host
		this.setProxyHost(config
				.getProperty(ConfigManipulator.PROXY_HOST_CONFIG));

		// Set Output Folder
		this.setDefaultOutputFolder(config
						.getProperty(ConfigManipulator.OUTPUT_FOLDER_CONFIG));
		
		String bku = config
				.getProperty(ConfigManipulator.MOBILE_BKU_URL_CONFIG);
		
		if(bku != null && !bku.equals("")) { //$NON-NLS-1$
			this.mobileBKU = bku;
		}
		
		// Set Proxy Port
		String proxyPortString = config
				.getProperty(ConfigManipulator.PROXY_PORT_CONFIG);

		if (proxyPortString != null && !proxyPortString.trim().equals("")) { //$NON-NLS-1$
			int port = Integer.parseInt(proxyPortString);

			if (port > 0 && port <= 0xFFFF) {
				this.setProxyPort(port);
			} else {
				log.warn("Proxy port is out of range!: " + port); //$NON-NLS-1$
			}
		}

		// Set Default BKU
		String bkuString = config.getProperty(ConfigManipulator.BKU_CONFIG);

		BKUs defaultBKU = BKUs.NONE;

		try {
			defaultBKU = BKUs.valueOf(bkuString);
		} catch (IllegalArgumentException ex) {
			log.error("Invalid BKU config value " + bkuString + " using none!"); //$NON-NLS-1$ //$NON-NLS-2$
			defaultBKU = BKUs.NONE;
		} catch (NullPointerException ex) {
			log.error("Invalid BKU config value " + bkuString + " using none!"); //$NON-NLS-1$ //$NON-NLS-2$
			defaultBKU = BKUs.NONE;
		}

		this.setDefaultBKU(defaultBKU);

		// Set Signature Position
		String signaturePosition = config
				.getProperty(ConfigManipulator.SIGNATURE_POSITION_CONFIG);

		SignaturePosition position = null;

		if (signaturePosition != null && !signaturePosition.trim().equals("")) { //$NON-NLS-1$

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
									"Signature Position read from config failed: Not a valid number", ex); //$NON-NLS-1$
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
					log.error("Signature Position read from config failed: wrong group Count!"); //$NON-NLS-1$
				}
			} else {
				log.error("Signature Position read from config failed: not matching string"); //$NON-NLS-1$
			}

		}

		this.setDefaultSignaturePosition(position);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getMobileBKUURL()
	 */
	@Override
	public String getMobileBKUURL() {
		return this.mobileBKU;
	}

}
