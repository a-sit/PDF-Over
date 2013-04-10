package at.asit.pdfover.pdfsigner.pdfas;

import at.asit.pdfover.pdfsigner.PDFSignatureException;
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
	private static PdfAs pdf_as_instance = null;
	
	/**
	 * Internal Pdf AS Object
	 */
	private static PdfAsInternal pdf_as_internal = null;
	
	/**
	 * Creates PDF AS Object
	 * @return
	 * @throws PdfAsException
	 */
	private static PdfAs CreatePdfAs() throws PdfAsException {
		return new at.gv.egiz.pdfas.impl.api.PdfAsObject();
	}
	
	/**
	 * Creates a PDF AS Internal object
	 * @return
	 * @throws PdfAsException
	 */
	private static PdfAsInternal CreatePdfAsInternal() throws PdfAsException {
		return new at.gv.egiz.pdfas.impl.api.internal.PdfAsInternalObject();
	}
	
	/**
	 * Gets PDF AS Object
	 * @return
	 * @throws PDFSignatureException
	 */
	public static synchronized PdfAs GetPdfAs() throws PDFSignatureException {
		if(pdf_as_instance == null) {
			try {
				pdf_as_instance = CreatePdfAs();
			} catch(PdfAsException e)  {
				throw new PDFSignatureException(e);
			}
		}
		return pdf_as_instance;
	}
	
	/**
	 * Gets PDF AS Internal object
	 * @return
	 * @throws PDFSignatureException
	 */
	public static synchronized PdfAsInternal GetPdfAsInternal() throws PDFSignatureException {
		if(pdf_as_internal == null) {
			try {
				pdf_as_internal = CreatePdfAsInternal();
			} catch(PdfAsException e)  {
				throw new PDFSignatureException(e);
			}
		}
		return pdf_as_internal;
	}
}
