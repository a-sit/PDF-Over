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
import java.io.ByteArrayOutputStream;

import at.asit.pdfover.signer.BkuSlConnector;
import at.asit.pdfover.signer.SignatureException;
import at.gv.egiz.pdfas.common.exceptions.PDFASError;
import at.gv.egiz.pdfas.lib.api.sign.IPlainSigner;
import at.gv.egiz.pdfas.lib.api.sign.SignParameter;
import at.gv.egiz.pdfas.sigs.pades.PAdESSignerKeystore;

/**
 * Signing State for PDFAS Wrapper
 */
public class PdfAs4SigningState {

	/**
	 * The Signature Layer request
	 */
	public PdfAs4SLRequest signatureRequest;

	/**
	 * The Signature Layer response
	 */
	public String signatureResponse;

	/**
	 * The Sign Parameters
	 */
	public SignParameter signParameter;

	public ByteArrayOutputStream output;

	public BkuSlConnector bkuConnector = null;

	private IPlainSigner keystoreSigner = null;

	public boolean useBase64Request;

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.SigningState#setKSSigner(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void setKeystoreSigner(String file, String alias, String kspassword,
			String keypassword, String type) throws SignatureException {
		try {
			this.keystoreSigner = new PAdESSignerKeystore(file, alias, kspassword, keypassword, type);
		} catch (PDFASError e) {
			throw new SignatureException(e);
		}
	}

	/**
	 * @return whether a KS signer was set
	 */
	public boolean hasKeystoreSigner() {
		return this.keystoreSigner != null;
	}

	/**
	 * @return the KS signer
	 */
	public IPlainSigner getKeystoreSigner() {
		return this.keystoreSigner;
	}

}
