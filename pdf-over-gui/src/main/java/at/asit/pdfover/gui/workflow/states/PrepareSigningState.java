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
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;
import at.asit.pdfover.signator.Emblem;
import at.asit.pdfover.signator.PDFFileDocumentSource;
import at.asit.pdfover.signer.pdfas.PdfAs4SignatureParameter;
import at.asit.pdfover.signer.pdfas.PdfAs4Signer;
import at.asit.pdfover.signer.pdfas.PdfAs4SigningState;

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

				Status status = this.state.getStateMachine().status;

				ConfigurationManager configuration = this.state.getStateMachine().configProvider;

				// SET PROXY HOST and PORT settings
				final String proxyHost = configuration.getProxyHost();
				final int proxyPort = configuration.getProxyPort();
				final String proxyUser = configuration.getProxyUser();
				final String proxyPass = configuration.getProxyPass();

				if (proxyHost != null && !proxyHost.isEmpty()) {
					log.debug("Setting proxy host to " + proxyHost);
					System.setProperty("http.proxyHost", proxyHost);
					System.setProperty("https.proxyHost", proxyHost);
				}

				if (proxyPort > 0 && proxyPort <= 0xFFFF) {
					String port = Integer.toString(proxyPort);
					log.debug("Setting proxy port to " + port);
					System.setProperty("http.proxyPort", port);
					System.setProperty("https.proxyPort", port);
				}

				if (proxyUser != null && !proxyUser.isEmpty()) {
					log.debug("Setting proxy username to " + proxyUser);
					System.setProperty("http.proxyUser", proxyUser);
					System.setProperty("https.proxyUser", proxyUser);
				}

				if (proxyPass != null) {
					log.debug("Setting proxy password");
					System.setProperty("http.proxyPassword", proxyPass);
					System.setProperty("https.proxyPassword", proxyPass);
				}

				if (proxyUser != null && !proxyUser.isEmpty() &&
					proxyPass != null && !proxyPass.isEmpty()) {
					log.debug("Enabling proxy authentication");
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

				if (this.state.signatureParameter == null) {
					this.state.signatureParameter = new PdfAs4SignatureParameter();
				}

				this.state.signatureParameter.inputDocument = new PDFFileDocumentSource(status.document);
				this.state.signatureParameter.signatureDevice = status.bku;
				if (status.signaturePosition != null) {
					this.state.signatureParameter.signaturePosition = status.signaturePosition;
				}

				if (configuration.getDefaultEmblemPath() != null && !configuration.getDefaultEmblemPath().isEmpty()) {
					this.state.signatureParameter.emblem = new Emblem(configuration.getDefaultEmblemPath());
				}

				if (configuration.getSignatureNote() != null
						&& !configuration.getSignatureNote().isEmpty()) {
					this.state.signatureParameter.setProperty(
							"SIG_NOTE", configuration.getSignatureNote());
				}

				this.state.signatureParameter.searchForPlaceholderSignatures = getStateMachine().status.searchForPlacehoderSignature;

				this.state.signatureParameter.signatureLanguage = configuration.getSignatureLocale().getLanguage();

				this.state.signatureParameter.enablePDFACompat = configuration.getSignaturePdfACompat();

				this.state.signatureParameter.signatureProfile = configuration.getSignatureProfile();

				this.state.signingState = PdfAs4Signer.prepare(this.state.signatureParameter);

			} catch (Exception e) {
				log.error("PrepareDocumentThread: ", e);
				this.state.threadException = e;
			} finally {
				this.state.updateStateMachine();
			}
		}
	}

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(PrepareSigningState.class);

	PdfAs4SignatureParameter signatureParameter;

	private WaitingComposite waitingComposite = null;

	private WaitingComposite getSelectionComposite() {
		if (this.waitingComposite == null) {
			this.waitingComposite = getStateMachine()
					.createComposite(WaitingComposite.class, SWT.RESIZE, this);
		}

		return this.waitingComposite;
	}

	PdfAs4SigningState signingState = null;

	Exception threadException = null;

	@Override
	public void run() {
		WaitingComposite waiting = this.getSelectionComposite();

		getStateMachine().display(waiting);

		Status status = getStateMachine().status;

		if (this.signatureParameter == null) {
			this.signatureParameter = new PdfAs4SignatureParameter();
		}

		if (this.signingState == null && this.threadException == null) {
			Thread t = new Thread(new PrepareDocumentThread(this));
			t.start();
			return;
		}

		if (this.threadException != null) {
			ErrorDialog error = new ErrorDialog(getStateMachine()
					.getMainShell(),
					Messages.getString("error.PrepareDocument"),
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
		status.signingState = this.signingState;

		switch (status.bku)
		{
			case LOCAL:
				this.setNextState(new LocalBKUState(getStateMachine()));
				break;
			case MOBILE:
				this.setNextState(new MobileBKUState(getStateMachine()));
				break;
			case KS:
				this.setNextState(new KSState(getStateMachine()));
				break;
			default:
				log.error("Invalid selected BKU Value \"NONE\" in PrepareSigningState!");
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
