package at.asit.pdfover.pdfsigner.pdfas;

import at.asit.pdfover.pdfsigner.PDFSigner;
import at.asit.pdfover.pdfsigner.PDFSignerFactory;
import at.asit.pdfover.pdfsigner.PDFSignerInterface;

public class PDFASSignerFactory extends PDFSignerFactory {

	@Override
	public PDFSignerInterface CreatePDFSigner() {
		return new PDFASSigner();
	}

	@Override
	public PDFSigner GetPDFSignerType() {
		return PDFSigner.PDFAS;
	}

}
