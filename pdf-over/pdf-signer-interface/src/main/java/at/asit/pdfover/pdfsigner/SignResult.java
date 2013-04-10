package at.asit.pdfover.pdfsigner;

import javax.security.cert.Certificate;

/**
 * Signature Result containing the signed document as document source
 * @author afitzek
 */
public interface SignResult {

	/** 
	 * Getter of the property <tt>signaturePosition</tt>
	 * @return  Returns the signaturePosition.
	 */
	public SignaturePosition GetSignaturePosition();
	
	/**
	 * Gets the signed Document
	 * @return  Returns the documentSource.
	 */
	public DocumentSource GetSignedDocument();

	/**
	 * Gets the signer certificate
	 * @return The signer x509 certificate
	 */
	public Certificate GetSignerCertificate();
}
