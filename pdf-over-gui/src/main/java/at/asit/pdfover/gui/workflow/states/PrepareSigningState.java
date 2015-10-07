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
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.composites.WaitingComposite;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.gui.workflow.config.ConfigProvider;
import at.asit.pdfover.signator.BKUs;
import at.asit.pdfover.signator.FileNameEmblem;
import at.asit.pdfover.signator.PDFFileDocumentSource;
import at.asit.pdfover.signator.SignatureParameter;
import at.asit.pdfover.signator.Signer;

/**
 * User waiting state, wait for PDF Signator library to prepare document for
 * signing.
 */
public class PrepareSigningState extends State {

	/**
	 * @param stateMachine
	 */
	public PrepareSigningState(StateMachine stateMachine) {
		super(stateMachine);
	}

	private final class PrepareDocumentThread implements Runnable {

		private PrepareSigningState state;

		/**
		 * Default constructor
		 *
		 * @param state
		 */
		public PrepareDocumentThread(PrepareSigningState state) {
			this.state = state;
		}

		@Override
		public void run() {
			try {

				Status status = this.state.getStateMachine().getStatus();

				ConfigProvider configuration = this.state.getStateMachine()
						.getConfigProvider();

				// SET PROXY HOST and PORT settings
				final String proxyHost = configuration.getProxyHost();
				final int proxyPort = configuration.getProxyPort();
				final String proxyUser = configuration.getProxyUser();
				final String proxyPass = configuration.getProxyPass();

				if (proxyHost != null && !proxyHost.isEmpty()) {
					log.debug("Setting proxy host to " + proxyHost); //$NON-NLS-1$
					System.setProperty("http.proxyHost", proxyHost); //$NON-NLS-1$
					System.setProperty("https.proxyHost", proxyHost); //$NON-NLS-1$
				}

				if (proxyPort > 0 && proxyPort <= 0xFFFF) {
					String port = Integer.toString(proxyPort);
					log.debug("Setting proxy port to " + port); //$NON-NLS-1$
					System.setProperty("http.proxyPort", port); //$NON-NLS-1$
					System.setProperty("https.proxyPort", port); //$NON-NLS-1$
				}

				if (proxyUser != null && !proxyUser.isEmpty()) {
					log.debug("Setting proxy username to " + proxyUser); //$NON-NLS-1$
					System.setProperty("http.proxyUser", proxyUser); //$NON-NLS-1$
					System.setProperty("https.proxyUser", proxyUser); //$NON-NLS-1$
				}

				if (proxyPass != null) {
					log.debug("Setting proxy password"); //$NON-NLS-1$
					System.setProperty("http.proxyPassword", proxyPass); //$NON-NLS-1$
					System.setProperty("https.proxyPassword", proxyPass); //$NON-NLS-1$
				}

				if (proxyUser != null && !proxyUser.isEmpty() &&
					proxyPass != null && !proxyPass.isEmpty()) {
					log.debug("Enabling proxy authentication"); //$NON-NLS-1$
					Authenticator.setDefault(new Authenticator() {
						/* (non-Javadoc)
						 * @see java.net.Authenticator#getPasswordAuthentication()
						 */
						@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							if (getRequestorType() == RequestorType.PROXY) {
								if (getRequestingHost().equalsIgnoreCase(proxyHost) &&
									(getRequestingPort() == proxyPort)) {
									return new PasswordAuthentication(proxyUser, 
											proxyPass.toCharArray());
								}
							}
							return super.getPasswordAuthentication();
						}
					});
				}
				if (this.state.signer == null) {
					this.state.signer = this.state.getStateMachine().getPDFSigner()
							.getPDFSigner();
				}

				if (this.state.signatureParameter == null) {
					this.state.signatureParameter = this.state.signer
							.newParameter();
				}

				this.state.signatureParameter
						.setInputDocument(new PDFFileDocumentSource(status
								.getDocument()));
				this.state.signatureParameter.setSignatureDevice(status
						.getBKU());
				this.state.signatureParameter.setSignaturePosition(status
						.getSignaturePosition());

				if (configuration.getDefaultEmblem() != null
						&& !configuration.getDefaultEmblem().isEmpty()) {
					this.state.signatureParameter.setEmblem(new FileNameEmblem(
							configuration.getDefaultEmblem()));
				}

				if (configuration.getSignatureNote() != null
						&& !configuration.getSignatureNote().isEmpty()) {
					this.state.signatureParameter.setProperty(
							"SIG_NOTE", configuration.getSignatureNote()); //$NON-NLS-1$
				}

				this.state.signatureParameter
						.setSignatureLanguage(configuration.getSignatureLocale()
								.getLanguage());

				this.state.signatureParameter
						.setSignaturePdfACompat(configuration.getSignaturePdfACompat());

				this.state.signingState = this.state.signer
						.prepare(this.state.signatureParameter);

			} catch (Exception e) {
				log.error("PrepareDocumentThread: ", e); //$NON-NLS-1$
				this.state.threadException = e;
			} finally {
				this.state.updateStateMachine();
			}
		}
	}

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(PrepareSigningState.class);

	SignatureParameter signatureParameter;

	private WaitingComposite waitingComposite = null;

	private WaitingComposite getSelectionComposite() {
		if (this.waitingComposite == null) {
			this.waitingComposite = getStateMachine().getGUIProvider()
					.createComposite(WaitingComposite.class, SWT.RESIZE, this);
		}

		return this.waitingComposite;
	}

	at.asit.pdfover.signator.SigningState signingState = null;

	Signer signer;

	Exception threadException = null;

	@Override
	public void run() {
		WaitingComposite waiting = this.getSelectionComposite();

		getStateMachine().getGUIProvider().display(waiting);

		this.signer = getStateMachine().getPDFSigner().getPDFSigner();

		Status status = getStateMachine().getStatus();

		if (this.signatureParameter == null) {
			this.signatureParameter = this.signer.newParameter();
		}

		if (this.signingState == null && this.threadException == null) {
			Thread t = new Thread(new PrepareDocumentThread(this));
			t.start();
			return;
		}

		if (this.threadException != null) {
			ErrorDialog error = new ErrorDialog(getStateMachine()
					.getGUIProvider().getMainShell(),
					Messages.getString("error.PrepareDocument"), //$NON-NLS-1$
					BUTTONS.RETRY_CANCEL);
			this.threadException = null;
			if (error.open() == SWT.RETRY) {
				run();
			} else {
				this.setNextState(new BKUSelectionState(getStateMachine()));
			}
			return;
		}

		// We got the Request set it into status and move on to next state ...
		status.setSigningState(this.signingState);

		if (status.getBKU() == BKUs.LOCAL) {
			this.setNextState(new LocalBKUState(getStateMachine()));
		} else if (status.getBKU() == BKUs.MOBILE) {
			this.setNextState(new MobileBKUState(getStateMachine()));
		} else if (status.getBKU() == BKUs.KS) {
			this.setNextState(new KSState(getStateMachine()));
		} else {
			log.error("Invalid selected BKU Value \"NONE\" in PrepareSigningState!"); //$NON-NLS-1$
			this.setNextState(new BKUSelectionState(getStateMachine()));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#cleanUp()
	 */
	@Override
	public void cleanUp() {
		if (this.waitingComposite != null)
			this.waitingComposite.dispose();
	}

	/*
	 * (non-Javadoc)
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
