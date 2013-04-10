package at.asit.pdfover.pdfsigner;

/**
 * The state of the pdf signing library
 * @author afitzek
 */
public interface SigningState {
	
	/**
	 * Gets the Security Layer Request to create the signature
	 * @return The SL Signature Request
	 */
	public SLRequest GetSLSignatureRequest();

	/**
	 * Sets the Security Layer Request to create the signature
	 * @param value The SL Signature Request
	 */
	public void SetSLSignatureResponse(SLResponse value);
}
