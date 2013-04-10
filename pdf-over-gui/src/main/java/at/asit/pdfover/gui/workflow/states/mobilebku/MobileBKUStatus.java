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
package at.asit.pdfover.gui.workflow.states.mobilebku;

/**
 * 
 */
public interface MobileBKUStatus {
	/**
	 * @return the identification_url
	 */
	public String getSessionID();

	/**
	 * @param sessionID the identification_url to set
	 */
	public void setSessionID(String sessionID);

	/**
	 * @return the phoneNumber
	 */
	public String getPhoneNumber();

	/**
	 * @param phoneNumber the phoneNumber to set
	 */
	public void setPhoneNumber(String phoneNumber);

	/**
	 * @return the mobilePassword
	 */
	public String getMobilePassword();

	/**
	 * @param mobilePassword the mobilePassword to set
	 */
	public void setMobilePassword(String mobilePassword);

	/**
	 * @return the reference value
	 */
	public String getRefVal();

	/**
	 * @param refVal the reference value to set
	 */
	public void setRefVal(String refVal);

	/**
	 * @return the tan
	 */
	public String getTan();

	/**
	 * @param tan the tan to set
	 */
	public void setTan(String tan);

	/**
	 * Get maximum number of TAN tries
	 * @return the maximum number of TAN tries
	 */
	public int getMaxTanTries();

	/**
	 * Get number of TAN tries left
	 * @return the number of TAN tries left
	 */
	public int getTanTries();

	/**
	 * Set number of TAN tries left
	 * @param tries the number of TAN tries left
	 */
	public void setTanTries(int tries);

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage();

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage);

	/**
	 * @return the baseURL
	 */
	public String getBaseURL();

	/**
	 * @param baseURL 
	 */
	public void setBaseURL(String baseURL);

	/**
	 * Return the SL request server
	 * @return the SL request server
	 */
	public String getServer();

	/**
	 * Set the SL request server
	 * @param server the SL request server
	 */
	public void setServer(String server);

	/**
	 * Get the signature data URL
	 * @return the signature data URL
	 */
	public String getSignatureDataURL();

	/**
	 * Set the signature data URL
	 * @param signatureDataURL the signature data URL
	 */
	public void setSignatureDataURL(String signatureDataURL);

	/**
	 * Ensure that given URL contains a session ID (if necessary)
	 * @param url URL to check for session ID
	 * @return resulting URL
	 */
	public String ensureSessionID(String url);
}
