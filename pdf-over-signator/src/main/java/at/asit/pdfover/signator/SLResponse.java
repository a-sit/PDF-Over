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

//Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security Layer response
 */
public class SLResponse {

	/**
	 * SLF4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(SLResponse.class);

	private String response;

	/**
	 * Create a new Security Layer response
	 * @param response the SLResponse
	 * @param server the server
	 * @param userAgent the user Agent
	 * @param signaturLayout the signature Layout
	 */
	public SLResponse(String response) {
		this.response = response;
	}

	/**
	 * Get the Security Layer response text
	 * @return the Security Layer response text
	 */
	public String getSLResponse() {
		return this.response;
	}
}
