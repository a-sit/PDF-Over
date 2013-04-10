package at.asit.pdfover.pdfsigner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ByteArrayDocumentSource implements DocumentSource {

	protected byte[] data;
	
	public ByteArrayDocumentSource(byte[] data) {
		this.data = data;
	}
	
	@Override
	public InputStream GetInputStream() {
		return new ByteArrayInputStream(this.data);
	}

	@Override
	public int GetLength() {
		return data.length;
	}

	@Override
	public byte[] GetByteArray() {
		return data;
	}

}
