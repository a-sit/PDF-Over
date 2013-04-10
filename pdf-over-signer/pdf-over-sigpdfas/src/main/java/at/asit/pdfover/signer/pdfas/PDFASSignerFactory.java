package at.asit.pdfover.signer.pdfas;

import at.asit.pdfover.signator.Signer;
import at.asit.pdfover.signator.SignerFactory;

public class PDFASSignerFactory extends SignerFactory {

	@Override
	public Signer createSigner() {
		return new PDFASSigner();
	}
}
