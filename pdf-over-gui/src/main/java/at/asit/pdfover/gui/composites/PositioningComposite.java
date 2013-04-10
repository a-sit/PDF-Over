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
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.SignaturePosition;

import com.sun.pdfview.PDFFile;

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

	private SignaturePanel viewer = null;

	private PDFFile pdf = null;

	private Frame frame = null;

	int currentPage = 0;

	int numPages = 0;

	private SignaturePosition position = null;

	ScrollBar scrollbar = null;

	/**
	 * Set the PDF Document to display
	 * @param document document to display
	 * @throws IOException I/O Error
	 */
	public void displayDocument(File document) throws IOException {
		RandomAccessFile rafile = new RandomAccessFile(document, "r"); //$NON-NLS-1$
		FileChannel chan = rafile.getChannel();
		ByteBuffer buf = chan.map(FileChannel.MapMode.READ_ONLY, 0, chan.size());
		chan.close();
		rafile.close();

		this.pdf = new PDFFile(buf);
		if (this.viewer == null)
		{
			this.viewer = new SignaturePanel(this.pdf);
			this.frame.add(this.viewer);
		}
		else
			this.viewer.setDocument(this.pdf);
		this.numPages = this.pdf.getNumPages();
		this.scrollbar.setValues(1, 1, this.numPages + 1, 1, 1, 1);
		showPage(this.numPages);
	}

	/**
	 * Set the signature placeholder image
	 * Must be called _after_ displayDocument
	 * @param placeholder signature placeholder
	 */
	public void setPlaceholder(Image placeholder) {
		if (this.viewer == null)
			return;
		this.viewer.setSignaturePlaceholder(placeholder);
	}

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 * @param state 
	 */
	public PositioningComposite(Composite parent, int style, State state) {
		super(parent, style | SWT.EMBEDDED | SWT.V_SCROLL, state);
		//this.setLayout(null);
		this.setBounds(0, 0, 10, 10);
		this.scrollbar = this.getVerticalBar();
		this.frame = SWT_AWT.new_Frame(this);
		this.addKeyListener(this.keyListener);
		this.frame.addMouseWheelListener(this.mouseListener);
		this.scrollbar.addSelectionListener(this.selectionListener);
	}

	private KeyListener keyListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			int newPage = PositioningComposite.this.currentPage;

			switch (e.keyCode)
			{
				case SWT.PAGE_DOWN:
					if (PositioningComposite.this.currentPage < PositioningComposite.this.numPages)
						++newPage;
					break;

				case SWT.PAGE_UP:
					if (PositioningComposite.this.currentPage > 1)
						--newPage;
					break;

				case SWT.END:
					newPage = PositioningComposite.this.numPages;
					break;

				case SWT.HOME:
					newPage = 1;
					break;

				case SWT.CR:
				case SWT.KEYPAD_CR:
					PositioningComposite.this.setFinalPosition();
					break;
			}

			if (newPage != PositioningComposite.this.currentPage)
				showPage(newPage);
		}
	};

	private MouseWheelListener mouseListener = new MouseWheelListener() {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int newPage = PositioningComposite.this.currentPage;

			if (e.getUnitsToScroll() < 0)
			{
				if (PositioningComposite.this.currentPage > 1)
					newPage--;
			}
			else if (e.getUnitsToScroll() > 0)
			{
				if (PositioningComposite.this.currentPage < PositioningComposite.this.numPages)
					newPage++;
			}

			if (newPage != PositioningComposite.this.currentPage)
				showPage(newPage);
		}		
	};

	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			PositioningComposite.this.showPage(PositioningComposite.this.scrollbar.getSelection());
		}
	};

	void showPage(int page) {
		this.currentPage = page;
		this.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				PositioningComposite.this.scrollbar.setSelection(PositioningComposite.this.currentPage);
			}
		});
		this.viewer.showPage(page);
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

	/**
	 * Set the signature position and continue to the next state
	 * @param position the signature position
	 */
	void setFinalPosition() {
		// TODO: check if this is the real position
		this.position = new SignaturePosition(this.viewer.getSignaturePositionX(), this.viewer.getSignaturePositionY(), this.currentPage);
		PositioningComposite.this.state.updateStateMachine();
	}

	/**
	 * Get the signature position
	 * @return the signature position
	 */
	public SignaturePosition getPosition() {
		return this.position;
	}
}
