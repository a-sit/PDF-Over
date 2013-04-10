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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import at.asit.pdfover.signator.SignatureDimension;
import at.asit.pdfover.signator.SignatureParameter;
import at.gv.egiz.pdfas.api.sign.pos.SignaturePositioning;
import at.gv.egiz.pdfas.api.io.DataSource;

/**
 * Implementation of SignatureParameter specific for PDF - AS Library
 */
public class PdfAsSignatureParameter extends SignatureParameter {

	/**
	 * SFL4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(PdfAsSignatureParameter.class);
	
	@Override
	public SignatureDimension getPlaceholderDimension() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Gets the PDFAS Positioning
	 * @return SignaturePositioning
	 */
	public SignaturePositioning getPDFASPositioning() {
		// TODO: implement Signature creation
		return new SignaturePositioning();
	}

	/**
	 * Gets PDF - AS specific data source
	 * @return ByteArrayPDFASDataSource
	 */
	public DataSource getPDFASDataSource() {
		// TODO: implement Signature creation
		return new ByteArrayPDFASDataSource(null);
	}

	@Override
	public void setProperty(String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getProperty(String key) {
		// TODO Auto-generated method stub
		return null;
	}
}
