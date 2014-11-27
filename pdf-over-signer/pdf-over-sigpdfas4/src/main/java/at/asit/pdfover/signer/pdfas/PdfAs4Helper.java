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

// Imports
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.signator.SignatureException;
import at.gv.egiz.pdfas.lib.api.PdfAs;
import at.gv.egiz.pdfas.lib.api.PdfAsFactory;

/**
 * PDF-AS 4 Helper
 */
public class PdfAs4Helper {
	/**
	 * SLF4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(PdfAs4Helper.class);

	/**
	 * PDF-AS Object
	 */
	private static PdfAs pdfAs = null;

	/**
	 * Gets PDF-AS Object
	 * @return the PDF-AS Object
	 * @throws SignatureException
	 */
	public static synchronized PdfAs getPdfAs() throws SignatureException {
		if (pdfAs == null) {
			try {
				pdfAs = createPdfAs();
			} catch(Exception e) {
				throw new SignatureException(e);
			}
		}
		return pdfAs;
	}

	/**
	 * Creates PDF-AS Object
	 * @return the PDF-AS Object
	 * @throws PdfAsException
	 */
	private static PdfAs createPdfAs() {
		File directory = new File (".");
		System.setProperty("log4j.configuration", directory.getAbsolutePath() +
				"/log4j.properties");
		return PdfAsFactory.createPdfAs(new File(getWorkDir()));
	}

	/**
	 * Provides the working directory
	 * @return the working directory
	 */
	public static String getWorkDir() {
		return System.getProperty("user.home") + "/.pdf-over";
	}
}
