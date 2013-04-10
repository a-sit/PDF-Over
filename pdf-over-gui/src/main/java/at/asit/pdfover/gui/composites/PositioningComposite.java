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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.Messages;
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

	private Composite mainArea = null;

	Composite bottomBar = null;

	Button btnNewPage = null;

	Label lblPage = null;

	ScrollBar scrollbar = null;

	private SignaturePosition position = null;

	int currentPage = 0;

	int numPages = 0;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 * @param state
	 */
	public PositioningComposite(Composite parent, int style, State state) {
		super(parent, style, state);
		this.setLayout(new FormLayout());

		this.bottomBar = new Composite(this, SWT.NONE);
		FormData fd_bottomBar = new FormData();
		fd_bottomBar.left = new FormAttachment(0);
		fd_bottomBar.right = new FormAttachment(100);
		fd_bottomBar.bottom = new FormAttachment(100);
		this.bottomBar.setLayoutData(fd_bottomBar);
		this.bottomBar.setLayout(new FormLayout());

		this.btnSign = new Button(this.bottomBar, SWT.PUSH);
		this.btnSign.setText(Messages.getString("positioning.sign")); //$NON-NLS-1$
		FormData fd_btnSign = new FormData();
		fd_btnSign.right = new FormAttachment(100);
		fd_btnSign.top = new FormAttachment(0);
		this.btnSign.setLayoutData(fd_btnSign);
		this.getShell().setDefaultButton(this.btnSign);
		this.btnSign.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PositioningComposite.this.setFinalPosition();
			}
		});

		this.btnNewPage = new Button(this.bottomBar, SWT.TOGGLE);
		this.btnNewPage.setText(Messages.getString("positioning.newPage")); //$NON-NLS-1$
		FormData fd_btnNewPage = new FormData();
		fd_btnNewPage.right = new FormAttachment(this.btnSign);
		fd_btnNewPage.top = new FormAttachment(0);
		this.btnNewPage.setLayoutData(fd_btnNewPage);
		this.btnNewPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (PositioningComposite.this.currentPage > PositioningComposite.this.numPages)
					showPage(PositioningComposite.this.numPages);
				else
					showPage(PositioningComposite.this.numPages + 1);
				
				PositioningComposite.this.requestFocus();
			}
		});

		this.lblPage = new Label(this.bottomBar, SWT.CENTER);
		FormData fd_lblPage = new FormData();
		fd_lblPage.left = new FormAttachment(0);
		fd_lblPage.right = new FormAttachment(this.btnNewPage, 5);
		fd_lblPage.bottom = new FormAttachment(100);
		this.lblPage.setLayoutData(fd_lblPage);

		this.mainArea = new Composite(this, SWT.EMBEDDED | SWT.V_SCROLL);
		FormData fd_mainArea = new FormData();
		fd_mainArea.left = new FormAttachment(0);
		fd_mainArea.right = new FormAttachment(100);
		fd_mainArea.top = new FormAttachment(0);
		fd_mainArea.bottom = new FormAttachment(this.bottomBar, -5);
		this.mainArea.setLayoutData(fd_mainArea);
		this.scrollbar = this.mainArea.getVerticalBar();

		this.frame = SWT_AWT.new_Frame(this.mainArea);
		this.mainArea.addKeyListener(this.keyListener);
		this.scrollbar.addSelectionListener(this.selectionListener);
		// Workaround for Windows: Scrollbar always gets the event
		if (!System.getProperty("os.name").toLowerCase().contains("windows")) //$NON-NLS-1$ //$NON-NLS-2$
			this.frame.addMouseWheelListener(this.mouseListener);
		requestFocus();
	}

	/**
	 * Set the PDF Document to display
	 * 
	 * @param document
	 *            document to display
	 * @throws IOException
	 *             I/O Error
	 */
	public void displayDocument(File document) throws IOException {
		RandomAccessFile rafile = new RandomAccessFile(document, "r"); //$NON-NLS-1$
		FileChannel chan = rafile.getChannel();
		ByteBuffer buf = chan
				.map(FileChannel.MapMode.READ_ONLY, 0, chan.size());
		chan.close();
		rafile.close();
		try
		{
			this.pdf = new PDFFile(buf);
		}
		catch (IOException e) {
			throw new IOException(Messages.getString("error.MayNotBeAPDF"), e); //$NON-NLS-1$
		}
		if (this.viewer == null) {
			this.viewer = new SignaturePanel(this.pdf);
			this.frame.add(this.viewer);
			this.viewer.setSignaturePlaceholderBorderColor(new Color(
					Constants.MAINBAR_ACTIVE_BACK_DARK.getRed(),
					Constants.MAINBAR_ACTIVE_BACK_DARK.getGreen(),
					Constants.MAINBAR_ACTIVE_BACK_DARK.getBlue()));
		} else
			this.viewer.setDocument(this.pdf);
		this.numPages = this.pdf.getNumPages();
		this.scrollbar.setValues(1, 1, this.numPages + 1, 1, 1, 1);
		showPage(this.numPages);
	}

	/**
	 * Request focus (to enable keyboard input)
	 */
	public void requestFocus() {
		this.mainArea.setFocus();
		if(!this.frame.hasFocus()) {
			this.frame.requestFocusInWindow();
		}
	}

	/**
	 * Set the signature placeholder image Must be called _after_
	 * displayDocument
	 * 
	 * @param placeholder
	 *            signature placeholder
	 * @param width
	 *            width of the placeholder in page space
	 * @param height
	 *            height of the placeholder in page space
	 * @param transparency
	 *            transparency of the signature placeholder (0 - 255)
	 */
	public void setPlaceholder(Image placeholder, int width, int height,
			int transparency) {
		if (this.viewer == null)
			return;
		this.viewer.setSignaturePlaceholder(placeholder, width, height,
				transparency);
	}

	private KeyListener keyListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			int newPage = PositioningComposite.this.currentPage;
			int sigXOffset = 0;
			int sigYOffset = 0;

			switch (e.keyCode) {
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
				PositioningComposite.this.translateSignaturePosition(
						sigXOffset, sigYOffset);
		}
	};

	private MouseWheelListener mouseListener = new MouseWheelListener() {
		private long lastEventTime = 0;

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			// Workaround for Linux: Events fire twice
			if (e.getWhen() == this.lastEventTime)
				return;
			this.lastEventTime = e.getWhen();

			int newPage = PositioningComposite.this.currentPage;

			if (e.getWheelRotation() < 0) {
				if (PositioningComposite.this.currentPage > 1)
					newPage--;
			} else if (e.getWheelRotation() > 0) {
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
			PositioningComposite.this
					.showPage(PositioningComposite.this.scrollbar
							.getSelection());
		}
	};

	private Button btnSign;

	void showPage(int page) {
		final int previousPage = this.currentPage;
		this.currentPage = page;
		this.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				int currentPage = PositioningComposite.this.currentPage;
				int numPages = PositioningComposite.this.numPages;
				if ((previousPage > numPages) && (currentPage <= numPages)) {
					// Was on new page
					PositioningComposite.this.btnNewPage.setText(
							Messages.getString("positioning.newPage")); //$NON-NLS-1$
					PositioningComposite.this.btnNewPage.setSelection(false);
					PositioningComposite.this.bottomBar.layout();
					PositioningComposite.this.scrollbar.setMaximum(numPages + 1);
				} else if ((previousPage <= numPages) && (currentPage > numPages)) {
					// Go to new page
					PositioningComposite.this.btnNewPage.setText(
							Messages.getString("positioning.removeNewPage")); //$NON-NLS-1$
					PositioningComposite.this.btnNewPage.setSelection(true);
					PositioningComposite.this.bottomBar.layout();
					PositioningComposite.this.scrollbar.setMaximum(numPages + 2);
				}
				PositioningComposite.this.scrollbar.setSelection(currentPage);
				PositioningComposite.this.lblPage.setText(String.format(
						Messages.getString("positioning.page"), currentPage, numPages)); //$NON-NLS-1$
			}
		});
		this.viewer.showPage(page);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.components.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		this.layout(true, true);
