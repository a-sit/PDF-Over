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
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.controls.Dialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.Dialog.ICON;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.states.MobileBKUState;
import at.asit.pdfover.signator.SLResponse;

/**
 * 
 */
public class IAIKHandler extends MobileBKUHandler {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(IAIKHandler.class);

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
		String credentialURL = MobileBKUHelper.extractTag(responseData,
				"name=\"userCredLogon\" method=\"post\" action=\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$
		URL baseURL = new URL(status.getBaseURL());
		int p = baseURL.getPort();
		String port = ((p != -1) && (p != baseURL.getDefaultPort())) ? ":" + p : ""; //$NON-NLS-1$ //$NON-NLS-2$
		credentialURL = baseURL.getProtocol() + "://" + baseURL.getHost() + port + //$NON-NLS-1$
		(credentialURL.startsWith("/") ? "" : "/") + credentialURL; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		String viewState = MobileBKUHelper.extractTag(responseData,
				"id=\"javax.faces.ViewState\" value=\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$

		String sessionID = credentialURL.substring(credentialURL.indexOf("jsessionid=") + 11); //$NON-NLS-1$

		log.info("credentialURL: " + credentialURL); //$NON-NLS-1$
		log.info("sessionID: " + sessionID); //$NON-NLS-1$
		log.info("viewState: " + viewState); //$NON-NLS-1$

		status.setBaseURL(credentialURL);
		status.setSessionID(sessionID);
		status.setViewState(viewState);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#postCredentials()
	 */
	@Override
	public String postCredentials() throws Exception {
		IAIKStatus status = getStatus();
		
		Protocol.registerProtocol("https", //$NON-NLS-1$
				new Protocol("https", new TrustedSocketFactory(), 443)); //$NON-NLS-1$
	
		HttpClient client = MobileBKUHelper.getHttpClient();

		PostMethod post = new PostMethod(status.getBaseURL());
		post.getParams().setContentCharset("utf-8"); //$NON-NLS-1$
		post.addParameter("javax.faces.ViewState", status.getViewState()); //$NON-NLS-1$
		post.addParameter("userCredLogon:phoneNR", status.getPhoneNumber()); //$NON-NLS-1$
		post.addParameter("userCredLogon:pwd", status.getMobilePassword()); //$NON-NLS-1$
		post.addParameter("userCredLogon:logonButton", "userCredLogon:logonButton"); //$NON-NLS-1$ //$NON-NLS-2$
		post.addParameter("javax.faces.partial.ajax", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		post.addParameter("javax.faces.source", "userCredLogon:logonButton"); //$NON-NLS-1$ //$NON-NLS-2$
		post.addParameter("javax.faces.partial.execute", "@all"); //$NON-NLS-1$ //$NON-NLS-2$
		post.addParameter("javax.faces.partial.render", "userCredLogon:errorMessagePanel userCredLogon:errorMessage"); //$NON-NLS-1$ //$NON-NLS-2$
		post.addParameter("userCredLogon", "userCredLogon"); //$NON-NLS-1$ //$NON-NLS-2$
		post.addParameter("userCredLogon:j_idt20_input", "de"); //$NON-NLS-1$ //$NON-NLS-2$

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

		status.setErrorMessage(null);

		if (!responseData.contains("referenceValueLogon.jsf")) { //$NON-NLS-1$
			// Assume that an error occurred

			String errorMessage;
			try {
				errorMessage = MobileBKUHelper.extractTag(responseData, ":errorMessage\">", "</span>"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (Exception e) {
				errorMessage = Messages.getString("error.Unexpected"); //$NON-NLS-1$
			}
			status.setErrorMessage(errorMessage);

			// force UI again!
			status.setMobilePassword(null);
			return;
		}

		HttpClient client = MobileBKUHelper.getHttpClient();

		String redirectURL = status.getBaseURL().substring(0,
				status.getBaseURL().lastIndexOf('/',
						status.getBaseURL().lastIndexOf('/') - 1) + 1); //Cut off last directory
		redirectURL += MobileBKUHelper.extractTag(responseData,
				"redirection_url\":\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$
		redirectURL = status.ensureSessionID(redirectURL);

		responseData = getRedirect(client, redirectURL);

		refVal = MobileBKUHelper.extractTag(responseData,
				"id=\"j_idt6:refValue\" class=\"strong\">", "</"); //$NON-NLS-1$ //$NON-NLS-2$

		String viewState = MobileBKUHelper.extractTag(responseData,
				"id=\"javax.faces.ViewState\" value=\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$
		status.setViewState(viewState);

		if (!responseData.contains("tanCodeLogon.jsf")) { //$NON-NLS-1$
			// Assume that we need to confirm reference value dialog
			log.debug("viewState: " + viewState); //$NON-NLS-1$

			PostMethod post = new PostMethod(redirectURL);
			post.getParams().setContentCharset("utf-8"); //$NON-NLS-1$
			post.addParameter("javax.faces.partial.ajax", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			post.addParameter("javax.faces.source", "j_idt6:j_idt14"); //$NON-NLS-1$ //$NON-NLS-2$
			post.addParameter("javax.faces.partial.execute", "@all"); //$NON-NLS-1$ //$NON-NLS-2$
			post.addParameter("j_idt6:j_idt14", "j_idt6:j_idt14"); //$NON-NLS-1$ //$NON-NLS-2$
			post.addParameter("j_idt6", "j_idt6"); //$NON-NLS-1$ //$NON-NLS-2$
			post.addParameter("javax.faces.ViewState", status.getViewState()); //$NON-NLS-1$
			responseData = executePost(client, post);

			log.debug("Response: " + responseData); //$NON-NLS-1$
			if (responseData.contains("/error")) { //$NON-NLS-1$
				// Error response - try again
				String errorMessage = Messages.getString("error.Unexpected"); //$NON-NLS-1$
				status.setErrorMessage(errorMessage);

				status.setMobilePassword(null);
				return;
			}

			redirectURL = MobileBKUHelper.extractTag(responseData,
					"redirect url=\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$
			URL baseURL = new URL(status.getBaseURL());
			int p = baseURL.getPort();
			String port = ((p != -1) && (p != baseURL.getDefaultPort())) ? ":" + p : ""; //$NON-NLS-1$ //$NON-NLS-2$
			redirectURL = baseURL.getProtocol() + "://" + baseURL.getHost() + port +//$NON-NLS-1$
					(redirectURL.startsWith("/") ? "" : "/") + redirectURL; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			responseData = getRedirect(client, redirectURL);

			viewState = MobileBKUHelper.extractTag(responseData,
					"id=\"javax.faces.ViewState\" value=\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$
			status.setViewState(viewState);
		}

		signatureDataURL = status.getBaseURL();
		signatureDataURL = signatureDataURL.substring(0, signatureDataURL.lastIndexOf('/') + 1);
		signatureDataURL += "viewer.jsf" + //$NON-NLS-1$
				MobileBKUHelper.extractTag(responseData, "viewer.jsf", "\""); //$NON-NLS-1$ //$NON-NLS-2$
		signatureDataURL += (signatureDataURL.contains("?") ? "&" : "?") + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"pdfoversessionid=" + status.getSessionID(); //$NON-NLS-1$

		String tanURL = MobileBKUHelper.extractTag(responseData,
				"name=\"j_idt6\" method=\"post\" action=\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$
		URL baseURL = new URL(status.getBaseURL());
		int p = baseURL.getPort();
		String port = ((p != -1) && (p != baseURL.getDefaultPort())) ? ":" + p : ""; //$NON-NLS-1$ //$NON-NLS-2$
		tanURL = baseURL.getProtocol() + "://" + baseURL.getHost() + port + //$NON-NLS-1$
				(tanURL.startsWith("/") ? "" : "/") + tanURL; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		tanURL = status.ensureSessionID(tanURL);

		log.debug("reference value: " + refVal); //$NON-NLS-1$
		log.debug("signatureDataURL: " + signatureDataURL); //$NON-NLS-1$
		log.debug("tanURL: " + tanURL); //$NON-NLS-1$
		log.debug("viewState: " + viewState); //$NON-NLS-1$

		status.setRefVal(refVal);
		status.setSignatureDataURL(signatureDataURL);
		status.setBaseURL(tanURL);
		getState().setCommunicationState(MobileBKUCommunicationState.POST_TAN);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#postTAN()
	 */
	@Override
	public String postTAN() throws Exception {
		IAIKStatus status = getStatus();
		
		Protocol.registerProtocol("https", //$NON-NLS-1$
				new Protocol("https", new TrustedSocketFactory(), 443)); //$NON-NLS-1$
	
		HttpClient client = MobileBKUHelper.getHttpClient();

		PostMethod post = new PostMethod(status.getBaseURL());
		post.getParams().setContentCharset("utf-8"); //$NON-NLS-1$
		post.addParameter("javax.faces.ViewState", status.getViewState()); //$NON-NLS-1$
		post.addParameter("j_idt6:signButton", ""); //$NON-NLS-1$ //$NON-NLS-2$
		post.addParameter("j_idt6:j_idt16", status.getTan()); //$NON-NLS-1$
		post.addParameter("j_idt6", "j_idt6"); //$NON-NLS-1$ //$NON-NLS-2$

		return executePost(client, post);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler#handleTANResponse(java.lang.String)
	 */
	@Override
	public void handleTANResponse(String responseData) throws Exception {
		getStatus().setErrorMessage(null);
		if (responseData.contains("sl:CreateXMLSignatureResponse xmlns:sl")) { //$NON-NLS-1$
			// success
			getSigningState().setSignatureResponse(
					new SLResponse(responseData, getStatus().getServer(), null, null));
			getState().setCommunicationState(MobileBKUCommunicationState.FINAL);
		} else {
			try {
				String errorMessage = MobileBKUHelper.extractTag(responseData,
						":errorMessage\">", "</span>"); //$NON-NLS-1$ //$NON-NLS-2$
				getStatus().setErrorMessage(errorMessage);
			} catch (Exception e) {
				// Assume that wrong TAN was entered too many times
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						Dialog dialog = new Dialog(IAIKHandler.this.shell, Messages.getString("common.warning"), //$NON-NLS-1$
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
	public IAIKStatus getStatus() {
		return (IAIKStatus) getState().getStatus();
	}

	private String getRedirect(HttpClient client, String redirectURL) throws HttpException, IOException {
		redirectURL = getStatus().ensureSessionID(redirectURL);
		log.debug("Sending get request to URL " + redirectURL); //$NON-NLS-1$

		GetMethod get = new GetMethod(redirectURL);
		int returnCode = client.executeMethod(get);
		if (returnCode != HttpStatus.SC_OK) {
			throw new HttpException(HttpStatus.getStatusText(returnCode));
		}
		String responseData = get.getResponseBodyAsString();
		log.debug("Response: " + responseData); //$NON-NLS-1$
		return responseData;
	}
}
