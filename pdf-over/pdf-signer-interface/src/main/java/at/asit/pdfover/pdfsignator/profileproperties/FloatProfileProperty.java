package at.asit.pdfover.pdfsignator.profileproperties;

import at.asit.pdfover.pdfsignator.InvalidPropertyTypeException;
import at.asit.pdfover.pdfsignator.InvalidPropertyValueException;

public class FloatProfileProperty extends ProfileProperty {
	/**
	 * Float value of property
	 */
	protected Float fvalue = null;

	/**
	 * Sets the float value of the Property
	 * @param value The float value
	 * @throws InvalidPropertyValueException
	 * @throws InvalidPropertyTypeException 
	 */
	public void SetValue(float value) throws InvalidPropertyValueException, InvalidPropertyTypeException {
		this.SetTextValue(Float.toString(value));		
		this.fvalue = value;
	}
	
	/**
	 * Gets the float value
	 * @return the float value of the property
	 */
	public Float GetValue() {
		return this.fvalue;
	}
}
