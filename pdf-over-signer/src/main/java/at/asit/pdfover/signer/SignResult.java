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
package at.asit.pdfover.signer;

//Imports
import java.security.cert.X509Certificate;

/**
 * The result of a signature operation
 */
public class SignResult {

	private SignaturePosition position;
	private DocumentSource source;
	private X509Certificate certificate;

	public SignaturePosition getSignaturePosition() {
		return this.position;
	}

	public DocumentSource getSignedDocument() {
		return this.source;
	}

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
