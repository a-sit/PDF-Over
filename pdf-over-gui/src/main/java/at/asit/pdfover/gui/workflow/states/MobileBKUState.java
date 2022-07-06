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

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

// Imports
import at.asit.pdfover.gui.exceptions.ATrustConnectionException;
import at.asit.pdfover.signator.SignatureException;
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
import at.asit.pdfover.gui.composites.MobileBKUFingerprintComposite;
import at.asit.pdfover.gui.composites.MobileBKUQRComposite;
import at.asit.pdfover.gui.composites.WaitingComposite;
import at.asit.pdfover.gui.composites.WaitingForAppComposite;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.config.ConfigProvider;
import at.asit.pdfover.signator.SigningState;

/**
 * Logical state for performing the BKU Request to the A-Trust Mobile BKU
 */
public class MobileBKUState extends State {
	static final Logger log = LoggerFactory.getLogger(MobileBKUState.class);

	SigningState signingState;

	public Exception threadException = null;

	public final MobileBKUStatus status;
	public final MobileBKUHandler handler;

	public MobileBKUState(StateMachine stateMachine) {
		super(stateMachine);
		ConfigProvider provider = stateMachine.getConfigProvider();
		switch(provider.getMobileBKUType()) {
			case A_TRUST:
				this.status = new ATrustStatus(provider);
				this.handler = new ATrustHandler(this, stateMachine.getGUIProvider().getMainShell(), provider.getMobileBKUBase64());
				break;

			case IAIK:
				this.status = new IAIKStatus(provider);
				this.handler = new IAIKHandler(this, stateMachine.getGUIProvider().getMainShell());
				break;

			default:
				throw new RuntimeException("Unexpected mobileBKUType");
		}
	}

	MobileBKUEnterTANComposite mobileBKUEnterTANComposite = null;

