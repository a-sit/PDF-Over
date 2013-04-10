package at.asit.pdfover.pdfsigner.pdfas;

import at.asit.pdfover.pdfsigner.SLRequest;
import at.asit.pdfover.pdfsigner.SLResponse;
import at.asit.pdfover.pdfsigner.SigningState;
import at.gv.egiz.pdfas.api.sign.SignatureDetailInformation;

/**
 * Signing State for PDFAS Wrapper
 * @author afitzek
 */
public class PDFASSigningState implements SigningState {

	protected SLRequest slrequest;
	
	protected SLResponse slresponse;
	
	@Override
	public SLRequest GetSLSignatureRequest() {
		return slrequest;
	}

	@Override
	public void SetSLSignatureResponse(SLResponse value) {
		this.slresponse = value;
	}

	/**
	 * Sets the SL Request
	 * @param request The SL Request
	 */
	public void SetSLSignatureRequest(SLRequest request) {
		this.slrequest = request;
	}
	
	/**
	 * Gets the SL Response
	 * @return The SL Repsonse object
	 */
	public SLResponse GetSLSignatureResponse() {
		return this.slresponse;
	}
	
	
	// ----------------------------------------
	// PDF AS Specific stuff
	// ----------------------------------------
	
	protected SignatureDetailInformation SignatureDetailInformation;

	protected at.gv.egiz.pdfas.api.sign.SignParameters SignParameters;
	
	protected PDFASSignatureParameter PDFAsSignatureParameter;
	
	public PDFASSignatureParameter getPDFAsSignatureParameter() {
		return PDFAsSignatureParameter;
	}

	public void setPDFAsSignatureParameter(
			PDFASSignatureParameter pDFAsSignatureParameter) {
		PDFAsSignatureParameter = pDFAsSignatureParameter;
	}

	public at.gv.egiz.pdfas.api.sign.SignParameters getSignParameters() {
		return SignParameters;
	}

	public void setSignParameters(
			at.gv.egiz.pdfas.api.sign.SignParameters signParameters) {
		SignParameters = signParameters;
	}

	public SignatureDetailInformation getSignatureDetailInformation() {
		return SignatureDetailInformation;
	}

	public void setSignatureDetailInformation(
			SignatureDetailInformation signatureDetailInformation) {
		SignatureDetailInformation = signatureDetailInformation;
	}
}
