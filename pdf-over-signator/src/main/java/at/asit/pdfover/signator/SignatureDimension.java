/*
 * Copyright 2012 by A-SIT, Secure Information Technology Center Austria
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package at.asit.pdfover.signator;

//Imports

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
