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
import java.io.FileNotFoundException;
import java.util.Locale;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.exceptions.InvalidEmblemFile;
import at.asit.pdfover.gui.exceptions.InvalidNumberException;
import at.asit.pdfover.gui.exceptions.InvalidPortException;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.states.mobilebku.ATrustHelper;
import at.asit.pdfover.signator.BKUs;

/**
 * 
 */
public class ConfigurationContainerImpl implements ConfigurationContainer {
	/**
	 * SLF4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(ConfigurationContainerImpl.class);


	/**
	 * the emblem File
	 */
	protected String emblemFile = null;

	/**
	 * The mobile phone number 
	 */
	protected String mobileNumber = null;
	
	/**
	 * Holds the proxy Host 
	 */
	protected String proxyHost = null;
	
	/**
	 * Holds the signatureNote
	 */
	protected String signatureNote = null;
	
	/**
	 * Holds the proxy port number
	 */
	protected int proxyPort = -1;
	
	/**
	 * Holds the locale
	 */
	protected Locale locale = null;
	
	/**
	 * Holds the locale
	 */
	protected Locale signLocale = null;
	
	/**
	 * Holds the output folder
	 */
	protected String folder = null;
	
	/**
	 * Holds the default BKU to use
	 */
	protected BKUs defaulBKU = BKUs.NONE;
	
	/**
	 * Holds the automatic positioning value
	 */
	protected boolean automaticPositioning = false;

	/**
	 * Holds the transparency of the signature placeholder
	 */
	protected int placeholderTransparency = 170;

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.ConfigurationContainer#getEmblem()
	 */
	@Override
	public String getEmblem() {
		return this.emblemFile;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.ConfigurationContainer#setEmblem(java.lang.String)
	 */
	@Override
	public void setEmblem(String emblemFile) throws InvalidEmblemFile {
		if (emblemFile == null || emblemFile.trim().equals("")) { //$NON-NLS-1$
			// Ok to set no file ...
		} else {
			File imageFile = new File(emblemFile);
			if (!imageFile.exists()) {
				throw new InvalidEmblemFile(imageFile,
						new FileNotFoundException(emblemFile));
			}

			try {
				Image img = new Image(Display.getDefault(), new ImageData(
						emblemFile));

				img.dispose();
			} catch (Exception ex) {
				throw new InvalidEmblemFile(imageFile, ex);
			}
		}

		this.emblemFile = emblemFile;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.ConfigurationContainer#getNumber()
	 */
	@Override
	public String getNumber() {
		return this.mobileNumber;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.ConfigurationContainer#setNumber(java.lang.String)
	 */
	@Override
	public void setNumber(String number) throws InvalidNumberException {
		if(number == null || number.trim().equals("")) { //$NON-NLS-1$
			this.mobileNumber = null;
			return;
		}
		try {
			this.mobileNumber = ATrustHelper.normalizeMobileNumber(number);
		} catch (InvalidNumberException e) {
			throw new InvalidNumberException(Messages.getString("error.InvalidPhoneNumber")); //$NON-NLS-1$
		}
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.ConfigurationContainer#getProxyHost()
	 */
	@Override
	public String getProxyHost() {
		return this.proxyHost;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.ConfigurationContainer#setProxyHost(java.lang.String)
	 */
	@Override
	public void setProxyHost(String host) {
		this.proxyHost = host;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.ConfigurationContainer#getProxyPort()
	 */
	@Override
	public int getProxyPort() {
		return this.proxyPort;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.ConfigurationContainer#setProxyPort(int)
	 */
	@Override
	public void setProxyPort(int port) throws InvalidPortException {
		if(port > 0 && port <= 0xFFFF) {
			this.proxyPort = port;
			return;
		}
		if(port == -1) {
			this.proxyPort = -1;
			return;
		}
		throw new InvalidPortException(port);
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.ConfigurationContainer#getAutomaticPosition()
	 */
	@Override
	public boolean getAutomaticPosition() {
		return this.automaticPositioning;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.ConfigurationContainer#setAutomaticPosition(boolean)
	 */
	@Override
	public void setAutomaticPosition(boolean automatic) {
		this.automaticPositioning = automatic;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigurationContainer#getPlaceholderTransparency()
	 */
	@Override
	public int getPlaceholderTransparency() {
		return this.placeholderTransparency;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigurationContainer#setPlaceholderTransparency(int)
	 */
	@Override
	public void setPlaceholderTransparency(int transparency) {
		this.placeholderTransparency = transparency;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.ConfigurationContainer#getBKUSelection()
	 */
	@Override
	public BKUs getBKUSelection() {
		return this.defaulBKU;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.ConfigurationContainer#setBKUSelection(at.asit.pdfover.signator.BKUs)
	 */
	@Override
	public void setBKUSelection(BKUs bkuSelection) {
		this.defaulBKU = bkuSelection;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.ConfigurationContainer#getOutputFolder()
	 */
	@Override
	public String getOutputFolder() {
		return this.folder;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.ConfigurationContainer#setOutputFolder(java.lang.String)
	 */
	@Override
	public void setOutputFolder(String folder) {
		this.folder = folder;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigurationContainer#getSignatureNote()
	 */
	@Override
	public String getSignatureNote() {
		return this.signatureNote;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigurationContainer#setSignatureNote(java.lang.String)
	 */
	@Override
	public void setSignatureNote(String note) {
		this.signatureNote = note;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigurationContainer#setLocale(java.util.Locale)
	 */
	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigurationContainer#getLocale()
	 */
	@Override
	public Locale getLocale() {
		return this.locale;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigurationContainer#setSignLocale(java.util.Locale)
	 */
	@Override
	public void setSignLocale(Locale locale) {
		this.signLocale = locale;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.ConfigurationContainer#getSignLocale()
	 */
	@Override
	public Locale getSignLocale() {
		return this.signLocale;
	}
	
}
