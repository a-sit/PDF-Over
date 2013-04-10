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
}
