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
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.bku.BKUHelper;
import at.asit.pdfover.gui.bku.LocalBKUConnector;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.signator.SLResponse;
import at.asit.pdfover.signator.SigningState;

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

	Exception threadException = null;

	/** Whether to use Base64 or FileUpload Request */
	boolean useBase64Request = false;

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
		private SigningState signingState;

		
		/**
		 * @param localBKUState
		 * @param signingState
		 */
		public SignLocalBKUThread(LocalBKUState localBKUState, SigningState signingState) {
			this.state = localBKUState;
			this.signingState = signingState;
		}

		@Override
		public void run() {
			try {

				HttpClient client = BKUHelper.getHttpClient();

				PostMethod method = new PostMethod(Constants.LOCAL_BKU_URL);

				String sl_request = NULL_OPERATION_REQUEST;
				method.addParameter("XMLRequest", sl_request); //$NON-NLS-1$
				int returnCode = client.executeMethod(method);

				String userAgent = getResponseHeader(method, BKU_RESPONSE_HEADER_USERAGENT);
				String server = getResponseHeader(method, BKU_RESPONSE_HEADER_SERVER);

				if (returnCode != HttpStatus.SC_OK) {
					this.state.threadException = new HttpException(
							method.getResponseBodyAsString());
				} else {
					server = getResponseHeader(method, BKU_RESPONSE_HEADER_SERVER);
					if (server == null)
						server = ""; //$NON-NLS-1$
					else
						if (server.contains("trustDeskbasic")) //$NON-NLS-1$
							LocalBKUState.this.useBase64Request = true;

					userAgent = getResponseHeader(method, BKU_RESPONSE_HEADER_USERAGENT);
					if (userAgent == null)
						userAgent = ""; //$NON-NLS-1$
					String signatureLayout = getResponseHeader(method, BKU_RESPONSE_HEADER_SIGNATURE_LAYOUT);

					String response = method.getResponseBodyAsString();
					log.debug("SL Response: " + response); //$NON-NLS-1$
					SLResponse slResponse = new SLResponse(response, server,
							userAgent, signatureLayout);
					this.signingState.setSignatureResponse(slResponse);
					this.signingState.setUseBase64Request(LocalBKUState.this.useBase64Request);
				}
			} catch (Exception e) {
				log.error("SignLocalBKUThread: ", e); //$NON-NLS-1$

				this.state.threadException = e;
			} finally {
				this.state.updateStateMachine();
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

		SigningState signingState = status.getSigningState();

		if (!signingState.hasSignatureResponse()
				&& this.threadException == null) {
			Thread t = new Thread(new SignLocalBKUThread(this, signingState));
			t.start();
			return;
		}
		signingState.setBKUConnector(new LocalBKUConnector());

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
		this.setNextState(new at.asit.pdfover.gui.workflow.states.SigningState(getStateMachine()));
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
