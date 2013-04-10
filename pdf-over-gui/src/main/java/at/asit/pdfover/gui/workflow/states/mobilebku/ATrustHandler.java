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
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.controls.Dialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.Dialog.ICON;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.states.MobileBKUState;
import at.asit.pdfover.signator.SLResponse;

/**
 * A-Trust mobile BKU handler
 */
public class ATrustHandler extends MobileBKUHandler {
	Shell shell;

	/**
	 * @param state
	 * @param shell
	 */
	public ATrustHandler(MobileBKUState state, Shell shell) {
		super(state);
		this.shell = shell;
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
		ATrustStatus status = getStatus();

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

		status.setSessionID(sessionID);

		status.setViewstate(viewState);

		status.setEventvalidation(eventValidation);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#postCredentials()
	 */
	@Override
	public String postCredentials() throws Exception {
		ATrustStatus status = getStatus();
	
		Protocol.registerProtocol("https", //$NON-NLS-1$
				new Protocol("https", new TrustedSocketFactory(), 443)); //$NON-NLS-1$
	
		HttpClient client = new HttpClient();
		client.getParams().setParameter("http.useragent", //$NON-NLS-1$
				Constants.USER_AGENT_STRING);
	
		PostMethod post = new PostMethod(status.getBaseURL() + "/identification.aspx?sid=" + status.getSessionID()); //$NON-NLS-1$
		post.getParams().setContentCharset("utf-8"); //$NON-NLS-1$
		post.addParameter("__VIEWSTATE", status.getViewstate()); //$NON-NLS-1$
		post.addParameter("__EVENTVALIDATION", status.getEventvalidation()); //$NON-NLS-1$
		post.addParameter("handynummer", status.getPhoneNumber()); //$NON-NLS-1$
		post.addParameter("signaturpasswort", status.getMobilePassword()); //$NON-NLS-1$
		post.addParameter("Button_Identification", "Identifizieren"); //$NON-NLS-1$ //$NON-NLS-2$
	
		return executePost(client, post);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#handleCredentialsResponse(java.lang.String)
	 */
	@Override
	public void handleCredentialsResponse(String responseData) throws Exception {
		ATrustStatus status = getStatus();
		String viewState = status.getViewstate();
		String eventValidation = status.getEventvalidation();
		String sessionID = status.getSessionID();
		String refVal = null;
		String signatureDataURL = null;

		status.setErrorMessage(null);

		if(responseData.contains("signature.aspx?sid=")) { //$NON-NLS-1$
			// credentials ok! TAN entry
			sessionID = MobileBKUHelper.extractTag(responseData, "signature.aspx?sid=", "\""); //$NON-NLS-1$ //$NON-NLS-2$
			viewState = MobileBKUHelper.extractTag(responseData, "id=\"__VIEWSTATE\" value=\"", "\""); //$NON-NLS-1$  //$NON-NLS-2$
			eventValidation = MobileBKUHelper.extractTag(responseData, "id=\"__EVENTVALIDATION\" value=\"", "\""); //$NON-NLS-1$  //$NON-NLS-2$
			refVal = MobileBKUHelper.extractTag(responseData, "id='vergleichswert'><b>Vergleichswert:</b>", "</div>");  //$NON-NLS-1$//$NON-NLS-2$
			signatureDataURL = status.getBaseURL() + "/ShowSigobj.aspx" +  //$NON-NLS-1$
					MobileBKUHelper.extractTag(responseData, "ShowSigobj.aspx", "'");  //$NON-NLS-1$//$NON-NLS-2$

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
		log.info("signatureDataURL: " + signatureDataURL); //$NON-NLS-1$

		status.setSessionID(sessionID);
		status.setRefVal(refVal);
		status.setViewstate(viewState);
		status.setEventvalidation(eventValidation);
		status.setSignatureDataURL(signatureDataURL);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#postTAN()
	 */
	@Override
	public String postTAN() throws IOException {
		ATrustStatus status = getStatus();
	
		Protocol.registerProtocol("https", //$NON-NLS-1$
				new Protocol("https", new TrustedSocketFactory(), 443)); //$NON-NLS-1$
	
		HttpClient client = new HttpClient();
		client.getParams().setParameter("http.useragent", //$NON-NLS-1$
				Constants.USER_AGENT_STRING);
	
		PostMethod post = new PostMethod(status.getBaseURL()
				+ "/signature.aspx?sid=" + status.getSessionID()); //$NON-NLS-1$
		post.getParams().setContentCharset("utf-8"); //$NON-NLS-1$
		post.addParameter("__VIEWSTATE", status.getViewstate()); //$NON-NLS-1$
		post.addParameter(
				"__EVENTVALIDATION", status.getEventvalidation()); //$NON-NLS-1$
		post.addParameter("input_tan", status.getTan()); //$NON-NLS-1$
		post.addParameter("SignButton", "Signieren"); //$NON-NLS-1$ //$NON-NLS-2$
		post.addParameter("Button1", "Identifizieren"); //$NON-NLS-1$ //$NON-NLS-2$
	
		return executePost(client, post);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#handleTANResponse(java.lang.String)
	 */
	@Override
	public void handleTANResponse(String responseData) {
		getStatus().setErrorMessage(null);
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
				getStatus().setTanTries(getStatus().getTanTries() - 1);
				log.debug("Error parsing TAN response", e); //$NON-NLS-1$
			}

			if (getStatus().getTanTries() <= 0) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						Dialog dialog = new Dialog(ATrustHandler.this.shell, Messages.getString("common.warning"), //$NON-NLS-1$
								Messages.getString("mobileBKU.tan_tries_exceeded"), //$NON-NLS-1$
								BUTTONS.OK_CANCEL, ICON.QUESTION);
						if (dialog.open() == SWT.CANCEL) {
							// Cancel
							getState().setCommunicationState(MobileBKUCommunicationState.CANCEL);
						} else {
							// move to POST_REQUEST again
							getState().setCommunicationState(MobileBKUCommunicationState.POST_REQUEST);
						}
					}
				});
			}
		}
	}

	@Override
	public ATrustStatus getStatus() {
		return (ATrustStatus) getState().getStatus();
	}
}
