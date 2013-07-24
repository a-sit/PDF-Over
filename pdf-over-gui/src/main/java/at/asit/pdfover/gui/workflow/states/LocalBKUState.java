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
package at.asit.pdfover.gui.workflow.states;

// Imports
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.utils.FileUploadSource;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHelper;
import at.asit.pdfover.signator.SLRequest;
import at.asit.pdfover.signator.SLResponse;

/**
 * Logical state for performing the BKU Request to a local BKU
 */
public class LocalBKUState extends State {

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(LocalBKUState.class);

	/**
	 * HTTP Response server HEADER
	 */
	public final static String BKU_RESPONSE_HEADER_SERVER = "server"; //$NON-NLS-1$

	/**
	 * HTTP Response user-agent HEADER
	 */
	public final static String BKU_RESPONSE_HEADER_USERAGENT = "user-agent"; //$NON-NLS-1$

	/**
	 * HTTP Response SignatureLayout HEADER
	 */
	public final static String BKU_RESPONSE_HEADER_SIGNATURE_LAYOUT = "SignatureLayout"; //$NON-NLS-1$

	at.asit.pdfover.signator.SigningState signingState;

	Exception threadException = null;

	/**
	 * Null-Operation SL-Request
	 */
	private final static String NULL_OPERATION_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + //$NON-NLS-1$
			"<sl:NullOperationRequest xmlns:sl=\"http://www.buergerkarte.at/namespaces/securitylayer/1.2#\"/>"; //$NON-NLS-1$

	/**
	 * Constructor
	 * @param stateMachine the StateMachine
	 */
	public LocalBKUState(StateMachine stateMachine) {
		super(stateMachine);
	}

	/**
	 * 
	 */
	private final class SignLocalBKUThread implements Runnable {

		private LocalBKUState state;

		/** Whether to use Base64 or FileUpload Request */
		private boolean useBase64Request = false;

		
		/**
		 * @param localBKUState
		 */
		public SignLocalBKUThread(LocalBKUState localBKUState) {
			this.state = localBKUState;
		}

		@Override
		public void run() {
			try {
				SLRequest request = this.state.signingState
						.getSignatureRequest();

				HttpClient client = MobileBKUHelper.getHttpClient();

				PostMethod method = new PostMethod(
						"http://127.0.0.1:3495/http-security-layer-request"); //$NON-NLS-1$

				String sl_request = NULL_OPERATION_REQUEST;
				method.addParameter("XMLRequest", sl_request); //$NON-NLS-1$
				int returnCode = client.executeMethod(method);

				String userAgent = getResponseHeader(method, BKU_RESPONSE_HEADER_USERAGENT);
				String server = getResponseHeader(method, BKU_RESPONSE_HEADER_SERVER);
				if (server != null && server.contains("trustDeskbasic")) //$NON-NLS-1$
					this.useBase64Request = true; // TDB doesn't support MultiPart requests

				method = new PostMethod(
						"http://127.0.0.1:3495/http-security-layer-request"); //$NON-NLS-1$

				if (this.useBase64Request)
				{
					sl_request = request.getBase64Request();
					method.addParameter("XMLRequest", sl_request); //$NON-NLS-1$
				} else {
					sl_request = request.getFileUploadRequest();
					StringPart xmlpart = new StringPart(
							"XMLRequest", sl_request, "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$

					FilePart filepart = new FilePart("fileupload",	//$NON-NLS-1$
							new FileUploadSource(request.getSignatureData()));

					Part[] parts = { xmlpart, filepart };

					method.setRequestEntity(new MultipartRequestEntity(parts, method
							.getParams()));
				}
				//log.debug("SL REQUEST: " + sl_request); //$NON-NLS-1$

				returnCode = client.executeMethod(method);

				if (returnCode == HttpStatus.SC_OK) {
					server = getResponseHeader(method, BKU_RESPONSE_HEADER_SERVER);
					if (server == null)
						server = ""; //$NON-NLS-1$
					userAgent = getResponseHeader(method, BKU_RESPONSE_HEADER_USERAGENT);
					if (userAgent == null)
						userAgent = ""; //$NON-NLS-1$
					String signatureLayout = getResponseHeader(method, BKU_RESPONSE_HEADER_SIGNATURE_LAYOUT);

					String response = method.getResponseBodyAsString();
					log.debug("SL Response: " + response); //$NON-NLS-1$
					SLResponse slResponse = new SLResponse(response, server,
							userAgent, signatureLayout);
					this.state.signingState.setSignatureResponse(slResponse);
				} else {
					this.state.threadException = new HttpException(
							method.getResponseBodyAsString());
				}

			} catch (Exception e) {
				log.error("SignLocalBKUThread: ", e); //$NON-NLS-1$
				//
				this.state.threadException = e;
			} finally {
				this.state.getStateMachine().invokeUpdate();
			}
		}

		/**
		 * Returns the value corresponding to the given header name
		 * @param method the HTTP method
		 * @param headerName the header name
		 * @return the header value (or null if not found)
		 */
		private String getResponseHeader(HttpMethod method, String headerName) {
			if (method.getResponseHeader(headerName) == null)
				return null;
			return method.getResponseHeader(headerName).getValue();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.workflow.WorkflowState#update(at.asit.pdfover.gui
	 * .workflow.Workflow)
	 */
	@Override
	public void run() {
		Status status = getStateMachine().getStatus();

		this.signingState = status.getSigningState();

		if (!this.signingState.hasSignatureResponse()
				&& this.threadException == null) {
			Thread t = new Thread(new SignLocalBKUThread(this));
			t.start();
			return;
		}

		if (this.threadException != null) {
			ErrorDialog dialog = new ErrorDialog(
					getStateMachine().getGUIProvider().getMainShell(),
					Messages.getString("error.LocalBKU"), //$NON-NLS-1$
					BUTTONS.RETRY_CANCEL);
			if (dialog.open() != SWT.RETRY) {
				//getStateMachine().exit();
				this.setNextState(new BKUSelectionState(getStateMachine()));
				return;
			}
			this.threadException = null;
			this.run();
			return;
		}

		// OK
		this.setNextState(new SigningState(getStateMachine()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.states.State#cleanUp()
	 */
	@Override
	public void cleanUp() {
		// No composite - no cleanup necessary
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.states.State#setMainWindowBehavior()
	 */
	@Override
	public void updateMainWindowBehavior() {
		MainWindowBehavior behavior = getStateMachine().getStatus()
				.getBehavior();
		behavior.reset();
		behavior.setActive(Buttons.OPEN, true);
		behavior.setActive(Buttons.POSITION, true);
		behavior.setActive(Buttons.SIGN, true);
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
