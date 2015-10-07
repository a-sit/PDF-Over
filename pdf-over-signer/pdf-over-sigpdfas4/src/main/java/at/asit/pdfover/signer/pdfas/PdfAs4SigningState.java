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
package at.asit.pdfover.signer.pdfas;

//Imports
import java.io.ByteArrayOutputStream;

import at.asit.pdfover.signator.BkuSlConnector;
import at.asit.pdfover.signator.SLRequest;
import at.asit.pdfover.signator.SLResponse;
import at.asit.pdfover.signator.SignatureException;
import at.asit.pdfover.signator.SigningState;
import at.gv.egiz.pdfas.common.exceptions.PDFASError;
import at.gv.egiz.pdfas.lib.api.sign.IPlainSigner;
import at.gv.egiz.pdfas.lib.api.sign.SignParameter;
import at.gv.egiz.pdfas.sigs.pades.PAdESSignerKeystore;

/**
 * Signing State for PDFAS Wrapper
 */
public class PdfAs4SigningState implements SigningState {

	/**
	 * The Signature Layer request
	 */
	private SLRequest slrequest;

	/**
	 * The Signature Layer response
	 */
	private SLResponse slresponse;

	/**
	 * The Sign Parameters
	 */
	private SignParameter parameter;

	private ByteArrayOutputStream output;

	private BkuSlConnector bkuconnector = null;

	private IPlainSigner kssigner = null;

	private boolean useBase64Request;

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.SigningState#getSignatureRequest()
	 */
	@Override
	public SLRequest getSignatureRequest() {
		return this.slrequest;
	}

	/**
	 * Sets the SL Request
	 * @param request The SL Request
	 */
	public void setSignatureRequest(SLRequest request) {
		this.slrequest = request;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.SigningState#setUseBase64Request(boolean)
	 */
	@Override
	public void setUseBase64Request(boolean useBase64Request) {
		this.useBase64Request = useBase64Request;
	}

	/**
	 * Gets whether to use base64 (or FileUpload) for request data
	 * @return whether to use base64 for request data
	 */
	public boolean getUseBase64Request() {
		return this.useBase64Request;
	}

	/**
	 * Gets the SL Response
	 * @return The SL Response object
	 */
	public SLResponse getSignatureResponse() {
		return this.slresponse;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.SigningState#setSignatureResponse(at.asit.pdfover.signator.SLResponse)
	 */
	@Override
	public void setSignatureResponse(SLResponse response) {
		this.slresponse = response;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.SigningState#hasSignatureResponse()
	 */
	@Override
	public boolean hasSignatureResponse() {
		return this.slresponse != null;
	}

	/**
	 * @return the output
	 */
	public ByteArrayOutputStream getOutput() {
		return this.output;
	}

	/**
	 * @param output the output to set
	 */
	public void setOutput(ByteArrayOutputStream output) {
		this.output = output;
	}

	/**
	 * @return the parameter
	 */
	public SignParameter getSignParameter() {
		return this.parameter;
	}

	/**
	 * @param parameter the parameter to set
	 */
	public void setSignParameter(SignParameter parameter) {
		this.parameter = parameter;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.SigningState#setBKUConnector(at.asit.pdfover.signator.BkuSlConnector)
	 */
	@Override
	public void setBKUConnector(BkuSlConnector connector) {
		this.bkuconnector = connector;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.SigningState#setKSSigner(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setKSSigner(String file, String alias, String kspassword,
			String keypassword, String type) throws SignatureException {
		try {
			this.kssigner = new PAdESSignerKeystore(file, alias, kspassword, keypassword, type);
		} catch (PDFASError e) {
			throw new SignatureException(e);
		}
	}

	/**
	 * @return whether a BKU connector was set
	 */
	public boolean hasBKUConnector() {
		return this.bkuconnector != null;
	}

	/**
	 * @return the BKU connector
	 */
	public BkuSlConnector getBKUConnector() {
		return this.bkuconnector;
	}

	/**
	 * @return whether a KS signer was set
	 */
	public boolean hasKSSigner() {
		return this.kssigner != null;
	}

	/**
	 * @return the KS signer
	 */
	public IPlainSigner getKSSigner() {
		return this.kssigner;
	}
}
