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
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import at.asit.pdfover.gui.controls.Dialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.Dialog.ICON;
import at.asit.pdfover.gui.exceptions.ATrustConnectionException;
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
			String errorCode = MobileBKUHelper.extractSubstring(responseData,
					"<sl:ErrorCode>", "</sl:ErrorCode>"); //$NON-NLS-1$ //$NON-NLS-2$
			String errorMsg = MobileBKUHelper.extractSubstring(responseData,
					"<sl:Info>", "</sl:Info>"); //$NON-NLS-1$ //$NON-NLS-2$
			throw new Exception("Error from mobile BKU: " + //$NON-NLS-1$
					errorCode + " - " + errorMsg); //$NON-NLS-1$
		}

		// Extract infos:
		String sessionID = MobileBKUHelper.extractSubstring(responseData,
				"identification.aspx?sid=", "\""); //$NON-NLS-1$ //$NON-NLS-2$

		String viewState = MobileBKUHelper.extractValueFromTagWithParam(
				responseData, "", "id", "__VIEWSTATE", "value"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		String eventValidation = MobileBKUHelper.extractValueFromTagWithParam(
				responseData, "", "id", "__EVENTVALIDATION", "value"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

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
		HttpClient client = MobileBKUHelper.getHttpClient(getStatus());

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
		String qrCode = null;
		String tanField = null;
		String tanTextTan = null;

		status.setErrorMessage(null);

		if (responseData.contains("ExpiresInfo.aspx?sid=")) { //$NON-NLS-1$
			// Certificate expiration interstitial - skip
			String notice = Messages.getString("mobileBKU.notice") + " " + //$NON-NLS-1$ //$NON-NLS-2$
					StringEscapeUtils.unescapeHtml4(MobileBKUHelper.extractContentFromTagWithParam(responseData, "span", "id", "Label2")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

			String t_sessionID = MobileBKUHelper.extractSubstring(responseData, "ExpiresInfo.aspx?sid=", "\""); //$NON-NLS-1$ //$NON-NLS-2$
			String t_viewState = MobileBKUHelper.extractValueFromTagWithParam(responseData, "", "id", "__VIEWSTATE", "value"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			String t_eventValidation = MobileBKUHelper.extractValueFromTagWithParam(responseData, "", "id", "__EVENTVALIDATION", "value"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

			// Post again to skip
			MobileBKUHelper.registerTrustedSocketFactory();
			HttpClient client = MobileBKUHelper.getHttpClient(getStatus());

			PostMethod post = new PostMethod(status.getBaseURL() + "/ExpiresInfo.aspx?sid=" + t_sessionID); //$NON-NLS-1$
			post.getParams().setContentCharset("utf-8"); //$NON-NLS-1$
			post.addParameter("__VIEWSTATE", t_viewState); //$NON-NLS-1$
			post.addParameter("__EVENTVALIDATION", t_eventValidation); //$NON-NLS-1$
			post.addParameter("Button_Next", "Weiter"); //$NON-NLS-1$ //$NON-NLS-2$

			responseData = executePost(client, post);
			log.trace("Response from mobile BKU: " + responseData); //$NON-NLS-1$
		} else if (responseData.contains("tanAppInfo.aspx?sid=")) { //$NON-NLS-1$
			// App info interstitial - skip
			log.info("Skipping tan app interstitial"); //$NON-NLS-1$

			String t_sessionID = MobileBKUHelper.extractSubstring(responseData, "tanAppInfo.aspx?sid=", "\""); //$NON-NLS-1$ //$NON-NLS-2$
			String t_viewState = MobileBKUHelper.extractValueFromTagWithParam(responseData, "", "id", "__VIEWSTATE", "value"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			String t_eventValidation = MobileBKUHelper.extractValueFromTagWithParam(responseData, "", "id", "__EVENTVALIDATION", "value"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

			// Post again to skip
			MobileBKUHelper.registerTrustedSocketFactory();
			HttpClient client = MobileBKUHelper.getHttpClient(getStatus());

			PostMethod post = new PostMethod(status.getBaseURL() + "/tanAppInfo.aspx?sid=" + t_sessionID); //$NON-NLS-1$
			post.getParams().setContentCharset("utf-8"); //$NON-NLS-1$
			post.addParameter("__VIEWSTATE", t_viewState); //$NON-NLS-1$
			post.addParameter("__EVENTVALIDATION", t_eventValidation); //$NON-NLS-1$
			post.addParameter("NextBtn", "Weiter"); //$NON-NLS-1$ //$NON-NLS-2$

			responseData = executePost(client, post);
			log.trace("Response from mobile BKU: " + responseData); //$NON-NLS-1$
		}

		if (responseData.contains("signature.aspx?sid=")) { //$NON-NLS-1$
			// credentials ok! TAN entry
			log.debug("Credentials accepted - TAN required"); //$NON-NLS-1$
			sessionID = MobileBKUHelper.extractSubstring(responseData, "signature.aspx?sid=", "\""); //$NON-NLS-1$ //$NON-NLS-2$
			viewState = MobileBKUHelper.extractValueFromTagWithParam(responseData, "", "id", "__VIEWSTATE", "value"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			eventValidation = MobileBKUHelper.extractValueFromTagWithParam(responseData, "", "id", "__EVENTVALIDATION", "value"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			refVal = MobileBKUHelper.extractSubstring(responseData, "id='vergleichswert'><b>Vergleichswert:</b>", "</div>"); //$NON-NLS-1$ //$NON-NLS-2$
			signatureDataURL = status.getBaseURL() + "/ShowSigobj.aspx" + //$NON-NLS-1$
					MobileBKUHelper.extractSubstring(responseData, "ShowSigobj.aspx", "'"); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				qrCode = MobileBKUHelper.extractValueFromTagWithParam(responseData, "img", "class", "qrcode", "src"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				log.debug("QR Code found: " + qrCode); //$NON-NLS-1$
				status.setQRCode(qrCode);
			} catch (Exception e) {
				log.debug("No QR Code found"); //$NON-NLS-1$
			}
			try {
				tanField = MobileBKUHelper.extractValueFromTagWithParam(responseData, "label", "id", "label_for_input_tan", "for"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				status.setTanField(tanField);
			} catch (Exception e) {
				log.debug("No tan field found"); //$NON-NLS-1$
			}
			try {
				tanTextTan = tanField = MobileBKUHelper.extractContentFromTagWithParam(responseData, "span", "id", "text_tan"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				status.setIsAPPTan(tanTextTan);
			}catch (Exception e) {
				log.debug("No text_tan tag"); //$NON-NLS-1$
			}
			
		} else if (responseData.contains("sl:InfoboxReadResponse")) { //$NON-NLS-1$
			// credentials ok! InfoboxReadResponse
			log.debug("Credentials accepted - Response given"); //$NON-NLS-1$
			getSigningState().setSignatureResponse(new SLResponse(responseData, getStatus().getServer(), null, null));
			return;
		} else if (responseData.contains("page_undecided")) { //$NON-NLS-1$
			// skip intermediate page 
			log.debug("Page Undecided"); //$NON-NLS-1$
			getSigningState().setSignatureResponse(new SLResponse(responseData, getStatus().getServer(), null, null));
			status.setErrorMessage("waiting..."); //$NON-NLS-1$
			return; 
		}else {
			// error page
			// extract error text!
			try {
				String errorMessage = MobileBKUHelper.extractContentFromTagWithParam(responseData, "span", "class", "ErrorClass"); //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$
				status.setErrorMessage(errorMessage);
			} catch (Exception e) {
				throw new SignatureException(MobileBKUHelper.extractSubstring(responseData, "<sl:ErrorCode>", "</sl:ErrorCode>") + ": " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						MobileBKUHelper.extractSubstring(responseData, "<sl:Info>", "</sl:Info>")); //$NON-NLS-1$ //$NON-NLS-2$
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
		HttpClient client = MobileBKUHelper.getHttpClient(getStatus());
	
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
				String tries = MobileBKUHelper.extractSubstring(
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

	/**
	 * Cancel QR process, request SMS TAN
	 * @return the response
	 * @throws IOException Error during posting
	 */
	public String postSMSRequest() throws IOException {
		ATrustStatus status = getStatus();

		MobileBKUHelper.registerTrustedSocketFactory();
		HttpClient client = MobileBKUHelper.getHttpClient(getStatus());

		GetMethod get = new GetMethod(status.getBaseURL()
				+ "/sendsms.aspx?sid=" + status.getSessionID()); //$NON-NLS-1$
		get.getParams().setContentCharset("utf-8"); //$NON-NLS-1$

		return executeGet(client, get);
	}

	/**
	 * Get the QR code image
	 * @return the QR code image as a String
	 */
	public InputStream getQRCode() {
		//TODO: Update HTTPClient here
		
		ATrustStatus status = getStatus();

		MobileBKUHelper.registerTrustedSocketFactory();
		HttpClient client = MobileBKUHelper.getHttpClient(getStatus());

		GetMethod get = new GetMethod(status.getBaseURL() + "/" + //$NON-NLS-1$
				status.getQRCode());

		try {
			log.debug("Getting " + get.getURI()); //$NON-NLS-1$
			int returnCode = client.executeMethod(get);

			if (returnCode != HttpStatus.SC_OK) {
				log.error("Error getting QR code"); //$NON-NLS-1$
				return null;
			}

			return get.getResponseBodyAsStream();
		} catch (Exception e) {
			log.error("Error getting QR code", e); //$NON-NLS-1$
			return null;
		}
	}

	/**
	 * Get Signature page after scanning QR code
	 * @return the response
	 * @throws IOException Error during get
	 */
	public String getSignaturePage() throws IOException {
		ATrustStatus status = getStatus();

		MobileBKUHelper.registerTrustedSocketFactory();
		HttpClient client = MobileBKUHelper.getHttpClient(getStatus());

		GetMethod get = new GetMethod(status.getBaseURL()
				+ "/signature.aspx?sid=" + status.getSessionID()); //$NON-NLS-1$

		return executeGet(client, get);
	}

	/**
	 * Parse QR code response
	 * @param responseData
	 * @return whether a SL response was received
	 */
	public boolean handleQRResponse(String responseData) {
		getStatus().setErrorMessage(null);
		if (responseData.contains("sl:CreateXMLSignatureResponse xmlns:sl") || //$NON-NLS-1$
		    responseData.contains("sl:CreateCMSSignatureResponse xmlns:sl")) { //$NON-NLS-1$
			// success !!

			getSigningState().setSignatureResponse(
					new SLResponse(responseData, getStatus().getServer(), null, null));
			return true;
		}
		return false;
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

	/*
	 * (non-Javadoc)
	 * 
	 */
	@Override
	public void handlePolling(String responseData) throws ATrustConnectionException {

		ATrustStatus status = getStatus();
		URLConnection urlconnection = null;
		String isReady = null;
		Status serverStatus = null;
		int waits = 0;
		final String ERROR = "Error: Server is not responding"; //$NON-NLS-1$

		try {
			do {
				urlconnection = new URL(status.getBaseURL() + "/UndecidedPolling.aspx?sid=" + status.getSessionID()) //$NON-NLS-1$
						.openConnection();
				InputStream in = new BufferedInputStream(urlconnection.getInputStream());

				isReady = IOUtils.toString(in, "utf-8"); //$NON-NLS-1$
				serverStatus = new Status(isReady);
				if (serverStatus.isWait())
					waits++;
				if (waits > 4) {
					status.setErrorMessage(ERROR);
					log.error(ERROR);
					throw new ATrustConnectionException();
				}

			} while (serverStatus.isWait());

			if (serverStatus.isFin()) {
				String response = getSignaturePage();
				handleCredentialsResponse(response);
			} else {
				status.setErrorMessage("Server reponded ERROR during polling"); //$NON-NLS-1$
				log.error("Server reponded ERROR during polling"); //$NON-NLS-1$
				throw new ATrustConnectionException();
			}

		} catch (Exception e) {
			log.error("handle polling failed" + e.getMessage()); //$NON-NLS-1$
			throw new ATrustConnectionException();
		}
	}
	
	private class Status {
		private final boolean fin; 
		private final boolean error; 
		private final boolean wait; 
		
		public Status(String status) {
			 JsonElement jelement = new JsonParser().parse(status.toLowerCase());
			 JsonObject  jobject = jelement.getAsJsonObject();
			 this.fin = jobject.get("fin").getAsBoolean(); //$NON-NLS-1$ 
			 this.error = jobject.get("error").getAsBoolean(); //$NON-NLS-1$ 
			 this.wait = jobject.get("wait").getAsBoolean(); //$NON-NLS-1$ 
		}
		
		public Status(boolean error) {
			this.error = error; 
			this.fin = false; 
			this.wait = false; 
		}

		public boolean isFin() {
			return fin;
		}

		public boolean isError() {
			return error;
		}

		public boolean isWait() {
			return wait;
		}
		
		
		
		
	}
	
}


