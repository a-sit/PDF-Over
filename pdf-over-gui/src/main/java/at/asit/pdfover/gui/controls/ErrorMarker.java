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
import java.io.InputStream;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class ErrorMarker {

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(ErrorMarker.class);

	private Label lbl;
	
	/**
	 * @param parent
	 * @param style
	 * @param exception
	 * @param message
	 */
	public ErrorMarker(Composite parent, int style, Throwable exception,
			String message, Control control) {
		//super(parent, style);

		this.lbl = new Label(parent, style);
		
		String imgPath = "/img/error.png"; //$NON-NLS-1$

		InputStream stream = this.getClass().getResourceAsStream(imgPath);

		Point size = control.getSize();

		int width = size.x == 0 ? 32 : size.x;
		int height = size.y == 0 ? 32 : size.y;
		
		this.lbl.setSize(new Point(width, height));
		
		this.orig = new Image(this.lbl.getDisplay(), new ImageData(stream).scaledTo(width, height));

		this.lbl.setToolTipText(message);
		
		this.lbl.setImage(this.orig);
	}
	
	public void setLayoutData(Object object) {
		this.lbl.setLayoutData(object);
	}
	
	public void setVisible(boolean visible) {
		this.lbl.setVisible(visible);
	}

	public void setToolTipText(String msg) {
		this.lbl.setToolTipText(msg);
	}
	
	
	public void resize(Point size) {
		String imgPath = "/img/error.png"; //$NON-NLS-1$

		InputStream stream = this.getClass().getResourceAsStream(imgPath);
		
		int width = size.x == 0 ? 32 : size.x;
		int height = size.y == 0 ? 32 : size.y;
		
		this.orig = new Image(this.lbl.getDisplay(), new ImageData(stream).scaledTo(width, height));

		this.lbl.setSize(size);
		this.lbl.setImage(this.orig);
	}

	public Point getSize() {
		return this.lbl.getSize();
	}
	
	private Image orig;
}
