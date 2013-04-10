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

import at.asit.pdfover.gui.workflow.states.LocalBKUState;
import at.asit.pdfover.gui.workflow.states.MobileBKUState;
import at.asit.pdfover.signator.DocumentSource;
import at.asit.pdfover.signator.SLRequest;
import at.asit.pdfover.signator.SigningState;

/**
 * A mobile BKU Handler
 */
public abstract class MobileBKUHandler {
	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(MobileBKUHandler.class);

	private MobileBKUState state;

	/**
	 * A FileUploadSource
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
	 * Constructor
	 * @param state the MobileBKUState
	 */
	public MobileBKUHandler(MobileBKUState state)
	{
		this.state = state;
	}

	/**
	 * Post the SL request
	 * @param mobileBKUUrl mobile BKU URL
	 * @return the response
	 * @throws IOException IO error
	 */
	public String postSLRequest(String mobileBKUUrl) throws IOException {
		/*
		 * String sl_request = this.state.getSigningState()
		 * .getSignatureRequest().getBase64Request();
		 */
		String sl_request = getSignatureRequest().getFileUploadRequest();

		log.debug("SL Request: " + sl_request); //$NON-NLS-1$

		Protocol.registerProtocol("https", //$NON-NLS-1$
				new Protocol("https", new TrustedSocketFactory(), 443)); //$NON-NLS-1$

		HttpClient client = new HttpClient();
		client.getParams().setParameter("http.useragent", //$NON-NLS-1$
				LocalBKUState.PDF_OVER_USER_AGENT_STRING);

		PostMethod post = new PostMethod(mobileBKUUrl);

		//method.addParameter("XMLRequest", sl_request); //$NON-NLS-1$

		StringPart xmlpart = new StringPart(
				"XMLRequest", sl_request, "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$

		FilePart filepart = new FilePart("fileupload",	//$NON-NLS-1$
				new FileUploadSource(getSignatureRequest().getSignatureData()));

		Part[] parts = { xmlpart, filepart };

		post.setRequestEntity(new MultipartRequestEntity(parts, post
				.getParams()));

		this.state.getStatus().setBaseURL(
				MobileBKUHelper.stripQueryString(mobileBKUUrl));

		return executePost(client, post);
	}

	/**
	 * Handle the response to the SL request post
	 * @param responseData response data
	 * @throws Exception Error during handling
	 */
	public abstract void handleSLRequestResponse(String responseData) throws Exception;


	/**
	 * Post the credentials
	 * @return the response
	 * @throws Exception Error during posting
	 */
	public abstract String postCredentials() throws Exception;

	/**
	 * Handle the response to credentials post
	 * @param responseData response data
	 * @throws Exception Error during handling
	 */
	public abstract void handleCredentialsResponse(String responseData) throws Exception;

	/**
	 * Post the TAN
	 * @return the response
	 * @throws Exception Error during posting
	 */
	public abstract String postTAN() throws Exception;

	/**
	 * Handle the response to TAN post
	 * @param responseData response data
	 * @throws Exception Error during handling
	 */
	public abstract void handleTANResponse(String responseData) throws Exception;

	/**
	 * Get the MobileBKUState
	 * @return the MobileBKUState
	 */
	protected MobileBKUState getState() {
		return this.state;
	}

	/**
	 * Get the MobileBKUStatus
	 * @return the MobileBKUStatus
	 */
	protected MobileBKUStatus getStatus() {
		return this.state.getStatus();
	}

	/**
	 * Get the SigningState
	 * @return the SigningState
	 */
	protected SigningState getSigningState() {
		return getState().getSigningState();
	}

	/**
	 * Get the SLRequest
	 * @return the SLRequest
	 */
	private SLRequest getSignatureRequest() {
		return getSigningState().getSignatureRequest();
	}

	/**
	 * Execute a post to the mobile BKU, following redirects
	 * @param client the HttpClient
	 * @param post the PostMethod
	 * @return the response
	 * @throws IOException IO error
	 */
	protected String executePost(HttpClient client, PostMethod post) throws IOException {
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

				Header locationHeader = post.getResponseHeader("location"); //$NON-NLS-1$
				if (locationHeader != null) {
					redirectLocation = locationHeader.getValue();
				} else {
					throw new IOException(
							"Got HTTP 302 but no location to follow!"); //$NON-NLS-1$
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
			} else {
				throw new HttpException(
						HttpStatus.getStatusText(returnCode));
			}

			if (redirectLocation != null) {
				get = new GetMethod(redirectLocation);
				get.setFollowRedirects(true);
				returnCode = client.executeMethod(get);
			}
		} while (redirectLocation != null);

		getStatus().setServer(server);
		if (server != null)
			log.info("Server: " + server); //$NON-NLS-1$

		return responseData;
	}
}
