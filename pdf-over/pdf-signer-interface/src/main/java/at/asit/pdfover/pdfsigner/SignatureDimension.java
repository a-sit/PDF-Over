package at.asit.pdfover.pdfsigner;

/**
 * The Dimensions of the visible signature block
 * @author afitzek
 */
public class SignatureDimension {

	/**
	 * The visible Signature block width
	 */
	protected int width;

	/**
	 * The visible Signature block height
	 */
	protected int height;

	/**
	 * Sets the width for the dimension
	 * @param value
	 */
	public void SetWidth(int value) {
		this.width = value;
	}

	/**
	 * Constructor
	 * @param width The width of the signature block
	 * @param height The height of the signature block
	 */
	public SignatureDimension(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Gets the width of the visible Signature block
	 * @return
	 */
	public int GetWidth() {
		return this.width;
	}
	
	/**
	 * Sets the height for the dimension
	 * @param value
	 */
	public void SetHeight(int value) {
		this.height = value;
	}

	/**
	 * Gets the height of the visible Signature block
	 * @return
	 */
	public int GetHeight() {
		return this.height;
	}
}
