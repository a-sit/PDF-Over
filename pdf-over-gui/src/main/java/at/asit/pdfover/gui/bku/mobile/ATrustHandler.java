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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.IOUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import at.asit.pdfover.gui.utils.FileUploadSource;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.states.LocalBKUState;
import at.asit.pdfover.gui.workflow.states.MobileBKUState;
import at.asit.pdfover.signer.pdfas.PdfAs4SLRequest;
import at.asit.pdfover.signer.pdfas.PdfAs4SigningState;

/**
 * A-Trust mobile BKU handler
 */
public class ATrustHandler {
	public final MobileBKUState state;
	public final Shell shell;

	/**
	 * @param state
	 * @param shell
	 */
	public ATrustHandler(MobileBKUState state, Shell shell) {
		this.state = state;
		this.shell = shell;
	}

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(ATrustHandler.class);

	private static boolean expiryNoticeDisplayed = false;

	private static final String ACTIVATION_URL = "https://www.handy-signatur.at/";

	/**
	 * Get the MobileBKUStatus
	 * @return the MobileBKUStatus
	 */
	protected ATrustStatus getStatus() {
		return this.state.status;
	}

	/**
	 * Get the SigningState
	 * @return the SigningState
	 */
	protected PdfAs4SigningState getSigningState() {
		return state.getSigningState();
	}

	/**
	 * Execute a post to the mobile BKU, following redirects
	 * @param client the HttpClient
	 * @param post the PostMethod
	 * @return the response
	 * @throws IOException IO error
	 */
	protected String executePost(HttpClient client, PostMethod post) throws IOException {
		if (log.isDebugEnabled()) {
			String req;
			if (post.getRequestEntity().getContentLength() < 1024) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				post.getRequestEntity().writeRequest(os);
				req = os.toString();
				if (req.contains("passwort="))
					req = req.replaceAll("passwort=[^&]*", "passwort=******");
				if (req.contains(":pwd="))
					req = req.replaceAll(":pwd=[^&]*", ":pwd=******");
				os.close();
			} else {
				req = post.getRequestEntity().getContentLength() + " bytes";
			}
			log.debug("Posting to " + post.getURI() + ": " + req);
		}
		int returnCode = client.executeMethod(post);

		String redirectLocation = null;
		GetMethod get = null;


		String responseData = null;

		String server = null;

		// Follow redirects
		do {
			// check return code
			if (returnCode == HttpStatus.SC_MOVED_TEMPORARILY ||
				returnCode == HttpStatus.SC_MOVED_PERMANENTLY) {

				Header locationHeader = post.getResponseHeader("location");
				if (locationHeader != null) {
					redirectLocation = locationHeader.getValue();
				} else {
					throw new IOException(
							"Got HTTP 302 but no location to follow!");
				}
			} else if (returnCode == HttpStatus.SC_OK) {
				if (get != null) {
					responseData = get.getResponseBodyAsString();
					Header serverHeader = get.getResponseHeader(
							LocalBKUState.BKU_RESPONSE_HEADER_SERVER);
					if (serverHeader != null)
						server = serverHeader.getValue();
				} else {
					responseData = post.getResponseBodyAsString();

					Header serverHeader = post.getResponseHeader(
							LocalBKUState.BKU_RESPONSE_HEADER_SERVER);
					if (serverHeader != null)
						server = serverHeader.getValue();
				}
				redirectLocation = null;
				String p = "<meta [^>]*http-equiv=\"refresh\" [^>]*content=\"([^\"]*)\"";
				Pattern pat = Pattern.compile(p);
				Matcher m = pat.matcher(responseData);
				if (m.find()) {
					String content = m.group(1);
					int start = content.indexOf("URL=");
					if (start != -1) {
						start += 9;
						redirectLocation  = content.substring(start, content.length() - 5);
					}
				}
			} else {
				throw new HttpException(
						HttpStatus.getStatusText(returnCode));
			}

			if (redirectLocation != null) {
				redirectLocation = MobileBKUHelper.getQualifiedURL(redirectLocation, new URL(post.getURI().toString()));
				log.debug("Redirected to " + redirectLocation);
				get = new GetMethod(redirectLocation);
				get.setFollowRedirects(true);
				returnCode = client.executeMethod(get);
			}
		} while (redirectLocation != null);

		getStatus().server = server;
		if (server != null)
			log.debug("Server: " + server);

