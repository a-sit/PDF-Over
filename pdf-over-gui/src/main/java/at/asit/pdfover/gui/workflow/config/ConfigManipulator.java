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

import java.io.IOException;
import java.util.Locale;

import org.eclipse.swt.graphics.Point;

import at.asit.pdfover.signator.BKUs;
import at.asit.pdfover.signator.SignaturePosition;

/**
 * An interface for setting the configuration
 */
public interface ConfigManipulator {
	/**
	 * Sets the default bku type
	 * @param bku the bku type
	 */
	public void setDefaultBKU(BKUs bku);

	/**
	 * Sets the default signature position
	 * @param signaturePosition the default signature position
	 */
	public void setDefaultSignaturePosition(SignaturePosition signaturePosition);

	/**
	 * Sets the signature placeholder transparency
	 * @param transparency the signature placeholder transparency
	 */
	void setPlaceholderTransparency(int transparency);

	/**
	 * Sets the default mobile number
	 * @param number the default mobile number
	 */
	public void setDefaultMobileNumber(String number);

	/**
	 * Sets the default password
	 * @param password the default password
	 */
	public void setDefaultMobilePassword(String password);

	/**
	 * Sets the default emblem
	 * @param emblem the default emblem
	 */
	public void setDefaultEmblem(String emblem);

	/**
	 * Sets the proxy host
	 * @param host the proxy host
	 */
	public void setProxyHost(String host);

	/**
	 * Sets the proxy port
	 * @param port the proxy port
	 */
	public void setProxyPort(int port);

	/**
	 * Sets the proxy username
	 * @param user the proxy username
	 */
	public void setProxyUser(String user);

	/**
	 * Sets the proxy password
	 * @param pass the proxy password
	 */
	public void setProxyPass(String pass);

	/**
	 * Sets the default output folder
	 * @param outputFolder the default output folder
	 */
	public void setDefaultOutputFolder(String outputFolder);

	/**
	 * Sets the signature note text
	 * @param note the signature note text
	 */
	public void setSignatureNote(String note);

	/**
	 * Sets the locale to be used
	 * @param locale the locale
	 */
	public void setLocale(Locale locale);

	/**
	 * Sets the signature locale to be used
	 * @param locale the signature locale
	 */
	public void setSignLocale(Locale locale);

	/**
	 * Sets the default main window size
	 * @param size a Point describing the size
	 */
	public void setMainWindowSize(Point size);

	/**
	 * Saves the current configuration to the current configuration file
	 * @throws IOException
	 */
	public void saveCurrentConfiguration() throws IOException;
}
