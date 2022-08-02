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
import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.controls.Dialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.Dialog.ICON;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.states.MobileBKUState;
import at.asit.pdfover.signator.SLResponse;

/**
 *
 */
public class IAIKHandler extends MobileBKUHandler {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(IAIKHandler.class);

	Shell shell;

	/**
	 * Constructor
	 * @param state the MobileBKUState
	 * @param shell the Shell
	 */
	public IAIKHandler(MobileBKUState state, Shell shell) {
		super(state);
		this.shell = shell;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#handleSLRequestResponse(java.lang.String)
	 */
	@Override
	public void handleSLRequestResponse(String responseData) throws Exception {
		IAIKStatus status = getStatus();

		// Extract infos:
		String credentialURL = MobileBKUHelper.extractValueFromTagWithParam(responseData,
				"form", "name", "userCredLogon", "action");
		URL baseURL = new URL(status.baseURL);
		credentialURL = MobileBKUHelper.getQualifiedURL(credentialURL, baseURL);

		String viewState = MobileBKUHelper.extractValueFromTagWithParam(
				responseData, "input", "name", "javax.faces.ViewState", "value");

		String sessionID = null;
		int si = credentialURL.indexOf("jsessionid=");
		if (si != -1)
			sessionID = credentialURL.substring(si + 11);
		else
			sessionID = status.sessionID;

		log.info("credentialURL: " + credentialURL);
		log.info("sessionID: " + sessionID);
		log.info("viewState: " + viewState);

		status.baseURL = credentialURL;
		if (sessionID != null)
			status.sessionID = sessionID;
		status.viewState = viewState;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#postCredentials()
	 */
	@Override
	public String postCredentials() throws Exception {
		IAIKStatus status = getStatus();

		MobileBKUHelper.registerTrustedSocketFactory();
		HttpClient client = MobileBKUHelper.getHttpClient(status);

		PostMethod post = new PostMethod(status.ensureSessionID(status.baseURL));
		post.getParams().setContentCharset("utf-8");
		post.addParameter("javax.faces.ViewState", status.viewState);
		post.addParameter("userCredLogon:phoneNr", status.phoneNumber);
		post.addParameter("userCredLogon:pwd", status.mobilePassword);
		post.addParameter("userCredLogon:logonButton", "userCredLogon:logonButton");
		post.addParameter("javax.faces.partial.ajax", "true");
		post.addParameter("javax.faces.source", "userCredLogon:logonButton");
		post.addParameter("javax.faces.partial.execute", "@all");
		post.addParameter("javax.faces.partial.render", "userCredLogon:userCredentialLogonPanel");
		post.addParameter("userCredLogon", "userCredLogon");
		post.addParameter("userCredLogon:j_idt33_input", "de");

		return executePost(client, post);
}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#handleCredentialsResponse(java.lang.String)
	 */
	@Override
	public void handleCredentialsResponse(String responseData) throws Exception {
		IAIKStatus status = getStatus();

		String refVal = null;
		String signatureDataURL = null;

		status.errorMessage = null;

		if (!responseData.contains("redirection_url")) {
			// Assume that an error occurred

			String errorMessage;
			try {
				errorMessage = MobileBKUHelper.extractSubstring(responseData, ":errorMessage\">", "</span>");
			} catch (Exception e) {
				errorMessage = Messages.getString("error.Unexpected");
			}
			status.errorMessage = errorMessage;

			// force UI again!
			status.mobilePassword = null;
			return;
		}

		HttpClient client = MobileBKUHelper.getHttpClient(status);

		String redirectURL = MobileBKUHelper.extractSubstring(responseData,
				"\"redirection_url\":\"", "\"");

		URL baseURL = new URL(status.baseURL);
		redirectURL = MobileBKUHelper.getQualifiedURL(redirectURL, baseURL);
		redirectURL = status.ensureSessionID(redirectURL);

		responseData = getRedirect(client, redirectURL);

		if (responseData.contains("sl:InfoboxReadResponse")) {
			// credentials ok! InfoboxReadResponse
			getSigningState().setSignatureResponse(
					new SLResponse(responseData, status.server, null, null));
			return;
		}

		if (responseData.contains("tanCodeLogon"))
		{
			refVal = MobileBKUHelper.extractContentFromTagWithParam(responseData,
					"span", "id", "tanCodeLogon:refValue");
		}
		else
		{
			refVal = MobileBKUHelper.extractContentFromTagWithParam(responseData,
					"span", "id", "j_idt5:refValue");
		}



		if (responseData.contains("/error")) {
			// Error response - try again
			String errorMessage = MobileBKUHelper.extractContentFromTagWithParam(
					responseData, "div", "id", "errorPanel:panel_content");
			if (errorMessage.contains("<br />"))
				errorMessage = errorMessage.substring(0, errorMessage.indexOf("<br />"));
			errorMessage.replace("\n", " ");
			status.errorMessage = errorMessage;

			status.mobilePassword = null;
			return;
		}




		String viewState = MobileBKUHelper.extractValueFromTagWithParam(
				responseData, "input", "name", "javax.faces.ViewState", "value");
		status.viewState = viewState;

		if (!responseData.contains("tanCodeLogon.jsf")) {
			// Assume that we need to confirm reference value dialog
			log.debug("viewState: " + viewState);



			PostMethod post = new PostMethod(redirectURL);
			post.getParams().setContentCharset("utf-8");
			post.addParameter("javax.faces.partial.ajax", "true");
			post.addParameter("javax.faces.source", "j_idt5:yesButton");
			post.addParameter("javax.faces.partial.execute", "@all");
			post.addParameter("j_idt5:yesButton", "j_idt5:yesButton");
			post.addParameter("j_idt5", "j_idt5");
			post.addParameter("javax.faces.ViewState", status.viewState);
			responseData = executePost(client, post);

			log.debug("Response: " + responseData);
			if (responseData.contains("/error")) {
				// Error response - try again
				String errorMessage = Messages.getString("error.Unexpected");
				status.errorMessage = errorMessage;

				status.mobilePassword = null;
				return;
			}

			redirectURL = MobileBKUHelper.extractSubstring(responseData,
					"redirect url=\"", "\"");
			baseURL = new URL(status.baseURL);
			redirectURL = MobileBKUHelper.getQualifiedURL(redirectURL, baseURL);
			redirectURL = status.ensureSessionID(redirectURL);

			responseData = getRedirect(client, redirectURL);

			viewState = MobileBKUHelper.extractValueFromTagWithParam(
					responseData, "input", "name", "javax.faces.ViewState", "value");
			status.viewState = viewState;
		}

		signatureDataURL = status.baseURL;
		signatureDataURL = signatureDataURL.substring(0, signatureDataURL.lastIndexOf('/') + 1);
		signatureDataURL += "viewer.jsf" +
				MobileBKUHelper.extractSubstring(responseData, "viewer.jsf", "\"");
		signatureDataURL += (signatureDataURL.contains("?") ? "&" : "?") +
				"pdfoversessionid=" + status.sessionID;

		String tanURL = MobileBKUHelper.extractValueFromTagWithParam(responseData,
				"form", "name", "tanCodeLogon", "action");
		baseURL = new URL(status.baseURL);
		tanURL = MobileBKUHelper.getQualifiedURL(tanURL, baseURL);
		tanURL = status.ensureSessionID(tanURL);

		log.debug("reference value: " + refVal);
		log.debug("signatureDataURL: " + signatureDataURL);
		log.debug("tanURL: " + tanURL);
		log.debug("viewState: " + viewState);

		status.refVal = refVal;
		status.signatureDataURL = signatureDataURL;
		status.baseURL = tanURL;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#postTAN()
	 */
	@Override
	public String postTAN() throws Exception {
		IAIKStatus status = getStatus();

		MobileBKUHelper.registerTrustedSocketFactory();
		HttpClient client = MobileBKUHelper.getHttpClient(status);

		PostMethod post = new PostMethod(status.baseURL);
		post.getParams().setContentCharset("utf-8");
		post.addParameter("javax.faces.ViewState", status.viewState);
		post.addParameter("tanCodeLogon", "tanCodeLogon");
		post.addParameter("tanCodeLogon:signButton", "");
		post.addParameter("tanCodeLogon:authCode", status.tan);
		post.addParameter("referenceValue", status.refVal);

		return executePost(client, post);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#handleTANResponse(java.lang.String)
	 */
	@Override
	public void handleTANResponse(String responseData) throws Exception {
		final IAIKStatus status = getStatus();
		status.errorMessage = null;
		if (responseData.contains("sl:CreateCMSSignatureResponse xmlns:sl")) {
			// success
			getSigningState().setSignatureResponse(
					new SLResponse(responseData, status.server, null, null));
		} else {
			try {
				String errorMessage = MobileBKUHelper.extractContentFromTagWithParam(
						responseData, "p", "class", "ui-messages-error ui-messages-error-signing");
				status.errorMessage = errorMessage;
				log.error(errorMessage);

				//Go back to TAN entry
				MobileBKUHelper.registerTrustedSocketFactory();
				HttpClient client = MobileBKUHelper.getHttpClient(status);

				PostMethod post = new PostMethod(status.baseURL);
				post.getParams().setContentCharset("utf-8");
				post.addParameter("javax.faces.partial.ajax", "true");
				post.addParameter("javax.faces.source", "tanCodeLogon:backbutton");
				post.addParameter("javax.faces.partial.execute", "@all");
				post.addParameter("javax.faces.partial.render", "tanCodeLogon:tanCodeLogonPanel");
				post.addParameter("tanCodeLogon:backbutton", "tanCodeLogon:backbutton");
				post.addParameter("tanCodeLogon", "tanCodeLogon");
				post.addParameter("javax.faces.ViewState", status.viewState);

				executePost(client, post);
			} catch (Exception e) {
				// Assume that wrong TAN was entered too many times
				Display.getDefault().syncExec(() -> {
					Dialog dialog = new Dialog(IAIKHandler.this.shell, Messages.getString("common.warning"),
							Messages.getString("mobileBKU.tan_tries_exceeded"),
							BUTTONS.OK_CANCEL, ICON.QUESTION);
					// TODO: ALSO A COLOSSAL HACK HERE
					if (dialog.open() == SWT.CANCEL) {
						// Go back to BKU Selection
						status.tanTries = -1;
					} else {
						// Start signature process over
						status.tanTries = -2;
					}
				});
			}
		}
	}

	@Override
	public IAIKStatus getStatus() {
		return (IAIKStatus) state.status;
	}

	private String getRedirect(HttpClient client, String redirectURL) throws HttpException, IOException {
		redirectURL = getStatus().ensureSessionID(redirectURL);
		log.debug("Sending get request to URL " + redirectURL);

		GetMethod get = new GetMethod(redirectURL);
		int returnCode = client.executeMethod(get);
		if (returnCode != HttpStatus.SC_OK) {
			throw new HttpException(HttpStatus.getStatusText(returnCode));
		}
		String responseData = get.getResponseBodyAsString();
		log.debug("Response: " + responseData);
		return responseData;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.bku.mobile.MobileBKUHandler#useBase64Request()
	 */
	@Override
	public boolean useBase64Request() {
		return false;
	}

	@Override
	public boolean handlePolling() {
		//nothing todo
		return true;
	}
}
