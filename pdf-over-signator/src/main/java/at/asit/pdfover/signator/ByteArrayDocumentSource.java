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
package at.asit.pdfover.signator;

//Imports
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * A DocumentSource using a byte[] to store the document content
 */
public class ByteArrayDocumentSource implements DocumentSource {

	/**
	 * Document content
	 */
	protected byte[] data;

	/**
	 * Constructor with byte[] content
	 * @param data the document content
	 */
	public ByteArrayDocumentSource(byte[] data) {
		this.data = data;
	}

	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(this.data);
	}

	@Override
	public int getLength() {
		return this.data.length;
	}

	@Override
	public byte[] getByteArray() {
		return this.data;
	}

}
