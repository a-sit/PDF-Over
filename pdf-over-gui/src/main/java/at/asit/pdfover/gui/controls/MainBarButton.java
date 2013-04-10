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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
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
 * 
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
				MainBarButton.this.paintButton(e);
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

	private Color activeBackground = null;

	private String text = ""; //$NON-NLS-1$

	private boolean active = true;

	private Image image = null;

	/**
	 * @return the imgage
	 */
	public Image getImage() {
		return this.image;
	}

	/**
	 * @param imgage
	 *            the imgage to set
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
	 * @return the text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(MainBarButton.class);

	/**
	 * @param e
	 */
	protected void paintButton(PaintEvent e) {
		Point size = this.getSize();
		int height = size.y - 2;

		int width = size.x;

		if (this.image == null) {
			int textlen = 0;

			if (this.getText() != null) {
				textlen = this.getText().length();
			}

			int texty = (height - e.gc.getFontMetrics().getHeight()) / 2;
			int textx = (width - e.gc.getFontMetrics().getAverageCharWidth()
					* textlen) / 2;
			e.gc.drawText(this.getText(), textx, texty);
		} else {
			int imgx = (width - height) / 2;
			Image tmp = new Image(getDisplay(), this.image.getImageData()
					.scaledTo(height, height));
			e.gc.drawImage(tmp, imgx, 0);
		}
	}

	/**
	 * @return
	 */
	protected abstract Region getCustomRegion();
}
