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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

/**
 * 
 */
public class SignaturePanel extends JPanel {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(SignaturePanel.class);

	/** Default serial version ID */
	private static final long serialVersionUID = 1L;

	/** The PDF file being displayed */
	private PDFFile pdf = null;
	/** The image of the rendered PDF page being displayed */
	Image currentImage = null;
	/** The current PDFPage that was rendered into currentImage */
	private PDFPage currentPage = null;
	/** The current transform from screen to page space */
	AffineTransform currentXform = null;
	/** The horizontal offset of the image from the left edge of the panel */
	int offX = 0;
	/** The vertical offset of the image from the top of the panel */
	int offY = 0;
	/** The size of the image */
	private Dimension prevSize = null;
	/** The position of the signature, in page space */
	Point2D sigPagePos = null;
	/** The position of the signature, in screen space */
	Point2D sigScreenPos = null;
	/** The signature placeholder image */
	private Image sigPlaceholder = null;
	/** Width of the signature placeholder in page space */
	private int sigPageWidth = 0;
	/** Height of the signature placeholder in page space */
	private int sigPageHeight = 0;
	/** Width of the signature placeholder in screen space */
	int sigScreenWidth = 0;
	/** Height of the signature placeholder in screen space */
	int sigScreenHeight = 0;

	/**
	 * Create a new PagePanel, with a default size of 800 by 600 pixels.
	 * @param pdf the PDFFile to display
	 */
	public SignaturePanel(PDFFile pdf) {
		super(new BorderLayout());
		setDocument(pdf);
		setPreferredSize(new Dimension(800, 600));
		setFocusable(true);
		addMouseListener(this.mouseListener);
		addMouseMotionListener(this.mouseListener);
	}

	/**
	 * Set a new document to be displayed
	 * @param pdf the PDFFile to be displayed
	 */
	public void setDocument(PDFFile pdf)
	{
		this.pdf = pdf;
		this.sigPagePos = null;
		showPage(pdf.getNumPages());
	}

	/**
	 * Set the signature placeholder image
	 * @param placeholder signature placeholder
	 * @param width width of the placeholder in page space
	 * @param height height of the placeholder in page space 
	 */
	public void setSignaturePlaceholder(Image placeholder, int width, int height)
	{
		this.sigPlaceholder = placeholder;
		this.sigPageWidth = width;
		this.sigPageHeight = height;
	}

	/**
	 * Change the currently displayed page
	 * @param page the number of the page to display
	 */
	public void showPage(int page) {
		//sigPagePos = null;
		showPage(this.pdf.getPage(page));
	}

	/**
	 * Get the current horizontal position of the signature
	 * @return signature x coordinate
	 */
	public float getSignaturePositionX() {
		return (float) this.sigPagePos.getX();
	}

	/**
	 * Get the current vertical position of the signature
	 * @return signature y coordinate
	 */
	public float getSignaturePositionY() {
		return (float) this.sigPagePos.getY();
	}

	/**
	 * Stop the generation of any previous page, and draw the new one.
	 * 
	 * @param page the PDFPage to draw.
	 */
	private synchronized void showPage(PDFPage page) {
		// stop drawing the previous page
		if (this.currentPage != null && this.prevSize != null) {
			this.currentPage.stop(this.prevSize.width, this.prevSize.height, null);
		}

		// set up the new page
		this.currentPage = page;

		if (this.currentPage == null) {
			// no page
			this.currentImage = null;
			this.currentXform = null;
			repaint();
		} else {
			// start drawing
			Dimension sz = getSize();
			if (sz.width + sz.height == 0) {
				// no image to draw.
				return;
			}

			Dimension pageSize = this.currentPage.getUnstretchedSize(sz.width, sz.height,
					null);

			// get the new image
			this.currentImage = this.currentPage.getImage(pageSize.width, pageSize.height,
					null, this);

			// calculate the transform from page to screen space
			this.currentXform = this.currentPage.getInitialTransform(pageSize.width,
					pageSize.height, null);

			if (this.sigPagePos != null)
				this.sigScreenPos = this.currentXform.transform(this.sigPagePos, this.sigScreenPos);
			this.sigScreenWidth = (int) Math.round(this.sigPageWidth * this.currentXform.getScaleX());
			this.sigScreenHeight = (int) Math.round(this.sigPageHeight * this.currentXform.getScaleX());

			// invert the transform (screen to page space)
			try {
				this.currentXform = this.currentXform.createInverse();
			} catch (NoninvertibleTransformException nte) {
				log.error("Error inverting page transform!", nte);
			}

			if (this.sigPagePos == null)
			{
				this.sigScreenPos = new Point2D.Double(
						clamp((int) (pageSize.getWidth() / 2), 0, this.currentImage.getWidth(null) - this.sigScreenWidth),
						clamp((int) ((pageSize.getHeight() / 4) * 3), 0, this.currentImage.getHeight(null) - this.sigScreenHeight));
				this.sigPagePos = this.currentXform.transform(this.sigScreenPos, this.sigPagePos);
			}

			this.prevSize = pageSize;

			repaint();
		}
	}

