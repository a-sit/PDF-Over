package at.asit.pdfover.signator;

import at.asit.pdfover.signator.Signer;

/**
 * A Signer factory
 * Creates Signer instances
 */
public abstract class SignerFactory {
	/**
	 * Create a Signer
	 * @return the new Signer
	 */
	public abstract Signer createSigner();
}
