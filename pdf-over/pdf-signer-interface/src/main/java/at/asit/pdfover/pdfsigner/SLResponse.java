package at.asit.pdfover.pdfsigner;

/**
 * Securtiy Layer Response
 * @author afitzek
 */
public class SLResponse {
	
	private String response;
	
	public SLResponse(String value) {
		response = value;
	}
	
	public String GetSLRespone() {
		return this.response;
	}
}
