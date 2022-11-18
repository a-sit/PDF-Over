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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.swing.JPanel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Messages;

/**
 *
 */
public class SignaturePanel extends JPanel {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(SignaturePanel.class);

	/** Default serial version ID */
	private static final long serialVersionUID = 1L;

	/** The PDF file being displayed */
	private PDDocument pdf = null;
	private PDFRenderer renderer = null;

	/** The image of the rendered PDF page being displayed */
	Image currentImage = null;

	/** The current scale for rendering pdf to image */
	private float pageToImageScale;
	/** The current scale for rendering image to screen */
	private double imageToScreenScale;
	/* scaling */
	private enum U {
		/* (0,0) is bottom-left of page */
		PAGE_ABS,
		/* (0,0) is top-left of page */
		PAGE_REL,
		/* pixels, (0,0) is top-left of image */
		IMAGE,
		/* pixels, (0,0) is top-left of image */
		SCREEN_REL,
		/* pixels, (0,0) is top-left of canvas */
		SCREEN_ABS };
	private enum Dim { X, Y };
	private double scale(double v, U from, U to, Dim d)
	{
		if (from == to) return v;

		if (from == U.PAGE_ABS) {
			return scale((d == Dim.X) ? v : (this.pageHeight - v), U.PAGE_REL, to, d);
		} else if (from == U.PAGE_REL) {
			if (to == U.PAGE_ABS)
				return ((d == Dim.X) ? v : (this.pageHeight - v));
			else
				return scale(v * this.pageToImageScale, U.IMAGE, to, d);
		} else if (from == U.IMAGE) {
			if ((to == U.PAGE_ABS) || (to == U.PAGE_REL))
				return scale(v / this.pageToImageScale, U.PAGE_REL, to, d);
			else
				return scale(v * this.imageToScreenScale, U.SCREEN_REL, to, d);
		} else if (from == U.SCREEN_REL) {
			if (to == U.SCREEN_ABS)
				return (v + ((d == Dim.X) ? this.offX : this.offY));
			else
				return scale(v / this.imageToScreenScale, U.IMAGE, to, d);
		} else if (from == U.SCREEN_ABS) {
			return scale(v - ((d == Dim.X) ? this.offX : this.offY), U.SCREEN_REL, to, d);
		} else throw new RuntimeException("unreachable");
	}

	private float pageWidth = 0;
	private float pageHeight = 0;
	/** The horizontal offset of the image from the left edge of the panel */
	private int offX = 0;
	/** The vertical offset of the image from the top of the panel */
	private int offY = 0;
	/** The position of the top-left corner of the signature, in absolute page space */
	private Point2D sigPagePos = null;
	public @CheckForNull Point2D getSigPagePos() { return this.sigPagePos; }
	/** The signature placeholder image */
	private Image sigPlaceholder = null;
	/** Width of the signature placeholder in page space */
	private int sigPageWidth = 0;
	/** Height of the signature placeholder in page space */
	private int sigPageHeight = 0;
	/** Color of the signature placeholder border */
	private Color sigPlaceholderBorderColor = Color.BLUE;
	/** Current page */
	private int currentPageNo = 0;
	/** Number of pages in the document */
	private int numPages = 0;
	/** Cursor types */
	private static enum Cursors {DEFAULT, HAND, MOVE};
	/** Default arrow cursor */
	private final Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	/** Hand cursor */
	private final Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	/** Move cursor */
	private final Cursor moveCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
	/** Current cursor */
	private Cursors currentCursor = Cursors.DEFAULT;

	/**
	 * Create a new PagePanel.
	 */
	public SignaturePanel() {
		super(new BorderLayout());
		setDocument(null);
		setFocusable(true);
		addMouseListener(this.mouseListener);
		addMouseMotionListener(this.mouseListener);
	}

	/**
	 * Set a new document to be displayed
	 * @param pdf the PDFFile to be displayed
	 */
	public void setDocument(PDDocument pdf) {
		this.pdf = pdf;
		this.sigPagePos = null;
		if (pdf != null)
		{
			this.renderer = new PDFRenderer(pdf);
			this.numPages = pdf.getNumberOfPages();
			this.currentPageNo = -1;
			showPage(this.numPages);
		}
		else
		{
			this.renderer = null;
			this.currentPageNo = 0;
			this.numPages = 0;
			renderPageToImage();
			repaint();
		}
	}

	/**
	 * Set the signature placeholder image
	 * @param placeholder signature placeholder
	 * @param width width of the placeholder in page space
	 * @param height height of the placeholder in page space
	 */
	public void setSignaturePlaceholder(Image placeholder) {
		this.sigPlaceholder = placeholder;
		// TODO figure out why this is divided by 4 (factor ported from old code)
		this.sigPageWidth = placeholder.getWidth(null) / 4;
		this.sigPageHeight = placeholder.getHeight(null) / 4;
		renderPageToImage();
		if (this.sigPagePos != null)
			setSignaturePosition(this.sigPagePos.getX(), this.sigPagePos.getY());
		repaint();
	}

