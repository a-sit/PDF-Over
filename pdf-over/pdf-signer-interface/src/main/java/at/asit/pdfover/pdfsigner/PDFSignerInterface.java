package at.asit.pdfover.pdfsigner;

import java.security.SignatureException;

/**
 * PDF Signator base Class
 * This class should be extended to support PDF-AS and PADES.
 * @author afitzek
 */
public interface PDFSignerInterface {
	
	/**
	 * Prepare a signature
	 * Defines signature parameters, the pdf library prepares the pdf document to sign and
	 * creates a Security Layer Request.
	 * @param parameter The signature parameters
	 * @return The siging state (contains the prepared document and the signature request
	 * @throws SignatureException
	 */
	public SigningState Prepare(SignatureParameter parameter) throws SignatureException;
	
	/**
	 * Adds the signature to the document.
	 * The SL Response has to be set in the state
	 * @param state The siging state
	 * @return The signature Result
	 * @throws SignatureException
	 */
	public SignResult Sign(SigningState state) throws SignatureException;
	
	/**
	 * Creates new signing profile
	 * @param base The profile id of the base profile
	 * @param profileID The id of the new profile
	 * @return The new Profile
	 */
	public SignatureParameter GetParameter();
}
