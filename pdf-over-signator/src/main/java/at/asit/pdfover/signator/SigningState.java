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

/**
 * The state of the pdf signing library
 */
public interface SigningState {
	
	/**
	 * Gets the Security Layer Request to create the signature
	 * @return The Signature Request
	 */
	public SLRequest getSignatureRequest();

	/**
	 * Sets whether to use base64 (or FileUpload) for request data
	 * @param useBase64Request whether to use base64 for request data
	 */
	public void setUseBase64Request(boolean useBase64Request);

	/**
	 * Sets the Security Layer Response to the Signature Request
	 * @param value The Signature Response
	 */
	public void setSignatureResponse(SLResponse value);

	/**
	 * Has the state a SignatureResponse set ?
	 * @return true if a SLResponse is set
	 */
	public boolean hasSignatureResponse();

	/**
	 * Set the BKU connector
	 * @param connector the BKU connector
	 */
	public void setBKUConnector(BkuSlConnector connector);

	/**
	 * Set the KeyStore signer
	 * @param file KeyStore filename
	 * @param alias KeyStore alias
	 * @param kspassword KeyStore password
	 * @param keypassword KeyStore private key password
	 * @param type KeyStore type
	 * @throws SignatureException
	 */
	public void setKSSigner(String file, String alias, String kspassword,
			String keypassword, String type) throws SignatureException;

}
