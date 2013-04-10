package at.asit.pdfover.signator;

/**
 * PDF Signer base Class
 * This class should be extended to support libraries such as PDF-AS or PADES.
 */
public interface Signer {
	
	/**
	 * Prepare a signature
	 * Defines signature parameters, the pdf library prepares the pdf document to sign and
	 * creates a Security Layer Request.
	 * @param parameter The signature parameters
	 * @return The signing state (contains the prepared document and the signature request
	 * @throws SignatureException
	 */
	public SigningState prepare(SignatureParameter parameter) throws SignatureException;
	
	/**
	 * Adds the signature to the document.
	 * The SL Response has to be set in the state
	 * @param state The signing state
	 * @return The signature Result
	 * @throws SignatureException
	 */
	public SignResult sign(SigningState state) throws SignatureException;
	
	/**
	 * Creates new signing profile
	 * @return The new Profile
	 */
	public SignatureParameter newParameter();
}
