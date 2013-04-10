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

// Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.workflow.ConfigProvider;

/**
 * 
 */
public class ATrustStatus implements MobileBKUStatus {
	/**
	 * SLF4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(ATrustStatus.class);

	/**
	 * Maximum number of TAN tries!
	 */
	public static final int MOBILE_MAX_TAN_TRIES = 3;

	private String viewstate;
	private String eventvalidation;
	private String sessionID;
	private String phoneNumber;
	private String mobilePassword;
	private String baseURL;
	private String refVal;
	private String errorMessage;
	private String tan;
	private String server;
	private int tanTries = MOBILE_MAX_TAN_TRIES;

	/**
	 * Constructor
	 * @param provider 
	 */
	public ATrustStatus(ConfigProvider provider) {
		this.setPhoneNumber(provider.getDefaultMobileNumber());
		this.setMobilePassword(provider.getDefaultMobilePassword());
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#getMaxTanTries()
	 */
	@Override
	public int getMaxTanTries() {
		return MOBILE_MAX_TAN_TRIES;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#getTanTries()
	 */
	@Override
	public int getTanTries() {
		return this.tanTries;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#setTanTries(int)
	 */
	@Override
	public void setTanTries(int tries) {
		this.tanTries = tries;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#getTan()
	 */
	@Override
	public String getTan() {
		return this.tan;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#setTan(java.lang.String)
	 */
	@Override
	public void setTan(String tan) {
		this.tan = tan;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		return this.errorMessage;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#setErrorMessage(java.lang.String)
	 */
	@Override
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#getRefVal()
	 */
	@Override
	public String getRefVal() {
		return this.refVal;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#setRefVal(java.lang.String)
	 */
	@Override
	public void setRefVal(String refVal) {
		this.refVal = refVal;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#getBaseURL()
	 */
	@Override
	public String getBaseURL() {
		return this.baseURL;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#setBaseURL(java.lang.String)
	 */
	@Override
	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	/**
	 * @return the viewstate
	 */
	public String getViewstate() {
		return this.viewstate;
	}

	/**
	 * @param viewstate
	 *            the viewstate to set
	 */
	public void setViewstate(String viewstate) {
		this.viewstate = viewstate;
	}
	
	/**
	 * @return the eventvalidation
	 */
	public String getEventvalidation() {
		return this.eventvalidation;
	}

	/**
	 * @param eventvalidation the eventvalidation to set
	 */
	public void setEventvalidation(String eventvalidation) {
		this.eventvalidation = eventvalidation;
	}
	
	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#getPhoneNumber()
	 */
	@Override
	public String getPhoneNumber() {
		return this.phoneNumber;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#setPhoneNumber(java.lang.String)
	 */
	@Override
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#getMobilePassword()
	 */
	@Override
	public String getMobilePassword() {
		return this.mobilePassword;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#setMobilePassword(java.lang.String)
	 */
	@Override
	public void setMobilePassword(String mobilePassword) {
		this.mobilePassword = mobilePassword;
	}
	
	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#getSessionID()
	 */
	@Override
	public String getSessionID() {
		return this.sessionID;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#setSessionID(java.lang.String)
	 */
	@Override
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#getServer()
	 */
	@Override
	public String getServer() {
		return this.server;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#setServer(java.lang.String)
	 */
	@Override
	public void setServer(String server) {
		this.server = server;
	}
}