	/**
	 * Draw the image.
	 */
	@Override
	public void paint(Graphics g) {
		Dimension sz = getSize();
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		if (this.currentImage == null) {
			g.setColor(Color.black);
			g.drawString("No page selected", getWidth() / 2 - 30,
					getHeight() / 2);
			if (this.currentPage != null) {
				showPage(this.currentPage);
			}
		} else {
			// draw the image
			int imwid = this.currentImage.getWidth(null);
			int imhgt = this.currentImage.getHeight(null);

			// draw it centered within the panel
			this.offX = (sz.width - imwid) / 2;
			this.offY = (sz.height - imhgt) / 2;

			if ((imwid == sz.width && imhgt <= sz.height)
					|| (imhgt == sz.height && imwid <= sz.width)) {

				// draw document
				g.drawImage(this.currentImage, this.offX, this.offY, this);

				// draw signature
				int sigX = (int) (this.offX + this.sigScreenPos.getX());
				int sigY = (int) (this.offY + this.sigScreenPos.getY());
				if (this.sigPlaceholder == null) {
					g.setColor(Color.red);
					g.drawRect(sigX, sigY, 100, 40);
				}
				else {
					Image placeholder = this.sigPlaceholder.getScaledInstance(
							this.sigScreenWidth, this.sigScreenHeight, Image.SCALE_SMOOTH);
					g.drawImage(placeholder, sigX, sigY, null);
				}

			} else {
				// the image is bogus. try again, or give up.
				if (this.currentPage != null) {
					showPage(this.currentPage);
				}
				g.setColor(Color.black);
				g.drawString("Could not render page", getWidth() / 2 - 30,
						getHeight() / 2);
			}
		}
	}

	/**
	 * Handles notification of the fact that some part of the image changed.
	 * Repaints that portion.
	 * 
	 * @return true if more updates are desired.
	 */
	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		if ((infoflags & (SOMEBITS | ALLBITS)) != 0) {
			repaint(x + this.offX, y + this.offY, width, height);
		}
		return ((infoflags & (ALLBITS | ERROR | ABORT)) == 0);
	}

	private MouseAdapter mouseListener = new MouseAdapter() {

		private boolean doDrag = false;
		private int dragXOffset = 0;
		private int dragYOffset = 0;

		private void updateSigPos(int sigx, int sigy) {
			if (SignaturePanel.this.currentImage == null)
				return;
			sigx -= SignaturePanel.this.offX;
			sigx = clamp(sigx, 0, SignaturePanel.this.currentImage.getWidth(null) - SignaturePanel.this.sigScreenWidth);
			sigy -= SignaturePanel.this.offY;
			sigy = clamp(sigy, 0, SignaturePanel.this.currentImage.getHeight(null) - SignaturePanel.this.sigScreenHeight);
			SignaturePanel.this.sigScreenPos = new Point2D.Double(sigx, sigy);
			SignaturePanel.this.sigPagePos = SignaturePanel.this.currentXform.transform(SignaturePanel.this.sigScreenPos, SignaturePanel.this.sigPagePos);
			repaint();
		}

		/** Handles a mousePressed event */
		@Override
		public void mousePressed(MouseEvent evt) {
			if (evt.getButton() == MouseEvent.BUTTON1)
			{
				this.doDrag = true;
				if (isOnSignature(evt.getX(), evt.getY())) {
					this.dragXOffset = (int)
							(SignaturePanel.this.sigScreenPos.getX() -
							(evt.getX() - SignaturePanel.this.offX));
					this.dragYOffset = (int)
							(SignaturePanel.this.sigScreenPos.getY() -
							(evt.getY() - SignaturePanel.this.offY));
				}
				else {
					this.dragXOffset = 0;
					this.dragYOffset = 0;
				}
				updateSigPos(evt.getX() + this.dragXOffset, evt.getY() + this.dragYOffset);
			}
		}

		/** Handles a mouseReleased event */
		@Override
		public void mouseReleased(MouseEvent evt) {
			this.doDrag = false;
		}

		/**
		 * Handles a mouseDragged event.
		 */
		@Override
		public void mouseDragged(MouseEvent evt) {
			if (this.doDrag)
				updateSigPos(evt.getX() + this.dragXOffset, evt.getY() + this.dragYOffset);
		}
	};

	/**
	 * Check whether given point is on signature placeholder
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return true if given point is on signature placeholder
	 */
	boolean isOnSignature(int x, int y)
	{
		Rectangle2D sig = new Rectangle2D.Double(
				this.sigScreenPos.getX() + this.offX,
				this.sigScreenPos.getY() + this.offY,
				this.sigScreenWidth,
				this.sigScreenHeight);
		Point2D pos = new Point2D.Double(x, y);
		return (sig.contains(pos));
	}

	/**
	 * Clamp x to be within [min-max]
	 * @param x int to clamp
	 * @param min minimum value
	 * @param max maximum value
	 * @return clamped x
	 */
	static int clamp(int x, int min, int max)
	{
		if (x < min)
			x = min;
		else if (x > max)
			x = max;
		return x;
	}
}
