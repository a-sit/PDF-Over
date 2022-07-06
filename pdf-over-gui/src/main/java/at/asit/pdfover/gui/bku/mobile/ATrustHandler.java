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
import java.net.URI;
import java.net.URLConnection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
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

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.controls.Dialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.Dialog.ICON;
import at.asit.pdfover.gui.exceptions.ATrustConnectionException;
import at.asit.pdfover.commons.Messages;
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
		
		String viewstateGenerator = MobileBKUHelper.extractValueFromTagWithParamOptional(responseData, "", "id", "__VIEWSTATEGENERATOR", "value"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		String dynamicAttrPhonenumber = MobileBKUHelper.getDynamicNameAttribute(responseData, Constants.LABEL_PHONE_NUMBER); 
		String dynamicAttrPassword = MobileBKUHelper.getDynamicNameAttribute(responseData, Constants.LABEL_SIGN_PASS); 
		String dynamicAttrButtonId = MobileBKUHelper.getDynamicNameAttribute(responseData, Constants.LABEL_BTN_IDF); 
		String dynamicAttrTan = MobileBKUHelper.getDynamicNameAttribute(responseData, Constants.LABEL_TAN); 
		
		
		log.info("sessionID: " + sessionID); //$NON-NLS-1$
		log.info("viewState: " + viewState); //$NON-NLS-1$
		log.info("eventValidation: " + eventValidation); //$NON-NLS-1$

		status.sessionID = sessionID;
		status.viewState = viewState;
		status.eventValidation = eventValidation;
		if (viewstateGenerator != null ) { status.viewStateGenerator = viewstateGenerator; }
		status.dynAttrPhoneNumber = dynamicAttrPhonenumber;
		status.dynAttrPassword = dynamicAttrPassword;
		status.dynAttrBtnId = dynamicAttrButtonId;
		status.dynAttrTan = dynamicAttrTan;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#postCredentials()
	 */
	@Override
	public String postCredentials() throws Exception {
		ATrustStatus status = getStatus();

		MobileBKUHelper.registerTrustedSocketFactory();
		HttpClient client = MobileBKUHelper.getHttpClient(getStatus());

		PostMethod post = new PostMethod(status.baseURL + "/identification.aspx?sid=" + status.sessionID); //$NON-NLS-1$
		post.getParams().setContentCharset("utf-8"); //$NON-NLS-1$
		post.addParameter("__VIEWSTATE", status.viewState); //$NON-NLS-1$
		post.addParameter("__VIEWSTATEGENERATOR", status.viewStateGenerator); //$NON-NLS-1$
		post.addParameter("__EVENTVALIDATION", status.eventValidation); //$NON-NLS-1$
		post.addParameter(status.dynAttrPhoneNumber, status.phoneNumber); 
		post.addParameter(status.dynAttrPassword, status.mobilePassword); 
		post.addParameter(status.dynAttrBtnId, "Identifizieren"); //$NON-NLS-1$ 

		return executePost(client, post);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#handleCredentialsResponse(java.lang.String)
	 */
	@Override
	public void handleCredentialsResponse(String responseData) throws Exception {
		ATrustStatus status = getStatus();
		String viewState = status.viewState;
		String eventValidation = status.eventValidation;
		String sessionID = status.sessionID;
		String refVal = null;
		String signatureDataURL = null;
		String viewstateGenerator = status.viewStateGenerator;

		status.errorMessage = null;

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

			PostMethod post = new PostMethod(status.baseURL + "/ExpiresInfo.aspx?sid=" + t_sessionID); //$NON-NLS-1$
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

			PostMethod post = new PostMethod(status.baseURL + "/tanAppInfo.aspx?sid=" + t_sessionID); //$NON-NLS-1$
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
			signatureDataURL = status.baseURL + "/ShowSigobj.aspx" + //$NON-NLS-1$
					MobileBKUHelper.extractSubstring(responseData, "ShowSigobj.aspx", "'"); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				String qrCode = MobileBKUHelper.extractValueFromTagWithParam(responseData, "img", "class", "qrcode", "src"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				log.debug("QR Code found: " + qrCode); //$NON-NLS-1$
				status.qrCodeURL = qrCode;
			} catch (Exception e) {
				log.debug("No QR Code found"); //$NON-NLS-1$
			}
			try {
				String tanTextTan = MobileBKUHelper.extractValueFromTagWithParam(responseData, "label", "id", "label_for_input_tan", "for"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				status.tanField = tanTextTan.equals("input_tan");
				status.dynAttrTan = MobileBKUHelper.getDynamicNameAttribute(responseData, Constants.LABEL_TAN);
				status.dynAttrSignButton = MobileBKUHelper.getDynamicNameAttribute(responseData, Constants.LABEL_SIGN_BTN);
			} catch (Exception e) {
				log.debug("No tan field found"); //$NON-NLS-1$
			}
			try {
				String tanTextTan = MobileBKUHelper.extractContentFromTagWithParam(responseData, "span", "id", "text_tan"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				status.isAPPTan = !tanTextTan.toLowerCase().contains("sms");
				status.dynAttrTan = MobileBKUHelper.getDynamicNameAttribute(responseData, Constants.LABEL_TAN);
				status.dynAttrSignButton = MobileBKUHelper.getDynamicNameAttribute(responseData, Constants.LABEL_SIGN_BTN);
			}catch (Exception e) {
				log.debug("No text_tan tag"); //$NON-NLS-1$
			}
			
		} else if (responseData.contains("sl:InfoboxReadResponse")) { //$NON-NLS-1$
			// credentials ok! InfoboxReadResponse
			log.debug("Credentials accepted - Response given"); //$NON-NLS-1$
			getSigningState().setSignatureResponse(new SLResponse(responseData, getStatus().server, null, null));
			return;
		} else if (responseData.contains("undecided.aspx?sid=")) { //$NON-NLS-1$
			// skip intermediate page 
			log.debug("Page Undecided"); //$NON-NLS-1$
			getSigningState().setSignatureResponse(new SLResponse(responseData, getStatus().server, null, null));
			status.errorMessage = "waiting..."; //$NON-NLS-1$
			return; 
		}else {
			// error page
			// extract error text!
			try {
				String errorMessage = MobileBKUHelper.extractContentFromTagWithParam(responseData, "span", "class", "ErrorClass"); //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$
				status.errorMessage = errorMessage;
			} catch (Exception e) {
				throw new SignatureException(MobileBKUHelper.extractSubstring(responseData, "<sl:ErrorCode>", "</sl:ErrorCode>") + ": " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						MobileBKUHelper.extractSubstring(responseData, "<sl:Info>", "</sl:Info>")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// force UI again!
			status.mobilePassword = null;
		}

		log.debug("sessionID: " + sessionID);
		log.debug("Vergleichswert: " + refVal);
		log.debug("viewState: " + viewState);
		log.debug("eventValidation: " + eventValidation);
		log.debug("signatureDataURL: " + signatureDataURL);

		status.sessionID = sessionID;
		status.refVal = refVal;
		status.viewState = viewState;
		status.eventValidation = eventValidation;
		status.signatureDataURL = signatureDataURL;
		status.viewStateGenerator = viewstateGenerator;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#postTAN()
	 */
	@Override
	public String postTAN() throws IOException {
		ATrustStatus status = getStatus();
	
		MobileBKUHelper.registerTrustedSocketFactory();
		HttpClient client = MobileBKUHelper.getHttpClient(getStatus());
	
		PostMethod post = new PostMethod(status.baseURL
				+ "/signature.aspx?sid=" + status.sessionID); //$NON-NLS-1$
		post.getParams().setContentCharset("utf-8"); //$NON-NLS-1$
		post.addParameter("__VIEWSTATE", status.viewState); //$NON-NLS-1$
		post.addParameter(
				"__EVENTVALIDATION", status.eventValidation); //$NON-NLS-1$
		post.addParameter(status.dynAttrTan, status.tan); 
		post.addParameter(status.dynAttrSignButton, "Signieren"); //$NON-NLS-1$ 
		post.addParameter("Button1", "Identifizieren"); //$NON-NLS-1$ //$NON-NLS-2$
	
		return executePost(client, post);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#handleTANResponse(java.lang.String)
	 */
	@Override
	public void handleTANResponse(String responseData) {
		getStatus().errorMessage = null;
		if (responseData.contains("sl:CreateXMLSignatureResponse xmlns:sl") || //$NON-NLS-1$
		    responseData.contains("sl:CreateCMSSignatureResponse xmlns:sl")) { //$NON-NLS-1$
			// success !!
			
			getSigningState().setSignatureResponse(
					new SLResponse(responseData, getStatus().server, null, null));
		} else {
			try {
				String tries = MobileBKUHelper.extractSubstring(
						responseData, "Sie haben noch", "Versuch"); //$NON-NLS-1$ //$NON-NLS-2$
				getStatus().tanTries = Integer.parseInt(tries.trim());
				getStatus().errorMessage = "mobileBKU.wrong_tan";
			} catch (Exception e) {
				getStatus().tanTries = (getStatus().tanTries - 1);
				log.debug("Error parsing TAN response", e); //$NON-NLS-1$
			}

			if (getStatus().tanTries <= 0) {
				getStatus().errorMessage = null;
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						Dialog dialog = new Dialog(ATrustHandler.this.shell, Messages.getString("common.warning"), //$NON-NLS-1$
								Messages.getString("mobileBKU.tan_tries_exceeded"), //$NON-NLS-1$
								BUTTONS.OK_CANCEL, ICON.QUESTION);
						
						// TODO: THIS IS A COLOSSAL HACK
						if (dialog.open() == SWT.CANCEL) {
							// Go back to BKU Selection
							getStatus().tanTries = -1;
						} else {
							// Start signature process over
							getStatus().tanTries = -2;
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

		GetMethod get = new GetMethod(status.baseURL
				+ "/sendsms.aspx?sid=" + status.sessionID);
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

		GetMethod get = new GetMethod(status.baseURL + "/" + status.qrCodeURL);

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

		//TODO check
		//String baseURL = "https://www.a-trust.at/mobile/https-security-layer-request";
		GetMethod get = new GetMethod(status.baseURL
				+ "/signature.aspx?sid=" + status.sessionID); //$NON-NLS-1$

		return executeGet(client, get);
	}
	
	/**
	 * @param responseData
	 * @return a boolean
	 */
	public Boolean handleWaitforAppResponse(String responseData) {
		
		getStatus().errorMessage = null;
		if (!responseData.toLowerCase().contains("Bitte starten Sie Ihre Handy-Signatur App!".toLowerCase())/* ||  //$NON-NLS-1$
		    responseData.toLowerCase().contains("TAN (Handy-Signatur App)".toLowerCase())*/) { //$NON-NLS-1$

			return true;
		}
		return false; 
	}

	/**
	 * Parse QR code response
	 * @param responseData
	 * @return whether a SL response was received
	 */
	public boolean handleQRResponse(String responseData) {
		getStatus().errorMessage = null;
		if (responseData.contains("sl:CreateXMLSignatureResponse xmlns:sl") || //$NON-NLS-1$
		    responseData.contains("sl:CreateCMSSignatureResponse xmlns:sl")) { //$NON-NLS-1$
			// success !!

			getSigningState().setSignatureResponse(
					new SLResponse(responseData, getStatus().server, null, null));
			return true;
		}
		return false;
	}

	@Override
	public ATrustStatus getStatus() {
		return (ATrustStatus) state.status;
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
	public boolean handlePolling() throws ATrustConnectionException {
		
		ATrustStatus status = getStatus();
		URLConnection urlconnection = null;
		String isReady = null;
		Status serverStatus = null;
		int waits = 0;
		final String ERROR = "Error: Server is not responding"; //$NON-NLS-1$
		HttpClient client;
		try {
			do {
				client = MobileBKUHelper.getHttpClient(getStatus());
				String uri = status.baseURL  + "/UndecidedPolling.aspx?sid=" + status.sessionID;
				GetMethod get = new GetMethod(uri);

				//client.setTimeout(35000);
				//client.setConnectionTimeout(35000);
				get.addRequestHeader("Accept", "application/json, text/javascript");
				get.addRequestHeader("Connection", "keep-alive");
				get.addRequestHeader("Referer", uri);


				int returnValue = client.executeMethod(get);
				InputStream in = new BufferedInputStream(get.getResponseBodyAsStream());

				isReady = IOUtils.toString(in, "utf-8"); //$NON-NLS-1$
				serverStatus = new Status(isReady);

				if (serverStatus.isFin()) {
					return true;
				} else if (serverStatus.isError()) {
					log.error("A-Trust returned Error code during polling");
					throw new ATrustConnectionException();
				}

			} while (serverStatus.isWait());

			if (serverStatus.isFin()) {
				return true; 
			}
			//else error
			status.errorMessage = "Server reponded ERROR during polling";
			log.error("Server reponded ERROR during polling"); //$NON-NLS-1$
			throw new ATrustConnectionException();

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


