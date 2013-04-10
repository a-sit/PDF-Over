package at.asit.pdfover.signer.pdfas;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import at.gv.egiz.pdfas.api.io.DataSource;

public class ByteArrayPDFASDataSource implements DataSource {

	private byte[] data;

	public ByteArrayPDFASDataSource(byte[] data) {
		this.data = data;
	}

	@Override
	public InputStream createInputStream() {
		return new ByteArrayInputStream(this.data);
	}

	@Override
	public int getLength() {
		return this.data.length;
	}

	@Override
	public byte[] getAsByteArray() {
		return this.data;
	}

	@Override
	public String getMimeType() {
		return "application/pdf";
	}

	@Override
	public String getCharacterEncoding() {
		//TODO
		return null;
	}

}