//		requestFocus();
	}

	/**
	 * Translate the signature placeholder position
	 * 
	 * @param sigXOffset
	 *            signature placeholder horizontal position offset
	 * @param sigYOffset
	 *            signature placeholder vertical position offset
	 */
	public void translateSignaturePosition(int sigXOffset, int sigYOffset) {
		PositioningComposite.this.viewer.translateSignaturePosition(sigXOffset,
				sigYOffset);
	}

	/**
	 * Set the signature position and continue to the next state
	 * 
	 * @param position
	 *            the signature position
	 */
	void setFinalPosition() {
		this.position = new SignaturePosition(
				this.viewer.getSignaturePositionX(),
				this.viewer.getSignaturePositionY(), this.currentPage);
		PositioningComposite.this.state.updateStateMachine();
	}

	/**
	 * Set the signature position
	 * 
	 * @param x
	 *            the horizontal signature position
	 * @param y
	 *            the vertical signature position
	 * @param page
	 *            the page the signature is on
	 */
	public void setPosition(float x, float y, int page) {
		showPage(page);
		this.viewer.setSignaturePosition(x, y);
	}

	/**
	 * Get the signature position
	 * 
	 * @return the signature position
	 */
	public SignaturePosition getPosition() {
		return this.position;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		if (this.currentPage <= this.numPages)
			this.btnNewPage.setText(Messages.getString("positioning.newPage")); //$NON-NLS-1$
		else
			this.btnNewPage.setText(Messages.getString("positioning.removeNewPage")); //$NON-NLS-1$
		this.btnSign.setText(Messages.getString("positioning.sign")); //$NON-NLS-1$
		this.lblPage.setText(String.format(Messages.getString("positioning.page"), //$NON-NLS-1$
				this.currentPage, this.numPages));
	}
}
