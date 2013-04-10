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

import java.util.Locale;

import org.eclipse.swt.graphics.Point;

import at.asit.pdfover.signator.BKUs;
import at.asit.pdfover.signator.SignaturePosition;

/**
 * An interface for reading the persistent configuration
 * 
 * This reads the configuration that will be saved
 */
public interface PersistentConfigProvider {
	/**
	 * Get the default configured BKU
	 * @return the default configured BKU
	 */
	public BKUs getDefaultBKUPersistent();

	/**
	 * Gets the default Mobile number
	 * @return the default mobile number
	 */
	public String getDefaultMobileNumberPersistent();

	/**
	 * Gets the password to use for Mobile BKU
	 * @return the password
	 */
	public String getDefaultMobilePasswordPersistent();

	/**
	 * Gets the filename of the default emblem
	 * @return the emblem
	 */
	public String getDefaultEmblemPersistent();
	
	/**
	 * Gets the proxy host
	 * @return the proxy hostname or ip address
	 */
	public String getProxyHostPersistent();

	/**
	 * Gets the proxy port
	 * @return the proxy port
	 */
	public int getProxyPortPersistent();

	/**
	 * Gets the default output folder for signed documents
	 * @return the default output folder 
	 */
	public String getDefaultOutputFolderPersistent();

	// Declare the other configuration getters for convenience

	/**
	 * Get the default configured SignaturePosition
	 * @return the default configured SignaturePosition or null if not configured
	 */
	public SignaturePosition getDefaultSignaturePosition();

	/**
	 * Get the transparency of the signature placeholder
	 * @return the transparency of the signature placeholder
	 */
	public int getPlaceholderTransparency();

	/**
	 * Gets the mobile BKU URL
	 * @return the mobile BKU URL
	 */
	public String getMobileBKUURL();

	/**
	 * Get the signature note text to use
	 * @return the signature note text
	 */
	public String getSignatureNote();

	/**
	 * Gets the configured locale
	 * @return the configured locale
	 */
	public Locale getConfigLocale();

	/**
	 * Gets the configured locale
	 * @return the configured locale
	 */
	public Locale getSignLocale();

	/**
	 * Gets the configured MainWindow size
	 * @return the configured MainWindow size
	 */
	public Point getMainWindowSize();
}