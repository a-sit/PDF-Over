package at.asit.pdfover.pdfsignator.profileproperties.validators;

import at.asit.pdfover.pdfsignator.InvalidPropertyTypeException;
import at.asit.pdfover.pdfsignator.InvalidPropertyValueException;
import at.asit.pdfover.pdfsignator.profileproperties.FloatProfileProperty;
import at.asit.pdfover.pdfsignator.profileproperties.ProfileProperty;

public class FloatRangeValidator extends PropertyValidator {
	
	/**
	 * The maximum value 
	 */
	protected float max;

	/**
	 * The minimum value
	 */
	protected float min;

	/**
	 * Constructor
	 * @param min The minimum allowed value
	 * @param max The maximum allowed value
	 */
	public FloatRangeValidator(float min, float max) {
		this.max = max;
		this.min = min;
	}

	@Override
	public void validate(ProfileProperty property)
			throws InvalidPropertyValueException, InvalidPropertyTypeException {
		this.CheckPropertyType(property);

		FloatProfileProperty prop = (FloatProfileProperty) property;

		if (prop.GetValue() == null) {
			throw new InvalidPropertyValueException(property,
					"Value is not set!");
		}

		float value = prop.GetValue();

		if (value < min || value > max) {
			throw new InvalidPropertyValueException(property, String.format(
					"Value has to be between %f and %f", min, max));
		}
	}

	@Override
	public void CheckPropertyType(ProfileProperty property)
			throws InvalidPropertyTypeException {
		if (!(property instanceof FloatProfileProperty)) {
			throw new InvalidPropertyTypeException(property, this);
		}
	}
}
