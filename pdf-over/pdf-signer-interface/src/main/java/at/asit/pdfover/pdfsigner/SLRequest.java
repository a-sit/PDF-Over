package at.asit.pdfover.pdfsigner;

/**
 * Securtiy Layer Request
 * @author afitzek
 */
public interface SLRequest {
	/**
	 * Gets the signature data for this request
	 * @return The document source
	 */
	public DocumentSource GetSignatureData();
}
