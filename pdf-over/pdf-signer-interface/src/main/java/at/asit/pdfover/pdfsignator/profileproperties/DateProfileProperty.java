package at.asit.pdfover.pdfsignator.profileproperties;

import java.util.Date;

import at.asit.pdfover.pdfsignator.InvalidPropertyTypeException;
import at.asit.pdfover.pdfsignator.InvalidPropertyValueException;

/**
 * A Date Property
 */
public class DateProfileProperty extends ProfileProperty {
	
	/**
	 * Date value of property
	 */
	protected Date dvalue = null;

	/**
	 * Sets the date value of the Property
	 * @param value The date value
	 * @throws InvalidPropertyValueException
	 * @throws InvalidPropertyTypeException 
	 */
	public void SetValue(Date value) throws InvalidPropertyValueException, InvalidPropertyTypeException {
		this.SetTextValue(value.toString());		
		this.dvalue = value;
	}
	
	/**
	 * Gets the date value
	 * @return the date value of the property
	 */
	public Date GetValue() {
		return this.dvalue;
	}
}
