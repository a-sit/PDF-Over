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

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.bku.BKUHelper;
import at.asit.pdfover.gui.bku.LocalBKUConnector;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.signer.pdfas.PdfAs4SigningState;
import lombok.extern.slf4j.Slf4j;

/**
 * Logical state for performing the BKU Request to a local BKU
 */
@Slf4j
public class LocalBKUState extends State {

	/**
	 * HTTP Response server HEADER
	 */
	public final static String BKU_RESPONSE_HEADER_SERVER = "server";

	/**
	 * HTTP Response user-agent HEADER
	 */
	public final static String BKU_RESPONSE_HEADER_USERAGENT = "user-agent";

	/**
	 * HTTP Response SignatureLayout HEADER
	 */
	public final static String BKU_RESPONSE_HEADER_SIGNATURE_LAYOUT = "SignatureLayout";

	Exception threadException = null;

	/** Whether to use Base64 or FileUpload Request */
	boolean useBase64Request = false;

	/**
	 * Null-Operation SL-Request
	 */
	private final static String NULL_OPERATION_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<sl:NullOperationRequest xmlns:sl=\"http://www.buergerkarte.at/namespaces/securitylayer/1.2#\"/>";

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
		private PdfAs4SigningState signingState;


		/**
		 * @param localBKUState
		 * @param signingState
		 */
		public SignLocalBKUThread(LocalBKUState localBKUState, PdfAs4SigningState signingState) {
			this.state = localBKUState;
			this.signingState = signingState;
		}

		@Override
		public void run() {
			try {

				HttpClient client = (HttpClient) BKUHelper.getHttpClient();

				PostMethod method = new PostMethod(Constants.LOCAL_BKU_URL);

				String sl_request = NULL_OPERATION_REQUEST;
				method.addParameter("XMLRequest", sl_request);
				int returnCode = client.executeMethod(method);

				if (returnCode != HttpStatus.SC_OK) {
					this.state.threadException = new HttpException(
							method.getResponseBodyAsString());
				} else {
					String server = getResponseHeader(method, BKU_RESPONSE_HEADER_SERVER);
					if ((server != null) && (server.contains("trustDeskbasic") || server.contains("asignSecurityLayer")))
						LocalBKUState.this.useBase64Request = true;

					this.signingState.signatureResponse = method.getResponseBodyAsString();
					this.signingState.useBase64Request = LocalBKUState.this.useBase64Request;
				}
			} catch (Exception e) {
				log.error("SignLocalBKUThread: ", e);

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
		Status status = getStateMachine().status;

		PdfAs4SigningState signingState = status.signingState;

		if ((signingState.signatureResponse == null)
				&& this.threadException == null) {
			Thread t = new Thread(new SignLocalBKUThread(this, signingState));
			t.start();
			return;
		}
		signingState.bkuConnector = new LocalBKUConnector();

		if (this.threadException != null) {
			ErrorDialog dialog = new ErrorDialog(
					getStateMachine().getMainShell(),
					Messages.getString("error.LocalBKU"),
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
		MainWindowBehavior behavior = getStateMachine().status.behavior;
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
