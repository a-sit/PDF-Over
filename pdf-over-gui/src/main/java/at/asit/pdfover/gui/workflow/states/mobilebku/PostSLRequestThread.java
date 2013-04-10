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

import javax.xml.ws.http.HTTPException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
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
public class PostSLRequestThread implements Runnable {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(PostSLRequestThread.class);

	private MobileBKUState state;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public PostSLRequestThread(MobileBKUState state) {
		this.state = state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			String sl_request = this.state.getSigningState()
					.getSignatureRequest().getBase64Request();

			Protocol.registerProtocol("https", //$NON-NLS-1$
					new Protocol("https", new TrustedSocketFactory(), 443)); //$NON-NLS-1$

			HttpClient client = new HttpClient();
			client.getParams().setParameter("http.useragent", //$NON-NLS-1$
					LocalBKUState.PDF_OVER_USER_AGENT_STRING);

			// TODO: move URL to config!!
			
			//String url = "https://www.a-trust.at/mobile/https-security-layer-request/default.aspx";
			String url = "https://test1.a-trust.at/https-security-layer-request/default.aspx";
			
			PostMethod method = new PostMethod(url);

			method.addParameter("XMLRequest", sl_request); //$NON-NLS-1$

			int returnCode = client.executeMethod(method);

			String redirectLocation = null;

			GetMethod gmethod = null;
			
			String responseData = null;
			
			this.state.getStatus().setBaseURL(ATrustHelper.stripQueryString(url));
			
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
			
			// Extract infos:
			
			String sessionID = ATrustHelper.extractTag(responseData, "identification.aspx?sid=", "\""); //$NON-NLS-1$ //$NON-NLS-2$
			
			String viewState = ATrustHelper.extractTag(responseData, "id=\"__VIEWSTATE\" value=\"", "\""); //$NON-NLS-1$  //$NON-NLS-2$
			
			String eventValidation = ATrustHelper.extractTag(responseData, "id=\"__EVENTVALIDATION\" value=\"", "\""); //$NON-NLS-1$  //$NON-NLS-2$
			
			log.info("sessionID: " + sessionID); //$NON-NLS-1$
			log.info("viewState: " + viewState); //$NON-NLS-1$
			log.info("eventValidation: " + eventValidation); //$NON-NLS-1$
			
			this.state.getStatus().setSessionID(sessionID);
			
			this.state.getStatus().setViewstate(viewState);
			
			this.state.getStatus().setEventvalidation(eventValidation);
			
			/*
			 * If all went well we can set the communication state to the new
			 * state
			 */
			this.state
					.setCommunicationState(MobileBKUCommunicationState.POST_NUMBER);
		} catch (Exception ex) {
			log.error("Error in PostSLRequestThread", ex); //$NON-NLS-1$
			this.state.setThreadException(ex);
		} finally {
			this.state.invokeUpdate();
		}
	}

}
