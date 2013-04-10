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
 * Basic implementation of a MobileBKUStatus
 */
public abstract class AbstractMobileBKUStatusImpl implements MobileBKUStatus {

	private String sessionID;
	private String phoneNumber;
	private String mobilePassword;
	private String baseURL;
	private String refVal;
	private String errorMessage;
	private String tan;
	private String server;
	private int tanTries = getMaxTanTries();

	@Override
	public int getTanTries() {
		return this.tanTries;
	}

	@Override
	public void setTanTries(int tries) {
		this.tanTries = tries;
	}

	@Override
	public String getTan() {
		return this.tan;
	}

	@Override
	public void setTan(String tan) {
		this.tan = tan;
	}

	@Override
	public String getErrorMessage() {
		return this.errorMessage;
	}

	@Override
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String getRefVal() {
		return this.refVal;
	}

	@Override
	public void setRefVal(String refVal) {
		this.refVal = refVal;
	}

	@Override
	public String getBaseURL() {
		return this.baseURL;
	}

	@Override
	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	@Override
	public String getPhoneNumber() {
		return this.phoneNumber;
	}

	@Override
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@Override
	public String getMobilePassword() {
		return this.mobilePassword;
	}

	@Override
	public void setMobilePassword(String mobilePassword) {
		this.mobilePassword = mobilePassword;
	}

	@Override
	public String getSessionID() {
		return this.sessionID;
	}

	@Override
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	@Override
	public String getServer() {
		return this.server;
	}

	@Override
	public void setServer(String server) {
		this.server = server;
	}
}