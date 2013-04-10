package at.asit.pdfover.pdfsignator.profileproperties.validators;

import java.util.Comparator;

/**
 * Compares the Priority of two PropertyValidators
 * @author afitzek
 *
 */
public class PropertyValidatorComparer implements Comparator<PropertyValidator>  {

	public int compare(PropertyValidator o1, PropertyValidator o2) {
		return o1.GetPriority() - o2.GetPriority();
	}

}
