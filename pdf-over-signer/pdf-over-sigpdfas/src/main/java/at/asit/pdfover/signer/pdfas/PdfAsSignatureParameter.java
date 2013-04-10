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
import java.util.HashMap;

import at.asit.pdfover.signator.SignatureDimension;
import at.asit.pdfover.signator.SignatureParameter;
import at.asit.pdfover.signator.SignaturePosition;
import at.gv.egiz.pdfas.api.io.DataSource;
import at.gv.egiz.pdfas.api.sign.pos.SignaturePositioning;
import at.knowcenter.wag.egov.egiz.exceptions.PDFDocumentException;

/**
 * Implementation of SignatureParameter specific for PDF - AS Library
 */
public class PdfAsSignatureParameter extends SignatureParameter {

	private HashMap<String, String> genericProperties = new HashMap<String, String>(); 
	
	@Override
	public SignatureDimension getPlaceholderDimension() {
		return new SignatureDimension(487, 206);
	}

	/**
	 * Gets the PDFAS Positioning
	 * @return SignaturePositioning
	 * @throws PDFDocumentException 
	 */
	public SignaturePositioning getPDFASPositioning() throws PDFDocumentException {
		SignaturePosition position = this.getSignaturePosition();
		position.useAutoPositioning();
		
		SignaturePositioning positioning = null;
		if(!position.useAutoPositioning()) {
			positioning = new SignaturePositioning(String.format("p:%d;x:%f;y:%f", 
					position.getPage(), position.getX(), position.getY()));
		} else {
			positioning = new SignaturePositioning();
		}
		
		return positioning;
	}

	/**
	 * Gets PDF - AS specific data source
	 * @return ByteArrayPDFASDataSource
	 */
	public DataSource getPDFASDataSource() {
		return new ByteArrayPDFASDataSource(this.getInputDocument().getByteArray());
	}

	@Override
	public void setProperty(String key, String value) {
		this.genericProperties.put(key, value);
	}

	@Override
	public String getProperty(String key) {
		return this.genericProperties.get(key);
	}
}
