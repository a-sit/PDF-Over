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
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.workflow.ConfigManipulator;
import at.asit.pdfover.gui.workflow.states.LocalBKUState;
import at.asit.pdfover.gui.workflow.states.MobileBKUState;
import at.asit.pdfover.signator.DocumentSource;

/**
 * 
 */
public class PostSLRequestThread implements Runnable {
	/**
	 * 
	 */
	private final class FileUploadSource implements PartSource {

		private DocumentSource source;

		/**
		 * Constructor
		 * 
		 * @param source
		 *            the source
		 */
		public FileUploadSource(DocumentSource source) {
			this.source = source;
		}

		@Override
		public long getLength() {
			// TODO Auto-generated method stub
			return this.source.getLength();
		}

		@Override
		public String getFileName() {
			return "sign.pdf"; //$NON-NLS-1$
		}

		@Override
		public InputStream createInputStream() throws IOException {
			return this.source.getInputStream();
		}
	}

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(PostSLRequestThread.class);

	private MobileBKUState state;

	private String mobileBKUUrl = ConfigManipulator.MOBILE_BKU_URL_CONFIG;

	/**
	 * Constructor
	 * 
	 * @param state
	 * @param mobileBKUUrl
	 */
	public PostSLRequestThread(MobileBKUState state, String mobileBKUUrl) {
		this.state = state;
		this.mobileBKUUrl = mobileBKUUrl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			/*
			 * String sl_request = this.state.getSigningState()
			 * .getSignatureRequest().getBase64Request();
			 */
			String sl_request = this.state.getSigningState()
					.getSignatureRequest().getFileUploadRequest();

			log.debug("SL Request: " + sl_request); //$NON-NLS-1$

			Protocol.registerProtocol("https", //$NON-NLS-1$
					new Protocol("https", new TrustedSocketFactory(), 443)); //$NON-NLS-1$

			HttpClient client = new HttpClient();
			client.getParams().setParameter("http.useragent", //$NON-NLS-1$
					LocalBKUState.PDF_OVER_USER_AGENT_STRING);

			String url = this.mobileBKUUrl;

			PostMethod method = new PostMethod(url);

			//method.addParameter("XMLRequest", sl_request); //$NON-NLS-1$

			StringPart xmlpart = new StringPart(
					"XMLRequest", sl_request, "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$

			FilePart filepart = new FilePart("fileupload",	//$NON-NLS-1$
					new FileUploadSource(this.state.getSigningState()
							.getSignatureRequest().getSignatureData())); 

			Part[] parts = { xmlpart, filepart };

			method.setRequestEntity(new MultipartRequestEntity(parts, method
					.getParams()));
			int returnCode = client.executeMethod(method);

			String redirectLocation = null;

			GetMethod gmethod = null;

			String responseData = null;

			this.state.getStatus().setBaseURL(
					ATrustHelper.stripQueryString(url));

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
					} else {
						responseData = method.getResponseBodyAsString();
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

			// Extract infos:

			String sessionID = ATrustHelper.extractTag(responseData,
					"identification.aspx?sid=", "\""); //$NON-NLS-1$ //$NON-NLS-2$

			String viewState = ATrustHelper.extractTag(responseData,
					"id=\"__VIEWSTATE\" value=\"", "\""); //$NON-NLS-1$  //$NON-NLS-2$

			String eventValidation = ATrustHelper.extractTag(responseData,
					"id=\"__EVENTVALIDATION\" value=\"", "\""); //$NON-NLS-1$  //$NON-NLS-2$

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
