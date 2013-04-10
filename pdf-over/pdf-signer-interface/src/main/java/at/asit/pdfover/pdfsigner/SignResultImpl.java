package at.asit.pdfover.pdfsigner;

import java.security.cert.X509Certificate;

public class SignResultImpl implements SignResult {

	private SignaturePosition position;
	private DocumentSource source;
	private X509Certificate certificate;
	
	@Override
	public SignaturePosition GetSignaturePosition() {
		return position;
	}

	@Override
	public DocumentSource GetSignedDocument() {
		return source;
	}

	@Override
	public X509Certificate GetSignerCertificate() {
		return certificate;
	}

	public void SetSignerCertificate(X509Certificate x509Certificate) {
		this.certificate = x509Certificate;
	}
	
	public void SetSignaturePosition(SignaturePosition postion) {
		this.position = postion;
	}

	public void SetSignedDocument(DocumentSource source) {
		this.source = source;
	}
}
