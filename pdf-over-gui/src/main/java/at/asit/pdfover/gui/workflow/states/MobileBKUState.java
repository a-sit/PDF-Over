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

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// Imports
import at.asit.pdfover.gui.exceptions.ATrustConnectionException;
import at.asit.pdfover.signer.SignatureException;
import at.asit.pdfover.signer.UserCancelledException;
import at.asit.pdfover.signer.pdfas.PdfAs4SigningState;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.bku.MobileBKUConnector;
import at.asit.pdfover.gui.bku.OLDMobileBKUConnector;
import at.asit.pdfover.gui.bku.OLDmobile.ATrustHandler;
import at.asit.pdfover.gui.bku.OLDmobile.ATrustStatus;
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
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;

/**
 * Logical state for performing the BKU Request to the A-Trust Mobile BKU
 */
public class MobileBKUState extends State {
	static final Logger log = LoggerFactory.getLogger(MobileBKUState.class);

	PdfAs4SigningState signingState;

	public Exception threadException = null;

	public final ATrustStatus status;
	public final ATrustHandler handler;

	public MobileBKUState(StateMachine stateMachine) {
		super(stateMachine);
		ConfigurationManager provider = stateMachine.configProvider;
		this.status = new ATrustStatus(provider);
		this.handler = new ATrustHandler(this, stateMachine.getMainShell());
	}

	MobileBKUEnterTANComposite mobileBKUEnterTANComposite = null;

	WaitingForAppComposite waitingForAppComposite = null;
	WaitingForAppComposite getWaitingForAppComposite() {
		if (this.waitingForAppComposite == null) {
			this.waitingForAppComposite = getStateMachine()
					.createComposite(WaitingForAppComposite.class, SWT.RESIZE, this);
		}

		return this.waitingForAppComposite;
	}

	WaitingComposite waitingComposite = null;
	WaitingComposite getWaitingComposite() {
		if (this.waitingComposite == null) {
			this.waitingComposite = getStateMachine()
					.createComposite(WaitingComposite.class, SWT.RESIZE, this);
		}

		return this.waitingComposite;
	}

	MobileBKUEnterTANComposite getMobileBKUEnterTANComposite() {
		if (this.mobileBKUEnterTANComposite == null) {
			this.mobileBKUEnterTANComposite = getStateMachine()
					.createComposite(MobileBKUEnterTANComposite.class, SWT.RESIZE, this);
		}

		return this.mobileBKUEnterTANComposite;
	}

	MobileBKUQRComposite mobileBKUQRComposite = null;
	MobileBKUQRComposite getMobileBKUQRComposite() {
		if (this.mobileBKUQRComposite == null) {
			this.mobileBKUQRComposite = getStateMachine()
					.createComposite(MobileBKUQRComposite.class, SWT.RESIZE, this);
		}

		return this.mobileBKUQRComposite;
	}

	MobileBKUEnterNumberComposite mobileBKUEnterNumberComposite = null;
	MobileBKUEnterNumberComposite getMobileBKUEnterNumberComposite() {
		if (this.mobileBKUEnterNumberComposite == null) {
			this.mobileBKUEnterNumberComposite = getStateMachine()
					.createComposite(MobileBKUEnterNumberComposite.class, SWT.RESIZE, this);
		}

		return this.mobileBKUEnterNumberComposite;
	}

	MobileBKUFingerprintComposite mobileBKUFingerprintComposite = null;
	MobileBKUFingerprintComposite getMobileBKUFingerprintComposite() {
		if (this.mobileBKUFingerprintComposite == null) {
			this.mobileBKUFingerprintComposite = getStateMachine()
					.createComposite(MobileBKUFingerprintComposite.class, SWT.RESIZE, this);
		}

		return this.mobileBKUFingerprintComposite;
	}

	/**
	 * @return the signingState
	 */
	public PdfAs4SigningState getSigningState() {
		return this.signingState;
	}

