package at.asit.pdfover.pdfsignator;

import javax.security.cert.Certificate;

public class SignResult {

	/** 
	 * The position of the signatur
	 * @uml.associationEnd multiplicity="(1 1)" aggregation="composite" inverse="signResult:at.asit.pdfover.pdfsignator.SignaturePosition"
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
	 * The signed Document
	 * @uml.associationEnd  multiplicity="(1 1)" aggregation="shared" inverse="signResult:at.asit.pdfover.pdfsignator.DocumentSource"
	 */
	protected DocumentSource documentSource = null;

	/**
	 * Gets the signed Document
	 * @return  Returns the documentSource.
	 */
	public DocumentSource GetDocumentSource() {
		return documentSource;
	}

	/**
	 * Setter of the property <tt>documentSource</tt>
	 * @param documentSource  The documentSource to set.
	 */
	public void SetDocumentSource(DocumentSource documentSource) {
		this.documentSource = documentSource;
	}
	
	/**
	 * The signer certificate
	 */
	protected Certificate signerCertificate;
	
	/**
	 * Sets the signer certificate
	 * @param cert The signer certificate
	 */
	public void SetSignerCertificate(Certificate cert) {
		this.signerCertificate = cert;
	}
	
	/**
	 * Gets the signer certificate
	 * @return The signer x509 certificate
	 */
	public Certificate SetSignerCertificate() {
		return this.signerCertificate;
	}
}
