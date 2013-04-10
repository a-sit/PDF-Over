package at.asit.pdfover.signer.pdfas;

import at.asit.pdfover.signator.SLRequest;
import at.asit.pdfover.signator.SLResponse;
import at.asit.pdfover.signator.SigningState;
import at.gv.egiz.pdfas.api.sign.SignatureDetailInformation;

/**
 * Signing State for PDFAS Wrapper
 * @author afitzek
 */
public class PDFASSigningState implements SigningState {

	/**
	 * The Signature Layer request
	 */
	protected SLRequest slrequest;
	
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
	
	
	// ----------------------------------------
	// PDF AS Specific stuff
	// ----------------------------------------
	
	protected SignatureDetailInformation signatureDetailInformation;

	protected at.gv.egiz.pdfas.api.sign.SignParameters signParameters;
	
	protected PdfAsSignatureParameter pdfAsSignatureParameter;
	
	public PdfAsSignatureParameter getPdfAsSignatureParameter() {
		return pdfAsSignatureParameter;
	}

	public void setPdfAsSignatureParameter(
			PdfAsSignatureParameter pdfAsSignatureParameter) {
		this.pdfAsSignatureParameter = pdfAsSignatureParameter;
	}

	public at.gv.egiz.pdfas.api.sign.SignParameters getSignParameters() {
		return signParameters;
	}

	public void setSignParameters(
			at.gv.egiz.pdfas.api.sign.SignParameters signParameters) {
		this.signParameters = signParameters;
	}

	public SignatureDetailInformation getSignatureDetailInformation() {
		return signatureDetailInformation;
	}

	public void setSignatureDetailInformation(
			SignatureDetailInformation signatureDetailInformation) {
		this.signatureDetailInformation = signatureDetailInformation;
	}
}
