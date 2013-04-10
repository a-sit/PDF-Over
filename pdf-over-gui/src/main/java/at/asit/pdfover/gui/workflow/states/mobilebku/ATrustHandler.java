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

import at.asit.pdfover.gui.workflow.states.MobileBKUState;
import at.asit.pdfover.signator.SLResponse;

/**
 * A-Trust mobile BKU handler
 */
public class ATrustHandler extends MobileBKUHandler {
	/**
	 * @param state
	 */
	public ATrustHandler(MobileBKUState state) {
		super(state);
	}

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(ATrustHandler.class);

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#handleSLRequestResponse(java.lang.String)
	 */
	@Override
	public void handleSLRequestResponse(String responseData) throws Exception {
		// Extract infos:

		String sessionID = MobileBKUHelper.extractTag(responseData,
				"identification.aspx?sid=", "\""); //$NON-NLS-1$ //$NON-NLS-2$

		String viewState = MobileBKUHelper.extractTag(responseData,
				"id=\"__VIEWSTATE\" value=\"", "\""); //$NON-NLS-1$  //$NON-NLS-2$

		String eventValidation = MobileBKUHelper.extractTag(responseData,
				"id=\"__EVENTVALIDATION\" value=\"", "\""); //$NON-NLS-1$  //$NON-NLS-2$

		log.info("sessionID: " + sessionID); //$NON-NLS-1$
		log.info("viewState: " + viewState); //$NON-NLS-1$
		log.info("eventValidation: " + eventValidation); //$NON-NLS-1$

		getStatus().setSessionID(sessionID);

		getStatus().setViewstate(viewState);

		getStatus().setEventvalidation(eventValidation);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#handleCredentialsResponse(java.lang.String)
	 */
	@Override
	public void handleCredentialsResponse(String responseData) throws Exception {
		MobileBKUStatus status = getStatus();
		String viewState = status.getViewstate();
		String eventValidation = status.getEventvalidation();
		String sessionID = status.getSessionID();
		String refVal = null;

		status.setRefVal(null);
		status.setErrorMessage(null);

		if(responseData.contains("signature.aspx?sid=")) { //$NON-NLS-1$
			// credentials ok! TAN entry
			sessionID = MobileBKUHelper.extractTag(responseData, "signature.aspx?sid=", "\""); //$NON-NLS-1$ //$NON-NLS-2$
			viewState = MobileBKUHelper.extractTag(responseData, "id=\"__VIEWSTATE\" value=\"", "\""); //$NON-NLS-1$  //$NON-NLS-2$
			eventValidation = MobileBKUHelper.extractTag(responseData, "id=\"__EVENTVALIDATION\" value=\"", "\""); //$NON-NLS-1$  //$NON-NLS-2$
			refVal = MobileBKUHelper.extractTag(responseData, "id='vergleichswert'><b>Vergleichswert:</b>", "</div>");  //$NON-NLS-1$//$NON-NLS-2$

			status.setRefVal(refVal);
			getState().setCommunicationState(MobileBKUCommunicationState.POST_TAN);
		} else {
			// error page
			// extract error text!
			String errorMessage = MobileBKUHelper.extractTag(responseData, "<span id=\"Label1\" class=\"ErrorClass\">", "</span>"); //$NON-NLS-1$ //$NON-NLS-2$

			status.setErrorMessage(errorMessage);

			// force UI again!
			status.setMobilePassword(null);
		}

		log.info("sessionID: " + sessionID); //$NON-NLS-1$
		log.info("Vergleichswert: " + refVal); //$NON-NLS-1$
		log.info("viewState: " + viewState); //$NON-NLS-1$
		log.info("eventValidation: " + eventValidation); //$NON-NLS-1$

		status.setSessionID(sessionID);
		status.setViewstate(viewState);
		status.setEventvalidation(eventValidation);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#handleTANResponse(java.lang.String)
	 */
	@Override
	public void handleTANResponse(String responseData) {
		if (responseData.contains("sl:CreateXMLSignatureResponse xmlns:sl")) { //$NON-NLS-1$
			// success !!
			
			getSigningState().setSignatureResponse(
					new SLResponse(responseData, getStatus().getServer(), null, null));
			getState().setCommunicationState(MobileBKUCommunicationState.FINAL);
		} else {
			try {
				String tries = MobileBKUHelper.extractTag(
						responseData, "Sie haben noch", "Versuch"); //$NON-NLS-1$ //$NON-NLS-2$
				getStatus().setTanTries(Integer.parseInt(tries.trim()));
			} catch (Exception e) {
				log.debug("Error parsing TAN response", e); //$NON-NLS-1$
			}

			if (getStatus().getTanTries() <= 0) {
				// move to POST_REQUEST
				getState().setCommunicationState(MobileBKUCommunicationState.POST_REQUEST);
			}
		}
	}

}
