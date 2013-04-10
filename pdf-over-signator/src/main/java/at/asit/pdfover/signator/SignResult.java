package at.asit.pdfover.signator;

import java.security.cert.X509Certificate;

/**
 * Signature Result containing the signed document as document source
 */
public interface SignResult {

	/** 
	 * Getter of the property <tt>signaturePosition</tt>
	 * @return  Returns the signaturePosition.
	 */
	public SignaturePosition getSignaturePosition();
	
	/**
	 * Gets the signed Document
	 * @return  Returns the documentSource.
	 */
	public DocumentSource getSignedDocument();

	/**
	 * Gets the signer certificate
	 * @return The signer x509 certificate
	 */
	public X509Certificate getSignerCertificate();
}
