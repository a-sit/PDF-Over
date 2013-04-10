package at.asit.pdfover.pdfsignator;

import java.util.ArrayList;
import java.util.List;

import at.asit.pdfover.pdfsignator.profileproperties.ProfileProperty;

/**
 * Represents a Signature profile
 * @author afitzek
 */
public abstract class SignatureProfile {

	/** 
	 * The profile ID
	 */
	protected String profileID;

	/**
	 * The Profile properties
	 * @uml.property  name="profileProperty"
	 * @uml.associationEnd  multiplicity="(0 -1)" ordering="true" aggregation="shared" inverse="signatureProfile:at.asit.pdfover.pdfsignator.profileproperties.ProfileProperty"
	 *
	 */
	protected List<ProfileProperty> properties = new ArrayList<ProfileProperty>();
	
	/**
	 * Gets the profile ID
	 * @return  Returns the profileID.
	 */
	public String GetProfileID() {
		return profileID;
	}

	/**
	 * Adds a property to this profile
	 * @param property
	 */
	public void AddProperty(ProfileProperty property) {
		
		ProfileProperty replace = this.GetProperty(property.GetKey());
		
		if(replace != null)
		{
			this.properties.remove(replace);
			replace = null;
		}
		
		this.properties.add(property);
	}
	
	/**
	 * Gets a property by its key
	 * @param key The property key
	 * @return The ProfileProperty or null if not available
	 */
	public ProfileProperty GetProperty(String key) {
		ProfileProperty find = null;
		for(ProfileProperty available : this.properties) {
			find = available;
			if(find.GetKey().equals(key)) {
				return find;
			}
		}
		return null;
	}
	
	/**
	 * Gets the Signature Dimension
	 * @return
	 */
	public abstract SignatureDimension GetSignatureDimension();
	
	/**
	 * Gets available Properties for this Profile 
	 * @return
	 */
	public ProfileProperty[] GetAvailableProperties() {
		return this.properties.toArray(new ProfileProperty[0]);
	}
}
