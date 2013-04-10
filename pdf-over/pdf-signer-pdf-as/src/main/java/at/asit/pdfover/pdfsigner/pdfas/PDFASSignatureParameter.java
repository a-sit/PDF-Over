package at.asit.pdfover.pdfsigner.pdfas;

import at.asit.pdfover.pdfsigner.SignatureDimension;
import at.asit.pdfover.pdfsigner.SignatureParameter;
import at.gv.egiz.pdfas.api.sign.pos.SignaturePositioning;
import at.gv.egiz.pdfas.api.io.DataSource;

public class PDFASSignatureParameter extends SignatureParameter {

	@Override
	public SignatureDimension GetPlaceholderDimension() {
		// TODO Auto-generated method stub
		return null;
	}

	public SignaturePositioning GetPDFASPositioning() {
		// TODO: implement Signature creation
		return new SignaturePositioning();
	}
	
	public DataSource GetPDFASDataSource() {
		// TODO: implement Signature creation
		return new ByteArrayPdfAsDataSource(null);
	}
}
