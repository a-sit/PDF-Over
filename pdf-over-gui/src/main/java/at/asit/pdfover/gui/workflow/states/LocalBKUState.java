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
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.signator.SLRequest;
import at.asit.pdfover.signator.SLResponse;

/**
 * Logical state for performing the BKU Request to a local BKU
 */
public class LocalBKUState extends State {
	
	/**
	 * HTTP Response server HEADER
	 */
	public final static String BKU_REPSONE_HEADER_SERVER = "server"; //$NON-NLS-1$
	
	/**
	 * HTTP Response user-agent HEADER
	 */
	public final static String BKU_REPSONE_HEADER_USERAGENT = "user-agent"; //$NON-NLS-1$
	
	/**
	 * HTTP Response SignatureLayout HEADER
	 */
	public final static String BKU_REPSONE_HEADER_SIGNATURE_LAYOUT = "SignatureLayout"; //$NON-NLS-1$
	
	/**
	 * TODO: move to a better location ...
	 */
	public static final String PDF_OVER_USER_AGENT_STRING = "PDF-Over 4.0"; //$NON-NLS-1$
	
	/**
	 * 
	 */
	private final class SignLocalBKUThread implements Runnable {
		
		private LocalBKUState state;

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

				String sl_request = request.getBase64Request();

				HttpClient client = new HttpClient();
				

				PostMethod method = new PostMethod(
						"http://127.0.0.1:3495/http-security-layer-request"); //$NON-NLS-1$
				
				log.debug("SL REQUEST: " + sl_request); //$NON-NLS-1$
				
				method.addParameter("XMLRequest", sl_request); //$NON-NLS-1$
				
				int returnCode = client.executeMethod(method);
				
				if(returnCode == HttpStatus.SC_OK)
				{
					String server = ""; //$NON-NLS-1$
					String userAgent = ""; //$NON-NLS-1$
					String signatureLayout = ""; //$NON-NLS-1$
					
					if(method.getResponseHeader(BKU_REPSONE_HEADER_SERVER) != null)
					{
						server = method.getResponseHeader(BKU_REPSONE_HEADER_SERVER).getValue();
					}
					
					if(method.getResponseHeader(BKU_REPSONE_HEADER_USERAGENT) != null)
					{
						userAgent = method.getResponseHeader(BKU_REPSONE_HEADER_USERAGENT).getValue();
					}
					
					if(method.getResponseHeader(BKU_REPSONE_HEADER_SIGNATURE_LAYOUT) != null)
					{
						signatureLayout = method.getResponseHeader(BKU_REPSONE_HEADER_SIGNATURE_LAYOUT).getValue();
					}
					
					String response = method.getResponseBodyAsString();
					log.debug("SL Response: " + response); //$NON-NLS-1$
					SLResponse slResponse = new SLResponse(response, server, userAgent, signatureLayout);
					this.state.signingState.setSignatureResponse(slResponse);
				} else {
					this.state.threadException = new HttpException(method.getResponseBodyAsString());
				}

			} catch (Exception e) {
				log.error("SignLocalBKUThread: ", e); //$NON-NLS-1$
				//
				this.state.threadException = e;
			} finally {
				this.state.stateMachine.invokeUpdate();
			}
		}
	}

	/**
	 * @param stateMachine
	 */
	public LocalBKUState(StateMachine stateMachine) {
		super(stateMachine);
	}

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(LocalBKUState.class);

	at.asit.pdfover.signator.SigningState signingState;

	Exception threadException = null;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.workflow.WorkflowState#update(at.asit.pdfover.gui
	 * .workflow.Workflow)
	 */
	@Override
	public void run() {
		Status status = this.stateMachine.getStatus();

		this.signingState = status.getSigningState();

		if (!this.signingState.hasSignatureResponse() && 
			this.threadException == null
			) {
			Thread t = new Thread(new SignLocalBKUThread(this));
			t.start();
			return;
		}

		if(this.threadException != null) {
			ErrorDialog dialog = new ErrorDialog(Display.getCurrent().getActiveShell(), SWT.NONE, "Please check if a local BKU is running", this.threadException);
			dialog.open();
			this.threadException = null;
			this.run();
			return;
		}
		
		// OK
		this.setNextState(new SigningState(this.stateMachine));
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
		MainWindowBehavior behavior = this.stateMachine.getStatus()
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
