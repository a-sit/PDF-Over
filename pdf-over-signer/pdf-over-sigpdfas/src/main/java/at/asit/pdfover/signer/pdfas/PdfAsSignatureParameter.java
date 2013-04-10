package at.asit.pdfover.signer.pdfas;

import at.asit.pdfover.signator.SignatureDimension;
import at.asit.pdfover.signator.SignatureParameter;
import at.gv.egiz.pdfas.api.sign.pos.SignaturePositioning;
import at.gv.egiz.pdfas.api.io.DataSource;

public class PdfAsSignatureParameter extends SignatureParameter {

	@Override
	public SignatureDimension getPlaceholderDimension() {
		// TODO Auto-generated method stub
		return null;
	}

	public SignaturePositioning getPDFASPositioning() {
		// TODO: implement Signature creation
		return new SignaturePositioning();
	}

	public DataSource getPDFASDataSource() {
		// TODO: implement Signature creation
		return new ByteArrayPDFASDataSource(null);
	}

	@Override
	public void setProperty(String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getProperty(String key) {
		// TODO Auto-generated method stub
		return null;
	}
}
