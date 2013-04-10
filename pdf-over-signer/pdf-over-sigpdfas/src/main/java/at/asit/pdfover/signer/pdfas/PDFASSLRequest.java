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
package at.asit.pdfover.signer.pdfas;

// Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.signator.ByteArrayDocumentSource;
import at.asit.pdfover.signator.DocumentSource;
import at.asit.pdfover.signator.SLRequest;

/**
 * PDF - AS Security Layer Request implementation
 */
public class PDFASSLRequest implements SLRequest {

	/**
	 * SFL4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(PDFASSLRequest.class);
	
	private String request;
	
	private ByteArrayDocumentSource source;
	
	/**
	 * Default constructor
	 * @param slRequest
	 * @param signData 
	 */
	public PDFASSLRequest(String slRequest, byte[] signData) {
		// Modifing SL Request ...
		this.request = slRequest.replace(PDFASSigner.LOC_REF, SLRequest.DATAOBJECT_STRING);
		
		if(!this.request.contains(DATAOBJECT_STRING)) {
			// TODO: throw Exception (Failed to prepare SL Request)
		}
		
		this.source = new ByteArrayDocumentSource(signData);
	}
	
	@Override
	public DocumentSource getSignatureData() {
		return this.source;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.SLRequest#getRequest()
	 */
	@Override
	public String getRequest() {
		return this.request;
	}

}
