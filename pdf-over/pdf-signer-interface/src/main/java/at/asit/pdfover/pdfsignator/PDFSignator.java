package at.asit.pdfover.pdfsignator;

import java.util.HashMap;

/**
 * PDF Signator base Class
 * This class should be extended to support PDF-AS and PADES.
 */
public abstract class PDFSignator {
	
	/**
	 * Map to store profiles
	 */
	protected HashMap<String, SignatureProfile> profiles = new HashMap<String, SignatureProfile>();
	
	/**
	 * Perfom signature creation
	 * @param parameter The signature parameters
	 * @return The signing result
	 */
	public abstract SignResult Sign(SignParameter parameter);
	
	/**
	 * Creates new signing profile
	 * @param base The profile id of the base profile
	 * @param profileID The id of the new profile
	 * @return The new Profile
	 */
	public abstract SignatureProfile CreateNewProfile(String base, String profileID);
	
	/**
	 * Creates new signing profile
	 * @param base The base profile
	 * @param profileID The id of the new profile
	 * @return The new Profile
	 */
	public abstract SignatureProfile CreateNewProfile(SignatureProfile base, String profileID);
	
	/**
	 * Creates new signing profile
	 * @param profileID The id of the new profile
	 * @return The new Profile
	 */
	public abstract SignatureProfile CreateNewProfile(String profileID);
	
	/**
	 * Returns Profile object for given profile id
	 * @param profileID The profile id
	 * @return The requested Profile
	 */
	public SignatureProfile GetProfile(String profileID) {
		if(this.profiles.containsKey(profileID)) {
			// TODO: Think about handing out a copy of the profile to keep default values ...
			return this.profiles.get(profileID);
		}
		
		// TODO: throw Exception
		return null;
	}
	
	/**
	 * Get all available profiles
	 * @return Array containing all knwon profiles
	 */
	public SignatureProfile[] GetAvailableProfiles() {
		// TODO: Think about handing out a copy of the profile to keep default values ...
		return this.profiles.values().toArray(new SignatureProfile[0]);
	}
	
	/**
	 * Gets all available profile ids
	 * @return Array containing all known profile ids
	 */
	public String[] GetAvailableProfileIDs() {
		return this.profiles.keySet().toArray(new String[0]);
	}
}
