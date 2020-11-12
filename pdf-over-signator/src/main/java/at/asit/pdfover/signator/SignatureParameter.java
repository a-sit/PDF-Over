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
package at.asit.pdfover.signator;

import java.awt.Image;

//Imports

/**
 * The Signature Parameter
 */
public abstract class SignatureParameter {

	/** The Signature Position */
	protected SignaturePosition signaturePosition = null;

	/** The Signature language */
	protected String signatureLanguage = null;

	/** The key identifier */
	protected String keyIdentifier = null;

	/** The input document */
	protected DocumentSource documentSource = null;

	/** Holds the emblem */
	protected Emblem emblem;

	/** Whether to use PDF/A compatibility */
	protected boolean pdfACompat;

	/** The signature device */
	protected BKUs signatureDevice;

	/** Whether so look for placeholder signatures or not. */
	protected boolean searchForPlaceholderSignatures = false;

	/**
	 * @return the searchForPlaceholderSignatures
	 */
	public boolean isSearchForPlaceholderSignatures() {
		return this.searchForPlaceholderSignatures;
	}

	/**
	 * @param value
	 *            the searchForPlaceholderSignatures to set
	 */
	public void setSearchForPlaceholderSignatures(boolean value) {
		this.searchForPlaceholderSignatures = value;
	}

	/**
	 * @return the signatureDevice
	 */
	public BKUs getSignatureDevice() {
		return this.signatureDevice;
	}

	/**
	 * @param signatureDevice
	 *            the signatureDevice to set
	 */
	public void setSignatureDevice(BKUs signatureDevice) {
		this.signatureDevice = signatureDevice;
	}

	/**
	 * Getter of the property <tt>signaturePosition</tt>
	 * 
	 * @return Returns the signaturePosition.
	 */
	public SignaturePosition getSignaturePosition() {
		return this.signaturePosition;
	}

	/**
	 * Setter of the property <tt>signaturePosition</tt>
	 * 
	 * @param signaturePosition
	 *            The signaturePosition to set.
	 */
	public void setSignaturePosition(SignaturePosition signaturePosition) {
		this.signaturePosition = signaturePosition;
	}

	/**
	 * Getter of the property <tt>signatureLanguage</tt>
	 * 
	 * @return Returns the signatureLanguage.
	 */
	public String getSignatureLanguage() {
		return this.signatureLanguage;
	}

	/**
	 * Setter of the property <tt>signatureLanguage</tt>
	 * 
	 * @param signatureLanguage
	 *            The signatureLanguage to set.
	 */
	public void setSignatureLanguage(String signatureLanguage) {
		this.signatureLanguage = signatureLanguage;
	}

	/**
	 * Getter of the property <tt>signaturePdfACompat</tt>
	 * 
	 * @return Returns the PDF/A compatibility setting.
	 */
	public boolean getSignaturePdfACompat() {
		return this.pdfACompat;
	}

	/**
	 * Setter of the property <tt>signaturePdfACompat</tt>
	 * 
	 * @param compat
	 *            The the PDF/A compatibility setting to set.
	 */
	public void setSignaturePdfACompat(boolean compat) {
		this.pdfACompat = compat;
	}

	/**
	 * Getter of the property <tt>keyIdentifier</tt>
	 * 
	 * @return Returns the keyIdentifier.
	 */
	public String getKeyIdentifier() {
		return this.keyIdentifier;
	}

	/**
	 * Setter of the property <tt>keyIdentifier</tt>
	 * 
	 * @param keyIdentifier
	 *            The keyIdentifier to set.
	 */
	public void setKeyIdentifier(String keyIdentifier) {
		this.keyIdentifier = keyIdentifier;
	}

	/**
	 * Getter of the property <tt>documentSource</tt>
	 * 
	 * @return Returns the documentSource.
	 */
	public DocumentSource getInputDocument() {
		return this.documentSource;
	}

	/**
	 * Setter of the property <tt>documentSource</tt>
	 * 
	 * @param inputDocument
	 *            The documentSource to set.
	 */
	public void setInputDocument(DocumentSource inputDocument) {
		this.documentSource = inputDocument;
	}

	/**
	 * Gets the Dimension to display the Placeholder
	 * 
	 * @return the placeholder dimensions
	 */
	public abstract SignatureDimension getPlaceholderDimension();

	/**
	 * Gets the Placeholder image
	 * 
	 * @return the placeholder image
	 */
	public abstract Image getPlaceholder();

	/**
	 * Gets the Emblem
	 * 
	 * @return the Emblem
	 */
	public Emblem getEmblem() {
		return this.emblem;
	}

	/**
	 * Sets the Emblem
	 * 
	 * @param emblem
	 *            The new Emblem
	 */
	public void setEmblem(Emblem emblem) {
		this.emblem = emblem;
	}

	/**
	 * Sets generic properties
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void setProperty(String key, String value);

	/**
	 * Gets generic properties
	 * 
	 * @param key
	 * @return associated value
	 */
	public abstract String getProperty(String key);


	public abstract void setSignatureProfile(String profile);

	public abstract String getSignatureProfile();
}
