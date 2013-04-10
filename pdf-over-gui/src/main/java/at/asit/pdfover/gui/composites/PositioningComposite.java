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
package at.asit.pdfover.gui.composites;

// Imports
import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.SignaturePosition;

/**
 * 
 *
 */
public class PositioningComposite extends StateComposite {
	/**
	 * SFL4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(PositioningComposite.class);

	PDFViewerComposite viewer = null;

	Canvas signature = null;

	private SignaturePosition position = null;

	boolean doDrag = false;

	Point origMousePos = null;
	Rectangle origSigPos = null;

	/**
	 * Gets the position of the signature
	 * @return the SignaturePosition
	 */
	public SignaturePosition getPosition() {
		return this.position;
	}

	/**
	 * Sets the position
	 * @param position
	 */
	public void setPosition(SignaturePosition position) {
		this.position = position;
	}

	/**
	 * Set the PDF Document to display
	 * @param document document to display
	 * @throws PDFException Error parsing PDF document
	 * @throws PDFSecurityException Error decrypting PDF document (not supported)
	 * @throws IOException I/O Error
	 */
	public void displayDocument(File document) throws PDFException, PDFSecurityException, IOException {
		if (this.viewer == null)
		{
			this.viewer = new PDFViewerComposite(this, SWT.EMBEDDED | SWT.NO_BACKGROUND, document);
			resizeViewer();
		}
		else
			this.viewer.setDocument(document);
	}

	void resizeViewer()
	{
		Rectangle clientArea = this.getClientArea();
		log.debug("Resizing to " + clientArea.width + "x" + clientArea.height);
		this.viewer.setBounds(0, 0, clientArea.width, clientArea.height);
	}

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 * @param state 
	 */
	public PositioningComposite(Composite parent, int style, State state) {
		super(parent, style, state);
		this.setLayout(null);

		this.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event e) {
				if (PositioningComposite.this.viewer != null)
					resizeViewer();
			}
		});

		this.signature = new Canvas(this, SWT.NO_BACKGROUND);
		this.signature.setBounds(200, 200, 150, 40);
		this.signature.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
//				PositioningComposite.this.viewer.redraw();
				Rectangle r = ((Canvas) e.widget).getBounds();
				e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLUE));
				e.gc.drawFocus(5, 5, r.width - 10, r.height - 10);
				e.gc.drawText("Position Signature", 10, 10);
			}
		});
		this.signature.addDragDetectListener(new DragDetectListener() {
			@Override
			public void dragDetected(DragDetectEvent e) {
				PositioningComposite.this.doDrag = true;
				origMousePos = Display.getCurrent().getCursorLocation();
				origSigPos = ((Canvas) e.widget).getBounds();
			}
		});
		this.signature.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				if (PositioningComposite.this.doDrag)
				{
					Point newMousePos = Display.getCurrent().getCursorLocation();
					int x = origSigPos.x + (newMousePos.x - origMousePos.x);
					int y = origSigPos.y + (newMousePos.y - origMousePos.y);
					PositioningComposite.this.signature.setBounds(x, y, origSigPos.width, origSigPos.height);
				}
			}
		});
		this.signature.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (PositioningComposite.this.doDrag)
				{
					PositioningComposite.this.doDrag = false;
					return;
				}
				// TODO: FIX to get real position
				PositioningComposite.this.setPosition(new SignaturePosition()); // Setting auto position for testing
				PositioningComposite.this.state.updateStateMachine();
			}
		});
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.components.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		this.layout(true, true);
	}
}
