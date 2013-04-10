package at.asit.pdfover.pdfsignator;

import at.asit.pdfover.pdfsignator.profileproperties.ProfileProperty;


public class InvalidPropertyValueException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3823266882732616374L;

	public InvalidPropertyValueException(ProfileProperty property, String message) {
		super(String.format("Invalid value for: %s: %s", property.GetKey(), message));
	}
}
