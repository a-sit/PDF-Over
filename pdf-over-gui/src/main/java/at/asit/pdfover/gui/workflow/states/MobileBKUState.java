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
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.bku.MobileBKUConnector;
import at.asit.pdfover.gui.bku.mobile.ATrustHandler;
import at.asit.pdfover.gui.bku.mobile.ATrustStatus;
import at.asit.pdfover.gui.bku.mobile.IAIKHandler;
import at.asit.pdfover.gui.bku.mobile.IAIKStatus;
import at.asit.pdfover.gui.bku.mobile.MobileBKUHandler;
import at.asit.pdfover.gui.bku.mobile.MobileBKUStatus;
import at.asit.pdfover.gui.composites.MobileBKUEnterNumberComposite;
import at.asit.pdfover.gui.composites.MobileBKUEnterTANComposite;
import at.asit.pdfover.gui.composites.WaitingComposite;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;

/**
 * Logical state for performing the BKU Request to the A-Trust Mobile BKU
 */
public class MobileBKUState extends State {
	/**
	 * @param stateMachine
	 */
	public MobileBKUState(StateMachine stateMachine) {
		super(stateMachine);
		switch(getStateMachine().getConfigProvider().getMobileBKUType()) {
			case A_TRUST:
				this.status = new ATrustStatus(getStateMachine().getConfigProvider());
				this.handler = new ATrustHandler(this, getStateMachine().getGUIProvider().getMainShell());
				break;

			case IAIK:
				this.status = new IAIKStatus(getStateMachine().getConfigProvider());
				this.handler = new IAIKHandler(this, getStateMachine().getGUIProvider().getMainShell());
				break;
		}

	}

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(MobileBKUState.class);

	at.asit.pdfover.signator.SigningState signingState;

	Exception threadException = null;

	MobileBKUStatus status = null;

	MobileBKUHandler handler = null;

	MobileBKUEnterNumberComposite mobileBKUEnterNumberComposite = null;

	MobileBKUEnterTANComposite mobileBKUEnterTANComposite = null;

	WaitingComposite waitingComposite = null;

	WaitingComposite getWaitingComposite() {
		if (this.waitingComposite == null) {
			this.waitingComposite = getStateMachine().getGUIProvider()
					.createComposite(WaitingComposite.class, SWT.RESIZE, this);
		}

		return this.waitingComposite;
	}

	MobileBKUEnterTANComposite getMobileBKUEnterTANComposite() {
		if (this.mobileBKUEnterTANComposite == null) {
			this.mobileBKUEnterTANComposite = getStateMachine()
					.getGUIProvider().createComposite(
							MobileBKUEnterTANComposite.class, SWT.RESIZE, this);
		}

		return this.mobileBKUEnterTANComposite;
	}

	MobileBKUEnterNumberComposite getMobileBKUEnterNumberComposite() {
		if (this.mobileBKUEnterNumberComposite == null) {
			this.mobileBKUEnterNumberComposite = getStateMachine()
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
		return getStateMachine().getConfigProvider().getMobileBKUURL();
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

	/**
	 * Display an error message
	 * 
	 * @param e
	 *            the exception
	 */
	public void displayError(Exception e) {
		String message = Messages.getString("error.Unexpected"); //$NON-NLS-1$
		log.error(message, e);
		String errormsg = e.getLocalizedMessage();
		if (errormsg != null && !errormsg.isEmpty())
			message += ": " + errormsg; //$NON-NLS-1$
		displayError(message);
	}

	/**
	 * Display an error message
	 * 
	 * @param message
	 *            the error message
	 */
	public void displayError(final String message) {
		log.error(message);
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				ErrorDialog error = new ErrorDialog(getStateMachine().getGUIProvider()
						.getMainShell(), message, BUTTONS.OK);
				error.open();
			}
		});
	}

	/**
	 * Make sure phone number and password are set in the MobileBKUStatus
	 */
	public void checkCredentials() {
		final MobileBKUStatus mobileStatus = this.getStatus();
		// check if we have everything we need!
		if (mobileStatus.getPhoneNumber() != null && !mobileStatus.getPhoneNumber().isEmpty() &&
		    mobileStatus.getMobilePassword() != null && !mobileStatus.getMobilePassword().isEmpty())
			return;

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				MobileBKUEnterNumberComposite ui = MobileBKUState.this
						.getMobileBKUEnterNumberComposite();
	
				if (!ui.isUserAck()) {
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
					getStateMachine().getGUIProvider().display(ui);

					Display display = getStateMachine().getGUIProvider().getMainShell().getDisplay(); 
					while (!ui.isUserAck()) {
						if (!display.readAndDispatch()) {
							display.sleep();
						}
					}
				}

				// user hit ok
				ui.setUserAck(false);

				// get number and password from UI
				mobileStatus.setPhoneNumber(ui.getMobileNumber());
				mobileStatus.setMobilePassword(ui.getMobilePassword());

				// show waiting composite
				getStateMachine().getGUIProvider().display(
						MobileBKUState.this.getWaitingComposite());
			}
		});
	}

	/**
	 * Make sure TAN is set in the MobileBKUStatus
	 */
	public void checkTAN() {
		final MobileBKUStatus mobileStatus = this.getStatus();

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				MobileBKUEnterTANComposite tan = MobileBKUState.this
						.getMobileBKUEnterTANComposite();
		
				if (!tan.isUserAck()) {
					// we need the TAN
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
					getStateMachine().getGUIProvider().display(tan);

					Display display = getStateMachine().getGUIProvider().getMainShell().getDisplay(); 
					while (!tan.isUserAck()) {
						if (!display.readAndDispatch()) {
							display.sleep();
						}
					}
				}

				// user hit ok!
				tan.setUserAck(false);

				mobileStatus.setTan(tan.getTan());

				// show waiting composite
				getStateMachine().getGUIProvider().display(
						MobileBKUState.this.getWaitingComposite());
			}
		});
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
		this.signingState = getStateMachine().getStatus().getSigningState();

		this.signingState.setBKUConnector(new MobileBKUConnector(this));

		if (this.threadException != null) {
			String message = Messages.getString("error.Unexpected"); //$NON-NLS-1$
			log.error(message, this.threadException);
			String errormsg = this.threadException.getLocalizedMessage();
			if (errormsg != null && !errormsg.isEmpty())
				message += ": " + errormsg; //$NON-NLS-1$
			ErrorDialog error = new ErrorDialog(
					getStateMachine().getGUIProvider().getMainShell(),
					message, BUTTONS.OK);
			// error.setException(this.threadException);
			// this.setNextState(error);
			error.open();
			getStateMachine().exit();
			return;
		}

		getStateMachine().getGUIProvider().display(
				this.getWaitingComposite());

		this.setNextState(new SigningState(getStateMachine()));
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
		MainWindowBehavior behavior = getStateMachine().getStatus()
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
		getStateMachine().invokeUpdate();
	}
}
