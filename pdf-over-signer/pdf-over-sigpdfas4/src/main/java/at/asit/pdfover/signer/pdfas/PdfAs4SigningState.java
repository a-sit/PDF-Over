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
import at.asit.pdfover.signator.SigningState;
import at.gv.egiz.pdfas.lib.api.sign.SignParameter;

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

	private BkuSlConnector connector;

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.SigningState#getSignatureRequest()
	 */
	@Override
	public SLRequest getSignatureRequest() {
		return this.slrequest;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.SigningState#setSignatureResponse(at.asit.pdfover.signator.SLResponse)
	 */
	@Override
	public void setSignatureResponse(SLResponse response) {
		this.slresponse = response;
	}

	/**
	 * Sets the SL Request
	 * @param request The SL Request
	 */
	public void setSignatureRequest(SLRequest request) {
		this.slrequest = request;
	}

	/**
	 * Gets the SL Response
	 * @return The SL Response object
	 */
	public SLResponse getSignatureResponse() {
		return this.slresponse;
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
		this.connector = connector;
	}

	/**
	 * @return the connector
	 */
	public BkuSlConnector getBKUConnector() {
		return this.connector;
	}
}
