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
 * IAIK MobileBKUStatus implementation
 */
public class IAIKStatus extends MobileBKUStatus {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(IAIKStatus.class);

	/** Maximum number of TAN tries */
	public static final int MOBILE_MAX_TAN_TRIES = 3;

	public String viewState;

	/**
	 * Constructor
	 * @param provider the ConfigProvider
	 */
	public IAIKStatus(ConfigProvider provider) {
		this.phoneNumber = provider.getDefaultMobileNumber();
		this.mobilePassword = provider.getDefaultMobilePassword();
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#getMaxTanTries()
	 */
	@Override
	public int getMaxTanTries() {
		return MOBILE_MAX_TAN_TRIES;
	}

	@Override
	public String ensureSessionID(String url)
	{
		if (url.contains("jsessionid="))
			return url;

		if (this.sessionID != null)
			url += ";jsessionid=" + this.sessionID;
		return url;
	}
}
