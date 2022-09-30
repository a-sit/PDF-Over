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
package at.asit.pdfover.gui.bku;

// Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.bku.mobile.ATrustHandler;
import at.asit.pdfover.gui.bku.mobile.ATrustStatus;
import at.asit.pdfover.gui.workflow.states.MobileBKUState;
import at.asit.pdfover.signer.BkuSlConnector;
import at.asit.pdfover.signer.SignatureException;
import at.asit.pdfover.signer.pdfas.PdfAs4SLRequest;
import at.asit.pdfover.signer.pdfas.PdfAs4SigningState;

/**
 *
 */
public class MobileBKUConnector implements BkuSlConnector {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(MobileBKUConnector.class);

	private MobileBKUState state;

	/**
	 *
	 * @param state
	 */
	public MobileBKUConnector(MobileBKUState state) {
		this.state = state;
	}

	/** (non-Javadoc)
	 * @see at.asit.pdfover.signer.BkuSlConnector#handleSLRequest(java.lang.String)
	 */
	@Override
	public String handleSLRequest(PdfAs4SLRequest request) throws SignatureException {
		PdfAs4SigningState signingState = this.state.getSigningState();
		signingState.signatureRequest = request;

		ATrustHandler handler = this.state.handler;

		do {
			// Post SL Request
			try {
				String responseData = handler.postSLRequest(Constants.MOBILE_BKU_URL, request);

				// Now we have received some data lets check it:
				log.trace("Response from mobile BKU: " + responseData);

				handler.handleSLRequestResponse(responseData);
			} catch (Exception ex) {
				log.error("Error in PostSLRequestThread", ex);
				this.state.threadException = ex;
				this.state.displayError(ex);
				throw new SignatureException(ex);
			}

			do {
				// Check if credentials are available, get them from user if not
				this.state.checkCredentials();

				if (consumeCancelError())
					throw new SignatureException(new IllegalStateException());

				// Post credentials
				try {
					String responseData = handler.postCredentials();

					if (responseData.contains("undecided.aspx?sid=")) {
						// handle polling
						this.state.showOpenAppMessageWithSMSandCancel();

						if (this.state.status.isSMSTan) {
							String response = handler.postSMSRequest();
							handler.handleCredentialsResponse(response);
						} else if (consumeCancelError()) {
							throw new SignatureException(new IllegalStateException());
						}
					} else {

					    // Now we have received some data lets check it:
						log.trace("Response from mobile BKU: " + responseData);
						handler.handleCredentialsResponse(responseData);
					}

				} catch (Exception ex) {
					log.error("Error in PostCredentialsThread", ex);
					this.state.threadException = new IllegalStateException();
					throw new SignatureException(new IllegalStateException());
				}
			} while(this.state.status.errorMessage != null);

			// Check if response is already available
			if (signingState.signatureResponse != null) {
				String response = signingState.signatureResponse;
				signingState.signatureResponse = null;
				return response;
			}

			do {
				ATrustStatus status = this.state.status;
				boolean enterTAN = true;
				String responseData = null;
				if (status.qrCodeURL != null) {
					this.state.showQR();
					if ("cancel".equals(this.state.status.errorMessage))
						throw new SignatureException(new IllegalStateException());
					if (status.qrCodeURL == null) {
						try {
							String response = handler.postSMSRequest();
							log.trace("Response from mobile BKU: " + response);
							handler.handleCredentialsResponse(response);
						} catch (Exception ex) {
							log.error("Error in PostCredentialsThread", ex);
							this.state.threadException = new IllegalStateException();
							throw new SignatureException(new IllegalStateException());
						}
					} else {
						enterTAN = false;
					}
				}
				if (enterTAN && !status.tanField) {
					try {

						this.state.showFingerPrintInformation();
						if ("cancel".equals(this.state.status.errorMessage))
							throw new SignatureException(new IllegalStateException());
					} catch (Exception ex) {
						log.error("Error in PostCredentialsThread", ex);
						this.state.threadException = new IllegalStateException();
						//this.state.displayError(ex);
						throw new SignatureException(new IllegalStateException());
					}

					if (this.state.getSMSStatus()) {
						String response;
						try {
							response = handler.postSMSRequest();
							handler.handleCredentialsResponse(response);
						} catch (Exception e) {
							log.error("Error in PostCredentialsThread", e);
							this.state.threadException = e;
							this.state.displayError(e);
							throw new SignatureException(e);
						}
					}
					else {
						enterTAN = false;
					}
				}

				if (enterTAN) {
					// Get TAN
					this.state.checkTAN();

					if ("cancel".equals(this.state.status.errorMessage))
						throw new SignatureException(new IllegalStateException());

					// Post TAN
					try {
						responseData = handler.postTAN();
						log.trace("Response from mobile BKU: " + responseData);

						// Now we have received some data lets check it:
						handler.handleTANResponse(responseData);
					} catch (Exception ex) {
						log.error("Error in PostTanThread", ex);
						this.state.threadException = ex;
						this.state.displayError(ex);
						throw new SignatureException(ex);
					}
				}
			} while (this.state.status.errorMessage != null);
			if (this.state.status.tanTries == -1)
				throw new SignatureException(new IllegalStateException());
		} while (this.state.status.tanTries == -2);

		return signingState.signatureResponse;
	}

	private boolean consumeCancelError() {
		if ("cancel".equals(this.state.status.errorMessage)) {
				this.state.status.errorMessage = null;
				return true;
		}
		return false;
	}

}
