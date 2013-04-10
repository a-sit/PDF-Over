package at.asit.pdfover.signator;

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
