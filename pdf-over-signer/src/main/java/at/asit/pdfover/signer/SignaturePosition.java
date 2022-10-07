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
package at.asit.pdfover.signer;

//Imports

/**
 * Represents the position of a visible signature block
 */
public class SignaturePosition {

	/**
	 * The x value of the position
	 */
	protected double x = 0;

	/**
	 * The y value of the position
	 */
	protected double y = 0;

	/**
	 * The page value of the position
	 */
	protected int page = 0;

	/**
	 * Whether automatic positioning is used
	 */
	protected boolean autoPositioning;

	/**
	 * Default constructor
	 * No position given, hence automatic positioning will be used
	 */
	public SignaturePosition() {
		this.autoPositioning = true;
	}

	/**
	 * X - Y Constructor (Page = 0)
	 * @param x The x value of the position
	 * @param y The y value of the position
	 */
	public SignaturePosition(double x, double y) {
		this.autoPositioning = false;
		setPosition(x, y);
	}

	/**
	 * Constructor
	 * @param x The x value of the signature position
	 * @param y The y value of the signature position
	 * @param page The page value of the signature position
	 */
	public SignaturePosition(double x, double y, int page) {
		this.autoPositioning = false;
		setPosition(x, y);
		setPage(page);
	}

	/**
	 * Sets new position
	 * @param x the new x value
	 * @param y the new y value
	 */
	public void setPosition(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Gets the X value of the position
	 * @return double the x value of the position
	 */
	public double getX() {
		return this.x;
	}

	/**
	 * Gets the Y value of the position
	 * @return double the y value of the position
	 */
	public double getY() {
		return this.y;
	}

	/**
	 * Sets Page value of position
	 * @param page the new page value
	 */
	public void setPage(int page) {
		this.page = page;
	}

	/**
	 * Gets the Page value of the position
	 * @return int the page value of the position
	 */
	public int getPage() {
		return this.page;
	}

	/**
	 * Gets whether automatic positioning is used
	 * @return true if the signature position is determined automatically
	 */
	public boolean useAutoPositioning() {
		return this.autoPositioning;
	}
}
