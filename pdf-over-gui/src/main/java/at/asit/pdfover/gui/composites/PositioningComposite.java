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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;

import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.SignaturePosition;

/**
 * 
 *
 */
public class PositioningComposite extends StateComposite {

	private PDFViewerComposite viewer = null;

	/**
	 * Set the PDF Document to display
	 * @param document document to display
	 * @throws PDFException Error parsing PDF document
	 * @throws PDFSecurityException Error decrypting PDF document (not supported)
	 * @throws IOException I/O Error
	 */
	public void displayDocument(File document) throws PDFException, PDFSecurityException, IOException {
		if (this.viewer == null)
			this.viewer = new PDFViewerComposite(this, SWT.EMBEDDED | SWT.NO_BACKGROUND, document);
		else
			this.viewer.setDocument(document);
	}

	/**
	 * Selection listener when position was fixed
	 */
	private final class PositionSelectedListener extends SelectionAdapter {
		/**
		 * Empty constructor
		 */
		public PositionSelectedListener() {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			// TODO: FIX to get real position
			PositioningComposite.this.setPosition(new SignaturePosition()); // Setting auto position for testing
			PositioningComposite.this.state.updateStateMachine();
		}
	}

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
	 * Create the composite.
	 * @param parent
	 * @param style
	 * @param state 
	 */
	public PositioningComposite(Composite parent, int style, State state) {
		super(parent, style, state);
		this.setLayout(new FillLayout());

//		Button btn_position = new Button(this, SWT.NATIVE | SWT.RESIZE);
//		btn_position.setBounds(10, 50, 100, 30);
//		btn_position.setText("FAKE Position");
//		btn_position.addSelectionListener(new PositionSelectedListener());
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
