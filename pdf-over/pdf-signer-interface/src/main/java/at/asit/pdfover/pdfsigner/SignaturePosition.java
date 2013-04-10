package at.asit.pdfover.pdfsigner;


/**
 * Represents the position of a visible signature block
 * @author afitzek
 */
public class SignaturePosition {
	/**
	 * The x value of the position
	 */
	protected int x = 0;
	
	/**
	 * The y value of the position
	 */
	protected int y = 0;
	
	/**
	 * The page value of the position
	 */
	protected int page = 1;
	
	/**
	 * Default constructor
	 */
	public SignaturePosition() {
	}
	
	/**
	 * X - Y Constructor Page = 1
	 * @param x The x value of the position
	 * @param y The y value of the position
	 */
	public SignaturePosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Constructor
	 * @param x The x value of the position
	 * @param y The y value of the position
	 * @param page The page value of the position
	 */
	public SignaturePosition(int x, int y, int page) {
		this.x = x;
		this.y = y;
		this.page = page;
	}
	
	/**
	 * Sets X value of position
	 * @param value the new x value
	 */
	public void SetX(int value) {
		this.x = value;
	}
	
	/**
	 * Gets the X value of the position
	 * @return int the x value of the position
	 */
	public int GetX() {
		return this.x;
	}
	
	/**
	 * Sets Y value of position
	 * @param value the new y value
	 */
	public void SetY(int value) {
		this.y = value;
	}
	
	/**
	 * Gets the Y value of the position
	 * @return int the y value of the position
	 */
	public int GetY() {
		return this.y;
	}
	
	/**
	 * Sets Page value of position
	 * @param value the new page value
	 */
	public void SetPage(int value) {
		this.page = value;
	}
	
	/**
	 * Gets the Page value of the position
	 * @return int the page value of the position
	 */
	public int GetPage() {
		return this.page;
	}

}
