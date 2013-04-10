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
import at.asit.pdfover.signator.SLResponse;

/**
 * 
 */
public class PostTanThread implements Runnable {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(PostTanThread.class);

	private MobileBKUState state;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public PostTanThread(MobileBKUState state) {
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
			MobileBKUStatus status = this.state.getStatus();

			Protocol.registerProtocol("https", //$NON-NLS-1$
					new Protocol("https", new TrustedSocketFactory(), 443)); //$NON-NLS-1$

			HttpClient client = new HttpClient();
			client.getParams().setParameter("http.useragent", //$NON-NLS-1$
					LocalBKUState.PDF_OVER_USER_AGENT_STRING);

			PostMethod method = new PostMethod(status.getBaseURL()
					+ "/signature.aspx?sid=" + status.getSessionID()); //$NON-NLS-1$

			method.addParameter("__VIEWSTATE", status.getViewstate()); //$NON-NLS-1$
			method.addParameter(
					"__EVENTVALIDATION", status.getEventvalidation()); //$NON-NLS-1$
			method.addParameter("input_tan", status.getTan()); //$NON-NLS-1$
			method.addParameter("SignButton", "Signieren"); //$NON-NLS-1$ //$NON-NLS-2$
			method.addParameter("Button1", "Identifizieren"); //$NON-NLS-1$ //$NON-NLS-2$

			int returnCode = client.executeMethod(method);

			String redirectLocation = null;

			GetMethod gmethod = null;

			String responseData = null;

			String server = ""; //$NON-NLS-1$

			// Follow redirects
			do {
				// check return code
				if (returnCode == HttpStatus.SC_MOVED_TEMPORARILY
						|| returnCode == HttpStatus.SC_MOVED_PERMANENTLY) {

					Header locationHeader = method
							.getResponseHeader("location"); //$NON-NLS-1$
					if (locationHeader != null) {
						redirectLocation = locationHeader.getValue();
					} else {
						throw new IOException(
								"Got HTTP 302 but no location to follow!"); //$NON-NLS-1$
					}
				} else if (returnCode == HttpStatus.SC_OK) {

					if (gmethod != null) {
						responseData = gmethod.getResponseBodyAsString();

						if (gmethod
								.getResponseHeader(LocalBKUState.BKU_REPSONE_HEADER_SERVER) != null) {
							server = gmethod.getResponseHeader(
									LocalBKUState.BKU_REPSONE_HEADER_SERVER)
									.getValue();
						}

					} else {
						responseData = method.getResponseBodyAsString();

						if (method
								.getResponseHeader(LocalBKUState.BKU_REPSONE_HEADER_SERVER) != null) {
							server = method.getResponseHeader(
									LocalBKUState.BKU_REPSONE_HEADER_SERVER)
									.getValue();
						}

					}
					redirectLocation = null;
				} else {
					throw new HttpException(
							HttpStatus.getStatusText(returnCode));
				}

				if (redirectLocation != null) {
					gmethod = new GetMethod(redirectLocation);
					gmethod.setFollowRedirects(true);
					returnCode = client.executeMethod(gmethod);
				}

			} while (redirectLocation != null);

			// Now we have received some data lets check it:

			log.debug("Repsonse from A-Trust: " + responseData); //$NON-NLS-1$

			log.info("Server: " + server); //$NON-NLS-1$

			if (responseData.contains("sl:CreateXMLSignatureResponse xmlns:sl")) { //$NON-NLS-1$
				// success !!

				this.state.getSigningState().setSignatureResponse(
						new SLResponse(responseData, server, null, null));
				this.state
						.setCommunicationState(MobileBKUCommunicationState.FINAL);
			} else {
				status.decreaseTanTries();

				if (status.getTanTries() <= 0) {
					// move to POST_REQUEST
					this.state.setCommunicationState(MobileBKUCommunicationState.POST_REQUEST);
				}
			}
		} catch (Exception ex) {
			log.error("Error in PostTanThread", ex); //$NON-NLS-1$
			this.state.setThreadException(ex);
		} finally {
			this.state.invokeUpdate();
		}
	}

}
