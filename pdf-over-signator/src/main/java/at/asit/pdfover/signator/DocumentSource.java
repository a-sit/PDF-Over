package at.asit.pdfover.signator;

import java.io.InputStream;

/**
 * A Document Source
 */
public interface DocumentSource {

	/**
	 * Gets Document as Input Stream
	 * @return InputStream of the document
	 */
	public InputStream getInputStream();

	/**
	 * Get Length of document
	 * @return length of the document
	 */
	public int getLength();

	/**
	 * Get Document as byte[]
	 * @return byte[] of the Document
	 */
	public byte[] getByteArray();
}
