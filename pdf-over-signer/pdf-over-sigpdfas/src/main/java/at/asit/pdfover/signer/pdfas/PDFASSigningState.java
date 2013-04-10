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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import at.asit.pdfover.signator.SLRequest;
import at.asit.pdfover.signator.SLResponse;
import at.asit.pdfover.signator.SigningState;
import at.gv.egiz.pdfas.api.io.DataSink;
import at.gv.egiz.pdfas.api.sign.SignatureDetailInformation;

/**
 * Signing State for PDFAS Wrapper
 */
public class PDFASSigningState implements SigningState {

	/**
	 * SFL4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(PDFASSigningState.class);
	
	/**
	 * The Signature Layer request
	 */
	protected SLRequest slrequest;
	
	/**
	 * The PDF AS DataSink
	 */
	protected DataSink output;
	
	/**
	 * Gets the DataSink
	 * @return the datasink
	 */
	public DataSink getOutput() {
		return this.output;
	}

	/**
	 * Sets the datasing
	 * @param output the pdf as datasink
	 */
	public void setOutput(DataSink output) {
		this.output = output;
	}

	/**
	 * The Signature Layer response
	 */
	protected SLResponse slresponse;
	
	@Override
	public SLRequest getSignatureRequest() {
		return this.slrequest;
	}

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
	 * @return The SL Repsonse object
	 */
	public SLResponse getSignatureResponse() {
		return this.slresponse;
	}
	
	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.SigningState#hasSignatureResponse()
	 */
	@Override
	public boolean hasSignatureResponse() {
		return this.getSignatureResponse() != null;
	}
	
	// ----------------------------------------
	// PDF AS Specific stuff
	// ----------------------------------------
	
	/**
	 * signature detail information
	 */
	protected SignatureDetailInformation signatureDetailInformation;

	/**
	 * PDF - AS sign parameters
	 */
	protected at.gv.egiz.pdfas.api.sign.SignParameters signParameters;
	
	/**
	 * Signature parameters 
	 */
	protected PdfAsSignatureParameter pdfAsSignatureParameter;
	
	/**
	 * Gets PDF - AS Signature Parameters
	 * @return PdfAsSignatureParameter
	 */
	public PdfAsSignatureParameter getPdfAsSignatureParameter() {
		return this.pdfAsSignatureParameter;
	}

	/**
	 * Sets PDF - AS Signature Parameters
	 * @param pdfAsSignatureParameter
	 */
	public void setPdfAsSignatureParameter(
			PdfAsSignatureParameter pdfAsSignatureParameter) {
		this.pdfAsSignatureParameter = pdfAsSignatureParameter;
	}

	/**
	 * Get Sign Parameters
	 * @return SignParameters
	 */
	public at.gv.egiz.pdfas.api.sign.SignParameters getSignParameters() {
		return this.signParameters;
	}

	/**
	 * Sets sign Parameter
	 * @param signParameters
	 */
	public void setSignParameters(
			at.gv.egiz.pdfas.api.sign.SignParameters signParameters) {
		this.signParameters = signParameters;
	}

	/**
	 * Gets the signature detail information
	 * @return SignatureDetailInformation
	 */
	public SignatureDetailInformation getSignatureDetailInformation() {
		return this.signatureDetailInformation;
	}

	/**
	 * Sets the SignatureDetailInformation
	 * @param signatureDetailInformation
	 */
	public void setSignatureDetailInformation(
			SignatureDetailInformation signatureDetailInformation) {
		this.signatureDetailInformation = signatureDetailInformation;
	}
}
