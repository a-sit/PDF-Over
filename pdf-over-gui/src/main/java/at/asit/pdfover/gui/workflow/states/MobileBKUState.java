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
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.function.Supplier;

// Imports
import at.asit.pdfover.signer.UserCancelledException;
import at.asit.pdfover.signer.pdfas.PdfAs4SigningState;
import at.asit.webauthnclient.PublicKeyCredential;
import at.asit.webauthnclient.responsefields.AuthenticatorAssertionResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.bku.MobileBKUConnector;
import at.asit.pdfover.gui.composites.WaitingComposite;
import at.asit.pdfover.gui.composites.mobilebku.MobileBKUEnterNumberComposite;
import at.asit.pdfover.gui.composites.mobilebku.MobileBKUEnterTANComposite;
import at.asit.pdfover.gui.composites.mobilebku.MobileBKUFido2Composite;
import at.asit.pdfover.gui.composites.mobilebku.MobileBKUFingerprintComposite;
import at.asit.pdfover.gui.composites.mobilebku.MobileBKUQRComposite;
import at.asit.pdfover.gui.composites.mobilebku.WaitingForAppComposite;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.Dialog.ICON;
import at.asit.pdfover.gui.utils.HttpClientUtils;
import at.asit.pdfover.gui.controls.Dialog;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;

/**
 * Logical state for performing the BKU Request to the A-Trust Mobile BKU
 */
@Slf4j
public class MobileBKUState extends State {

	PdfAs4SigningState signingState;

	public Exception threadException = null;

