package at.asit.pdfover.signator;

/**
 * Base class for signature exceptions
 */
public class SignatureException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 711578398780816710L;
	

	/**
	 * Empty constructor
	 */
	public SignatureException() {
		super();
	}

	/**
	 * Constructor with causing exception
	 * @param cause the cause
	 */
	public SignatureException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Constructor with message
	 * @param msg the message
	 */
	public SignatureException(String msg) {
		super(msg);
	}

	/**
	 * Constructor with message and causing exception
	 * @param message the message
	 * @param cause the cause
	 */
	public SignatureException(String message, Throwable cause) {
		super(message, cause);
	}
}
