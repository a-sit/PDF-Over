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
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.composites.MobileBKUEnterNumberComposite;
import at.asit.pdfover.gui.composites.MobileBKUEnterTANComposite;
import at.asit.pdfover.gui.composites.WaitingComposite;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.states.mobilebku.ATrustHandler;
import at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUCommunicationState;
import at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHandler;
import at.asit.pdfover.gui.workflow.states.mobilebku.ATrustStatus;
import at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus;
import at.asit.pdfover.gui.workflow.states.mobilebku.PostCredentialsThread;
import at.asit.pdfover.gui.workflow.states.mobilebku.PostSLRequestThread;
import at.asit.pdfover.gui.workflow.states.mobilebku.PostTanThread;

/**
 * Logical state for performing the BKU Request to the A-Trust Mobile BKU
 */
public class MobileBKUState extends State {
	/**
	 * @param stateMachine
	 */
	public MobileBKUState(StateMachine stateMachine) {
		super(stateMachine);
		switch(this.stateMachine.getConfigProvider().getMobileBKUType()) {
			case A_TRUST:
				this.status = new ATrustStatus(this.stateMachine.getConfigProvider());
				this.handler = new ATrustHandler(this);
				break;

			case IAIK:
				//TODO
				break;
		}

	}

	/**
	 * SLF4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(MobileBKUState.class);

	at.asit.pdfover.signator.SigningState signingState;

	Exception threadException = null;

	MobileBKUCommunicationState communicationState = MobileBKUCommunicationState.POST_REQUEST;

	MobileBKUStatus status = null;

	MobileBKUHandler handler = null;

	MobileBKUEnterNumberComposite mobileBKUEnterNumberComposite = null;

	MobileBKUEnterTANComposite mobileBKUEnterTANComposite = null;

	WaitingComposite waitingComposite = null;

	private WaitingComposite getWaitingComposite() {
		if (this.waitingComposite == null) {
			this.waitingComposite = this.stateMachine.getGUIProvider()
					.createComposite(WaitingComposite.class, SWT.RESIZE, this);
		}

		return this.waitingComposite;
	}

	private MobileBKUEnterTANComposite getMobileBKUEnterTANComposite() {
		if (this.mobileBKUEnterTANComposite == null) {
			this.mobileBKUEnterTANComposite = this.stateMachine
					.getGUIProvider().createComposite(
							MobileBKUEnterTANComposite.class, SWT.RESIZE, this);
		}

		return this.mobileBKUEnterTANComposite;
	}

	private MobileBKUEnterNumberComposite getMobileBKUEnterNumberComposite() {
		if (this.mobileBKUEnterNumberComposite == null) {
			this.mobileBKUEnterNumberComposite = this.stateMachine
					.getGUIProvider().createComposite(
							MobileBKUEnterNumberComposite.class, SWT.RESIZE,
							this);
		}

		return this.mobileBKUEnterNumberComposite;
	}

	/**
	 * Get the MobileBKUStatus
	 * @return the MobileBKUStatus
	 */
	public MobileBKUStatus getStatus() {
		return this.status;
	}

	/**
	 * Get the MobileBKUHandler
	 * @return the MobileBKUHandler
	 */
	public MobileBKUHandler getHandler() {
		return this.handler;
	}

	/**
	 * Get the mobile BKU URL
	 * @return the mobile BKU URL
	 */
	public String getURL() {
		return this.stateMachine.getConfigProvider().getMobileBKUURL();
	}

	/**
	 * @return the communicationState
	 */
	public MobileBKUCommunicationState getCommunicationState() {
		return this.communicationState;
	}

	/**
	 * @param communicationState
	 *            the communicationState to set
	 */
	public void setCommunicationState(
			MobileBKUCommunicationState communicationState) {
		this.communicationState = communicationState;
	}

	/**
	 * @return the signingState
	 */
	public at.asit.pdfover.signator.SigningState getSigningState() {
		return this.signingState;
	}

