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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.utils.SWTUtils;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signer.SignaturePosition;

/**
 * Composite which allows to position the signature on a preview of the document
 */
public class PositioningComposite extends StateComposite {
	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(PositioningComposite.class);

	SignaturePanel viewer = null;
	Frame frame = null;
	Composite mainArea = null;
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
		SWTUtils.anchor(bottomBar).left(0).right(100).bottom(100);
		this.bottomBar.setLayout(new FormLayout());

		this.btnSign = new Button(this.bottomBar, SWT.PUSH);
		SWTUtils.anchor(btnSign).right(100).top(0);
		this.getShell().setDefaultButton(this.btnSign);

		this.btnNewPage = new Button(this.bottomBar, SWT.TOGGLE);
		SWTUtils.anchor(btnNewPage).right(btnSign).top(0);

		this.lblPage = new Label(this.bottomBar, SWT.CENTER);
		SWTUtils.anchor(lblPage).left(0).right(btnNewPage, 5).bottom(100);

		this.mainArea = new Composite(this, SWT.EMBEDDED | SWT.V_SCROLL);
		SWTUtils.anchor(mainArea).left(0).right(100).top(0).bottom(bottomBar, -5);
		this.scrollbar = this.mainArea.getVerticalBar();

		this.frame = SWT_AWT.new_Frame(this.mainArea);
		this.frame.addKeyListener(this.keyListener);
		this.frame.addMouseWheelListener(this.mouseListener);

		this.viewer = new SignaturePanel();
		this.viewer.setSignaturePlaceholderBorderColor(new Color(
				Constants.MAINBAR_ACTIVE_BACK_DARK.getRed(),
				Constants.MAINBAR_ACTIVE_BACK_DARK.getGreen(),
				Constants.MAINBAR_ACTIVE_BACK_DARK.getBlue()));
		this.frame.add(this.viewer, BorderLayout.CENTER);

		SWTUtils.addSelectionListener(btnNewPage, () -> {
			if (this.currentPage > this.numPages)
				showPage(this.numPages);
			else
				showPage(this.numPages + 1);
			requestFocus();
		});

		SWTUtils.addSelectionListener(btnSign, this::setFinalPosition);

		SWTUtils.addSelectionListener(scrollbar, () -> { this.showPage(this.scrollbar.getSelection()); });

