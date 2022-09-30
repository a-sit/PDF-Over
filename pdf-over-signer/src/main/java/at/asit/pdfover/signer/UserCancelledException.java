package at.asit.pdfover.signer;

/**
 * The user cancelled the operation.
 */
public class UserCancelledException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -7341854663858331004L;

    /**
	 * Empty constructor
	 */
	public UserCancelledException() {
		super();
	}

	/**
	 * Constructor with causing exception
	 * @param cause the cause
	 */
	public UserCancelledException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with message
	 * @param msg the message
	 */
	public UserCancelledException(String msg) {
		super(msg);
	}

	/**
	 * Constructor with message and causing exception
	 * @param message the message
	 * @param cause the cause
	 */
	public UserCancelledException(String message, Throwable cause) {
		super(message, cause);
	}
}
