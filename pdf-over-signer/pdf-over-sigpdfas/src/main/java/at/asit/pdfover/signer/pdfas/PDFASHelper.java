package at.asit.pdfover.signer.pdfas;

import at.asit.pdfover.signator.SignatureException;
import at.gv.egiz.pdfas.api.PdfAs;
import at.gv.egiz.pdfas.api.exceptions.PdfAsException;
import at.gv.egiz.pdfas.api.internal.PdfAsInternal;

/**
 * Encapsulates PDF AS API Object to need just one initialization
 * @author afitzek
 */
public class PDFASHelper {
	
	/**
	 * PDF AS Object
	 */
	private static PdfAs pdfAs = null;
	
	/**
	 * Internal Pdf AS Object
	 */
	private static PdfAsInternal pdfAsInternal = null;
	
	/**
	 * Creates PDF AS Object
	 * @return
	 * @throws PdfAsException
	 */
	private static PdfAs createPdfAs() throws PdfAsException {
		return new at.gv.egiz.pdfas.impl.api.PdfAsObject();
	}
	
	/**
	 * Creates a PDF-AS Internal object
	 * @return the PDF-AS Internal object
	 * @throws PdfAsException
	 */
	private static PdfAsInternal createPdfAsInternal() throws PdfAsException {
		return new at.gv.egiz.pdfas.impl.api.internal.PdfAsInternalObject();
	}
	
	/**
	 * Gets PDF-AS Object
	 * @return the PDF-AS Object
	 * @throws SignatureException
	 */
	public static synchronized PdfAs getPdfAs() throws SignatureException {
		if (pdfAs == null) {
			try {
				pdfAs = createPdfAs();
			} catch(PdfAsException e)  {
				throw new SignatureException(e);
			}
		}
		return pdfAs;
	}
	
	/**
	 * Gets PDF-AS Internal object
	 * @return the PDF-AS Internal object
	 * @throws SignatureException
	 */
	public static synchronized PdfAsInternal getPdfAsInternal() throws SignatureException {
		if(pdfAsInternal == null) {
			try {
				pdfAsInternal = createPdfAsInternal();
			} catch(PdfAsException e)  {
				throw new SignatureException(e);
			}
		}
		return pdfAsInternal;
	}
}