		reloadResources();
		requestFocus();
	}

	@Override public void onDisplay() { if (this.viewer.getSigPagePos() == null) this.btnSign.setEnabled(false); }

	/**
	 * Set the PDF Document to display
	 *
	 * @param document
	 *            document to display
	 */
	public void displayDocument(final PDDocument document) {
		EventQueue.invokeLater(() -> {
			this.viewer.setDocument(document);
			getDisplay().asyncExec(() -> this.btnSign.setEnabled(true));
		});

		if (document != null)
		{
			this.numPages = document.getNumberOfPages();
			this.scrollbar.setValues(1, 1, this.numPages + 1, 1, 1, 1);
			showPage(this.numPages);
		}
	}

	@Override
	public void dispose() {
		this.viewer.setDocument(null);
		super.dispose();
	}

	/**
	 * Request focus (to enable keyboard input)
	 */
	public void requestFocus() {
		getDisplay().asyncExec(() -> {
			if (!this.isDisposed() && !this.mainArea.isDisposed()) {
				this.mainArea.setFocus();
				EventQueue.invokeLater(() -> {
					if (!this.isDisposed()) {
						if (!this.frame.hasFocus()) {
							this.frame.requestFocus();
						}
					}
				});
			}
		});
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
	 */
	public void setPlaceholder(final Image placeholder) {
		EventQueue.invokeLater(() -> {
			if (this.viewer == null)
				return;
			this.viewer.setSignaturePlaceholder(placeholder);
		});
	}

	KeyListener keyListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			int newPage = PositioningComposite.this.currentPage;
			int sigXOffset = 0;
			int sigYOffset = 0;

			switch (e.getKeyCode()) {
			case KeyEvent.VK_PAGE_DOWN:
				if (PositioningComposite.this.currentPage < PositioningComposite.this.numPages)
					++newPage;
				break;

			case KeyEvent.VK_PAGE_UP:
				if (PositioningComposite.this.currentPage > 1)
					--newPage;
				break;

			case KeyEvent.VK_END:
				newPage = PositioningComposite.this.numPages;
				break;

			case KeyEvent.VK_HOME:
				newPage = 1;
				break;

			case KeyEvent.VK_ENTER:
				setFinalPosition();
				break;

			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_KP_LEFT:
				sigXOffset -= Constants.SIGNATURE_KEYBOARD_POSITIONING_OFFSET;
				break;

			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_KP_RIGHT:
				sigXOffset += Constants.SIGNATURE_KEYBOARD_POSITIONING_OFFSET;
				break;

			case KeyEvent.VK_UP:
			case KeyEvent.VK_KP_UP:
				sigYOffset += Constants.SIGNATURE_KEYBOARD_POSITIONING_OFFSET;
				break;

			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_KP_DOWN:
				sigYOffset -= Constants.SIGNATURE_KEYBOARD_POSITIONING_OFFSET;
				break;
			}

			if (newPage != PositioningComposite.this.currentPage)
				showPage(newPage);

			if (sigXOffset != 0 || sigYOffset != 0)
				translateSignaturePosition(sigXOffset, sigYOffset);
		}
	};

	MouseWheelListener mouseListener = new MouseWheelListener() {
		private long lastEventTime = 0;

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			e.consume();
			// Workaround for Linux: Events fire twice
			if (e.getWhen() == this.lastEventTime)
				return;
			this.lastEventTime = e.getWhen();

			int change = e.isShiftDown() ? 5 : 1;
			int newPage = PositioningComposite.this.currentPage;

			if (e.getWheelRotation() < 0) {
				newPage = Math.max(1, newPage - change);
			} else if (e.getWheelRotation() > 0) {
				newPage = Math.min(newPage + change, PositioningComposite.this.numPages);
			}

			if (newPage != PositioningComposite.this.currentPage)
				showPage(newPage);
		}
	};

	private Button btnSign;

	void showPage(final int page) {
		final int previousPage = this.currentPage;
		this.currentPage = page;
		getDisplay().asyncExec(() -> {
			int currentPage = this.currentPage;
			int numPages = this.numPages;
			if ((previousPage > numPages) && (currentPage <= numPages)) {
				// Was on new page
				SWTUtils.setLocalizedText(btnNewPage, "positioning.newPage");
				this.btnNewPage.setSelection(false);
				this.bottomBar.layout();
				this.scrollbar.setMaximum(numPages + 1);
			} else if ((previousPage <= numPages) && (currentPage > numPages)) {
				// Go to new page
				SWTUtils.setLocalizedText(btnNewPage, "positioning.removeNewPage");
				this.btnNewPage.setSelection(true);
				this.bottomBar.layout();
				this.scrollbar.setMaximum(numPages + 2);
			}
			this.scrollbar.setSelection(currentPage);
			SWTUtils.setLocalizedText(lblPage, "positioning.page", currentPage, numPages);
		});
		EventQueue.invokeLater(() -> {
			PositioningComposite.this.viewer.showPage(page);
		});
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
	 *            signature placeholder vertical position offset (negative is down)
	 */
	public void translateSignaturePosition(final int sigXOffset, final int sigYOffset) {
		EventQueue.invokeLater(() -> {
			this.viewer.translateSignaturePagePosition(sigXOffset, sigYOffset);
		});
	}

	/**
	 * Set the signature position and continue to the next state
	 *
	 * @param position
	 *            the signature position
	 */
	void setFinalPosition() {
		if (this.currentPage == 0) {
			this.position = new SignaturePosition();
		} else {
			Point2D pos = this.viewer.getSigPagePos();
			if (pos == null) return;

			this.position = new SignaturePosition(
					pos.getX(),
					pos.getY(),
					this.currentPage);
		}
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
	public void setPosition(double x, double y, int page) {
		showPage(page);
		if (this.viewer != null)
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
		SWTUtils.setLocalizedText(btnNewPage, (this.currentPage <= this.numPages) ? "positioning.newPage" : "positioning.removeNewPage");
		SWTUtils.setLocalizedText(btnSign, "positioning.sign");
		SWTUtils.setLocalizedText(lblPage, "positioning.page", this.currentPage, this.numPages);
	}
}
