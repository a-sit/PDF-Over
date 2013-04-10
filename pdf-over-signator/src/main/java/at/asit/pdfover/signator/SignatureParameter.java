package at.asit.pdfover.signator;

/**
 * The Signature Parameter
 */
public abstract class SignatureParameter {

	/**
	 * The Signature Position
	 * @uml.property  name="signaturePosition"
	 * @uml.associationEnd  
	 */
	protected SignaturePosition signaturePosition = null;

	/**
	 * The signature Device
	 */
	protected String keyIdentifier = null;

	/**
	 * The input document
	 * @uml.property  name="documentSource"
	 * @uml.associationEnd  
	 */
	protected DocumentSource documentSource = null;

	/**
	 * holds the collimating mark
	 * @uml.property  name="collimark"
	 * @uml.associationEnd  
	 */
	protected Emblem emblem;

	/** 
	 * Getter of the property <tt>signaturePosition</tt>
	 * @return  Returns the signaturePosition.
	 */
	public SignaturePosition getSignaturePosition() {
		return this.signaturePosition;
	}
	
	/** 
	 * Setter of the property <tt>signaturePosition</tt>
	 * @param signaturePosition  The signaturePosition to set.
	 */
	public void setSignaturePosition(SignaturePosition signaturePosition) {
		this.signaturePosition = signaturePosition;
	}
	
	/**
	 * Getter of the property <tt>keyIdentifier</tt>
	 * @return  Returns the keyIdentifier.
	 */
	public String getKeyIdentifier() {
		return this.keyIdentifier;
	}
	
	/**
	 * Setter of the property <tt>keyIdentifier</tt>
	 * @param keyIdentifier  The keyIdentifier to set.
	 */
	public void setKeyIdentifier(String keyIdentifier) {
		this.keyIdentifier = keyIdentifier;
	}

	/**
	 * Getter of the property <tt>documentSource</tt>
	 * @return  Returns the documentSource.
	 */
	public DocumentSource getInputDocument() {
		return this.documentSource;
	}
	
	/**
	 * Setter of the property <tt>documentSource</tt>
	 * @param inputDocument  The documentSource to set.
	 */
	public void setInputDocument(DocumentSource inputDocument) {
		this.documentSource = inputDocument;
	}
	
	/**
	 * Gets the Dimension to display the Placeholder
	 * @return the placeholder dimensions
	 */
	public abstract SignatureDimension getPlaceholderDimension();
	
	/**
	 * Gets the Emblem
	 * @return the Emblem
	 */
	public Emblem getEmblem() {
		return this.emblem;
	}

	/**
	 * Sets the Emblem
	 * @param emblem The new Emblem
	 */
	public void setEmblem(Emblem emblem) {
		this.emblem = emblem;
	}
	
	/**
	 * Sets generic properties
	 * @param key
	 * @param value
	 */
	public abstract void setProperty(String key, String value);
	
	/**
	 * Gets generic properties
	 * @param key 
	 * @return associated value
	 */
	public abstract String getProperty(String key);
}
