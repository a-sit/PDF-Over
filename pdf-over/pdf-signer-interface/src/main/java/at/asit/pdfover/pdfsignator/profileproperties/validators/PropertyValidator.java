package at.asit.pdfover.pdfsignator.profileproperties.validators;

import at.asit.pdfover.pdfsignator.InvalidPropertyTypeException;
import at.asit.pdfover.pdfsignator.InvalidPropertyValueException;
import at.asit.pdfover.pdfsignator.profileproperties.ProfileProperty;

/**
 * Validates the value of a property
 */
public abstract class PropertyValidator {
	
	/**
	 * The priority of this property should determine the order of validations
	 */
	protected int priority = 1;

	/**
	 * Called to validate the value of the given property and throws an InvalidPropertyValueException if value is invalid
	 * @param propety
	 * @throws InvalidPropertyValueException
	 */
	public abstract void validate(ProfileProperty property) throws InvalidPropertyValueException, InvalidPropertyTypeException;
	
	/**
	 * Sets the priority of this validator
	 * @param value The new priority
	 */
	public void SetPriority(int value) {
		this.priority = value;
	}
	
	/**
	 * Gets the priority of this validator
	 * @return The priority of this validator
	 */
	public int GetPriority() {
		return this.priority;
	}
	
	public abstract void CheckPropertyType(ProfileProperty property) throws InvalidPropertyTypeException;
}