	/**
	 * Display an error message
	 *
	 * @param e
	 *            the exception
	 */
	public void displayError(Exception e) {
		String message = null;
		if (e instanceof UnknownHostException)
		{
			log.error("Failed to resolve hostname", e);
			message = String.format(Messages.getString("error.CouldNotResolveHostname"), e.getMessage());
		} else if (e instanceof ConnectException) {
			log.error("Failed to connect", e);
			message = String.format(Messages.getString("error.FailedToConnect"), e.getMessage());
		} else {
			message = Messages.getString("error.Unexpected");
			log.error(message, e);
			String errormsg = e.getLocalizedMessage();
			if (errormsg != null && !errormsg.isEmpty())
				message += ": " + errormsg;
		}
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
			ErrorDialog error = new ErrorDialog(getStateMachine().getMainShell(), message, BUTTONS.OK);
			error.open();
		});
	}

	public static class UsernameAndPassword {
		public @CheckForNull String username;
		public @CheckForNull String password;
		public UsernameAndPassword() {}
		public UsernameAndPassword(@Nullable String u, @Nullable String p) { this.username = u; this.password = p; }
	}
	public @Nonnull UsernameAndPassword getRememberedCredentials() {
		UsernameAndPassword r = new UsernameAndPassword();
		storeRememberedCredentialsTo(r);
		return r;
	}
	public void storeRememberedCredentialsTo(@Nonnull UsernameAndPassword output) {
		output.username = getStateMachine().configProvider.getDefaultMobileNumber();
		output.password = getStateMachine().configProvider.getDefaultMobilePassword();
	}

	public void rememberCredentialsIfNecessary(@Nullable String username, @Nullable String password) {
		if (getStateMachine().configProvider.getRememberMobilePassword())
		{
			getStateMachine().configProvider.setDefaultMobileNumberOverlay(username);
			getStateMachine().configProvider.setDefaultMobilePasswordOverlay(password);
		}
	}
	public void rememberCredentialsIfNecessary(@Nonnull UsernameAndPassword credentials) {
		rememberCredentialsIfNecessary(credentials.username, credentials.password);
	}

	public void clearRememberedPassword() {
		getStateMachine().configProvider.setDefaultMobilePasswordOverlay(null);
		status.mobilePassword = null;
	}

	public @Nonnull UsernameAndPassword getCredentialsFromUser(@Nullable String currentUsername, @Nullable String errorMessage) throws UserCancelledException {
		UsernameAndPassword r = new UsernameAndPassword(currentUsername, null);
		getCredentialsFromUserTo(r, errorMessage);
		return r;
	}

	public void getCredentialsFromUserTo(@Nonnull UsernameAndPassword credentials, @Nullable String errorMessage) throws UserCancelledException {
		Display.getDefault().syncCall(() -> {
			MobileBKUEnterNumberComposite ui = this.getMobileBKUEnterNumberComposite();

			if (!ui.userAck) { // We need number and password => show UI!
				
				if (errorMessage != null)
					ui.setErrorMessage(errorMessage);
				else
					ui.setErrorMessage(Messages.getString("mobileBKU.aTrustDisclaimer"));

				if ((ui.getMobileNumber() == null) || ui.getMobileNumber().isEmpty()) {
					// set possible phone number
					ui.setMobileNumber(credentials.username);
				}

				ui.setRememberPassword(getStateMachine().configProvider.getRememberMobilePassword());

				ui.enableButton();
				getStateMachine().display(ui);

				Display display = getStateMachine().getMainShell().getDisplay();
				while (!ui.userAck && !ui.userCancel) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}

			if (!(ui.userCancel && ui.isRememberPassword())) /* don't allow "remember" to be enabled via cancel button */
				getStateMachine().configProvider.setRememberMobilePasswordPersistent(ui.isRememberPassword());

			if (ui.userCancel) {
				ui.userCancel = false;
				throw new UserCancelledException();
			}

			// user hit ok
			ui.userAck = false;

			// get number and password from UI
			credentials.username = ui.getMobileNumber();
			credentials.password = ui.getMobilePassword();

			// show waiting composite
			getStateMachine().display(this.getWaitingComposite());

			return true; /* dummy return for lambda type deduction */
		});
	}

	/**
	 * Make sure phone number and password are set in the MobileBKUStatus
	 * OLD METHOD (todo for nuking)
	 */
	public void checkCredentials() {
		final ATrustStatus mobileStatus = this.status;
		// check if we have everything we need!
		if (mobileStatus.phoneNumber != null && !mobileStatus.phoneNumber.isEmpty() &&
		    mobileStatus.mobilePassword != null && !mobileStatus.mobilePassword.isEmpty())
			return;

		try {
			String errorMessage = mobileStatus.errorMessage;
			mobileStatus.errorMessage = null;
			UsernameAndPassword creds = getCredentialsFromUser(mobileStatus.phoneNumber, errorMessage);
			mobileStatus.phoneNumber = creds.username;
			mobileStatus.mobilePassword = creds.password;
		} catch (UserCancelledException e) {
			mobileStatus.errorMessage = "cancel";
		}
	}

	/**
	 * Make sure TAN is set in the MobileBKUStatus
	 */
	public void checkTAN() {
		final ATrustStatus mobileStatus = this.status;

		Display.getDefault().syncExec(() -> {
			MobileBKUEnterTANComposite tan = getMobileBKUEnterTANComposite();

			if (!tan.isUserAck()) {
				// we need the TAN
				tan.setRefVal(mobileStatus.refVal);
				tan.setSignatureData(mobileStatus.signatureDataURL);
				tan.setErrorMessage(mobileStatus.errorMessage);
				if (mobileStatus.tanTries < ATrustStatus.MOBILE_MAX_TAN_TRIES
						&& mobileStatus.tanTries > 0) {
					// show warning message x tries left!
					// overrides error message

					tan.setTries(mobileStatus.tanTries);
				}
				tan.enableButton();
				getStateMachine().display(tan);

				Display display = getStateMachine().getMainShell().getDisplay();
				while (!tan.isUserAck() && !tan.isUserCancel()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}

			if (tan.isUserCancel()) {
				tan.setUserCancel(false);
				clearRememberedPassword();
				mobileStatus.errorMessage = "cancel";
				return;
			}

			// user hit ok!
			tan.setUserAck(false);

			mobileStatus.tan = tan.getTan();

			// show waiting composite
			getStateMachine().display(getWaitingComposite());
		});
	}

	public enum QRResult {
		/* the user has pressed the FIDO2 button */
		TO_FIDO2,
		/* the user has pressed the SMS button */
		TO_SMS,
		/* signalQRScanned has been called; this indicates that we should refresh the page */
		UPDATE
	};

	/**
	 * start showing the QR code at the indicated URI
	 * this method will block until the QR code state completes
	 * (due to QR code being scanned, or the user pressing a button)
	 * <p>
	 * it is the responsibility of the caller to perform AJAX long polling
	 * @return
	 */
	public QRResult showQRCode(@Nonnull String referenceValue, @Nonnull URI qrCodeURI, @Nullable String errorMessage) throws UserCancelledException {
		byte[] qrCode;
		try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
			try (final CloseableHttpResponse response = httpClient.execute(new HttpGet(qrCodeURI))) {
				qrCode = EntityUtils.toByteArray(response.getEntity());
			}
		} catch (IOException e) {
			log.warn("Failed to load QR code.");
			qrCode = null;
		}
		
		final byte[] qrCodeCopy = qrCode; /* because java is silly */
		return Display.getDefault().syncCall(() -> {
			MobileBKUQRComposite qr = getMobileBKUQRComposite();
			qr.setUserCancel(false);
			qr.setUserSMS(false);
			qr.setDone(false);

			qr.setRefVal(status.refVal);
			qr.setSignatureData(status.signatureDataURL);
			qr.setErrorMessage(status.errorMessage);
			qr.setQR(qrCodeCopy);
			getStateMachine().display(qr);

			Display display = getStateMachine().getMainShell().getDisplay();
			while (!qr.isUserCancel() && !qr.isUserSMS() && !qr.isDone()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}

			getStateMachine().display(this.getWaitingComposite());

			if (qr.isUserCancel()) {
				clearRememberedPassword();
				throw new UserCancelledException();
			}

			if (qr.isUserSMS())
				return QRResult.TO_SMS;

			if (qr.isDone())
				return QRResult.UPDATE;
			
			throw new RuntimeException("unexpected display wake");
		});
	}

	/**
	 * indicate that the long polling operation completed
	 * (any ongoing showQRCode call will then return)
	 */
	public void signalQRScanned() {
		getMobileBKUQRComposite().setDone(true);
		Display display = getStateMachine().
				getMainShell().getDisplay();
		display.wake();
	}

	/**
	 * Show QR code
	 * @throws URISyntaxException
	 * @throws UserCancelledException
	 * @throws IOException
	 */
	public void OLDshowQR() throws IOException, UserCancelledException, URISyntaxException {
		final ATrustStatus status = this.status;
		final ATrustHandler handler = this.handler;

		final Timer checkDone = new Timer();
		checkDone.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// ping signature page to see if code has been scanned
				try {
					String resp = handler.getSignaturePage();
					if (handler.handleQRResponse(resp)) {
						log.debug("Signature page response: " + resp);
						checkDone.cancel();
						signalQRScanned();
					}
					Display.getDefault().wake();
				} catch (Exception e) {
					log.error("Error getting signature page", e);
				}
			}
		}, 0, 5000);

		QRResult result = showQRCode(status.refVal, new URI(status.baseURL).resolve(status.qrCodeURL), status.errorMessage);
		checkDone.cancel();
		if (result == QRResult.TO_SMS)
			status.qrCodeURL = null;
	}

	/**
	 *  This composite notifies the user to open the signature-app
	 */
	public void showOpenAppMessageWithSMSandCancel() throws SignatureException {

		final ATrustStatus status = this.status;

		Display.getDefault().syncExec(() -> {
			WaitingForAppComposite waitingForAppcomposite = this.getWaitingForAppComposite();
			getStateMachine().display(waitingForAppcomposite);

			Display display = getStateMachine().getMainShell().getDisplay();
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
				getStateMachine().display(this.getWaitingComposite());
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
		final ATrustHandler handler = this.handler;

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
		final ATrustStatus status = this.status;
		final ATrustHandler handler = this.handler;

		Timer checkDone = new Timer();
		checkDone.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				// ping signature page to see if code has been scanned
				try {
					String resp = handler.getSignaturePage();
					if (handler.handleQRResponse(resp)) {
						log.debug("Signature page response: " + resp);
						getMobileBKUFingerprintComposite().setDone(true);
						Display display = getStateMachine().getMainShell().getDisplay();
						display.wake();
						checkDone.cancel();
					}
					Display.getDefault().wake();
				} catch (Exception e) {
					log.error("Error getting signature page", e);
				}
			}
		}, 0, 5000);
		Display.getDefault().syncExec(() -> {
			MobileBKUFingerprintComposite fingerprintComposite = getMobileBKUFingerprintComposite();

			fingerprintComposite.setRefVal(status.refVal);
			fingerprintComposite.setSignatureData(status.signatureDataURL);
			fingerprintComposite.setErrorMessage(status.errorMessage);
			getStateMachine().display(fingerprintComposite);

			Display display = getStateMachine().getMainShell().getDisplay();
			while (!fingerprintComposite.isUserCancel() && !fingerprintComposite.isUserSMS() && !fingerprintComposite.isDone()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			checkDone.cancel();

			if (fingerprintComposite.isUserCancel()) {
				fingerprintComposite.setUserCancel(false);
				clearRememberedPassword();
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
			getStateMachine().display(this.getWaitingComposite());
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
		this.signingState = getStateMachine().status.signingState;

		this.signingState.bkuConnector = new MobileBKUConnector(this);
		this.signingState.useBase64Request = false;

		if (this.threadException != null) {
			displayError(this.threadException);
			return;
		}

		getStateMachine().display(
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
		MainWindowBehavior behavior = getStateMachine().status.behavior;
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
