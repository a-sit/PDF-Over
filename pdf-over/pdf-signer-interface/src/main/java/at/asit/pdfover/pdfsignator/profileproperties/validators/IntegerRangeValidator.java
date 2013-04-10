package at.asit.pdfover.pdfsignator.profileproperties.validators;

import at.asit.pdfover.pdfsignator.InvalidPropertyTypeException;
import at.asit.pdfover.pdfsignator.InvalidPropertyValueException;
import at.asit.pdfover.pdfsignator.profileproperties.IntegerProfileProperty;
import at.asit.pdfover.pdfsignator.profileproperties.ProfileProperty;

public class IntegerRangeValidator extends PropertyValidator {

	/**
	 * Maximum value of property
	 */
	protected int max;
	
	/**
	 * Minimum value of property
	 */
	protected int min;
	
	/**
	 * Constructor
	 * @param min The minimum allowed value
	 * @param max The maximum allowed value
	 */
	public IntegerRangeValidator(int min, int max) {
		this.max = max;
		this.min = min;
	}
	
	@Override
	public void validate(ProfileProperty property)
			throws InvalidPropertyValueException, InvalidPropertyTypeException {
		this.CheckPropertyType(property);
		
		IntegerProfileProperty prop = (IntegerProfileProperty) property;
		
		if(prop.GetValue() == null)
		{
			throw new InvalidPropertyValueException(property, "Value is not set!");
		}
		
		int value = prop.GetValue();
		
		if(value < min || value > max) {
			throw new InvalidPropertyValueException(property, 
					String.format("Value has to be between %d and %d", min, max));
		}
	}

	@Override
	public void CheckPropertyType(ProfileProperty property)
			throws InvalidPropertyTypeException {
		if(!(property instanceof IntegerProfileProperty)) {
			throw new InvalidPropertyTypeException(property, this);
		}
	}

}
