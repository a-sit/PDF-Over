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
	private String server;

	/**
	 * Gets the server string
	 * @return the server
	 */
	public String getServer() {
		return this.server;
	}

	/**
	 * Gets the user Agent string
	 * @return the user Agent
	 */
	public String getUserAgent() {
		return this.userAgent;
	}

	/**
	 * Gets the signature Layout value
	 * @return the signature Layout
	 */
	public String getSignaturLayout() {
		return this.signaturLayout;
	}

	private String userAgent;
	private String signaturLayout;

	/**
	 * Create a new Security Layer response
	 * @param response the SLResponse
	 * @param server the server
	 * @param userAgent the user Agent
	 * @param signaturLayout the signature Layout
	 */
	public SLResponse(String response, String server, String userAgent, String signaturLayout) {
		this.response = response;
		this.server = server;
		this.userAgent = userAgent;
		this.signaturLayout = signaturLayout;
	}

	/**
	 * Get the Security Layer response text
	 * @return the Security Layer response text
	 */
	public String getSLResponse() {
		return this.response;
	}
}
