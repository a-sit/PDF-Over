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
 * Security Layer Request
 */
public class SLRequest {

	/**
	 * The security layer request
	 */
	private String request;

	/**
	 * The document to be signed
	 */
	private DocumentSource signatureData;

	/**
	 * Set the SL request
	 * @param request the request to set
	 */
	protected void setRequest(String request) {
		this.request = request;
	}

	/**
	 * Set the signature data (document to be signed)
	 * @param signatureData the signatureData to set
	 */
	protected void setSignatureData(DocumentSource signatureData) {
		this.signatureData = signatureData;
	}

	/**
	 * Gets the signature data for this request
	 * 
	 * @return The document source
	 */
	public DocumentSource getSignatureData()
	{
		return this.signatureData;
	}

	/**
	 * Gets the request String
	 * 
	 * @return the request
	 */
	public String getRequest() {
		return this.request;
	}
}
