package at.asit.pdfover.pdfsignator.profileproperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.asit.pdfover.pdfsignator.InvalidPropertyTypeException;
import at.asit.pdfover.pdfsignator.InvalidPropertyValueException;
import at.asit.pdfover.pdfsignator.profileproperties.validators.PropertyValidator;
import at.asit.pdfover.pdfsignator.profileproperties.validators.PropertyValidatorComparer;

/**
 * Defines a Profile Property
 */
public abstract class ProfileProperty {
	
	/**
	 * Is this property optional
	 */
	protected boolean optional;
	
	/**
	 * The value of the property
	 */
	private String value;
	
	/**
	 * The key of the property
	 */
	private String key;
	
	/**
	 * The list of PropertyValidator
	 * @uml.property  name="propertyValidator"
	 * @uml.associationEnd  multiplicity="(0 -1)" ordering="true" aggregation="shared" inverse="profileProperty:at.asit.pdfover.pdfsignator.profileproperties.validators.PropertyValidator"
	 */
	protected List<PropertyValidator> validators = new ArrayList<PropertyValidator>();
	
	/**
	 * Validates the ProfileProperty
	 * @throws InvalidPropertyValueException
	 * @throws InvalidPropertyTypeException 
	 */
	protected void Validate() throws InvalidPropertyValueException, InvalidPropertyTypeException {
		for(PropertyValidator validator : validators) {
			validator.validate(this);
		}
	}
	
	/**
	 * Adds a new PropertyValidator to this Property and sorts the validators according to their priority
	 * @param validator
	 * @throws InvalidPropertyTypeException 
	 */
	public void AddValidator(PropertyValidator validator) throws InvalidPropertyTypeException {
		validator.CheckPropertyType(this);
		validators.add(validator);
		Collections.sort(validators, new PropertyValidatorComparer());
	}
	
	/**
	 * Sets if the property is optional
	 * @param value The new optional value
	 */
	public void SetOptional(boolean value) {
		this.optional = value;
	}
	
	/**
	 * Gets if the Property is Optional
	 * @return Is the property optional
	 */
	public boolean GetOptional() {
		return this.optional;
	}
	
	/**
	 * Sets the string value of the property and validates the Property
	 * (All subclasses should set the value via this method!)
	 * @param value The new value
	 * @throws InvalidPropertyValueException
	 * @throws InvalidPropertyTypeException 
	 */
	public void SetTextValue(String value) throws InvalidPropertyValueException, InvalidPropertyTypeException {
		this.value = value;
		this.Validate();
	}
	
	/**
	 * Gets the property text value
	 * @return The property text value
	 */
	public String GetTextValue() {
		return this.value;
	}
	
	/**
	 * Sets the propety key
	 * @param value The new property key
	 */
	public void SetKey(String value) {
		this.key = value;
	}
	
	/**
	 * Gets the property Key
	 * @return The property key
	 */
	public String GetKey() {
		return this.key;
	}
}
