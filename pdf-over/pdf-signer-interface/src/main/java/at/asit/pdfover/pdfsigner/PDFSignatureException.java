package at.asit.pdfover.pdfsigner;

/**
 * base class for signature exceptions
 * @author afitzek
 */
public class PDFSignatureException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 711578398780816710L;
	
	public PDFSignatureException(Throwable e) {
		super(e);
	}
	
	public PDFSignatureException(String msg) {
		super(msg);
	}
}
