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
import java.net.ConnectException;

import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.signer.SignatureException;
import at.asit.pdfover.signer.UserCancelledException;
import at.asit.pdfover.signer.pdfas.PdfAs4Signer;

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
				Status status = this.state.getStateMachine().status;
				status.signResult = PdfAs4Signer.sign(status.signingState);
			} catch(Exception e) {
				this.state.threadException = e;
			} finally {
				this.state.updateStateMachine();
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
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(SigningState.class);

	Exception threadException = null;

	@Override
	public void run() {
		Status status = getStateMachine().status;

		if(status.signResult == null &&
			this.threadException == null) {
			Thread t = new Thread(new FinishSignThread(this));
			t.start();
			return;
		}

		if(this.threadException != null) {
			String message = Messages.getString("error.Signatur");
			if (this.threadException instanceof SignatureException) {
				Throwable cause = this.threadException;
				while (cause.getCause() != null)
					cause = cause.getCause();
				if (cause instanceof ConnectException)
					message += ": " + cause.getMessage();
				if (cause instanceof IllegalStateException) {
					// TODO legacy hack
					this.threadException = new UserCancelledException();
				}
			}
			if (this.threadException instanceof UserCancelledException) {
				// don't display error, clear remembered password and go back to BKU Selection
				if (this.getConfig().getRememberMobilePassword())
					this.getConfig().setDefaultMobilePasswordOverlay(null);
				this.setNextState(new BKUSelectionState(getStateMachine()));
				return;
			}

			// if we have gotten to this point, this is an actual exception
			log.error("FinishSignThread: ", this.threadException);

			ErrorDialog error = new ErrorDialog(getStateMachine().getMainShell(),
					message, BUTTONS.RETRY_CANCEL);
			this.threadException = null;
			if(error.open() == SWT.RETRY) {
				this.setNextState(new PrepareSigningState(getStateMachine()));
			} else {
				this.setNextState(new BKUSelectionState(getStateMachine()));
			}
			return;
		}

		this.setNextState(new OutputState(getStateMachine()));
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
		MainWindowBehavior behavior = getStateMachine().status.behavior;
		behavior.reset();
		behavior.setActive(Buttons.OPEN, true);
		behavior.setActive(Buttons.POSITION, true);
		behavior.setActive(Buttons.SIGN, true);
		//behavior.setEnabled(Buttons.OPEN, true);
		//behavior.setEnabled(Buttons.POSITION, true);
		//behavior.setEnabled(Buttons.SIGN, true);
	}

	@Override
	public String toString()  {
		return this.getClass().getName();
	}
}
