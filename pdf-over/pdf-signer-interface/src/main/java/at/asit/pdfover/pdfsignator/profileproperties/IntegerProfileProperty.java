package at.asit.pdfover.pdfsignator.profileproperties;

import at.asit.pdfover.pdfsignator.InvalidPropertyTypeException;
import at.asit.pdfover.pdfsignator.InvalidPropertyValueException;

public class IntegerProfileProperty extends ProfileProperty {

	/**
	 * Integer value of property
	 */
	protected Integer ivalue = null;

	/**
	 * Sets the integer value of the Property
	 * @param value The integer value
	 * @throws InvalidPropertyValueException
	 * @throws InvalidPropertyTypeException 
	 */
	public void SetValue(int value) throws InvalidPropertyValueException, InvalidPropertyTypeException {
		this.SetTextValue(Integer.toString(value));		
		this.ivalue = value;
	}
	
	/**
	 * Gets the integer value
	 * @return the integer value of the property
	 */
	public Integer GetValue() {
		return this.ivalue;
	}
}