	/**
	 * Set the color of the signature placeholder border
	 * @param color new signature placeholder border color
	 */
	public void setSignaturePlaceholderBorderColor(Color color) {
		this.sigPlaceholderBorderColor = color;
	}

	/**
	 * Change the currently displayed page
	 * @param page the number of the page to display
	 */
	public void showPage(int page) {
		if (this.currentPageNo == page) return;
		this.currentPageNo = page;
		renderPageToImage();
		repaint();
	}

	/**
	 * Add and display a new page at the end of the document
	 *
	 * This page has the same dimensions as the old last page
	 */
	public void addNewLastPage() {
		showPage(this.numPages + 1);
	}

	/**
	 * Set the signature placeholder position
	 * @param x the horizontal signature position
	 * @param y the vertical signature position
	 */
	public void setSignaturePosition(double x, double y)
	{
		this.sigPagePos = new Point2D.Double(
			clamp(x, 0, this.pageWidth - this.sigPageWidth),
			clamp(y, this.sigPageHeight, this.pageHeight)
		);
		repaint();
	}

	public void translateSignaturePagePosition(float dX, float dY) {
		setSignaturePosition(this.sigPagePos.getX() + dX, this.sigPagePos.getY() + dY);
	}

	/**
	 * Stop the generation of any previous page, and draw the new one.
	 *
	 * @param page the PDFPage to draw.
	 */
	private synchronized void renderPageToImage() {
		if (this.pdf == null)
		{
			this.currentImage = null;
			return;
		}

		boolean newPage = false;
		PDPage currentPage;
		// set up the new page
		if (this.currentPageNo > this.numPages)
		{
			// New last page - use old last page as template
			currentPage = this.pdf.getPage(this.numPages-1);
			newPage = true;
		}
		else
			currentPage = this.pdf.getPage(this.currentPageNo-1);


		if (currentPage == null) {
			// no page
			this.currentImage = null;
			return;
		}
		
		boolean isRotated = ((currentPage.getRotation()%180) == 90);
		PDRectangle actualPageSize = currentPage.getBBox();
		this.pageWidth = isRotated ? actualPageSize.getHeight() : actualPageSize.getWidth();
		this.pageHeight = isRotated ? actualPageSize.getWidth() : actualPageSize.getHeight();
		this.pageToImageScale = getToolkit().getScreenSize().height / this.pageHeight;

		// get the new image
		if (newPage)
		{
			int renderHeight = (int)(0.5 + this.scale(actualPageSize.getHeight(), U.PAGE_REL, U.IMAGE, Dim.X));
			int renderWidth = (int)(0.5 + this.scale(actualPageSize.getWidth(), U.PAGE_REL, U.IMAGE, Dim.Y));
			this.currentImage = new BufferedImage(renderWidth, renderHeight, BufferedImage.TYPE_INT_RGB);
			Graphics g = this.currentImage.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, renderWidth, renderHeight);
		}
		else
		{
			int whichPage = Math.min(this.currentPageNo, this.numPages);

			try {
				this.currentImage = renderer.renderImage(whichPage-1, this.pageToImageScale);
			} catch (IOException e) {
				log.error(String.format("Failed to render image for page %d of %d", whichPage, this.numPages), e);
				this.currentImage = null;
			}
		}

