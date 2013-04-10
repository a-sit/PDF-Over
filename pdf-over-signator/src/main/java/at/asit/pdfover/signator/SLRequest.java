package at.asit.pdfover.signator;

/**
 * Security Layer Request
 */
public interface SLRequest {
	/**
	 * Gets the signature data for this request
	 * @return The document source
	 */
	public DocumentSource getSignatureData();
}
