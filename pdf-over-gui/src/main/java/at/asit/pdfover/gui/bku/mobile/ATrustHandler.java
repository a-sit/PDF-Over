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
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.bku.BKUHelper;
import at.asit.pdfover.gui.controls.Dialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.Dialog.ICON;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.states.MobileBKUState;
import at.asit.pdfover.signator.SLResponse;
import at.asit.pdfover.signator.SignatureException;

/**
 * A-Trust mobile BKU handler
 */
public class ATrustHandler extends MobileBKUHandler {
	Shell shell;

	/**
	 * @param state
	 * @param shell
	 * @param useBase64
	 */
	public ATrustHandler(MobileBKUState state, Shell shell, boolean useBase64) {
		super(state);
		this.shell = shell;
		this.useBase64 = useBase64;
	}

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(ATrustHandler.class);

	private static boolean expiryNoticeDisplayed = false;

	private static final String ACTIVATION_URL = "https://www.handy-signatur.at/"; //$NON-NLS-1$

	private boolean useBase64 = false;

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#handleSLRequestResponse(java.lang.String)
	 */
	@Override
	public void handleSLRequestResponse(String responseData) throws Exception {
		ATrustStatus status = getStatus();

		if (responseData.contains("<sl:ErrorResponse")) { //$NON-NLS-1$
			String errorCode = MobileBKUHelper.extractTag(responseData,
					"<sl:ErrorCode>", "</sl:ErrorCode>"); //$NON-NLS-1$ //$NON-NLS-2$
			String errorMsg = MobileBKUHelper.extractTag(responseData,
					"<sl:Info>", "</sl:Info>"); //$NON-NLS-1$ //$NON-NLS-2$
			throw new Exception("Error from mobile BKU: " + //$NON-NLS-1$
					errorCode + " - " + errorMsg); //$NON-NLS-1$
		}

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

		MobileBKUHelper.registerTrustedSocketFactory();
		HttpClient client = BKUHelper.getHttpClient();
	
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

		if (responseData.contains("ExpiresInfo.aspx?sid=")) { //$NON-NLS-1$
			// Certification expiration interstitial - skip
			String notice = Messages.getString("mobileBKU.notice") + " " + //$NON-NLS-1$ //$NON-NLS-2$
					StringEscapeUtils.unescapeHtml4(MobileBKUHelper.extractTag(responseData, "<span id=\"Label2\">", "</span>")) //$NON-NLS-1$ //$NON-NLS-2$
					.replaceAll("\\<.*?\\>", ""); //$NON-NLS-1$ //$NON-NLS-2$
			log.info(notice);

			if (!expiryNoticeDisplayed) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						Dialog d = new Dialog(ATrustHandler.this.shell, Messages.getString("common.info"), Messages.getString("mobileBKU.certExpiresSoon"), BUTTONS.YES_NO, ICON.WARNING); //$NON-NLS-1$ //$NON-NLS-2$
						if (d.open() == SWT.YES) {
							log.debug("Trying to open " + ACTIVATION_URL); //$NON-NLS-1$
							if (Desktop.isDesktopSupported()) {
								try {
									Desktop.getDesktop().browse(new URI(ACTIVATION_URL));
									return;
								} catch (Exception e) {
									log.debug("Error opening URL", e); //$NON-NLS-1$
								}
							}
							log.info("SWT Desktop is not supported on this platform"); //$NON-NLS-1$
							Program.launch(ACTIVATION_URL);
						}
					}
				});
				expiryNoticeDisplayed = true;
			}

			String t_sessionID = MobileBKUHelper.extractTag(responseData, "ExpiresInfo.aspx?sid=", "\""); //$NON-NLS-1$ //$NON-NLS-2$
			String t_viewState = MobileBKUHelper.extractTag(responseData, "id=\"__VIEWSTATE\" value=\"", "\""); //$NON-NLS-1$  //$NON-NLS-2$
			String t_eventValidation = MobileBKUHelper.extractTag(responseData, "id=\"__EVENTVALIDATION\" value=\"", "\""); //$NON-NLS-1$  //$NON-NLS-2$

			// Post again to skip
			MobileBKUHelper.registerTrustedSocketFactory();
			HttpClient client = BKUHelper.getHttpClient();

			PostMethod post = new PostMethod(status.getBaseURL() + "/ExpiresInfo.aspx?sid=" + t_sessionID); //$NON-NLS-1$
			post.getParams().setContentCharset("utf-8"); //$NON-NLS-1$
			post.addParameter("__VIEWSTATE", t_viewState); //$NON-NLS-1$
			post.addParameter("__EVENTVALIDATION", t_eventValidation); //$NON-NLS-1$
			post.addParameter("Button_Next", "Weiter"); //$NON-NLS-1$ //$NON-NLS-2$

			responseData = executePost(client, post);
			log.trace("Response from mobile BKU: " + responseData); //$NON-NLS-1$
		}

		if (responseData.contains("signature.aspx?sid=")) { //$NON-NLS-1$
			// credentials ok! TAN entry
			log.debug("Credentials accepted - TAN required"); //$NON-NLS-1$
			sessionID = MobileBKUHelper.extractTag(responseData, "signature.aspx?sid=", "\""); //$NON-NLS-1$ //$NON-NLS-2$
			viewState = MobileBKUHelper.extractTag(responseData, "id=\"__VIEWSTATE\" value=\"", "\""); //$NON-NLS-1$  //$NON-NLS-2$
			eventValidation = MobileBKUHelper.extractTag(responseData, "id=\"__EVENTVALIDATION\" value=\"", "\""); //$NON-NLS-1$  //$NON-NLS-2$
			refVal = MobileBKUHelper.extractTag(responseData, "id='vergleichswert'><b>Vergleichswert:</b>", "</div>");  //$NON-NLS-1$//$NON-NLS-2$
			signatureDataURL = status.getBaseURL() + "/ShowSigobj.aspx" +  //$NON-NLS-1$
					MobileBKUHelper.extractTag(responseData, "ShowSigobj.aspx", "'");  //$NON-NLS-1$//$NON-NLS-2$
		} else if (responseData.contains("sl:InfoboxReadResponse")) { //$NON-NLS-1$
			// credentials ok! InfoboxReadResponse
			log.debug("Credentials accepted - Response given"); //$NON-NLS-1$
			getSigningState().setSignatureResponse(
					new SLResponse(responseData, getStatus().getServer(), null, null));
			return;
		} else {
			// error page
			// extract error text!
			try {
				String errorMessage = MobileBKUHelper.extractTag(responseData, "<span id=\"Label1\" class=\"ErrorClass\">", "</span>"); //$NON-NLS-1$ //$NON-NLS-2$
				status.setErrorMessage(errorMessage);
			} catch (Exception e) {
				throw new SignatureException(MobileBKUHelper.extractTag(responseData, "<sl:ErrorCode>", "</sl:ErrorCode>") + ": " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						MobileBKUHelper.extractTag(responseData, "<sl:Info>", "</sl:Info>")); //$NON-NLS-1$ //$NON-NLS-2$
			}

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
	
		MobileBKUHelper.registerTrustedSocketFactory();
		HttpClient client = BKUHelper.getHttpClient();
	
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
		if (responseData.contains("sl:CreateXMLSignatureResponse xmlns:sl") || //$NON-NLS-1$
		    responseData.contains("sl:CreateCMSSignatureResponse xmlns:sl")) { //$NON-NLS-1$
			// success !!
			
			getSigningState().setSignatureResponse(
					new SLResponse(responseData, getStatus().getServer(), null, null));
		} else {
			try {
				String tries = MobileBKUHelper.extractTag(
						responseData, "Sie haben noch", "Versuch"); //$NON-NLS-1$ //$NON-NLS-2$
				getStatus().setTanTries(Integer.parseInt(tries.trim()));
				getStatus().setErrorMessage("mobileBKU.wrong_tan"); //$NON-NLS-1$
			} catch (Exception e) {
				getStatus().setTanTries(getStatus().getTanTries() - 1);
				log.debug("Error parsing TAN response", e); //$NON-NLS-1$
			}

			if (getStatus().getTanTries() <= 0) {
				getStatus().setErrorMessage(null);
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						Dialog dialog = new Dialog(ATrustHandler.this.shell, Messages.getString("common.warning"), //$NON-NLS-1$
								Messages.getString("mobileBKU.tan_tries_exceeded"), //$NON-NLS-1$
								BUTTONS.OK_CANCEL, ICON.QUESTION);
						if (dialog.open() == SWT.CANCEL) {
							// Go back to BKU Selection
							getStatus().setTanTries(-1);
						} else {
							// Start signature process over
							getStatus().setTanTries(-2);
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

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.bku.mobile.MobileBKUHandler#useBase64Request()
	 */
	@Override
	public boolean useBase64Request() {
		return this.useBase64;
	}
}
