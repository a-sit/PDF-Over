package at.asit.pdfover.pdfsignator;

import at.asit.pdfover.pdfsignator.profileproperties.ProfileProperty;
import at.asit.pdfover.pdfsignator.profileproperties.validators.PropertyValidator;

public class InvalidPropertyTypeException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6174277563400848906L;

	public InvalidPropertyTypeException(ProfileProperty property, PropertyValidator validator) {
		super(String.format("Cannot add validator: %s to Property: %s (incompatible)", 
				validator.getClass().getName(), property.getClass().getName()));
	}
}
