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
import java.awt.Color;
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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.SignaturePosition;

import com.sun.pdfview.PDFFile;

/**
 * Composite which allows to position the signature on a preview of the document 
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
	 * Create the composite.
	 * @param parent
	 * @param style
	 * @param state
	 */
	public PositioningComposite(Composite parent, int style, State state) {
		super(parent, style, state);
		this.setLayout(new FormLayout());

		Composite bottomBar = new Composite(this, SWT.NONE);
		FormData fd_bottomBar = new FormData();
		fd_bottomBar.left = new FormAttachment(0);
		fd_bottomBar.right = new FormAttachment(100);
		fd_bottomBar.bottom = new FormAttachment(100);
		bottomBar.setLayoutData(fd_bottomBar);
		bottomBar.setLayout(new FormLayout());

		Button btnSign = new Button(bottomBar, SWT.NONE);
		btnSign.setText("Sign");
		FormData fd_btnSign = new FormData();
		fd_btnSign.right = new FormAttachment(100);
		fd_btnSign.bottom = new FormAttachment(100);
		btnSign.setLayoutData(fd_btnSign);

		Composite mainArea = new Composite(this, SWT.BORDER | SWT.EMBEDDED | SWT.V_SCROLL);
		FormData fd_mainArea = new FormData();
		fd_mainArea.left = new FormAttachment(0);
		fd_mainArea.right = new FormAttachment(100);
		fd_mainArea.top = new FormAttachment(0);
		fd_mainArea.bottom = new FormAttachment(bottomBar, -5);
		mainArea.setLayoutData(fd_mainArea);
		this.scrollbar = mainArea.getVerticalBar();

		this.frame = SWT_AWT.new_Frame(mainArea);
		this.addKeyListener(this.keyListener);
		this.frame.addMouseWheelListener(this.mouseListener);
		this.scrollbar.addSelectionListener(this.selectionListener);
		requestFocus();
	}

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
			this.viewer.setSignaturePlaceholderBorderColor(new Color(
					Constants.MAINBAR_ACTIVE_BACK_DARK.getRed(),
					Constants.MAINBAR_ACTIVE_BACK_DARK.getGreen(),
					Constants.MAINBAR_ACTIVE_BACK_DARK.getBlue()));
		}
		else
			this.viewer.setDocument(this.pdf);
		this.numPages = this.pdf.getNumPages();
		this.scrollbar.setValues(1, 1, this.numPages + 1, 1, 1, 1);
		showPage(this.numPages);
	}

	/**
	 * Request focus (to enable keyboard input)
	 */
	private void requestFocus()
	{
		this.frame.requestFocus();
		setFocus();
	}

	/**
	 * Set the signature placeholder image
	 * Must be called _after_ displayDocument
	 * @param placeholder signature placeholder
	 * @param width width of the placeholder in page space
	 * @param height height of the placeholder in page space 
	 * @param transparency transparency of the signature placeholder (0 - 255)
	 */
	public void setPlaceholder(Image placeholder, int width, int height, int transparency) {
		if (this.viewer == null)
			return;
		this.viewer.setSignaturePlaceholder(placeholder, width, height, transparency);
	}

	private KeyListener keyListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			int newPage = PositioningComposite.this.currentPage;
			int sigXOffset = 0;
			int sigYOffset = 0;

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

				case SWT.ARROW_LEFT:
					sigXOffset -= Constants.SIGNATURE_KEYBOARD_POSITIONING_OFFSET;
					break;

				case SWT.ARROW_RIGHT:
					sigXOffset += Constants.SIGNATURE_KEYBOARD_POSITIONING_OFFSET;
					break;

				case SWT.ARROW_UP:
					sigYOffset -= Constants.SIGNATURE_KEYBOARD_POSITIONING_OFFSET;
					break;

				case SWT.ARROW_DOWN:
					sigYOffset += Constants.SIGNATURE_KEYBOARD_POSITIONING_OFFSET;
					break;
			}

			if (newPage != PositioningComposite.this.currentPage)
				showPage(newPage);

			if (sigXOffset != 0 || sigYOffset != 0)
				PositioningComposite.this.translateSignaturePosition(sigXOffset, sigYOffset);
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
		requestFocus();
	}

	/**
	 * Translate the signature placeholder position
	 * @param sigXOffset signature placeholder horizontal position offset
	 * @param sigYOffset signature placeholder vertical position offset
	 */
	public void translateSignaturePosition(int sigXOffset, int sigYOffset) {
		PositioningComposite.this.viewer.translateSignaturePosition(sigXOffset, sigYOffset);
	}

	/**
	 * Set the signature position and continue to the next state
	 * @param position the signature position
	 */
	void setFinalPosition() {
		this.position = new SignaturePosition(
				this.viewer.getSignaturePositionX(),
				this.viewer.getSignaturePositionY(),
				this.currentPage);
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
