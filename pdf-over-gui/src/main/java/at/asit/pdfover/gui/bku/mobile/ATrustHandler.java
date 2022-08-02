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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
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
	static final Logger log = LoggerFactory.getLogger(ATrustHandler.class);

	private static boolean expiryNoticeDisplayed = false;

	private static final String ACTIVATION_URL = "https://www.handy-signatur.at/";

	private boolean useBase64 = false;

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#handleSLRequestResponse(java.lang.String)
	 */
	@Override
	public void handleSLRequestResponse(String responseData) throws Exception {
		ATrustStatus status = getStatus();

		if (responseData.contains("<sl:ErrorResponse")) {
			String errorCode = MobileBKUHelper.extractSubstring(responseData,
					"<sl:ErrorCode>", "</sl:ErrorCode>");
			String errorMsg = MobileBKUHelper.extractSubstring(responseData,
					"<sl:Info>", "</sl:Info>");
			throw new Exception("Error from mobile BKU: " +
					errorCode + " - " + errorMsg);
		}

		// Extract infos:
		String sessionID = MobileBKUHelper.extractSubstring(responseData,
				"identification.aspx?sid=", "\"");

		String viewState = MobileBKUHelper.extractValueFromTagWithParam(
				responseData, "", "id", "__VIEWSTATE", "value");

		String eventValidation = MobileBKUHelper.extractValueFromTagWithParam(
				responseData, "", "id", "__EVENTVALIDATION", "value");

		String viewstateGenerator = MobileBKUHelper.extractValueFromTagWithParamOptional(responseData, "", "id", "__VIEWSTATEGENERATOR", "value");

		String dynamicAttrPhonenumber = MobileBKUHelper.getDynamicNameAttribute(responseData, Constants.LABEL_PHONE_NUMBER);
		String dynamicAttrPassword = MobileBKUHelper.getDynamicNameAttribute(responseData, Constants.LABEL_SIGN_PASS);
		String dynamicAttrButtonId = MobileBKUHelper.getDynamicNameAttribute(responseData, Constants.LABEL_BTN_IDF);
		String dynamicAttrTan = MobileBKUHelper.getDynamicNameAttribute(responseData, Constants.LABEL_TAN);


		log.info("sessionID: " + sessionID);
		log.info("viewState: " + viewState);
		log.info("eventValidation: " + eventValidation);

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

		PostMethod post = new PostMethod(status.baseURL + "/identification.aspx?sid=" + status.sessionID);
		post.getParams().setContentCharset("utf-8");
		post.addParameter("__VIEWSTATE", status.viewState);
		post.addParameter("__VIEWSTATEGENERATOR", status.viewStateGenerator);
		post.addParameter("__EVENTVALIDATION", status.eventValidation);
		post.addParameter(status.dynAttrPhoneNumber, status.phoneNumber);
		post.addParameter(status.dynAttrPassword, status.mobilePassword);
		post.addParameter(status.dynAttrBtnId, "Identifizieren");

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

		if (responseData.contains("ExpiresInfo.aspx?sid=")) {
			// Certificate expiration interstitial - skip
			if (!expiryNoticeDisplayed) {
				Display.getDefault().syncExec(()->  {
					Dialog d = new Dialog(ATrustHandler.this.shell, Messages.getString("common.info"), Messages.getString("mobileBKU.certExpiresSoon"), BUTTONS.YES_NO, ICON.WARNING);
					if (d.open() == SWT.YES) {
						log.debug("Trying to open " + ACTIVATION_URL);
						if (Desktop.isDesktopSupported()) {
							try {
								Desktop.getDesktop().browse(new URI(ACTIVATION_URL));
								return;
							} catch (Exception e) {
								log.debug("Error opening URL", e);
							}
						}
						log.info("SWT Desktop is not supported on this platform");
						Program.launch(ACTIVATION_URL);
					}
				});
				expiryNoticeDisplayed = true;
			}

			String t_sessionID = MobileBKUHelper.extractSubstring(responseData, "ExpiresInfo.aspx?sid=", "\"");
			String t_viewState = MobileBKUHelper.extractValueFromTagWithParam(responseData, "", "id", "__VIEWSTATE", "value");
			String t_eventValidation = MobileBKUHelper.extractValueFromTagWithParam(responseData, "", "id", "__EVENTVALIDATION", "value");

			// Post again to skip
			MobileBKUHelper.registerTrustedSocketFactory();
			HttpClient client = MobileBKUHelper.getHttpClient(getStatus());

			PostMethod post = new PostMethod(status.baseURL + "/ExpiresInfo.aspx?sid=" + t_sessionID);
			post.getParams().setContentCharset("utf-8");
			post.addParameter("__VIEWSTATE", t_viewState);
			post.addParameter("__EVENTVALIDATION", t_eventValidation);
			post.addParameter("Button_Next", "Weiter");

			responseData = executePost(client, post);
			log.trace("Response from mobile BKU: " + responseData);
		} else if (responseData.contains("tanAppInfo.aspx?sid=")) {
			// App info interstitial - skip
			log.info("Skipping tan app interstitial");

			String t_sessionID = MobileBKUHelper.extractSubstring(responseData, "tanAppInfo.aspx?sid=", "\"");
			String t_viewState = MobileBKUHelper.extractValueFromTagWithParam(responseData, "", "id", "__VIEWSTATE", "value");
			String t_eventValidation = MobileBKUHelper.extractValueFromTagWithParam(responseData, "", "id", "__EVENTVALIDATION", "value");

			// Post again to skip
			MobileBKUHelper.registerTrustedSocketFactory();
			HttpClient client = MobileBKUHelper.getHttpClient(getStatus());

			PostMethod post = new PostMethod(status.baseURL + "/tanAppInfo.aspx?sid=" + t_sessionID);
			post.getParams().setContentCharset("utf-8");
			post.addParameter("__VIEWSTATE", t_viewState);
			post.addParameter("__EVENTVALIDATION", t_eventValidation);
			post.addParameter("NextBtn", "Weiter");

			responseData = executePost(client, post);
			log.trace("Response from mobile BKU: " + responseData);
		}

		if (responseData.contains("signature.aspx?sid=")) {
			// credentials ok! TAN entry
			state.rememberCredentialsIfNecessary();
			log.debug("Credentials accepted - TAN required");
			sessionID = MobileBKUHelper.extractSubstring(responseData, "signature.aspx?sid=", "\"");
			viewState = MobileBKUHelper.extractValueFromTagWithParam(responseData, "", "id", "__VIEWSTATE", "value");
			eventValidation = MobileBKUHelper.extractValueFromTagWithParam(responseData, "", "id", "__EVENTVALIDATION", "value");
			refVal = MobileBKUHelper.extractSubstring(responseData, "id='vergleichswert'><b>Vergleichswert:</b>", "</div>");
			signatureDataURL = status.baseURL + "/ShowSigobj.aspx" +
					MobileBKUHelper.extractSubstring(responseData, "ShowSigobj.aspx", "'");
			try {
				String qrCode = MobileBKUHelper.extractValueFromTagWithParam(responseData, "img", "class", "qrcode", "src");
				log.debug("QR Code found: " + qrCode);
				status.qrCodeURL = qrCode;
			} catch (Exception e) {
				log.debug("No QR Code found");
			}
			try {
				String tanTextTan = MobileBKUHelper.extractValueFromTagWithParam(responseData, "label", "id", "label_for_input_tan", "for");
				status.tanField = tanTextTan.equals("input_tan");
				status.dynAttrTan = MobileBKUHelper.getDynamicNameAttribute(responseData, Constants.LABEL_TAN);
				status.dynAttrSignButton = MobileBKUHelper.getDynamicNameAttribute(responseData, Constants.LABEL_SIGN_BTN);
			} catch (Exception e) {
				log.debug("No tan field found");
			}
			try {
				String tanTextTan = MobileBKUHelper.extractContentFromTagWithParam(responseData, "span", "id", "text_tan");
				status.isAPPTan = !tanTextTan.toLowerCase().contains("sms");
				status.dynAttrTan = MobileBKUHelper.getDynamicNameAttribute(responseData, Constants.LABEL_TAN);
				status.dynAttrSignButton = MobileBKUHelper.getDynamicNameAttribute(responseData, Constants.LABEL_SIGN_BTN);
			}catch (Exception e) {
				log.debug("No text_tan tag");
			}

		} else if (responseData.contains("sl:InfoboxReadResponse")) {
			// credentials ok! InfoboxReadResponse
			state.rememberCredentialsIfNecessary();
			log.debug("Credentials accepted - Response given");
			getSigningState().setSignatureResponse(new SLResponse(responseData, getStatus().server, null, null));
			return;
		} else if (responseData.contains("undecided.aspx?sid=")) {
			// skip intermediate page
			log.debug("Page Undecided");
			getSigningState().setSignatureResponse(new SLResponse(responseData, getStatus().server, null, null));
			status.errorMessage = "waiting...";
			return;
		}else {
			// error page

			// force UI again!
			state.clearRememberedCredentials();
			// extract error text!
			try {
				String errorMessage = MobileBKUHelper.extractContentFromTagWithParam(responseData, "span", "id", "Label1");
				if (errorMessage.startsWith("Fehler: "))
					errorMessage = errorMessage.substring(8);
				status.errorMessage = errorMessage.strip();
			} catch (Exception e) {
				log.error("Failed to get credentials error message", e);
				String msg = null;
				try
				{
					msg = MobileBKUHelper.extractSubstring(responseData, "<sl:ErrorCode>", "</sl:ErrorCode>") + ": " +
					  MobileBKUHelper.extractSubstring(responseData, "<sl:Info>", "</sl:Info>");
				} catch (Exception e2) {
					log.error("Failed to get credentials error code", e2);
					msg = Messages.getString("error.Unexpected");
				}
				status.errorMessage = msg.strip();
			}
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
				+ "/signature.aspx?sid=" + status.sessionID);
		post.getParams().setContentCharset("utf-8");
		post.addParameter("__VIEWSTATE", status.viewState);
		post.addParameter(
				"__EVENTVALIDATION", status.eventValidation);
		post.addParameter(status.dynAttrTan, status.tan);
		post.addParameter(status.dynAttrSignButton, "Signieren");
		post.addParameter("Button1", "Identifizieren");

		return executePost(client, post);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#handleTANResponse(java.lang.String)
	 */
	@Override
	public void handleTANResponse(String responseData) {
		getStatus().errorMessage = null;
		if (responseData.contains("sl:CreateXMLSignatureResponse xmlns:sl") ||
		    responseData.contains("sl:CreateCMSSignatureResponse xmlns:sl")) {
			// success !!

			getSigningState().setSignatureResponse(
					new SLResponse(responseData, getStatus().server, null, null));
		} else {
			try {
				String tries = MobileBKUHelper.extractSubstring(
						responseData, "Sie haben noch", "Versuch");
				getStatus().tanTries = Integer.parseInt(tries.trim());
				getStatus().errorMessage = "mobileBKU.wrong_tan";
			} catch (Exception e) {
				getStatus().tanTries = (getStatus().tanTries - 1);
				log.debug("Error parsing TAN response", e);
			}

			if (getStatus().tanTries <= 0) {
				getStatus().errorMessage = null;
				Display.getDefault().syncExec(() -> {
					Dialog dialog = new Dialog(ATrustHandler.this.shell, Messages.getString("common.warning"),
							Messages.getString("mobileBKU.tan_tries_exceeded"),
							BUTTONS.OK_CANCEL, ICON.QUESTION);

					// TODO: THIS IS A COLOSSAL HACK
					if (dialog.open() == SWT.CANCEL) {
						// Go back to BKU Selection
						getStatus().tanTries = -1;
					} else {
						// Start signature process over
						getStatus().tanTries = -2;
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
		get.getParams().setContentCharset("utf-8");

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
			log.debug("Getting " + get.getURI());
			int returnCode = client.executeMethod(get);

			if (returnCode != HttpStatus.SC_OK) {
				log.error("Error getting QR code");
				return null;
			}

			return get.getResponseBodyAsStream();
		} catch (Exception e) {
			log.error("Error getting QR code", e);
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
				+ "/signature.aspx?sid=" + status.sessionID);

		return executeGet(client, get);
	}

	/**
	 * @param responseData
	 * @return a boolean
	 */
	public Boolean handleWaitforAppResponse(String responseData) {

		getStatus().errorMessage = null;
		if (!responseData.toLowerCase().contains("Bitte starten Sie Ihre Handy-Signatur App!".toLowerCase())/* ||
		    responseData.toLowerCase().contains("TAN (Handy-Signatur App)".toLowerCase())*/) {

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
		if (responseData.contains("sl:CreateXMLSignatureResponse xmlns:sl") ||
		    responseData.contains("sl:CreateCMSSignatureResponse xmlns:sl")) {
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
		String isReady = null;
		Status serverStatus = null;
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


				client.executeMethod(get);
				InputStream in = new BufferedInputStream(get.getResponseBodyAsStream());

				isReady = IOUtils.toString(in, "utf-8");
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
			log.error("Server reponded ERROR during polling");
			throw new ATrustConnectionException();

		} catch (Exception e) {
			log.error("handle polling failed" + e.getMessage());
			throw new ATrustConnectionException();
		}
	}

	private class Status {
		private final boolean fin;
		private final boolean error;
		private final boolean wait;

		public Status(String status) {
			 JsonElement jelement = JsonParser.parseString(status.toLowerCase());
			 JsonObject  jobject = jelement.getAsJsonObject();
			 this.fin = jobject.get("fin").getAsBoolean();
			 this.error = jobject.get("error").getAsBoolean();
			 this.wait = jobject.get("wait").getAsBoolean();
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


