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

import at.asit.pdfover.gui.bku.mobile.ATrustHandler;
import at.asit.pdfover.gui.bku.mobile.ATrustStatus;
import at.asit.pdfover.gui.bku.mobile.MobileBKUHandler;
import at.asit.pdfover.gui.bku.mobile.MobileBKUStatus;
import at.asit.pdfover.gui.workflow.states.MobileBKUState;
import at.asit.pdfover.signator.BkuSlConnector;
import at.asit.pdfover.signator.SLRequest;
import at.asit.pdfover.signator.SLResponse;
import at.asit.pdfover.signator.SignatureException;
import at.asit.pdfover.signer.pdfas.PdfAs4SigningState;

/**
 * 
 */
public class MobileBKUConnector implements BkuSlConnector {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(MobileBKUConnector.class);

	private MobileBKUState state;

	/**
	 * 
	 * @param state 
	 */
	public MobileBKUConnector(MobileBKUState state) {
		this.state = state;
	}

	/** (non-Javadoc)
	 * @see at.asit.pdfover.signator.BkuSlConnector#handleSLRequest(java.lang.String)
	 */
	@Override
	public SLResponse handleSLRequest(SLRequest request) throws SignatureException {
		PdfAs4SigningState signingState = (PdfAs4SigningState) this.state.getSigningState();
		signingState.setSignatureRequest(request);

		MobileBKUHandler handler = this.state.getHandler();

		do {
			// Post SL Request
			try {
				String responseData = handler.postSLRequest(this.state.getURL(), request);
	
				// Now we have received some data lets check it:
				log.trace("Response from mobile BKU: " + responseData); //$NON-NLS-1$
	
				handler.handleSLRequestResponse(responseData);
			} catch (Exception ex) {
				log.error("Error in PostSLRequestThread", ex); //$NON-NLS-1$
				this.state.setThreadException(ex);
				this.state.displayError(ex);
				throw new SignatureException(ex);
			}
	
			do {
				// Check if credentials are available, get them from user if not
				this.state.checkCredentials();

				if (this.state.getStatus().getErrorMessage() != null &&
						this.state.getStatus().getErrorMessage().equals("cancel")) //$NON-NLS-1$
					throw new SignatureException(new IllegalStateException());

				// Post credentials
				try {
					String responseData = handler.postCredentials();

					if (responseData.contains("undecided.aspx?sid=")) { //$NON-NLS-1$
						// handle polling
						this.state.showOpenAppMessageWithSMSandCancel();

						if (((ATrustStatus) this.state.getStatus()).isSmsTan()) {
							ATrustHandler aHandler = (ATrustHandler) handler;
							String response = aHandler.postSMSRequest();
							aHandler.handleCredentialsResponse(response);
						} else if (handleErrorMessage()) {
							throw new SignatureException(new IllegalStateException());
						} 
					} else {

					    // Now we have received some data lets check it:
						log.trace("Response from mobile BKU: " + responseData); //$NON-NLS-1$
						handler.handleCredentialsResponse(responseData);
					}

				} catch (Exception ex) {
					log.error("Error in PostCredentialsThread", ex); //$NON-NLS-1$
					this.state.setThreadException(new IllegalStateException());
					throw new SignatureException(new IllegalStateException());
				}
			} while(this.state.getStatus().getErrorMessage() != null);
	
			// Check if response is already available
			if (signingState.hasSignatureResponse()) {
				SLResponse response = signingState.getSignatureResponse();
				signingState.setSignatureResponse(null);
				return response;
			}
	
			do {
				MobileBKUStatus status = this.state.getStatus();
				boolean enterTAN = true;
				String responseData = null;
				if (status instanceof ATrustStatus) {
					ATrustStatus aStatus = (ATrustStatus) status;
					ATrustHandler aHandler = (ATrustHandler) handler;
					if (aStatus.getQRCode() != null) {
						this.state.showQR();
						if (this.state.getStatus().getErrorMessage() != null &&
								this.state.getStatus().getErrorMessage().equals("cancel")) //$NON-NLS-1$
							throw new SignatureException(new IllegalStateException());
						if (aStatus.getQRCode() == null) {
							try {
								String response = aHandler.postSMSRequest();
								log.trace("Response from mobile BKU: " + response); //$NON-NLS-1$
								handler.handleCredentialsResponse(response);
							} catch (Exception ex) {
								log.error("Error in PostCredentialsThread", ex); //$NON-NLS-1$
								this.state.setThreadException(new IllegalStateException());
								throw new SignatureException(new IllegalStateException());
							}
						} else {
							enterTAN = false;
						}
					} 
					if (enterTAN && !aStatus.getTanField()) {
						try {
							 
							this.state.showFingerPrintInformation();
							if (this.state.getStatus().getErrorMessage() != null &&
									this.state.getStatus().getErrorMessage().equals("cancel")) //$NON-NLS-1$
								throw new SignatureException(new IllegalStateException());
						} catch (Exception ex) {
							log.error("Error in PostCredentialsThread", ex); //$NON-NLS-1$
							this.state.setThreadException(new IllegalStateException());
							//this.state.displayError(ex);
							throw new SignatureException(new IllegalStateException());
						}
						
						if (this.state.getSMSStatus()) {
							String response;
							try {
								response = aHandler.postSMSRequest();
								handler.handleCredentialsResponse(response);
							} catch (Exception e) {
								log.error("Error in PostCredentialsThread", e); //$NON-NLS-1$
								this.state.setThreadException(e);
								this.state.displayError(e);
								throw new SignatureException(e);
							}
						}
						else {
							enterTAN = false; 
						}
					}
				}
				
				if (enterTAN) {
					// Get TAN
					this.state.checkTAN();
					

					if (this.state.getStatus().getErrorMessage() != null &&
							this.state.getStatus().getErrorMessage().equals("cancel")) //$NON-NLS-1$
						throw new SignatureException(new IllegalStateException());

					// Post TAN
					try {
						responseData = handler.postTAN();
						log.trace("Response from mobile BKU: " + responseData); //$NON-NLS-1$

						// Now we have received some data lets check it:
						handler.handleTANResponse(responseData);
					} catch (Exception ex) {
						log.error("Error in PostTanThread", ex); //$NON-NLS-1$
						this.state.setThreadException(ex);
						this.state.displayError(ex);
						throw new SignatureException(ex);
					}
				}
			} while (this.state.getStatus().getErrorMessage() != null);
			if (this.state.getStatus().getTanTries() == -1)
				throw new SignatureException(new IllegalStateException());
		} while (this.state.getStatus().getTanTries() == -2);

		return signingState.getSignatureResponse();
	}	
	
	private boolean handleErrorMessage() {
		
		if (this.state.getStatus() instanceof ATrustStatus) {
			ATrustStatus aStatus = (ATrustStatus)this.state.getStatus() ; 
			if (aStatus.getErrorMessage() != null && 
				aStatus.getErrorMessage().equals("cancel")) { //$NON-NLS-1$
					((ATrustStatus)this.state.getStatus()).setErrorMessage(null);
					return true;
			}
		}
		return false; 
	}
	
}