	/**
	 * @param threadException
	 *            the threadException to set
	 */
	public void setThreadException(Exception threadException) {
		this.threadException = threadException;
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

		this.signingState = this.stateMachine.getStatus().getSigningState();

		MobileBKUStatus mobileStatus = this.getStatus();

		if (this.threadException != null) {
			ErrorDialog error = new ErrorDialog(
					this.stateMachine.getGUIProvider().getMainShell(),
					Messages.getString("error.Unexpected"), BUTTONS.OK); //$NON-NLS-1$
			// error.setException(this.threadException);
			// this.setNextState(error);
			error.open();
			this.stateMachine.exit();
			return;
		}

		switch (this.communicationState) {
		case POST_REQUEST:
			this.stateMachine.getGUIProvider().display(
					this.getWaitingComposite());
			Thread postSLRequestThread = new Thread(
					new PostSLRequestThread(this));
			postSLRequestThread.start();
			break;
		case POST_NUMBER:
			// Check if number and password is set ...
			// if not show UI
			// else start thread

			// check if we have everything we need!
			if (mobileStatus.getPhoneNumber() != null
					&& !mobileStatus.getPhoneNumber().isEmpty()
					&& mobileStatus.getMobilePassword() != null
					&& !mobileStatus.getMobilePassword().isEmpty()) {
				// post to bku
				Thread postCredentialsThread = new Thread(
						new PostCredentialsThread(this));
				postCredentialsThread.start();
				// resets password if incorrect to null
			} else {

				MobileBKUEnterNumberComposite ui = this
						.getMobileBKUEnterNumberComposite();

				if (ui.isUserAck()) {
					// user hit ok

					ui.setUserAck(false);

					// get number and password from UI
					mobileStatus.setPhoneNumber(ui.getMobileNumber());
					mobileStatus.setMobilePassword(ui.getMobilePassword());

					// show waiting composite
					this.stateMachine.getGUIProvider().display(
							this.getWaitingComposite());

					// post to BKU
					Thread postCredentialsThread = new Thread(
							new PostCredentialsThread(this));
					postCredentialsThread.start();

				} else {
					// We need number and password => show UI!

					if (mobileStatus.getErrorMessage() != null
							&& !mobileStatus.getErrorMessage().isEmpty()) {
						// set possible error message
						ui.setErrorMessage(mobileStatus.getErrorMessage());
						mobileStatus.setErrorMessage(null);
					}
					

					if (ui.getMobileNumber() == null
							|| ui.getMobileNumber().isEmpty()) {
						// set possible phone number
						ui.setMobileNumber(mobileStatus.getPhoneNumber());
					}

					if (ui.getMobilePassword() == null
							|| ui.getMobilePassword().isEmpty()) {
						// set possible password
						ui.setMobilePassword(mobileStatus.getMobilePassword());
					}
					ui.enableButton();
					this.stateMachine.getGUIProvider().display(ui);
				}
			}
			break;
		case POST_TAN:
			// Get TAN from UI

			MobileBKUEnterTANComposite tan = this
					.getMobileBKUEnterTANComposite();

			if (tan.isUserAck()) {
				// user hit ok!
				tan.setUserAck(false);

				mobileStatus.setTan(tan.getTan());

				// show waiting composite
				this.stateMachine.getGUIProvider().display(
						this.getWaitingComposite());
				
				// post to BKU!
				Thread postTanThread = new Thread(new PostTanThread(this));
				postTanThread.start();

			} else {
				tan.setRefVal(mobileStatus.getRefVal());
				tan.setSignatureData(mobileStatus.getSignatureDataURL());
				tan.setErrorMessage(mobileStatus.getErrorMessage());
				if (mobileStatus.getTanTries() < mobileStatus.getMaxTanTries()
						&& mobileStatus.getTanTries() > 0) {
					// show warning message x tries left!
					// overrides error message

					tan.setTries(mobileStatus.getTanTries());
				}
				tan.enableButton();
				this.stateMachine.getGUIProvider().display(tan);
			}

			break;
		case FINAL:
			this.setNextState(new SigningState(this.stateMachine));
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.states.State#cleanUp()
	 */
	@Override
	public void cleanUp() {
		if (this.mobileBKUEnterNumberComposite != null)
			this.mobileBKUEnterNumberComposite.dispose();
		if (this.mobileBKUEnterTANComposite != null)
			this.mobileBKUEnterTANComposite.dispose();
		if (this.waitingComposite != null)
			this.waitingComposite.dispose();
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
		behavior.setEnabled(Buttons.OPEN, true);
		behavior.setEnabled(Buttons.POSITION, true);
		//behavior.setEnabled(Buttons.SIGN, true);
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}

	/**
	 * invoke state machine update in main thread
	 */
	public void invokeUpdate() {
		this.stateMachine.invokeUpdate();
	}
}