	public MobileBKUState(StateMachine stateMachine) {
		super(stateMachine);
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

	MobileBKUFido2Composite mobileBKUFido2Composite = null;
	MobileBKUFido2Composite getMobileBKUFido2Composite() {
		if (this.mobileBKUFido2Composite == null) {
			this.mobileBKUFido2Composite = getStateMachine()
					.createComposite(MobileBKUFido2Composite.class, SWT.RESIZE, this);
		}

		return this.mobileBKUFido2Composite;
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
			message = Messages.formatString("error.CouldNotResolveHostname", e.getMessage());
		} else if (e instanceof ConnectException) {
			log.error("Failed to connect", e);
			message = Messages.formatString("error.FailedToConnect", e.getMessage());
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

	public void showInformationMessage(final @NonNull String message) throws UserCancelledException {
		Display.getDefault().syncCall(() -> {
			Dialog dialog = new Dialog(getStateMachine().getMainShell(), Messages.getString("common.info"), message, BUTTONS.OK, ICON.INFORMATION);
			int result = dialog.open();
			if (result == SWT.CANCEL)
				throw new UserCancelledException();
			return true; /* dummy return to keep java happy */
		});
	}

	/**
	 * Show an error message to the user with "retry" or "cancel" as options
	 * returns normally on "retry", throws UserCancelledException on "cancel"
	 */
	public void showRecoverableError(final @NonNull String errorMessage) throws UserCancelledException {
		Display.getDefault().syncCall(() -> {
			ErrorDialog error = new ErrorDialog(getStateMachine().getMainShell(), Messages.formatString("atrusterror.message", errorMessage), BUTTONS.RETRY_CANCEL);
			int result = error.open();
			if (result == SWT.CANCEL)
				throw new UserCancelledException();
			return true; /* dummy return */
		});
	}

	/**
	 * Show an error message to the user with only an "ok" option;
	 * throws UserCancelledException afterwards
	 */
	public void showUnrecoverableError(final @NonNull String errorMessage) throws UserCancelledException {
		Display.getDefault().syncCall(() -> {
			ErrorDialog error = new ErrorDialog(getStateMachine().getMainShell(), Messages.formatString("atrusterror.message", errorMessage), BUTTONS.OK);
			error.open();
			throw new UserCancelledException();
		});
	}

	public static class UsernameAndPassword {
		public String username;
		public String password;
		public UsernameAndPassword() {}
		public UsernameAndPassword(String u, String p) { this.username = u; this.password = p; }
	}
	public @NonNull UsernameAndPassword getRememberedCredentials() {
		UsernameAndPassword r = new UsernameAndPassword();
		storeRememberedCredentialsTo(r);
		return r;
	}
	public void storeRememberedCredentialsTo(@NonNull UsernameAndPassword output) {
		output.username = getStateMachine().configProvider.getDefaultMobileNumber();
		output.password = getStateMachine().configProvider.getDefaultMobilePassword();
	}

	public void rememberCredentialsIfNecessary(String username, String password) {
		if (getStateMachine().configProvider.getRememberMobilePassword())
		{
			getStateMachine().configProvider.setDefaultMobileNumberPersistent(username);
			getStateMachine().configProvider.setDefaultMobilePasswordOverlay(password);
		}
	}
	public void rememberCredentialsIfNecessary(@NonNull UsernameAndPassword credentials) {
		rememberCredentialsIfNecessary(credentials.username, credentials.password);
	}

	public void clearRememberedPassword() {
		getStateMachine().configProvider.setDefaultMobilePasswordOverlay(null);
	}

	public @NonNull UsernameAndPassword getCredentialsFromUser(String currentUsername, String errorMessage) throws UserCancelledException {
		UsernameAndPassword r = new UsernameAndPassword(currentUsername, null);
		getCredentialsFromUserTo(r, errorMessage);
		return r;
	}

	private void updateRememberPasswordSetting(boolean enabled, boolean allowEnabling) {
		final var config = getStateMachine().configProvider;
		if (enabled == config.getRememberMobilePassword()) /* nothing to do here */
			return;
		if (enabled && !allowEnabling) /* do not allow "cancel" to set the remember checkbox */
			return;
		config.setRememberMobilePasswordPersistent(enabled);
		if (!enabled) { /* clear remembered info */
			config.setDefaultMobileNumberPersistent(null);
			config.setDefaultMobilePasswordOverlay(null);
		}
	}

	public void readAndDispatchSWTUntil(Supplier<Boolean> pred) throws UserCancelledException {
		Shell shell = getStateMachine().getMainShell();
		Display display = shell.getDisplay();
		while (!pred.get()) {
			if (!display.readAndDispatch())
				display.sleep();
			if (shell.isDisposed())
				throw new UserCancelledException();
		}
	}

	public void getCredentialsFromUserTo(@NonNull UsernameAndPassword credentials, String errorMessage) throws UserCancelledException {
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

				readAndDispatchSWTUntil(() -> (ui.userAck || ui.userCancel));
			}

			updateRememberPasswordSetting(ui.isRememberPassword(), !ui.userCancel);

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

	public static class SMSTanResult {
		public static enum ResultType { TO_FIDO2, SMSTAN };
		public final @NonNull ResultType type;
		public final String smsTan;

		private SMSTanResult(String smsTan) { this.type = ResultType.SMSTAN; this.smsTan = smsTan; }
		private SMSTanResult(@NonNull ResultType type) { this.type = type; this.smsTan = null; }
	}

	public @NonNull SMSTanResult getSMSTanFromUser(final @NonNull String referenceValue, final URI signatureDataURI, final boolean showFido2, final String errorMessage) throws UserCancelledException {
		return Display.getDefault().syncCall(() -> {
			MobileBKUEnterTANComposite tan = getMobileBKUEnterTANComposite();
			
			tan.reset();
			tan.setRefVal(referenceValue);
			tan.setSignatureDataURI(signatureDataURI);
			tan.setErrorMessage(errorMessage);
			tan.setFIDO2Enabled(showFido2);
			getStateMachine().display(tan);

			readAndDispatchSWTUntil(() -> tan.isDone());
			getStateMachine().display(getWaitingComposite());

			if (tan.isUserCancel())
				throw new UserCancelledException();
			
			if (tan.isUserFido2())
				return new SMSTanResult(SMSTanResult.ResultType.TO_FIDO2);
			
			return new SMSTanResult(tan.getTan());
		});
	}

	/**
	 * start showing the QR code at the indicated URI
	 * this method will return immediately */
	public void showQRCode(final @NonNull String referenceValue, @NonNull URI qrCodeURI, URI signatureDataURI, final boolean showSmsTan, final boolean showFido2, final String errorMessage) {
		byte[] qrCode;
		try (final CloseableHttpClient httpClient = HttpClientUtils.builderWithSettings().build()) {
			try (final CloseableHttpResponse response = httpClient.execute(new HttpGet(qrCodeURI))) {
				qrCode = EntityUtils.toByteArray(response.getEntity());
			}
		} catch (IOException e) {
			log.warn("Failed to load QR code.");
			qrCode = null;
		}
		
		final byte[] qrCodeCopy = qrCode; /* because java is silly */
		Display.getDefault().syncExec(() -> {
			MobileBKUQRComposite qr = getMobileBKUQRComposite();
			qr.reset();

			qr.setRefVal(referenceValue);
			qr.setSignatureDataURI(signatureDataURI);
			qr.setErrorMessage(errorMessage);
			qr.setQR(qrCodeCopy);
			qr.setSMSEnabled(showSmsTan);
			qr.setFIDO2Enabled(showFido2);
			getStateMachine().display(qr);
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

	public @NonNull QRResult waitForQRCodeResult() throws UserCancelledException {
		return Display.getDefault().syncCall(() -> {
			MobileBKUQRComposite qr = getMobileBKUQRComposite();

			readAndDispatchSWTUntil(() -> qr.isDone());

			getStateMachine().display(this.getWaitingComposite());

			if (qr.wasCancelClicked()) {
				clearRememberedPassword();
				throw new UserCancelledException();
			}

			if (qr.wasSMSClicked())
				return QRResult.TO_SMS;
			
			if (qr.wasFIDO2Clicked())
				return QRResult.TO_FIDO2;

			return QRResult.UPDATE;
		});
	}

	/**
	 * indicate that the long polling operation completed
	 * (any ongoing waitForQRCodeResult call will then return)
	 */
	public void signalQRScanned() {
		getMobileBKUQRComposite().signalPollingDone();
	}

	/**
	 * start showing the "waiting for app" screen
	 * this method will return immediately */
	public void showWaitingForAppOpen(final @NonNull String referenceValue, URI signatureDataURI, final boolean showSmsTan, final boolean showFido2) {
		Display.getDefault().syncExec(() -> {
			WaitingForAppComposite wfa = getWaitingForAppComposite();
			wfa.reset();

			// TODO composite does not currently support: refval, signature data
			wfa.setSMSEnabled(showSmsTan);
			wfa.setFIDO2Enabled(showFido2);
			getStateMachine().display(wfa);
		});
	}

	public enum AppOpenResult {
		/* the user has pressed the FIDO2 button */
		TO_FIDO2,
		/* the user has pressed the SMS button */
		TO_SMS,
		/* signalAppOpened has been called; this indicates that we should refresh the page */
		UPDATE
	};

	public @NonNull AppOpenResult waitForAppOpen() throws UserCancelledException {
		return Display.getDefault().syncCall(() -> {
			WaitingForAppComposite wfa = getWaitingForAppComposite();

			readAndDispatchSWTUntil(() -> wfa.isDone());

			getStateMachine().display(this.getWaitingComposite());

			if (wfa.wasCancelClicked()) {
				clearRememberedPassword();
				throw new UserCancelledException();
			}

			if (wfa.wasSMSClicked())
				return AppOpenResult.TO_SMS;
			
			if (wfa.wasFIDO2Clicked())
				return AppOpenResult.TO_FIDO2;

			return AppOpenResult.UPDATE;
		});
	}

	/**
	 * indicate that the long polling operation completed
	 * (any ongoing waitForAppOpen call will then return)
	 */
	public void signalAppOpened() {
		getWaitingForAppComposite().signalPollingDone();
	}

	public void showWaitingForAppBiometry(final @NonNull String referenceValue, URI signatureDataURI, final boolean showSmsTan, final boolean showFido2) {
		Display.getDefault().syncExec(() -> {
			MobileBKUFingerprintComposite bio = getMobileBKUFingerprintComposite();
			bio.reset();

			bio.setRefVal(referenceValue);
			bio.signatureDataURI = signatureDataURI;
			bio.setErrorMessage(null); // TODO
			bio.setSMSEnabled(showSmsTan);
			bio.setFIDO2Enabled(showFido2);
			getStateMachine().display(bio);
		});
	}

	// TODO can we maybe deduplicate the various waiting screens' logic?

	public enum AppBiometryResult {
		/* the user has pressed the FIDO2 button */
		TO_FIDO2,
		/* the user has pressed the SMS button */
		TO_SMS,
		/* signalAppBiometryDone has been called; this indicates that we should refresh the page */
		UPDATE
	};

	public @NonNull AppBiometryResult waitForAppBiometry() throws UserCancelledException {
		return Display.getDefault().syncCall(() -> {
			MobileBKUFingerprintComposite bio = getMobileBKUFingerprintComposite();

			readAndDispatchSWTUntil(() -> bio.isDone());

			getStateMachine().display(this.getWaitingComposite());

			if (bio.wasCancelClicked()) {
				clearRememberedPassword();
				throw new UserCancelledException();
			}

			if (bio.wasSMSClicked())
				return AppBiometryResult.TO_SMS;
			
			if (bio.wasFIDO2Clicked())
				return AppBiometryResult.TO_FIDO2;

			return AppBiometryResult.UPDATE;
		});
	}

	public void signalAppBiometryDone() {
		getMobileBKUFingerprintComposite().signalPollingDone();
	}

	public static class FIDO2Result {
		public static enum ResultType { TO_SMS, CREDENTIAL };
		public final @NonNull ResultType type;
		public final PublicKeyCredential<AuthenticatorAssertionResponse> credential;

		private FIDO2Result(@NonNull ResultType type) { this.type = type; this.credential = null; }
		private FIDO2Result(@NonNull PublicKeyCredential<AuthenticatorAssertionResponse> cred) { this.type = ResultType.CREDENTIAL; this.credential = cred; }
	}

	/**
	 * prompts user for fido2 auth and blocks until result is available
	 * @param fido2Options JSON data from A-Trust
	 * @return
	 * @throws UserCancelledException
	 */
	public @NonNull FIDO2Result promptUserForFIDO2Auth(final @NonNull String fido2Options, URI signatureDataURI, final boolean showSmsTan) throws UserCancelledException {
		return Display.getDefault().syncCall(() -> {
			MobileBKUFido2Composite fido2 = getMobileBKUFido2Composite();
			fido2.initialize(fido2Options);
			fido2.setSMSEnabled(showSmsTan);
			fido2.setSignatureDataURI(signatureDataURI);
			
			getStateMachine().display(fido2);

			readAndDispatchSWTUntil(() -> fido2.isDone());

			getStateMachine().display(this.getWaitingComposite());

			if (fido2.wasUserCancelClicked())
				throw new UserCancelledException();
			
			if (fido2.wasUserSMSClicked())
				return new FIDO2Result(FIDO2Result.ResultType.TO_SMS);
			
			return new FIDO2Result(fido2.getResultingCredential());
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
