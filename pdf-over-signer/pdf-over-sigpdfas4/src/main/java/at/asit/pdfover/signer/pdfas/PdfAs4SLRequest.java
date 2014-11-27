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
import at.asit.pdfover.signator.SLRequest;
import at.asit.pdfover.signer.pdfas.exceptions.PdfAs4SLRequestException;

/**
 * PDF - AS Security Layer Request implementation
 */
public class PdfAs4SLRequest extends SLRequest {

	/**
	 * SLF4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(PdfAs4SLRequest.class);

	/**
	 * Default constructor
	 * @param slRequest
	 * @param signData 
	 * @throws PdfAs4SLRequestException 
	 */
	public PdfAs4SLRequest(String slRequest, byte[] signData) throws PdfAs4SLRequestException {
//		if(!slRequest.contains(PdfAs4Signer.LOC_REF)) {
//			log.error("PDF-AS SL request doesn't contain " + PdfAs4Signer.LOC_REF);
//			log.debug("Request: " + slRequest);
//			throw new PdfAs4SLRequestException("PDF-AS SL request doesn't contain " + PdfAs4Signer.LOC_REF);
//		}
//
//		// Modifing SL Request ...
//		setRequest(slRequest.replace(PdfAs4Signer.LOC_REF, SLRequest.DATAOBJECT_STRING));

		setRequest(slRequest);
		setSignatureData(signData == null ? null : new ByteArrayDocumentSource(signData));
	}
}
