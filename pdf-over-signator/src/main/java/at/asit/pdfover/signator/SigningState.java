package at.asit.pdfover.signator;

/**
 * The state of the pdf signing library
 */
public interface SigningState {
	
	/**
	 * Gets the Security Layer Request to create the signature
	 * @return The Signature Request
	 */
	public abstract SLRequest getSignatureRequest();

	/**
	 * Sets the Security Layer Response to the Signature Request
	 * @param value The Signature Response
	 */
	public abstract void setSignatureResponse(SLResponse value);
}
