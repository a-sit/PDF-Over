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
package at.asit.pdfover.gui.workflow;

// Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.signator.Signator;
import at.asit.pdfover.signator.Signator.Signers;
import at.asit.pdfover.signator.Signer;

/**
 *
 */
public class PDFSignerImpl implements PDFSigner {
	/**
	 * SLF4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(PDFSignerImpl.class);

	private Signers signer = Signator.Signers.PDFAS4;

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.PDFSigner#getPDFSignerType()
	 */
	@Override
	public Signers getUsedPDFSignerLibrary() {
		return this.signer;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.PDFSigner#setPDFSignerType(at.asit.pdfover.signator.Signator.Signers)
	 */
	@Override
	public void setUsedPDFSignerLibrary(Signers signer) {
		if(signer != this.signer) {
			// TYPE CHANGE remove cached signer!
			this.signerLib = null;
		}
		this.signer = signer;
	}

	private Signer signerLib;

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.PDFSigner#getPDFSigner()
	 */
	@Override
	public Signer getPDFSigner() {
		if(this.signerLib == null) {
			this.signerLib = Signator.getSigner(getUsedPDFSignerLibrary());
		}
		return this.signerLib;
	}

}
