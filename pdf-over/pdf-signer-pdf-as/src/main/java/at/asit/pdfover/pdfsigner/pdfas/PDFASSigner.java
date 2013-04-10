package at.asit.pdfover.pdfsigner.pdfas;

import at.asit.pdfover.pdfsigner.ByteArrayDocumentSource;
import at.asit.pdfover.pdfsigner.PDFSignatureException;
import at.asit.pdfover.pdfsigner.PDFSignerInterface;
import at.asit.pdfover.pdfsigner.SignResult;
import at.asit.pdfover.pdfsigner.SignResultImpl;
import at.asit.pdfover.pdfsigner.SignatureParameter;
import at.asit.pdfover.pdfsigner.SignaturePosition;
import at.asit.pdfover.pdfsigner.SigningState;
import at.gv.egiz.pdfas.api.PdfAs;
import at.gv.egiz.pdfas.api.sign.SignParameters;
import at.gv.egiz.pdfas.api.sign.SignatureDetailInformation;
import at.gv.egiz.pdfas.io.ByteArrayDataSink;
import at.gv.egiz.pdfas.api.commons.Constants;
import at.gv.egiz.pdfas.api.exceptions.PdfAsException;
import at.gv.egiz.pdfas.api.internal.LocalBKUParams;
import at.gv.egiz.pdfas.api.internal.PdfAsInternal;

/**
 * PDF AS Signer Implemtation
 * 
 * @author afitzek
 */
public class PDFASSigner implements PDFSignerInterface {

	protected static final String PROFILE_ID = "";

	protected static final String URL_TEMPLATE = "";

	@Override
	public SigningState Prepare(SignatureParameter parameter)
			throws PDFSignatureException {
		try {
			PDFASSignatureParameter sign_para = null;

			if (PDFASSignatureParameter.class.isInstance(parameter)) {
				sign_para = PDFASSignatureParameter.class.cast(parameter);
			}

			if (sign_para == null) {
				throw new PDFSignatureException("Incorrect SignatureParameter!");
			}

			PdfAs pdfas = PDFASHelper.GetPdfAs();
			
			PDFASSigningState state = new PDFASSigningState();

			SignParameters params = new SignParameters();
			params.setSignaturePositioning(sign_para.GetPDFASPositioning());
			params.setSignatureDevice(Constants.SIGNATURE_DEVICE_BKU);
			params.setSignatureType(Constants.SIGNATURE_TYPE_BINARY);
			params.setSignatureProfileId(PROFILE_ID);
			
			if(parameter.GetCollimatingMark() != null) {
				// TODO: Define CollimatingMark and use 
				params.setProfileOverrideValue("SIG_LABEL", "./images/signatur-logo_en.png");   
			}
			
			
			params.setDocument(sign_para.GetPDFASDataSource());

			state.setSignParameters(params);

			
			PdfAsInternal pdfasInternal = PDFASHelper.GetPdfAsInternal();

			// Prepares the document
			SignatureDetailInformation sdi = pdfas.prepareSign(params);

			state.setSignatureDetailInformation(sdi);

			// Retrieve the SL Request
			String slRequest = pdfasInternal.prepareLocalSignRequest(params,
					false, URL_TEMPLATE, sdi);

			PDFASSLRequest request = new PDFASSLRequest(slRequest);

			state.SetSLSignatureRequest(request);

			return state;
		} catch (PdfAsException e) {
			throw new PDFSignatureException(e);
		}
	}

	@Override
	public SignResult Sign(SigningState state) throws PDFSignatureException {
		try {
			PDFASSigningState sstate = null;

			if (PDFASSigningState.class.isInstance(state)) {
				sstate = PDFASSigningState.class.cast(state);
			}

			if (sstate == null) {
				throw new PDFSignatureException("Incorrect SigningState!");
			}

			// Retrieve objects
			PdfAs pdfas = PDFASHelper.GetPdfAs();

			PdfAsInternal pdfasInternal = PDFASHelper.GetPdfAsInternal();

			SignParameters params = sstate.getSignParameters();
			
			// Prepare Output sink
			ByteArrayDataSink data = new ByteArrayDataSink();
			params.setOutput(data);
			
			SignatureDetailInformation sdi = sstate
					.getSignatureDetailInformation();

			LocalBKUParams bkuParams = new LocalBKUParams(null, null, null);

			// Perform signature
			// TODO: NEED TO check GetSLSignatureResponse() Interface to retrieve SL Response ...
			at.gv.egiz.pdfas.api.sign.SignResult signResult = pdfasInternal
					.finishLocalSign(pdfas, params, sdi, bkuParams, false,
							sstate.GetSLSignatureResponse().GetSLRespone());

			// Preparing Result Response 
			SignResultImpl result = new SignResultImpl();

			// Set Signer Certificate
			result.SetSignerCertificate(signResult.getSignerCertificate());
			at.gv.egiz.pdfas.api.sign.pos.SignaturePosition pdfasPos = signResult
					.getSignaturePosition();

			// Set Signature position
			SignaturePosition pos = new SignaturePosition(pdfasPos.getX(),
					pdfasPos.getY(), pdfasPos.getPage());
			pos.SetAuto(sstate.getPDFAsSignatureParameter()
					.GetSignaturePosition().GetAuto());
			result.SetSignaturePosition(pos);

			// Set signed Document 
			result.SetSignedDocument(new ByteArrayDocumentSource(data.getData()));
			
			return result;
		} catch (PdfAsException e) {
			throw new PDFSignatureException(e);
		}
	}

	@Override
	public SignatureParameter GetParameter() {
		return new PDFASSignatureParameter();
	}

}
