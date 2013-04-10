/**
 * 
 */
package at.asit.pdfover.pdfsignator;

import at.asit.pdfover.pdfsigner.SignResult;
import at.asit.pdfover.pdfsigner.SignatureParameter;
import at.asit.pdfover.pdfsigner.SLRequest;
import at.asit.pdfover.pdfsigner.SLResponse;
import at.asit.pdfover.pdfsigner.SigningState;

/**
 * The PDF-Signator Interface
 * 
 * @author afitzek
 */
public interface PDFSignatorInterface {
	
	/**
	 * Creates signature parameter for the given pdf signer
	 * @param signer The pdf signer library
	 * @return The signature parameter
	 */
	public SignatureParameter GetSignatureParameters(PDFSigner signer);
	
	/**
	 * Prepares the signature
	 * @param parameter The signature parameter
	 * @return A Signature State for the signing library
	 */
	public SigningState SignPrepare(SignatureParameter parameter);
	
	/**
	 * Performs the signature
	 * @param response The signing state
	 * @return The signed document
	 */
	public SignResult SignPerform(SigningState state);
}