		return responseData;
	}

	/**
	 * Execute a get from the mobile BKU, following redirects
	 * @param client the HttpClient
	 * @param get the GetMethod
	 * @return the response
	 * @throws IOException IO error
	 */
	protected String executeGet(HttpClient client, GetMethod get) throws IOException {
		log.debug("Getting " + get.getURI());

		int returnCode = client.executeMethod(get);

		String redirectLocation = null;

		GetMethod get2 = null;

		String responseData = null;

		String server = null;

		// Follow redirects
		do {
			// check return code
			if (returnCode == HttpStatus.SC_MOVED_TEMPORARILY ||
				returnCode == HttpStatus.SC_MOVED_PERMANENTLY) {

				Header locationHeader = get.getResponseHeader("location");
				if (locationHeader != null) {
					redirectLocation = locationHeader.getValue();
				} else {
					throw new IOException(
							"Got HTTP 302 but no location to follow!");
				}
			} else if (returnCode == HttpStatus.SC_OK) {
				if (get2 != null) {
					responseData = get2.getResponseBodyAsString();
					Header serverHeader = get2.getResponseHeader(
							LocalBKUState.BKU_RESPONSE_HEADER_SERVER);
					if (serverHeader != null)
						server = serverHeader.getValue();
				} else {
					responseData = get.getResponseBodyAsString();

					Header serverHeader = get.getResponseHeader(
							LocalBKUState.BKU_RESPONSE_HEADER_SERVER);
					if (serverHeader != null)
						server = serverHeader.getValue();
				}
				redirectLocation = null;
				String p = "<meta [^>]*http-equiv=\"refresh\" [^>]*content=\"([^\"]*)\"";
				Pattern pat = Pattern.compile(p);
				Matcher m = pat.matcher(responseData);
				if (m.find()) {
					String content = m.group(1);
					int start = content.indexOf("URL=");
					if (start != -1) {
						start += 9;
						redirectLocation  = content.substring(start, content.length() - 5);
					}
				}
			} else {
				throw new HttpException(
						HttpStatus.getStatusText(returnCode));
			}

			if (redirectLocation != null) {
				redirectLocation = MobileBKUHelper.getQualifiedURL(redirectLocation, new URL(get.getURI().toString()));
				log.debug("Redirected to " + redirectLocation);
				get2 = new GetMethod(redirectLocation);
				get2.setFollowRedirects(true);
				returnCode = client.executeMethod(get2);
			}
		} while (redirectLocation != null);

		getStatus().server = server;
		if (server != null)
			log.debug("Server: " + server);

		return responseData;
	}

	/**
	 * Post the SL request
	 * @param mobileBKUUrl mobile BKU URL
	 * @param request SLRequest
	 * @return the response
	 * @throws IOException IO error
	 */
	public String postSLRequest(String mobileBKUUrl, PdfAs4SLRequest request) throws IOException {
		MobileBKUHelper.registerTrustedSocketFactory();
		HttpClient client = MobileBKUHelper.getHttpClient(getStatus());

		PostMethod post = new PostMethod(mobileBKUUrl);
		String sl_request;
		if (request.getSignatureData() != null) {
			sl_request = request.getRequest();
			StringPart xmlpart = new StringPart(
					"XMLRequest", sl_request, "UTF-8");

			FilePart filepart = new FilePart("fileupload",
					new FileUploadSource(request.getSignatureData()),
					"application/pdf", "UTF-8");

			Part[] parts = { xmlpart, filepart };

			post.setRequestEntity(new MultipartRequestEntity(parts, post
					.getParams()));
		} else { /* TODO is this ever false? */
			sl_request = request.getRequest();
			post.addParameter("XMLRequest", sl_request);
		}
		log.trace("SL Request: " + sl_request);

		state.status.baseURL = MobileBKUHelper.stripQueryString(mobileBKUUrl);

		return executePost(client, post);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#handleSLRequestResponse(java.lang.String)
	 */
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


		log.debug("sessionID: " + sessionID);
		log.debug("viewState: " + viewState);
		log.debug("eventValidation: " + eventValidation);

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
	public String postCredentials() throws IOException {
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
	public void handleCredentialsResponse(final String responseData) throws Exception {
		ATrustStatus status = getStatus();
		String viewState = status.viewState;
		String eventValidation = status.eventValidation;
		String sessionID = status.sessionID;
		String refVal = null;
		String signatureDataURL = null;
		String viewstateGenerator = status.viewStateGenerator;

		status.errorMessage = null;

		final Document responseDocument = Jsoup.parse(responseData);

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

			handleCredentialsResponse(executePost(client, post));
			return;
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

			handleCredentialsResponse(executePost(client, post));
			return;
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
			try {
				String webauthnLink = MobileBKUHelper.extractValueFromTagWithParam(responseData, "a", "id", "FidoButton", "href");
				log.info("Webauthn link: {}", webauthnLink);
			} catch (Exception e) {
				log.info("No webauthnLink");
			}
			try {
				String webauthnData = MobileBKUHelper.extractValueFromTagWithParam(responseData, "input", "id", "credentialOptions", "value");
				log.info("Fido credential options: {}", webauthnData);
			} catch (Exception e) {
				log.info("No webauthnData");
			}

		} else if (responseData.contains("sl:InfoboxReadResponse")) {
			// credentials ok! InfoboxReadResponse
			state.rememberCredentialsIfNecessary();
			log.debug("Credentials accepted - Response given");
			getSigningState().signatureResponse = responseData;
			return;
		} else if (responseData.contains("undecided.aspx?sid=")) {
			// skip intermediate page
			log.debug("Page Undecided");
			getSigningState().signatureResponse = responseData;
			status.errorMessage = "waiting..."; // TODO: this looks incorrect...?
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
	public void handleTANResponse(String responseData) {
		getStatus().errorMessage = null;
		if (responseData.contains("sl:CreateXMLSignatureResponse xmlns:sl") ||
		    responseData.contains("sl:CreateCMSSignatureResponse xmlns:sl")) {
			// success !!

			getSigningState().signatureResponse = responseData;
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
	 * Parse QR code response
	 * @param responseData
	 * @return whether a SL response was received
	 */
	public boolean handleQRResponse(String responseData) {
		getStatus().errorMessage = null;
		if (responseData.contains("sl:CreateXMLSignatureResponse xmlns:sl") ||
		    responseData.contains("sl:CreateCMSSignatureResponse xmlns:sl")) {
			// success !!

			getSigningState().signatureResponse = responseData;
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 */
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


