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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
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

	private Canvas signature = null;

	private SignaturePosition position = null;

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
		log.debug("Resizing to 0,0," + clientArea.width + "," + clientArea.height);
		this.viewer.setBounds(0, 0, clientArea.width, clientArea.height);

		this.signature.setBounds(clientArea.width - 250, clientArea.height -200, 150, 40);
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
			public void handleEvent(Event arg0) {
				if (PositioningComposite.this.viewer != null)
					resizeViewer();
			}
		});

		this.signature = new Canvas(this, SWT.NO_BACKGROUND);
		this.signature.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				PositioningComposite.this.viewer.redraw();
				Rectangle r = ((Canvas) e.widget).getBounds();
				e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_RED));
//				e.gc.setB
				e.gc.drawFocus(5, 5, r.width - 10, r.height - 10);
				e.gc.drawText("Position Signature", 10, 10);
			}
		});
		this.signature.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
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
