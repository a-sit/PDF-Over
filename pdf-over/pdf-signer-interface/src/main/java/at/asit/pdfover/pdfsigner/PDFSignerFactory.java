package at.asit.pdfover.pdfsigner;

public abstract class PDFSignerFactory {
	public abstract PDFSignerInterface CreatePDFSigner();
	public abstract PDFSigner GetPDFSignerType();
}
