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
package at.asit.pdfover.gui.controls;

// Imports
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Bar Button implementation
 */
public abstract class MainBarButton extends Canvas {
	/**
	 * @param parent
	 * @param style
	 */
	public MainBarButton(Composite parent, int style) {
		super(parent, style);
		this.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				MainBarButton.this.paintControl(e);
			}
		});

		final Cursor hand = new Cursor(this.getDisplay(), SWT.CURSOR_HAND);

		this.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event event) {
				MainBarButton.this.setRegion(MainBarButton.this
						.getCustomRegion());
				MainBarButton.this.redraw();
			}
		});

		this.setCursor(hand);

		this.inactiveBackground = new Color(getDisplay(), 0x4B, 0x95, 0x00);
		this.activeBackground = new Color(getDisplay(), 0x98, 0xF2, 0x3D);
		this.textColor = this.getForeground();
		this.borderColor = new Color(getDisplay(), 0x76, 0xC4, 0xC8);
		this.textsize = 12;
	}

	private Color inactiveBackground = null;

	/**
	 * @param inactiveBackground
	 *            the inactiveBackground to set
	 */
	public void setInactiveBackground(Color inactiveBackground) {
		this.inactiveBackground = inactiveBackground;
	}

	/**
	 * @param activeBackground
	 *            the activeBackground to set
	 */
	public void setActiveBackground(Color activeBackground) {
		this.activeBackground = activeBackground;
	}

	/**
	 * the text size
	 */
	protected int textsize = 12;
	
	/**
	 * @return the textsize
	 */
	public int getTextsize() {
		return this.textsize;
	}

	/**
	 * @param textsize the textsize to set
	 */
	public void setTextsize(int textsize) {
		this.textsize = textsize;
	}

	/**
	 * the used text color
	 */
	protected Color textColor = null;

	/**
	 * @param textColor the textColor to set
	 */
	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	/**
	 * @return the borderColor
	 */
	public Color getBorderColor() {
		return this.borderColor;
	}

	/**
	 * @param borderColor the borderColor to set
	 */
	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	private Color borderColor = null;
	
	private Color activeBackground = null;

	private String text = ""; //$NON-NLS-1$

	private boolean active = true;

	private Image image = null;

	/**
	 * Gets the image
	 * @return the image
	 */
	public Image getImage() {
		return this.image;
	}

	/**
	 * Sets the Image
	 * @param image the imgage to set
	 */
	public void setImage(Image image) {
		this.image = image;
	}

	/**
	 * Sets if this button is active
	 * 
	 * @param active
	 *            the active state
	 */
	public void setActive(boolean active) {

		this.active = active;
		if (this.active) {
			this.setBackground(this.activeBackground);
		} else {
			this.setBackground(this.inactiveBackground);
		}
	}

	/**
	 * Gets if this button is active
	 * 
	 * @return the active state
	 */
	public boolean getActive() {
		return this.active;
	}

	/**
	 * Gets the button text
	 * @return the text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Sets the text for the button
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * SLF4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(MainBarButton.class);

	/**
	 * Paint 3D style borders
	 * 
	 * @param e
	 */
	protected void paintBackground(PaintEvent e) {

		/*
		Point size = this.getSize();
		int height = size.y - 2;

		int width = size.x;

		int factor = 4;

		// TOP
		e.gc.fillGradientRectangle(0, 0, width, factor, true);

		// BOTTOM
		e.gc.fillGradientRectangle(0, height, width, -1 * (factor), true);
		*/
		// LEFT
		//e.gc.fillGradientRectangle(0, 0, factor, height, false);

		// RIGTH
		//e.gc.fillGradientRectangle(width, 0, -1 * (width / factor), height,
		//		false);

	}

	/**
	 * Main painting method
	 * @param e
	 */
	void paintControl(PaintEvent e) {
		this.paintBackground(e);
		
		Color current = e.gc.getForeground();
		
		e.gc.setForeground(getBorderColor());
		
		this.paintButton(e);
		
		e.gc.setForeground(current);
		
		this.paintText(e);
	}

	/**
	 * paint the inner button
	 * @param e
	 */
	protected void paintButton(PaintEvent e) {
		// could be overwritten by subclasses
	}

	/**
	 * Paint the text or image on the button
	 * @param e
	 */
	protected void paintText(PaintEvent e) {
		Point size = this.getSize();
		int height = size.y - 2;

		int width = size.x;

		// e.gc.fillGradientRectangle(0, 1, width, height / 4, true);

		if (this.image == null) {
			int textlen = 0;

			if (this.getText() != null) {
				textlen = this.getText().length();
			}
			
			Color current = e.gc.getForeground();
			
			e.gc.setForeground(this.textColor);
			
			String font_name = e.gc.getFont().getFontData()[0].getName();
			
			Font font = new Font(this.getDisplay(), font_name, this.getTextsize(), e.gc.getFont().getFontData()[0].getStyle() );
			
			e.gc.setFont(font);
			
			int texty = (height - e.gc.getFontMetrics().getHeight()) / 2;
			int textx = (width - e.gc.getFontMetrics().getAverageCharWidth()
					* textlen) / 2;
			
			e.gc.drawText(this.getText(), textx, texty);
			
			font.dispose();
			
			e.gc.setForeground(current);
		} else {
			int imgx = (width - height) / 2;
			Image tmp = new Image(getDisplay(), this.image.getImageData()
					.scaledTo(height, height));
			e.gc.drawImage(tmp, imgx, 0);
		}

	}

	/**
	 * Gets the region of the button
	 * @return the button region
	 */
	protected abstract Region getCustomRegion();
}
