package at.asit.pdfover.pdfsignator;

/**
 * The state of the pdf signing library
 * @author afitzek
 */
public interface SigningState {
	
	/**
	 * Gets the prepared Document
	 * @return
	 */
	public DocumentSource GetPreparedDocument();

	/**
	 * Gets the Security Layer Request to create the signature
	 * @return The SL Signature Request
	 */
	public String GetSLSignatureRequest();

	/**
	 * Sets the Security Layer Request to create the signature
	 * @param value The SL Signature Request
	 */
	public void SetSLSignatureResponse(String value);
}
