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
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class MainBarRectangleButton extends MainBarButton {
	/**
	 * @param parent
	 * @param style
	 */
	public MainBarRectangleButton(Composite parent, int style) {
		super(parent, style);
		this.setActiveBackground(null);
		this.setInactiveBackground(null);
	}

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(MainBarRectangleButton.class);

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.controls.MainBarButton#paintButton(org.eclipse.swt.events.PaintEvent)
	 */
	@Override
	protected void paintButton(PaintEvent e) {
		Point size = this.getSize();

		int height = size.y - 2;

		int width = size.x;
		
		e.gc.drawLine(0, 0, width, 0);
		e.gc.drawLine(width, 0, width, height);
		e.gc.drawLine(width, height, 0, height);
		e.gc.drawLine(0, height, 0, 0);
		
		super.paintButton(e);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.controls.MainBarButton#getCustomRegion()
	 */
	@Override
	protected Region getCustomRegion() {
		Point size = this.getSize();

		int height = size.y - 2;

		int width = size.x;

		Region reg = new Region();
		reg.add(new int[] { 0, 0, width, 0, width, height, 0, height, 0, 0 });
		return reg;
	}

}
