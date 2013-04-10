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
package at.asit.pdfover.gui.composites;

import at.asit.pdfover.gui.exceptions.InvalidEmblemFile;
import at.asit.pdfover.gui.exceptions.InvalidNumberException;
import at.asit.pdfover.gui.exceptions.InvalidPortException;
import at.asit.pdfover.signator.BKUs;

/**
 * 
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
	public String getNumber();
	
	/**
	 * Sets the mobile phone number 
	 * @param number the mobile phone number
	 * @throws InvalidNumberException
	 */
	public void setNumber(String number) throws InvalidNumberException;
	
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
	 * @return the proxy port
	 */
	public int getProxyPort();
	
	/**
	 * Sets the proxy port
	 * @param port the proxy port
	 * @throws InvalidPortException
	 */
	public void setProxyPort(int port) throws InvalidPortException;
	
	/**
	 * Gets the automatic position
	 * @return the automatic position
	 */
	public boolean getAutomaticPosition();
	
	/**
	 * Sets the automatic position
	 * @param automatic the automatic position
	 */
	public void setAutomaticPosition(boolean automatic);
	
	/**
	 * Gets the default BKU
	 * @return the default BKU
	 */
	public BKUs getBKUSelection();
	
	/**
	 * Sets the default BKU 
	 * @param bkuSelection the default BKU
	 */
	public void setBKUSelection(BKUs bkuSelection);
	
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
}
