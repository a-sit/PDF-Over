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
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 *
 */
public class ClickableCanvas extends Canvas {

	private Image image = null;

	/**
	 * @param parent
	 * @param style
	 */
	public ClickableCanvas(Composite parent, int style) {
		super(parent, style);

		this.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				ClickableCanvas.this.paintControl(e);
			}
		});

		final Cursor hand = new Cursor(this.getDisplay(), SWT.CURSOR_HAND);

		this.addListener(SWT.Resize, (Event event) -> {
			ClickableCanvas.this.redraw();
		});

		this.setCursor(hand);

	}

	/**
	 * Gets the image
	 *
	 * @return the image
	 */
	public Image getImage() {
		return this.image;
	}

	/**
	 * Sets the Image
	 *
	 * @param image
	 *            the imgage to set
	 */
	public void setImage(Image image) {
		this.image = image;
	}

	/**
	 * Main painting method
	 *
	 * @param e
	 */
	void paintControl(PaintEvent e) {
		this.paintText(e);
	}

	/**
	 * Paint the text or image on the button
	 *
	 * @param e
	 */
	protected void paintText(PaintEvent e) {
		Point size = this.getSize();
		int width = size.x;

		// e.gc.fillGradientRectangle(0, 1, width, height / 4, true);

		if (this.image != null) {

			//log.debug("Width: " + width + " Height: " + height);

			int w = 0;
			Image tmp = null;
			if(this.image.getImageData().width < width) {
				tmp = new Image(getDisplay(), this.image.getImageData());
				w = (width - this.image.getImageData().width) / 2;
			} else if(this.image.getImageData().width > width) {
				tmp = new Image(getDisplay(), this.image.getImageData().scaledTo(width, width));
			} else {
				tmp = new Image(getDisplay(), this.image.getImageData());
			}

			e.gc.drawImage(tmp, w, w);
		}

	}
}
