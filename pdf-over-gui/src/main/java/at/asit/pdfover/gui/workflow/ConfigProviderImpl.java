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
import java.util.Properties;

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
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(ConfigProviderImpl.class);
	
	private BKUs defaultBKU = BKUs.NONE;
	
	private SignaturePosition defaultSignaturePosition = null;
	
	private String defaultMobileNumber = null;
	
	private String defaultPassword = null;
	
	private String emblem = null;
	
	private String proxyHost = null;
	
	private String configurationFile = ConfigManipulator.DEFAULT_CONFIG_FILE;
	
	private int proxyPort = -1;
	
	/**
	 * Sets the default bku type
	 * @param bku the bku type
	 */
	@Override
	public void setDefaultBKU(BKUs bku) {
		this.defaultBKU = bku;
	}
	
	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultBKU()
	 */
	@Override
	public BKUs getDefaultBKU() {
		return this.defaultBKU;
	}

	/**
	 * Sets the default signature position
	 * 
	 * @param signaturePosition the default signature position
	 */
	@Override
	public void setDefaultSignaturePosition(SignaturePosition signaturePosition) {
		this.defaultSignaturePosition = signaturePosition;
	}
	
	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultSignaturePosition()
	 */
	@Override
	public SignaturePosition getDefaultSignaturePosition() {
		return this.defaultSignaturePosition;
	}

	/**
	 * Sets the default mobile number
	 * @param number the default mobile number
	 */
	@Override
	public void setDefaultMobileNumber(String number) {
		if(number == null || number.trim().equals("")) { //$NON-NLS-1$
			this.defaultMobileNumber = null;
		} else {
			this.defaultMobileNumber = number;
		}
	}
	
	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultMobileNumber()
	 */
	@Override
	public String getDefaultMobileNumber() {
		return this.defaultMobileNumber;
	}
	
	/**
	 * Sets the default password
	 * @param password the default password
	 */
	@Override
	public void setDefaultPassword(String password) {
		if(password == null || password.trim().equals("")) { //$NON-NLS-1$
			this.defaultPassword = null;
		} else {
			this.defaultPassword = password;
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultPassword()
	 */
	@Override
	public String getDefaultPassword() {
		return this.defaultPassword;
	}

	/**
	 * Sets the default emblem
	 * 
	 * @param emblem the default emblem
	 */
	@Override
	public void setDefaultEmblem(String emblem) {
		if(emblem == null || emblem.trim().equals("")) { //$NON-NLS-1$
			this.emblem = null;
		} else {
			this.emblem = emblem;
		}
	}
	
	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getDefaultEmblem()
	 */
	@Override
	public String getDefaultEmblem() {
		return this.emblem;
	}

	/**
	 * Sets the proxy host
	 * @param host the proxy host
	 */
	@Override
	public void setProxyHost(String host) {
		if(host == null || host.trim().equals("")) { //$NON-NLS-1$
			this.emblem = null;
		} else {
			this.emblem = host;
		}
	}
	
	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getProxyHost()
	 */
	@Override
	public String getProxyHost() {
		return this.proxyHost;
	}

	/**
	 * Sets the proxy port
	 * 
	 * @param port the proxy port
	 */
	@Override
	public void setProxyPort(int port) {
		this.proxyPort = port;
	}
	
	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getProxyPort()
	 */
	@Override
	public int getProxyPort() {
		return this.proxyPort;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigManipulator#setConfigurationFile(java.lang.String)
	 */
	@Override
	public void setConfigurationFile(String configurationFile) {
		this.configurationFile = configurationFile;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigProvider#getConfigurationFile()
	 */
	@Override
	public String getConfigurationFile() {
		return this.configurationFile;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigManipulator#saveCurrentConfiguration()
	 */
	@Override
	public void saveCurrentConfiguration() throws IOException{
		String filename = this.getConfigurationFile();
		
		File configFile = new File(filename);
		
		Properties props = new Properties();
		
		props.setProperty(BKU_CONFIG, this.getDefaultBKU().toString());
		props.setProperty(PROXY_HOST_CONFIG, this.getProxyHost());
		props.setProperty(PROXY_PORT_CONFIG, Integer.toString(this.getProxyPort()));
		props.setProperty(EMBLEM_CONFIG, this.getDefaultEmblem());
		props.setProperty(MOBILE_NUMBER_CONFIG, this.getDefaultMobileNumber());
		
		SignaturePosition pos = this.getDefaultSignaturePosition();
		
		if(pos == null) {
			props.setProperty(SIGNATURE_POSITION_CONFIG, ""); //$NON-NLS-1$
		} else if(pos.useAutoPositioning()) {
			props.setProperty(SIGNATURE_POSITION_CONFIG, "auto"); //$NON-NLS-1$
		} else {
			props.setProperty(SIGNATURE_POSITION_CONFIG, 
					String.format("x=%f;y=%f;p=%d",  //$NON-NLS-1$
							pos.getX(),
							pos.getY(),
							pos.getPage()));
		}
		
		FileOutputStream outputstream = new FileOutputStream(configFile, false);
		
		props.store(outputstream, "Configuration file was generated!"); //$NON-NLS-1$
		
	}
	
}
