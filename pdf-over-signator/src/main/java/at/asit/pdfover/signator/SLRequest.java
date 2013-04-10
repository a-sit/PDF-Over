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

import org.apache.commons.codec.binary.Base64;

/**
 * Security Layer Request
 */
public class SLRequest {

	/**
	 * The String constant to replace the SL DATAOBJECT
	 */
	public static final String DATAOBJECT_STRING = "##DATAOBJECT##";

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
	 * The data object should contain the placeholder DATAOBJECT_STRING
	 * @param request the request to set
	 */
	protected void setRequest(String request) {
		this.request = request;
	}

	/**
	 * The SL request String with the document encoded in base64
	 * 
	 * This SL Request is always a detached signature request which contains a
	 * reference to the data object in base64 encoding.
	 * 
	 * @return SL request String
	 */
	public String getBase64Request() {
		String b64content = new String(Base64.encodeBase64(getSignatureData().getByteArray()));

		String b64request = this.request.replace(
						DATAOBJECT_STRING,
						"<sl:Base64Content>" + b64content //$NON-NLS-1$
								+ "</sl:Base64Content>"); //$NON-NLS-1$

		return b64request;
	}

	/**
	 * The SL request String with the document referenced as an URI
	 *
	 * This SL Request is always a detached signature request which contains a
	 * reference to the data object as an URI
	 * The URI has to be provided and should be a valid reference to
	 * the document provided by getSignatureData().
	 *
	 * @param uri The URI pointing to the signature data 
	 * @return SL request String
	 */
	public String getURIRequest(String uri) {
		String urirequest = this.request.replace(
				DATAOBJECT_STRING,
				"<sl:LocRefContent>" + uri //$NON-NLS-1$
						+ "</sl:LocRefContent>"); //$NON-NLS-1$

		return urirequest;
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
}
