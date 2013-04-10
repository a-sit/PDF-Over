package at.asit.pdfover.pdfsignator.profileproperties.validators;

import at.asit.pdfover.pdfsignator.InvalidPropertyTypeException;
import at.asit.pdfover.pdfsignator.InvalidPropertyValueException;
import at.asit.pdfover.pdfsignator.profileproperties.ProfileProperty;

public class RegExValidator extends PropertyValidator {

	/**
	 * The regex value
	 */
	protected String regex;

	/**
	 * Constructor
	 * @param regex The regex to check
	 */
	public RegExValidator(String regex) {
		this.regex = regex;
	}

	@Override
	public void validate(ProfileProperty property)
			throws InvalidPropertyValueException, InvalidPropertyTypeException {
		
		if(!property.GetTextValue().matches(this.regex)) {
			throw new InvalidPropertyValueException(property, String.format(
					"Value is invalid!"));
		}
	}

	@Override
	public void CheckPropertyType(ProfileProperty property)
			throws InvalidPropertyTypeException {
		// Is valid on all Property Types
	}
}
