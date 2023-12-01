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

/**
 *
 */
public class MainBarStartButton extends MainBarButton {
	/**
	 * @param parent
	 * @param style
	 */
	public MainBarStartButton(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected void paintBackground(PaintEvent e) {
		super.paintBackground(e);
		/*
		Point size = this.getSize();
		int height = size.y - 2;

		int split = SplitFactor;
		int width = size.x - split;

		int factor = GradientFactor;

		Region left_reg = new Region();
		left_reg.add(new int[] { 0, 0, factor, factor, factor, height-factor, 0, height, 0, 0 });

		Region right1_reg = new Region();
		right1_reg.add(new int[] { width, 0,
				width - factor, factor,
				width - factor, height-factor, width, height, width, 0 });

		Region top_reg = new Region();
		top_reg.add(new int[] {
				0, 0,
				factor, factor,
				width - factor, factor,
				width, 0,
				0, 0 });

		Region bottom_reg = new Region();
		bottom_reg.add(new int[] {
				0, height,
				factor, height-factor,
				width - factor, height-factor,
				width, height, 0, height });

		e.gc.setClipping(top_reg);

		//TOP
		 e.gc.fillGradientRectangle(0, 0, width, factor, true);

		 e.gc.setClipping(bottom_reg);

		 //BOTTOM
		 e.gc.fillGradientRectangle(0, height, width, -1 * (factor),
		  true);

		 e.gc.setClipping(left_reg);

		// LEFT
		e.gc.fillGradientRectangle(0, 0, factor, height, false);


		e.gc.setClipping(right1_reg);
		// RIGTH
		e.gc.fillGradientRectangle(width, 0, -1 * factor, height,
				false);

		e.gc.setClipping((Region)null);*/
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.controls.MainBarButton#paintButton(org.eclipse.swt.events.PaintEvent)
	 */
	@Override
	protected void paintButton(PaintEvent e) {

		Point size = this.getSize();

		int height = size.y - 3;

		int split = SplitFactor;
		int width = size.x - split;

		e.gc.drawLine(0, 0, width, 0);
		e.gc.drawLine(width, 0, width + split, (height) / 2);
		e.gc.drawLine(width + split, (height) / 2, width, height);
		e.gc.drawLine(width, height, 0, height);
		e.gc.drawLine(0, height, 0, 0);

	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.controls.MainBarButton#getRegion()
	 */
	@Override
	protected Region getCustomRegion() {
		Point size = this.getSize();

		int height = size.y - 2;

		int split = SplitFactor;

		int width = size.x - split;

		Region reg = new Region();
		reg.add(new int[] { 0, 0, width, 0, width + split,
				(height) / 2, width, height, 0, height, 0, 0 });
		return reg;
	}


	/**
	 * change the text position
	 * @param positionX the position
	 * @return the new position
	 */
	@Override
	protected int changeTextPosition(int positionX) {
		return positionX - (SplitFactor / 2);
	}
}
