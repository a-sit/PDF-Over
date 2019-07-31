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

// Imports
import org.apache.commons.httpclient.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.workflow.config.ConfigProvider;

/**
 * A-Trust MobileBKUStatus implementation
 */
public class ATrustStatus extends AbstractMobileBKUStatusImpl {
	/**
	 * SLF4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(ATrustStatus.class);

	/** Maximum number of TAN tries */
	public static final int MOBILE_MAX_TAN_TRIES = 3;

	private String viewstate;
	private String eventvalidation;
	private String qrcode = null;
	private boolean tanField = false;
	private boolean isAPPTan = false;
	private String viewstateGenerator; 
	private String dynAttrPhonenumber; 
	private String dynAttrPassword; 
	private String dynAttrBtnId; 

	/**
	 * Constructor
	 * @param provider the ConfigProvider
	 */
	public ATrustStatus(ConfigProvider provider) {
		setPhoneNumber(provider.getDefaultMobileNumber());
		setMobilePassword(provider.getDefaultMobilePassword());
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#getMaxTanTries()
	 */
	@Override
	public int getMaxTanTries() {
		return MOBILE_MAX_TAN_TRIES;
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

	/**
	 * @return the QR code
	 */
	public String getQRCode() {
		return this.qrcode;
	}

	/**
	 * @param qrcode
	 *            the QR code to set
	 */
	public void setQRCode(String qrcode) {
		this.qrcode = qrcode;
	}
	
	/**
	 * @param tanField
	 */
	public void setTanField(String tanField) {
		this.tanField = tanField.equals("input_tan"); //$NON-NLS-1$
	}
	
	/**
	 * @return boolean if response contained tan field
	 */
	public boolean getTanField() {
		return this.tanField;
	}
	
	/**
	 * @param tanString the tan string from the response
	 */
	public void setIsAPPTan(String tanString) {
		this.isAPPTan = !tanString.toLowerCase().contains("sms"); //$NON-NLS-1$
	}
	
	/**
	 * @return true if the user receives the tan via app
	 */
	public boolean getIsAPPTan() {
		return this.isAPPTan;
	}
	
	/**
	 * @param viewstateGenerator
	 */
	public void setViewStateGenerator(String viewstateGenerator) {
		this.viewstateGenerator = viewstateGenerator; 
	}
	
	/**
	 * @return
	 */
	public String getViewstateGenerator() {
		return this.viewstateGenerator;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.bku.mobile.MobileBKUStatus#parseCookies(org.apache.commons.httpclient.Cookie[])
	 */
	@Override
	public void parseCookies(Cookie[] cookies) {
		//not needed yet
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.bku.mobile.MobileBKUStatus#getCookies()
	 */
	@Override
	public Cookie[] getCookies() {
		//not needed yet
		return null;
	}
	
	
	
	/**
	 * @return the dynAttrPhonenumber
	 */
	public String getDynAttrPhonenumber() {
		return this.dynAttrPhonenumber;
	}

	/**
	 * @param dynAttrPhonenumber the dynAttrPhonenumber to set
	 */
	public void setDynAttrPhonenumber(String dynAttrPhonenumber) {
		this.dynAttrPhonenumber = dynAttrPhonenumber;
	}

	/**
	 * @return the dynAttrPassword
	 */
	public String getDynAttrPassword() {
		return this.dynAttrPassword;
	}

	/**
	 * @param dynAttrPassword the dynAttrPassword to set
	 */
	public void setDynAttrPassword(String dynAttrPassword) {
		this.dynAttrPassword = dynAttrPassword;
	}

	/**
	 * @return the dynAttrBtnId
	 */
	public String getDynAttrBtnId() {
		return this.dynAttrBtnId;
	}

	/**
	 * @param dynAttrBtnId the dynAttrBtnId to set
	 */
	public void setDynAttrBtnId(String dynAttrBtnId) {
		this.dynAttrBtnId = dynAttrBtnId;
	}

}
