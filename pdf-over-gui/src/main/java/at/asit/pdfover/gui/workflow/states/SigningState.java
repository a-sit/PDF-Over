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

//Imports
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.signator.Signer;

/**
 * Logical state for signing process, usually show BKU Dialog during this state.
 */
public class SigningState extends State {

	/**
	 * 
	 */
	private final class FinishSignThread implements Runnable {
		
		private SigningState state;
		
		/**
		 * @param signingState
		 */
		public FinishSignThread(SigningState signingState) {
			this.state = signingState;
		}

		@Override
		public void run() {
			try {
				Signer signer = this.state.stateMachine.getPDFSigner().getPDFSigner();
				Status status = this.state.stateMachine.getStatus();
				
				status.setSignResult(signer.sign(status.getSigningState()));
			} catch(Exception e) {
				log.error("FinishSignThread: ", e); //$NON-NLS-1$
				this.state.threadException = e;
			} finally {
				this.state.stateMachine.invokeUpdate();
			}
		}
	}

	/**
	 * @param stateMachine
	 */
	public SigningState(StateMachine stateMachine) {
		super(stateMachine);
	}

	/**
	 * SFL4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(SigningState.class);
	
	Exception threadException = null;
	
	@Override
	public void run() {
		Status status = this.stateMachine.getStatus();
		
		if(status.getSignResult() == null && 
			this.threadException == null) {
			Thread t = new Thread(new FinishSignThread(this));
			t.start();
			return;
		}
		
		if(this.threadException != null) {
			ErrorDialog error = new ErrorDialog(this.stateMachine.getGUIProvider().getMainShell(),
					Messages.getString("error.Signatur"), BUTTONS.RETRY_CANCEL);  //$NON-NLS-1$
			this.threadException = null;
			if(error.open() == SWT.RETRY) {
				this.setNextState(new BKUSelectionState(this.stateMachine));
			} else {
				// FIXME: Exit?
				this.stateMachine.exit();
			}
			return;
		}

		this.setNextState(new OutputState(this.stateMachine));
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#cleanUp()
	 */
	@Override
	public void cleanUp() {
		// No composite - no cleanup necessary
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#setMainWindowBehavior()
	 */
	@Override
	public void updateMainWindowBehavior() {
		//no change of behavior necessary
	}

	@Override
	public String toString()  {
		return this.getClass().getName();
	}
}