		if (this.sigPagePos == null)
		{
			setSignaturePosition(
				actualPageSize.getWidth() * .5,
				actualPageSize.getHeight() * .75
			);
		}
	}

	/**
	 * Draw the image.
	 */
	@Override
	public void paint(Graphics g) {
		Dimension renderPanelSize = getSize();
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		if (this.currentImage == null) {
			g.setColor(Color.black);
			g.drawString(Messages.getString("common.working"), getWidth() / 2 - 30, getHeight() / 2);
		} else {
			this.imageToScreenScale = Math.min(
				renderPanelSize.getWidth() / this.currentImage.getWidth(null),
				renderPanelSize.getHeight() / this.currentImage.getHeight(null));
			// draw the image
			int actualRenderWidth = (int)(this.currentImage.getWidth(null) * this.imageToScreenScale);
			int actualRenderHeight = (int)(this.currentImage.getHeight(null) * this.imageToScreenScale);

			// draw it centered within the panel
			this.offX = (renderPanelSize.width - actualRenderWidth) / 2;
			this.offY = (renderPanelSize.height - actualRenderHeight) / 2;

			// draw document
			g.drawImage(this.currentImage, this.offX, this.offY, actualRenderWidth, actualRenderHeight, null);
			

			// draw signature
			int sigX = (int) this.scale(this.sigPagePos.getX(), U.PAGE_ABS, U.SCREEN_ABS, Dim.X);
			int sigY = (int) this.scale(this.sigPagePos.getY(), U.PAGE_ABS, U.SCREEN_ABS, Dim.Y);
			if (this.sigPlaceholder == null) {
				g.setColor(Color.red);
				g.drawRect(sigX, sigY, 100, 40);
			}
			else {
				int sigScreenWidth = (int)this.scale(this.sigPageWidth, U.PAGE_REL, U.SCREEN_REL, Dim.X);
				int sigScreenHeight = (int)this.scale(this.sigPageHeight, U.PAGE_REL, U.SCREEN_REL, Dim.Y);
				g.drawImage(this.sigPlaceholder, sigX, sigY, sigScreenWidth, sigScreenHeight, null);
				g.setColor(this.sigPlaceholderBorderColor);
				g.drawRect(sigX, sigY, sigScreenWidth-1, sigScreenHeight-1);
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

		private void updateSigPosDrag(MouseEvent evt) {
			SignaturePanel.this.setSignaturePosition(
				SignaturePanel.this.scale(evt.getX() - this.dragXOffset, U.SCREEN_ABS, U.PAGE_ABS, Dim.X),
				SignaturePanel.this.scale(evt.getY() - this.dragYOffset, U.SCREEN_ABS, U.PAGE_ABS, Dim.Y)
			);
		}

		/** Handles a mouseMoved event */
		@Override
		public void mouseMoved(MouseEvent evt) {
			try {
				boolean onSig = isOnSignature(evt);
				setCursor(onSig ? Cursors.HAND : Cursors.DEFAULT);
			} catch (NullPointerException e) {
				// do nothing
			}
		}

		/** Handles a mousePressed event */
		@Override
		public void mousePressed(MouseEvent evt) {
			if (evt.getButton() == MouseEvent.BUTTON1)
			{
				this.doDrag = true;
				if (isOnSignature(evt)) {
					/* offsets (in screen units) from top-left corner of signature to cursor on drag start */
					this.dragXOffset = (int)(evt.getX() - SignaturePanel.this.scale(SignaturePanel.this.sigPagePos.getX(), U.PAGE_ABS, U.SCREEN_ABS, Dim.X));
					this.dragYOffset = (int)(evt.getY() - SignaturePanel.this.scale(SignaturePanel.this.sigPagePos.getY(), U.PAGE_ABS, U.SCREEN_ABS, Dim.Y));
				} else {
					this.dragXOffset = 0;
					this.dragYOffset = 0;
				}
				updateSigPosDrag(evt);
				setCursor(Cursors.MOVE);
			}
		}

		/** Handles a mouseReleased event */
		@Override
		public void mouseReleased(MouseEvent evt) {
			this.doDrag = false;
			boolean onSig = isOnSignature(evt);
			setCursor(onSig ? Cursors.HAND : Cursors.DEFAULT);
		}

		/**
		 * Handles a mouseDragged event.
		 */
		@Override
		public void mouseDragged(MouseEvent evt) {
			if (this.doDrag)
				updateSigPosDrag(evt);
		}
	};

	/**
	 * Sets the mouse cursor
	 * @param cursor cursor to set
	 */
	void setCursor(Cursors cursor)
	{
		if (this.currentCursor == cursor)
			return;
		this.currentCursor = cursor;
		Cursor cur = null;
		switch (cursor) {
			case DEFAULT:
				cur = this.defaultCursor;
				break;
			case HAND:
				cur = this.handCursor;
				break;
			case MOVE:
				cur = this.moveCursor;
				break;
		}
		this.getParent().setCursor(cur);
	}

	/**
	 * Check whether given point is on signature placeholder
	 * @param x x coordinate (screen)
	 * @param y y coordinate (screen)
	 * @return true if given point is on signature placeholder
	 */
	private boolean isOnSignature(MouseEvent evt)
	{
		if (this.sigPagePos == null)
			return false;

		Rectangle2D sig = new Rectangle2D.Double(
			this.scale(this.sigPagePos.getX(), U.PAGE_ABS, U.SCREEN_ABS, Dim.X),
			this.scale(this.sigPagePos.getY(), U.PAGE_ABS, U.SCREEN_ABS, Dim.Y),
			this.scale(this.sigPageWidth, U.PAGE_REL, U.SCREEN_REL, Dim.X),
			this.scale(this.sigPageHeight, U.PAGE_REL, U.SCREEN_REL, Dim.Y)
		);
		return sig.contains(evt.getX(), evt.getY());
	}

	/**
	 * Clamp x to be within [min-max]
	 * @param x int to clamp
	 * @param min minimum value
	 * @param max maximum value
	 * @return clamped x
	 */
	private static double clamp(double x, double min, double max)
	{
		if (x < min)
			x = min;
		else if (x > max)
			x = max;
		return x;
	}
}