	WaitingForAppComposite waitingForAppComposite = null;
	WaitingForAppComposite getWaitingForAppComposite() {
		if (this.waitingForAppComposite == null) {
			this.waitingForAppComposite = getStateMachine().getGUIProvider()
					.createComposite(WaitingForAppComposite.class, SWT.RESIZE, this);
		}

		return this.waitingForAppComposite;
	}

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
			this.mobileBKUEnterTANComposite = getStateMachine().getGUIProvider()
					.createComposite(MobileBKUEnterTANComposite.class, SWT.RESIZE, this);
		}

		return this.mobileBKUEnterTANComposite;
	}

	MobileBKUQRComposite mobileBKUQRComposite = null;
	MobileBKUQRComposite getMobileBKUQRComposite() {
		if (this.mobileBKUQRComposite == null) {
			this.mobileBKUQRComposite = getStateMachine().getGUIProvider()
					.createComposite(MobileBKUQRComposite.class, SWT.RESIZE, this);
		}

		return this.mobileBKUQRComposite;
	}

	MobileBKUEnterNumberComposite mobileBKUEnterNumberComposite = null;
	MobileBKUEnterNumberComposite getMobileBKUEnterNumberComposite() {
		if (this.mobileBKUEnterNumberComposite == null) {
			this.mobileBKUEnterNumberComposite = getStateMachine().getGUIProvider()
					.createComposite(MobileBKUEnterNumberComposite.class, SWT.RESIZE, this);
		}

		return this.mobileBKUEnterNumberComposite;
	}

	MobileBKUFingerprintComposite mobileBKUFingerprintComposite = null;
	MobileBKUFingerprintComposite getMobileBKUFingerprintComposite() {
		if (this.mobileBKUFingerprintComposite == null) {
			this.mobileBKUFingerprintComposite = getStateMachine().getGUIProvider()
					.createComposite(MobileBKUFingerprintComposite.class, SWT.RESIZE, this);
		}

		return this.mobileBKUFingerprintComposite;
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
	public SigningState getSigningState() {
		return this.signingState;
	}

	/**
	 * Display an error message
	 *
	 * @param e
	 *            the exception
	 */
	public void displayError(Exception e) {
		String message = Messages.getString("error.Unexpected"); //
		log.error(message, e);
		String errormsg = e.getLocalizedMessage();
		if (errormsg != null && !errormsg.isEmpty())
			message += ": " + errormsg; //
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
		Display.getDefault().syncExec(() -> {
			ErrorDialog error = new ErrorDialog(getStateMachine().getGUIProvider().getMainShell(), message, BUTTONS.OK);
			error.open();
		});
	}

	/**
	 * Make sure phone number and password are set in the MobileBKUStatus
	 */
	public void checkCredentials() {
		final MobileBKUStatus mobileStatus = this.status;
		// check if we have everything we need!
		if (mobileStatus.phoneNumber != null && !mobileStatus.phoneNumber.isEmpty() &&
		    mobileStatus.mobilePassword != null && !mobileStatus.mobilePassword.isEmpty())
			return;

		Display.getDefault().syncExec(() -> {
			MobileBKUEnterNumberComposite ui = this.getMobileBKUEnterNumberComposite();

			if (!ui.userAck) {
				// We need number and password => show UI!
				if (mobileStatus.errorMessage != null
						&& !mobileStatus.errorMessage.isEmpty()) {
					// set possible error message
					ui.setErrorMessage(mobileStatus.errorMessage);
					mobileStatus.errorMessage = null;
				} else if (mobileStatus instanceof ATrustStatus) {
					ui.setErrorMessage(Messages.getString("mobileBKU.aTrustDisclaimer")); //
				}

				if (ui.getMobileNumber() == null
						|| ui.getMobileNumber().isEmpty()) {
					// set possible phone number
					ui.setMobileNumber(mobileStatus.phoneNumber);
				}

				if (ui.getMobilePassword() == null
						|| ui.getMobilePassword().isEmpty()) {
					// set possible password
					ui.setMobilePassword(mobileStatus.mobilePassword);
				}
				ui.enableButton();
				getStateMachine().getGUIProvider().display(ui);

				Display display = getStateMachine().getGUIProvider().getMainShell().getDisplay();
				while (!ui.userAck && !ui.userCancel) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}

			if (ui.userCancel) {
				ui.userCancel = false;
				mobileStatus.errorMessage = "cancel"; //
				return;
			}

			// user hit ok
			ui.userAck = false;

			// get number and password from UI
			mobileStatus.phoneNumber = ui.getMobileNumber();
			mobileStatus.mobilePassword = ui.getMobilePassword();

			// show waiting composite
			getStateMachine().getGUIProvider().display(MobileBKUState.this.getWaitingComposite());
		});
	}

	/**
	 * Make sure TAN is set in the MobileBKUStatus
	 */
	public void checkTAN() {
		final MobileBKUStatus mobileStatus = this.status;

		Display.getDefault().syncExec(() -> {
			MobileBKUEnterTANComposite tan = getMobileBKUEnterTANComposite();

			if (!tan.isUserAck()) {
				// we need the TAN
				tan.setRefVal(mobileStatus.refVal);
				tan.setSignatureData(mobileStatus.signatureDataURL);
				tan.setErrorMessage(mobileStatus.errorMessage);
				if (mobileStatus.tanTries < mobileStatus.getMaxTanTries()
						&& mobileStatus.tanTries > 0) {
					// show warning message x tries left!
					// overrides error message

					tan.setTries(mobileStatus.tanTries);
				}
				tan.enableButton();
				getStateMachine().getGUIProvider().display(tan);

				Display display = getStateMachine().getGUIProvider().getMainShell().getDisplay();
				while (!tan.isUserAck() && !tan.isUserCancel()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}

			if (tan.isUserCancel()) {
				tan.setUserCancel(false);
				mobileStatus.errorMessage = "cancel";
				return;
			}

			// user hit ok!
			tan.setUserAck(false);

			mobileStatus.tan = tan.getTan();

			// show waiting composite
			getStateMachine().getGUIProvider().display(getWaitingComposite());
		});
	}

	/**
	 * Show QR code
	 */
	public void showQR() {
		final ATrustStatus status = (ATrustStatus) this.status;
		final ATrustHandler handler = (ATrustHandler) this.handler;

		final Timer checkDone = new Timer();
		checkDone.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// ping signature page to see if code has been scanned
				try {
					String resp = handler.getSignaturePage();
					if (handler.handleQRResponse(resp)) {
						log.debug("Signature page response: " + resp); //
						getMobileBKUQRComposite().setDone(true);
						Display display = getStateMachine().getGUIProvider().
								getMainShell().getDisplay();
						display.wake();
						checkDone.cancel();
					}
					Display.getDefault().wake();
				} catch (Exception e) {
					log.error("Error getting signature page", e); //
				}
			}
		}, 0, 5000);

		Display.getDefault().syncExec(() -> {
			MobileBKUQRComposite qr = getMobileBKUQRComposite();

			qr.setRefVal(status.refVal);
			qr.setSignatureData(status.signatureDataURL);
			qr.setErrorMessage(status.errorMessage);
			InputStream qrcode = handler.getQRCode();
			if (qrcode == null) {
				MobileBKUState.this.threadException = new Exception(
						Messages.getString("error.FailedToLoadQRCode")); //
			}
			qr.setQR(qrcode);
			getStateMachine().getGUIProvider().display(qr);

			Display display = getStateMachine().getGUIProvider().getMainShell().getDisplay();
			while (!qr.isUserCancel() && !qr.isUserSMS() && !qr.isDone()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}

			checkDone.cancel();

			if (qr.isUserCancel()) {
				qr.setUserCancel(false);
				status.errorMessage = "cancel";
				return;
			}

			if (qr.isUserSMS()) {
				qr.setUserSMS(false);
				status.qrCodeURL = null;
			}

			if (qr.isDone())
				qr.setDone(false);

			// show waiting composite
			getStateMachine().getGUIProvider().display(
					MobileBKUState.this.getWaitingComposite());
		});
	}


	/**
	 *  This composite notifies the user to open the signature-app
	 */
	public void showOpenAppMessage() {
		Display.getDefault().syncExec(() -> {
			getStateMachine().getGUIProvider().display(this.getWaitingForAppComposite());
		});

	}

	/**
	 *  This composite notifies the user to open the signature-app
	 */
	public void showOpenAppMessageWithSMSandCancel() throws SignatureException {

		final ATrustStatus status = (ATrustStatus) this.status;

		Display.getDefault().syncExec(() -> {
			WaitingForAppComposite waitingForAppcomposite = MobileBKUState.this.getWaitingForAppComposite();
			getStateMachine().getGUIProvider().display(waitingForAppcomposite);

			Display display = getStateMachine().getGUIProvider().getMainShell().getDisplay();
			undecidedPolling();
			long timeoutTime = System.nanoTime() + (300 * ((long)1e9));

			while (!waitingForAppcomposite.getUserCancel() && !waitingForAppcomposite.getUserSMS()
					&& !waitingForAppcomposite.getIsDone() && (System.nanoTime() < timeoutTime)) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}

			if (waitingForAppcomposite.getUserCancel()) {
				waitingForAppcomposite.setUserCancel(false);
				status.errorMessage = "cancel";
				return;
			}

			if (waitingForAppcomposite.getUserSMS()) {
				status.qrCodeURL = null;
				waitingForAppcomposite.setUserSMS(false);
				status.errorMessage = "sms";
				status.isSMSTan = true;
				// show waiting composite
				getStateMachine().getGUIProvider().display(MobileBKUState.this.getWaitingComposite());
				return;

			}

			if (waitingForAppcomposite.getIsDone())
				waitingForAppcomposite.setIsDone(false);

			if (!(System.nanoTime() < timeoutTime)) {
				log.warn("The undecided polling got a timeout");
				status.qrCodeURL = null;
				status.errorMessage = "Polling Timeout";


			}
		});
	}

	private void undecidedPolling(){
		final ATrustHandler handler = (ATrustHandler) this.handler;

		Thread pollingThread = new Thread(() -> {
			try {
				if (handler.handlePolling()){
					String response = handler.getSignaturePage();
					handler.handleCredentialsResponse(response);
					Display.getDefault().syncExec(() ->
							getWaitingForAppComposite().setIsDone(true));
				}
			} catch (ATrustConnectionException e) {
				log.error("Error when calling polling endpoint");
			} catch (Exception e) {
				log.error("Exception occurred during calling polling endpoint");
			}
		});
		pollingThread.start();
	}

	/**
	 *  when fingerprint or faceid is selected in the app
	 *  this information is shown
	 */
	public void showFingerPrintInformation() {
		final ATrustStatus status = (ATrustStatus) this.status;
		final ATrustHandler handler = (ATrustHandler) this.handler;

		Timer checkDone = new Timer();
		checkDone.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				// ping signature page to see if code has been scanned
				try {
					String resp = handler.getSignaturePage();
					if (handler.handleQRResponse(resp)) {
						log.debug("Signature page response: " + resp); //
						getMobileBKUFingerprintComposite().setDone(true);
						Display display = getStateMachine().getGUIProvider().getMainShell().getDisplay();
						display.wake();
						checkDone.cancel();
					}
					Display.getDefault().wake();
				} catch (Exception e) {
					log.error("Error getting signature page", e); //
				}
			}
		}, 0, 5000);
		Display.getDefault().syncExec(() -> {
			MobileBKUFingerprintComposite fingerprintComposite = getMobileBKUFingerprintComposite();

			fingerprintComposite.setRefVal(status.refVal);
			fingerprintComposite.setSignatureData(status.signatureDataURL);
			fingerprintComposite.setErrorMessage(status.errorMessage);
			getStateMachine().getGUIProvider().display(fingerprintComposite);

			Display display = getStateMachine().getGUIProvider().getMainShell().getDisplay();
			while (!fingerprintComposite.isUserCancel() && !fingerprintComposite.isUserSMS() && !fingerprintComposite.isDone()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			checkDone.cancel();

			if (fingerprintComposite.isUserCancel()) {
				fingerprintComposite.setUserCancel(false);
				status.errorMessage = "cancel";
				return;
			}

			if (fingerprintComposite.isUserSMS()) {
//					fingerprintComposite.setUserSMS(false);
				status.qrCodeURL = null;
			}

			if (fingerprintComposite.isDone())
				fingerprintComposite.setDone(false);

			// show waiting composite
			getStateMachine().getGUIProvider().display(
					MobileBKUState.this.getWaitingComposite());
		});
	}

	/**
	 * @return a boolean true if the user has pressed the sms tan button
	 */
	public boolean getSMSStatus() {

		return this.getMobileBKUFingerprintComposite().isUserSMS();
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
		log.debug("Setting base64 request to " + this.handler.useBase64Request()); //
		this.signingState.setUseBase64Request(this.handler.useBase64Request());

		if (this.threadException != null) {
			String message = Messages.getString("error.Unexpected"); //
			log.error(message, this.threadException);
			String errormsg = this.threadException.getLocalizedMessage();
			if (errormsg != null && !errormsg.isEmpty())
				message += ": " + errormsg; //
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

		this.setNextState(new at.asit.pdfover.gui.workflow.states.SigningState(getStateMachine()));
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
		if (this.waitingForAppComposite != null)
			this.waitingForAppComposite.dispose();
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
