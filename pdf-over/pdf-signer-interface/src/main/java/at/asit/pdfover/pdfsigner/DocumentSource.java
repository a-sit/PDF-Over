package at.asit.pdfover.pdfsigner;

import java.io.InputStream;

/**
 * A Document Source
 * 
 * @author afitzek
 */
public interface DocumentSource {

	/**
	 * Gets Document as INput Stream
	 * @return
	 */
	public InputStream GetInputStream();

	/**
	 * Get Length of document
	 * @return
	 */
	public int GetLength();

	/**
	 * Get byte[]
	 * @return
	 */
	public byte[] GetByteArray();
}
