package at.asit.pdfover.pdfsignator.profileproperties;

import at.asit.pdfover.pdfsignator.InvalidPropertyTypeException;
import at.asit.pdfover.pdfsignator.InvalidPropertyValueException;

public class StringProfileProperty extends ProfileProperty {
	/**
	 * Sets the string value of the Property
	 * @param value The string value
	 * @throws InvalidPropertyValueException
	 * @throws InvalidPropertyTypeException 
	 */
	public void SetValue(String value) throws InvalidPropertyValueException, InvalidPropertyTypeException {
		this.SetTextValue(value);		
	}
	
	/**
	 * Gets the string value
	 * @return the string value of the property
	 */
	public String GetValue() {
		return this.GetTextValue();
	}
}
