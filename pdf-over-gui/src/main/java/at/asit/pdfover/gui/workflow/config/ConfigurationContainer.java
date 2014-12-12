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

import java.util.Locale;

import org.eclipse.swt.graphics.Point;

import at.asit.pdfover.gui.bku.mobile.MobileBKUs;
import at.asit.pdfover.gui.exceptions.InvalidEmblemFile;
import at.asit.pdfover.gui.exceptions.InvalidNumberException;
import at.asit.pdfover.gui.exceptions.InvalidPortException;
import at.asit.pdfover.signator.BKUs;
import at.asit.pdfover.signator.SignaturePosition;

/**
 * Configuration container
 */
public interface ConfigurationContainer {
	/**
	 * Gets the configured emblem
	 * @return the configured emblem
	 */
	public String getEmblem();

	/**
	 * Sets the emblem
	 * @param emblem the emblem
	 * @throws InvalidEmblemFile
	 */
	public void setEmblem(String emblem) throws InvalidEmblemFile;

	/**
	 * Gets the mobile phone number
	 * @return the mobile phone number
	 */
	public String getMobileNumber();

	/**
	 * Sets the mobile phone number 
	 * @param number the mobile phone number
	 * @throws InvalidNumberException
	 */
	public void setMobileNumber(String number) throws InvalidNumberException;

	/**
	 * Gets the mobile phone number
	 * @return the mobile phone number
	 */
	public String getMobilePassword();

	/**
	 * Sets the mobile phone password 
	 * @param password the mobile phone password
	 */
	public void setMobilePassword(String password);

	/**
	 * Gets the proxy host
	 * @return the proxy host
	 */
	public String getProxyHost();

	/**
	 * Sets the proxy host
	 * @param host the proxy host
	 */
	public void setProxyHost(String host);

	/**
	 * Gets the proxy port
	 * 
	 * if port is -1 no port is selected
	 * 
	 * @return the proxy port
	 */
	public int getProxyPort();

	/**
	 * Sets the proxy port
	 * 
	 * set to -1 for no port
	 * 
	 * @param port the proxy port
	 * @throws InvalidPortException
	 */
	public void setProxyPort(int port) throws InvalidPortException;

	/**
	 * Gets the proxy username
	 * @return the proxy username
	 */
	public String getProxyUser();

	/**
	 * Sets the proxy username
	 * @param user the proxy username
	 */
	public void setProxyUser(String user);

	/**
	 * Gets the proxy password
	 * @return the proxy password
	 */
	public String getProxyPass();

	/**
	 * Sets the proxy password
	 * @param pass the proxy password
	 */
	public void setProxyPass(String pass);

	/**
	 * Gets the signature note
	 * @return the signature note
	 */
	public String getSignatureNote();

	/**
	 * Sets the signature note
	 * @param note the signature note
	 */
	public void setSignatureNote(String note);

	/**
	 * Gets the transparency of the placeholder
	 * @return transparency of the placeholder (0-255)
	 */
	public int getPlaceholderTransparency();

	/**
	 * Sets the transparency of the placeholder
	 * @param transparency transparency of the placeholder (0-255)
	 */
	public void setPlaceholderTransparency(int transparency);

	/**
	 * Gets the default BKU
	 * @return the default BKU
	 */
	public BKUs getDefaultBKU();

	/**
	 * Sets the default BKU 
	 * @param defaultBKU the default BKU
	 */
	public void setDefaultBKU(BKUs defaultBKU);

	/**
	 * Gets the default output folder
	 * @return the default output folder
	 */
	public String getOutputFolder();

	/**
	 * Sets the default output folder
	 * @param folder the default output folder
	 */
	public void setOutputFolder(String folder);

	/**
	 * Gets the locale
	 * @return the locale
	 */
	public Locale getLocale();

	/**
	 * Sets the locale
	 * @param locale the locale
	 */
	public void setLocale(Locale locale);

	/**
	 * Gets the signature locale
	 * @return the signature locale
	 */
	public Locale getSignatureLocale();

	/**
	 * Sets the signature locale
	 * @param locale the signature locale
	 */
	public void setSignatureLocale(Locale locale);

	/**
	 * Gets the signature PDF/A compatibility setting
	 * @return the signature PDF/A compatibility setting
	 */
	public boolean getSignaturePdfACompat();

	/**
	 * Sets the signature PDF/A compatibility setting
	 * @param compat the signature PDF/A compatibility setting
	 */
	public void setSignaturePdfACompat(boolean compat);

	/**
	 * Gets the mobile BKU URL
	 * @return the mobile BKU URL
	 */
	public String getMobileBKUURL();

	/**
	 * Sets the mobile BKU URL
	 * @param bkuUrl the mobile BKU URL
	 */
	public void setMobileBKUURL(String bkuUrl);

	/**
	 * Gets the mobile BKU type
	 * @return the mobile BKU type
	 */
	public MobileBKUs getMobileBKUType();

	/**
	 * Sets the mobile BKU type
	 * @param bkuType the mobile BKU type
	 */
	public void setMobileBKUType(MobileBKUs bkuType);

	/**
	 * Gets the default signature position
	 * @return the default signature position
	 */
	public SignaturePosition getDefaultSignaturePosition();

	/**
	 * Gets the default signature position
	 * @param signaturePosition the default signature position
	 */
	public void setDefaultSignaturePosition(SignaturePosition signaturePosition);

	/**
	 * Gets whether to automatically check for application updates
	 * @return whether to automatically check for application updates
	 */
	public boolean getUpdateCheck();

	/**
	 * Sets whether to automatically check for application updates
	 * @param checkUpdate whether to automatically check for application updates
	 */
	public void setUpdateCheck(boolean checkUpdate);

	/**
	 * Gets the main window size
	 * @return the main window size
	 */
	public Point getMainWindowSize();

	/**
	 * Sets the main window size
	 * @param size the main window size
	 */
	public void setMainWindowSize(Point size);

	/**
	 * Gets whether to skip the finish screen
	 * @return whether to skip the finish screen
	 */
	public boolean getSkipFinish();

	/**
	 * Sets whether to skip the finish screen
	 * @param skipFinish whether to skip the finish screen
	 */
	public void setSkipFinish(boolean skipFinish);
}
