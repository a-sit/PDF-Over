package at.asit.pdfover.pdfsigner.pdfas;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import at.gv.egiz.pdfas.api.io.DataSource;

public class ByteArrayPdfAsDataSource implements DataSource {

	private byte[] data;

	public ByteArrayPdfAsDataSource(byte[] data) {
		this.data = data;
	}

	public InputStream createInputStream() {
		return new ByteArrayInputStream(this.data);
	}

	public int getLength() {
		return this.data.length;
	}

	public byte[] getAsByteArray() {
		return this.data;
	}

	public String getMimeType() {
		return "application/pdf";
	}

	public String getCharacterEncoding() {
		return null;
	}

}