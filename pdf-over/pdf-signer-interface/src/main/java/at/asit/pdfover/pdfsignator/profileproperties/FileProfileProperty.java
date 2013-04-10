package at.asit.pdfover.pdfsignator.profileproperties;

import java.io.File;

import at.asit.pdfover.pdfsignator.InvalidPropertyTypeException;
import at.asit.pdfover.pdfsignator.InvalidPropertyValueException;

public class FileProfileProperty extends ProfileProperty {

	/**
	 * File value of property
	 */
	protected File fvalue = null;

	/**
	 * Sets the file value of the Property
	 * @param value The file value
	 * @throws InvalidPropertyValueException
	 * @throws InvalidPropertyTypeException 
	 */
	public void SetValue(File value) throws InvalidPropertyValueException, InvalidPropertyTypeException {
		this.SetTextValue(value.getAbsolutePath());		
		this.fvalue = value;
	}
	
	/**
	 * Gets the file value
	 * @return the file value of the property
	 */
	public File GetValue() {
		return this.fvalue;
	}
}
