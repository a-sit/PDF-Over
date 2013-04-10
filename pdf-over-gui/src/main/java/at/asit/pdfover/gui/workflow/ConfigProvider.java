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

import java.io.IOException;
import java.io.InputStream;

import at.asit.pdfover.signator.BKUs;
import at.asit.pdfover.signator.SignaturePosition;

/**
 * 
 */
public interface ConfigProvider {
	/**
	 * Regex for parsing signature position
	 */
	public static final String SIGN_POS_REGEX = "(x=(\\d\\.?\\d?);y=(\\d\\.?\\d?);p=(\\d))|(auto)|(x=(\\d\\.?\\d?);y=(\\d\\.?\\d?))"; //$NON-NLS-1$

	
	/**
	 * Gets the configuration file
	 * @return the configuration file
	 */
	public String getConfigurationFile();
	
	/**
	 * Gets the default Mobile number
	 * @return the default mobile number
	 */
	public String getDefaultMobileNumber();
	
	/**
	 * Gets the password to use for Mobile BKU
	 * @return the password
	 */
	public String getDefaultPassword();
	
	/**
	 * Gets the filename of the default emblem
	 * @return the emblem
	 */
	public String getDefaultEmblem();
	
	/**
	 * Gets the proxy host
	 * @return the proxy hostname or ip address
	 */
	public String getProxyHost();
	
	/**
	 * Gets the proxy port
	 * @return the proxy port
	 */
	public int getProxyPort();
	
	/**
	 * Get the default configured BKU
	 * @return the default configured BKU
	 */
	public BKUs getDefaultBKU();
	
	/**
	 * Get the default configured SignaturePosition
	 * @return the default configured SignaturePosition or null if not configured
	 */
	public SignaturePosition getDefaultSignaturePosition();
	
	/**
	 * Gets the default output folder for signed documents
	 * @return the default output folder 
	 */
	public String getDefaultOutputFolder();
	
	/**
	 * Loads the current configuration to the current configuration file
	 * @param configSource 
	 * @throws IOException 
	 */
	public void loadConfiguration(InputStream configSource) throws IOException;
}
