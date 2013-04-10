package at.asit.pdfover.pdfsigner;

import java.util.HashMap;

/**
 * The Signature Parameter
 * @author  afitzek
 */
public abstract class SignatureParameter {
	
	/**
	 * The Signature Position
	 * @uml.property  name="signaturePosition"
	 * @uml.associationEnd  
	 */
	protected SignaturePosition signaturePosition = null;
	
	/** 
	 * Getter of the property <tt>signaturePosition</tt>
	 * @return  Returns the signaturePosition.
	 */
	public SignaturePosition GetSignaturePosition() {
		return signaturePosition;
	}
	
	/** 
	 * Setter of the property <tt>signaturePosition</tt>
	 * @param signaturePosition  The signaturePosition to set.
	 */
	public void SetSignaturePosition(SignaturePosition signaturePosition) {
		this.signaturePosition = signaturePosition;
	}
	
	/**
	 * The signature Device
	 */
	protected String KeyIdentifier = null;
	
	/**
	 * Getter of the property <tt>KeyIdentifier</tt>
	 * @return  Returns the KeyIdentifier.
	 */
	public String GetKeyIdentifier() {
		return KeyIdentifier;
	}
	
	/**
	 * Setter of the property <tt>KeyIdentifier</tt>
	 * @param value  The KeyIdentifier to set.
	 */
	public void SetKeyIdentifier(String value) {
		this.KeyIdentifier = value;
	}
	
	/**
	 * The signature Device
	 * @uml.property  name="documentSource"
	 * @uml.associationEnd  
	 */
	protected DocumentSource documentSource = null;
	
	/**
	 * Getter of the property <tt>documentSource</tt>
	 * @return  Returns the documentSource.
	 */
	public DocumentSource GetInputDocument() {
		return documentSource;
	}
	
	/**
	 * Setter of the property <tt>documentSource</tt>
	 * @param value  The documentSource to set.
	 */
	public void SetInputDocument(DocumentSource value) {
		this.documentSource = value;
	}
	
	/**
	 * Gets the Dimension to display the Placeholder
	 * @return the placeholder dimensions
	 */
	public abstract SignatureDimension GetPlaceholderDimension();
	
	/**
	 * holds the collimating mark
	 * @uml.property  name="collimark"
	 * @uml.associationEnd  
	 */
	protected CollimatingMark collimark;

	/**
	 * Gets the collimating mark
	 * @return
	 */
	public CollimatingMark GetCollimatingMark() {
		return collimark;
	}

	/**
	 * Sets the collimating mark
	 * @param value The new colimating mark
	 */
	public void SetCollimatingMark(CollimatingMark value) {
		this.collimark = value;
	}
	
	protected HashMap<String, String> _properties = new HashMap<String, String>();
	
	/**
	 * Sets generic properties
	 * @param key
	 * @param value
	 */
	public void SetProperty(String key, String value) {
		this._properties.put(key, value);
	}
	
	/**
	 * Gets generic properties
	 * @param key 
	 * @return
	 */
	public String GetProperty(String key) {
		return this._properties.get(key);
	}
}
