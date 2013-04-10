package at.asit.pdfover.signator;

/**
 * Security Layer response
 */
public class SLResponse {
	
	private String response;
	
	/**
	 * Create a new Security Layer response
	 * @param response the SLResponse
	 */
	public SLResponse(String response) {
		this.response = response;
	}
	
	/**
	 * Get the Security Layer response text
	 * @return the Security Layer response text
	 */
	public String getSLRespone() {
		return this.response;
	}
}
