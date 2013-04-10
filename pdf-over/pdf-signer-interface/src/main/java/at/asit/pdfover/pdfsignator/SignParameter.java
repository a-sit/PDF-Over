package at.asit.pdfover.pdfsignator;

/**
 * The Signature Parameter
 */
public class SignParameter {
	
	/** 
	 * The Signature Position
	 * @uml.property name="signaturePosition"
	 * @uml.associationEnd multiplicity="(1 1)" aggregation="shared" inverse="signParameter:at.asit.pdfover.pdfsignator.SignaturePosition"
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
	 * The Signature Profile
	 * @uml.property  name="signatureProfile"
	 * @uml.associationEnd  multiplicity="(1 1)" aggregation="shared" inverse="signParameter:at.asit.pdfover.pdfsignator.SignatureProfile"
	 */
	protected SignatureProfile signatureProfile = null;
	
	/**
	 * Getter of the property <tt>signatureProfile</tt>
	 * @return  Returns the signatureProfile.
	 */
	public SignatureProfile GetSignatureProfile() {
		return signatureProfile;
	}
	
	/**
	 * Setter of the property <tt>signatureProfile</tt>
	 * @param signatureProfile  The signatureProfile to set.
	 */
	public void SetSignatureProfile(SignatureProfile signatureProfile) {
		this.signatureProfile = signatureProfile;
	}
	
	/**
	 * The signature Device
	 */
	protected String signatureDevice = null;
	
	/**
	 * Getter of the property <tt>signatureDevice</tt>
	 * @return  Returns the signatureDevice.
	 */
	public String GetSignatureDevice() {
		return signatureDevice;
	}
	
	/**
	 * Setter of the property <tt>signatureDevice</tt>
	 * @param value  The signatureDevice to set.
	 */
	public void SetSignatureDevice(String value) {
		this.signatureDevice = value;
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
	 * 
	 * @uml.associationEnd  multiplicity="(1 1)" aggregation="shared" inverse="signParameter:at.asit.pdfover.pdfsignator.DocumentSource"
	 */
	protected DocumentSource documentSource = null;
	
	/**
	 * Getter of the property <tt>documentSource</tt>
	 * @return  Returns the documentSource.
	 */
	public DocumentSource GetDocumentSource() {
		return documentSource;
	}
	
	/**
	 * Setter of the property <tt>documentSource</tt>
	 * @param value  The documentSource to set.
	 */
	public void SetDocumentSource(DocumentSource value) {
		this.documentSource = value;
	}
}
