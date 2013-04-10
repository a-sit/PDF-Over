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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.workflow.states.LocalBKUState;
import at.asit.pdfover.gui.workflow.states.MobileBKUState;

/**
 * 
 */
public class PostCredentialsThread implements Runnable {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(PostCredentialsThread.class);

	private MobileBKUState state;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public PostCredentialsThread(MobileBKUState state) {
		this.state = state;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			MobileBKUStatus status = this.state.getStatus();
			
			
			Protocol.registerProtocol("https", //$NON-NLS-1$
					new Protocol("https", new TrustedSocketFactory(), 443)); //$NON-NLS-1$

			HttpClient client = new HttpClient();
			client.getParams().setParameter("http.useragent", //$NON-NLS-1$
					LocalBKUState.PDF_OVER_USER_AGENT_STRING);
			
			
			
			PostMethod method = new PostMethod(status.getBaseURL() + "/identification.aspx?sid=" + status.getSessionID()); //$NON-NLS-1$
			method.getParams().setContentCharset("utf-8"); //$NON-NLS-1$
			method.addParameter("__VIEWSTATE", status.getViewstate()); //$NON-NLS-1$
			method.addParameter("__EVENTVALIDATION", status.getEventvalidation()); //$NON-NLS-1$
			method.addParameter("handynummer", status.getPhoneNumber()); //$NON-NLS-1$
			method.addParameter("signaturpasswort", status.getMobilePassword()); //$NON-NLS-1$
			method.addParameter("Button_Identification", "Identifizieren"); //$NON-NLS-1$ //$NON-NLS-2$
			
			
			
			int returnCode = client.executeMethod(method);

			String redirectLocation = null;

			GetMethod gmethod = null;
			
			String responseData = null;
			
			// Follow redirects
			do {
				// check return code
				if (returnCode == HttpStatus.SC_MOVED_TEMPORARILY ||
					returnCode == HttpStatus.SC_MOVED_PERMANENTLY) {

					Header locationHeader = method
							.getResponseHeader("location");  //$NON-NLS-1$
					if (locationHeader != null) {
						redirectLocation = locationHeader.getValue();
					} else {
						throw new IOException(
								"Got HTTP 302 but no location to follow!");  //$NON-NLS-1$
					}
				} else if(returnCode == HttpStatus.SC_OK) {
					if(gmethod != null) {
						responseData = gmethod.getResponseBodyAsString();
					} else {
						responseData = method.getResponseBodyAsString();
					} 
					redirectLocation = null;
				} else {
					throw new HttpException(HttpStatus.getStatusText(returnCode));
				}
				
				if(redirectLocation != null) {
					gmethod = new GetMethod(redirectLocation);
					gmethod.setFollowRedirects(true);
					returnCode = client.executeMethod(gmethod);
				}
				
			} while(redirectLocation != null);

			// Now we have received some data lets check it:
			
			log.debug("Repsonse from A-Trust: " + responseData); //$NON-NLS-1$
			
			String viewState = status.getViewstate();
			String eventValidation = status.getEventvalidation();
			String sessionID = status.getSessionID();
			
			String refVal = null;
			
			status.setRefVal(null);
			status.setErrorMessage(null);
			
			if(responseData.contains("signature.aspx?sid=")) { //$NON-NLS-1$
				// credentials ok! TAN eingabe
				sessionID = MobileBKUHelper.extractTag(responseData, "signature.aspx?sid=", "\""); //$NON-NLS-1$ //$NON-NLS-2$
				viewState = MobileBKUHelper.extractTag(responseData, "id=\"__VIEWSTATE\" value=\"", "\""); //$NON-NLS-1$  //$NON-NLS-2$
				
				eventValidation = MobileBKUHelper.extractTag(responseData, "id=\"__EVENTVALIDATION\" value=\"", "\""); //$NON-NLS-1$  //$NON-NLS-2$
				
				refVal = MobileBKUHelper.extractTag(responseData, "id='vergleichswert'><b>Vergleichswert:</b>", "</div>");  //$NON-NLS-1$//$NON-NLS-2$
				
				status.setRefVal(refVal);
				
				this.state.setCommunicationState(MobileBKUCommunicationState.POST_TAN);
			} else {
				// error seite
				// extract error text!
				
				String errorMessage = MobileBKUHelper.extractTag(responseData, "<span id=\"Label1\" class=\"ErrorClass\">", "</span>"); //$NON-NLS-1$ //$NON-NLS-2$
				
				this.state.getStatus().setErrorMessage(errorMessage);
				
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
			
		} catch (Exception ex) {
			log.error("Error in PostCredentialsThread", ex); //$NON-NLS-1$
			this.state.setThreadException(ex);
		} finally {
			this.state.invokeUpdate();
		}
	}

}
