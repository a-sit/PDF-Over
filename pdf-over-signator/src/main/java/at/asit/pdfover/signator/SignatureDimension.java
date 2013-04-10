package at.asit.pdfover.signator;

/**
 * The dimensions of the visible signature block
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
	 * Constructor
	 * @param width The width of the signature block
	 * @param height The height of the signature block
	 */
	public SignatureDimension(int width, int height) {
		setDimension(width, height);
	}

	/**
	 * Sets the the dimension of the signature block
	 * @param width The width
	 * @param height The height
	 */
	public void setDimension(int width, int height)
	{
		setWidth(width);
		setHeight(height);
	}

	/**
	 * Sets the width for the dimension
	 * @param width
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Gets the width of the visible Signature block
	 * @return the width
	 */
	public int getWidth() {
		return this.width;
	}
	
	/**
	 * Sets the height for the dimension
	 * @param height
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Gets the height of the visible Signature block
	 * @return the height
	 */
	public int getHeight() {
		return this.height;
	}
}
