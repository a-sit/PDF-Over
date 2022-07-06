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
package at.asit.pdfover.gui.bku.mobile;

import org.apache.commons.httpclient.Cookie;

/**
 * 
 */
public abstract class MobileBKUStatus {
	public String sessionID;
	public String phoneNumber;
	public String mobilePassword;
	public String baseURL;
	public String refVal;
	public String errorMessage;
	public String tan;
	public String server;
	public String signatureDataURL;
	public int tanTries = getMaxTanTries();

	/**
	 * Get maximum number of TAN tries
	 * @return the maximum number of TAN tries
	 */
	public abstract int getMaxTanTries();

	/**
	 * Ensure that given URL contains a session ID (if necessary)
	 * @param url URL to check for session ID
	 * @return resulting URL
	 */
	public abstract String ensureSessionID(String url);
}
