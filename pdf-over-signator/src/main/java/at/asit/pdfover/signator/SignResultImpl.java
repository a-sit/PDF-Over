package at.asit.pdfover.signator;

import java.security.cert.X509Certificate;

/**
 * The result of a signature operation
 */
public class SignResultImpl implements SignResult {

	private SignaturePosition position;
	private DocumentSource source;
	private X509Certificate certificate;

	@Override
	public SignaturePosition getSignaturePosition() {
		return this.position;
	}

	@Override
	public DocumentSource getSignedDocument() {
		return this.source;
	}

	@Override
	public X509Certificate getSignerCertificate() {
		return this.certificate;
	}

	/**
	 * Set the signer certificate
	 * @param x509Certificate the signer certificate
	 */
	public void setSignerCertificate(X509Certificate x509Certificate) {
		this.certificate = x509Certificate;
	}
	
	/**
	 * Set the signature position
	 * @param postion the signature position
	 */
	public void setSignaturePosition(SignaturePosition postion) {
		this.position = postion;
	}

	/**
	 * Set the signed document
	 * @param source DocumentSource containing the signed document
	 */
	public void setSignedDocument(DocumentSource source) {
		this.source = source;
	}
}
