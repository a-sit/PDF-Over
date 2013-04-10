package at.asit.pdfover.pdfsigner;


/**
 * Represents the position of a visible signature block
 * @author afitzek
 */
public class SignaturePosition {
	/**
	 * The x value of the position
	 */
	protected float x = 0;
	
	/**
	 * The y value of the position
	 */
	protected float y = 0;
	
	/**
	 * The page value of the position
	 */
	protected int page = 1;
	
	protected boolean auto = true;
	
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
	public SignaturePosition(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Constructor
	 * @param x The x value of the position
	 * @param y The y value of the position
	 * @param page The page value of the position
	 */
	public SignaturePosition(float x, float y, int page) {
		this.x = x;
		this.y = y;
		this.page = page;
	}
	
	/**
	 * Sets X value of position
	 * @param value the new x value
	 */
	public void SetX(float value) {
		this.x = value;
	}
	
	/**
	 * Gets the X value of the position
	 * @return float the x value of the position
	 */
	public float GetX() {
		return this.x;
	}
	
	/**
	 * Sets Y value of position
	 * @param value the new y value
	 */
	public void SetY(float value) {
		this.y = value;
	}
	
	/**
	 * Gets the Y value of the position
	 * @return float the y value of the position
	 */
	public float GetY() {
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

	/**
	 * Sets Page value of position
	 * @param value the new page value
	 */
	public void SetAuto(boolean value) {
		this.auto = value;
	}
	
	/**
	 * Gets the Page value of the position
	 * @return int the page value of the position
	 */
	public boolean GetAuto() {
		return this.auto;
	}
}
