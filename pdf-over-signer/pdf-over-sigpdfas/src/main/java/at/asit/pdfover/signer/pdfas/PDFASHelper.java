/*
 * Copyright 2012 by A-SIT, Secure Information Technology Center Austria
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package at.asit.pdfover.signer.pdfas;

//Imports
import java.io.File;

import at.asit.pdfover.signator.SignatureException;
import at.gv.egiz.pdfas.api.PdfAs;
import at.gv.egiz.pdfas.api.exceptions.PdfAsException;
import at.gv.egiz.pdfas.api.internal.PdfAsInternal;

/**
 * Encapsulates PDF AS API Object to need just one initialization
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
		File directory = new File (".");
		System.setProperty("log4j.configuration", directory.getAbsolutePath()+"/log4j.properties");
		System.setProperty("pdf-as.work-dir", System.getProperty("user.home")+"/.pdfover");
		return new at.gv.egiz.pdfas.impl.api.PdfAsObject();
	}
	
	public static String getWorkDir() {
		return System.getProperty("user.home")+"/.pdfover";
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
