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
	public abstract String getSessionID();

	/**
	 * @param sessionID the identification_url to set
	 */
	public abstract void setSessionID(String sessionID);

	/**
	 * @return the phoneNumber
	 */
	public abstract String getPhoneNumber();

	/**
	 * @param phoneNumber the phoneNumber to set
	 */
	public abstract void setPhoneNumber(String phoneNumber);

	/**
	 * @return the mobilePassword
	 */
	public abstract String getMobilePassword();

	/**
	 * @param mobilePassword the mobilePassword to set
	 */
	public abstract void setMobilePassword(String mobilePassword);

	/**
	 * @return the reference value
	 */
	public abstract String getRefVal();

	/**
	 * @param refVal the reference value to set
	 */
	public abstract void setRefVal(String refVal);

	/**
	 * @return the tan
	 */
	public abstract String getTan();

	/**
	 * @param tan the tan to set
	 */
	public abstract void setTan(String tan);

	/**
	 * Get maximum number of TAN tries
	 * @return the maximum number of TAN tries
	 */
	public abstract int getMaxTanTries();

	/**
	 * Get number of TAN tries left
	 * @return the number of TAN tries left
	 */
	public abstract int getTanTries();

	/**
	 * Set number of TAN tries left
	 * @param tries the number of TAN tries left
	 */
	public abstract void setTanTries(int tries);

	/**
	 * @return the errorMessage
	 */
	public abstract String getErrorMessage();

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public abstract void setErrorMessage(String errorMessage);

	/**
	 * @return the baseURL
	 */
	public abstract String getBaseURL();

	/**
	 * @param baseURL 
	 */
	public abstract void setBaseURL(String baseURL);

	/**
	 * Return the SL request server
	 * @return the SL request server
	 */
	public abstract String getServer();

	/**
	 * Set the SL request server
	 * @param server the SL request server
	 */
	public abstract void setServer(String server);
}
