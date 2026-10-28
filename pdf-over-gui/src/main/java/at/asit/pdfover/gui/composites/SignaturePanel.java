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

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.commons.utils.ImageUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Displays a PDF page and allows the user to place the signature placeholder.
 */
@Slf4j
public class SignaturePanel extends Canvas {

	/** The PDF file being displayed */
	private PDDocument pdf = null;
	private PDFRenderer renderer = null;

	/** The image of the rendered PDF page being displayed */
	private Image currentImage = null;

	/** The current scale for rendering pdf to image */
	private float pageToImageScale;
	/** The current scale for rendering image to screen */
	private double imageToScreenScale = 1.0;
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
	private Point2D pendingSigPagePos = null;
	public Point2D getSigPagePos() { return this.sigPagePos; }
	/** The signature placeholder image */
	private Image sigPlaceholder = null;
	/** Width of the signature placeholder in page space */
	private int sigPageWidth = 0;
	/** Height of the signature placeholder in page space */
	private int sigPageHeight = 0;
	/** Color of the signature placeholder border */
	private Color sigPlaceholderBorderColor = null;
	/** Current page */
	private int currentPageNo = 0;
	/** Number of pages in the document */
	private int numPages = 0;
	/** Cursor types */
	private static enum Cursors {DEFAULT, HAND, MOVE};
	/** Current cursor */
	private Cursors currentCursor = Cursors.DEFAULT;
	private final AtomicInteger renderGeneration = new AtomicInteger();
	private final Object renderLock = new Object();
	private boolean doDrag = false;
	private int dragXOffset = 0;
	private int dragYOffset = 0;
	private Runnable signaturePositionAvailableCallback = null;

	/**
	 * Create a new PagePanel.
	 */
	public SignaturePanel(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
		setDocument(null);
		addPaintListener(e -> paint(e.gc));
		addMouseListener(this.mouseListener);
		addMouseMoveListener(this.mouseMoveListener);
		addDisposeListener(e -> {
			this.renderGeneration.incrementAndGet();
			disposeCurrentImage();
			disposePlaceholder();
		});
	}

	/**
	 * Set a new document to be displayed
	 * @param pdf the PDFFile to be displayed
	 */
	public void setDocument(PDDocument pdf) {
		if (isDisposed())
			return;
		this.renderGeneration.incrementAndGet();
		synchronized (this.renderLock) {
			this.pdf = pdf;
			this.sigPagePos = null;
			this.pendingSigPagePos = null;
			if (pdf != null)
			{
				this.renderer = new PDFRenderer(pdf);
				this.numPages = pdf.getNumberOfPages();
				this.currentPageNo = -1;
			}
			else
			{
				this.renderer = null;
				this.currentPageNo = 0;
				this.numPages = 0;
			}
		}
		if (pdf != null)
			showPage(this.numPages);
		else {
			disposeCurrentImage();
			if (!isDisposed())
				redraw();
		}
	}

	/**
	 * Set the signature placeholder image
	 * @param placeholder signature placeholder
	 */
	public void setSignaturePlaceholder(ImageData placeholder) {
		if (isDisposed())
			return;
		disposePlaceholder();
		if (placeholder != null) {
			this.sigPlaceholder = new Image(getDisplay(), placeholder);
			// TODO figure out why this is divided by 4 (factor ported from old code)
			this.sigPageWidth = placeholder.width / 4;
			this.sigPageHeight = placeholder.height / 4;
		} else {
			this.sigPageWidth = 0;
			this.sigPageHeight = 0;
		}
		if (this.sigPagePos != null)
			setSignaturePosition(this.sigPagePos.getX(), this.sigPagePos.getY());
		redraw();
	}

	/**
	 * Set the color of the signature placeholder border
	 * @param color new signature placeholder border color
	 */
	public void setSignaturePlaceholderBorderColor(Color color) {
		if (isDisposed())
			return;
		this.sigPlaceholderBorderColor = color;
	}

	public void setSignaturePositionAvailableCallback(Runnable callback) {
		if (isDisposed())
			return;
		this.signaturePositionAvailableCallback = callback;
	}

	/**
	 * Change the currently displayed page
	 * @param page the number of the page to display
	 */
	public void showPage(int page) {
		if (isDisposed())
			return;
		if (this.currentPageNo == page) return;
		this.currentPageNo = page;
		renderPageToImage();
		redraw();
	}

	/**
	 * Set the signature placeholder position
	 * @param x the horizontal signature position
	 * @param y the vertical signature position
	 */
	public void setSignaturePosition(double x, double y)
	{
		if (isDisposed())
			return;
		if (this.pageWidth <= 0 || this.pageHeight <= 0) {
			this.pendingSigPagePos = new Point2D.Double(x, y);
			return;
		}
		boolean hadNoSignaturePosition = (this.sigPagePos == null);
		this.sigPagePos = new Point2D.Double(
			clamp(x, 0, this.pageWidth - this.sigPageWidth),
			clamp(y, this.sigPageHeight, this.pageHeight)
		);
		if (hadNoSignaturePosition && this.signaturePositionAvailableCallback != null)
			this.signaturePositionAvailableCallback.run();
		redraw();
	}

	public void translateSignaturePagePosition(float dX, float dY) {
		if (isDisposed())
			return;
		if (this.sigPagePos != null)
			setSignaturePosition(this.sigPagePos.getX() + dX, this.sigPagePos.getY() + dY);
	}

	private void disposeCurrentImage() {
		if (this.currentImage != null && !this.currentImage.isDisposed())
			this.currentImage.dispose();
		this.currentImage = null;
	}

	private void disposePlaceholder() {
		if (this.sigPlaceholder != null && !this.sigPlaceholder.isDisposed())
			this.sigPlaceholder.dispose();
		this.sigPlaceholder = null;
	}

	/**
	 * Stop the generation of any previous page, and draw the new one.
	 */
	private void renderPageToImage() {
		if (isDisposed())
			return;
		final int generation = this.renderGeneration.incrementAndGet();
		final int pageNo = this.currentPageNo;
		final int screenHeight = getShell().getMonitor().getBounds().height;
		final Display display = getDisplay();

		Thread renderThread = new Thread(() -> {
			ImageData imageData = null;
			float renderedPageWidth = 0;
			float renderedPageHeight = 0;
			float renderedScale = 1;

			try {
				synchronized (this.renderLock) {
					if (this.pdf == null || generation != this.renderGeneration.get())
						return;

					boolean newPage = false;
					PDPage currentPage;
					if (pageNo > this.numPages)
					{
						currentPage = this.pdf.getPage(this.numPages-1);
						newPage = true;
					}
					else
						currentPage = this.pdf.getPage(pageNo-1);

					if (currentPage == null)
						return;

					boolean isRotated = ((currentPage.getRotation()%180) == 90);
					PDRectangle actualPageSize = currentPage.getBBox();
					renderedPageWidth = isRotated ? actualPageSize.getHeight() : actualPageSize.getWidth();
					renderedPageHeight = isRotated ? actualPageSize.getWidth() : actualPageSize.getHeight();
					renderedScale = screenHeight / renderedPageHeight;

					if (newPage)
					{
						int renderHeight = (int)(0.5 + (renderedPageHeight * renderedScale));
						int renderWidth = (int)(0.5 + (renderedPageWidth * renderedScale));
						BufferedImage blankImage = new BufferedImage(renderWidth, renderHeight, BufferedImage.TYPE_INT_RGB);
						java.awt.Graphics g = blankImage.getGraphics();
						g.setColor(java.awt.Color.WHITE);
						g.fillRect(0, 0, renderWidth, renderHeight);
						g.dispose();
						imageData = ImageUtil.convertToSWT(blankImage);
					}
					else
					{
						int whichPage = Math.min(pageNo, this.numPages);
						BufferedImage renderedImage = this.renderer.renderImage(whichPage-1, renderedScale);
						imageData = ImageUtil.convertToSWT(convertToRGB(renderedImage));
					}
				}
			} catch (IOException e) {
				log.error(String.format("Failed to render image for page %d of %d", Math.min(pageNo, this.numPages), this.numPages), e);
			} catch (RuntimeException e) {
				log.error(String.format("Failed to prepare image for page %d of %d", Math.min(pageNo, this.numPages), this.numPages), e);
			}

			final ImageData finalImageData = imageData;
			final float finalPageWidth = renderedPageWidth;
			final float finalPageHeight = renderedPageHeight;
			final float finalScale = renderedScale;
			if (display.isDisposed())
				return;
			display.asyncExec(() -> {
				if (isDisposed() || generation != this.renderGeneration.get())
					return;
				disposeCurrentImage();
				this.pageWidth = finalPageWidth;
				this.pageHeight = finalPageHeight;
				this.pageToImageScale = finalScale;
				if (finalImageData != null)
					this.currentImage = new Image(display, finalImageData);
				if (this.pendingSigPagePos != null) {
					Point2D pendingPosition = this.pendingSigPagePos;
					this.pendingSigPagePos = null;
					setSignaturePosition(pendingPosition.getX(), pendingPosition.getY());
				} else if (this.sigPagePos != null) {
					setSignaturePosition(this.sigPagePos.getX(), this.sigPagePos.getY());
				} else if (this.sigPagePos == null && this.pageWidth > 0 && this.pageHeight > 0) {
					setSignaturePosition(this.pageWidth * .5, this.pageHeight * .25);
				}
				redraw();
			});
		}, "PDF-Over page renderer");
		renderThread.setDaemon(true);
		renderThread.start();
	}

	private static BufferedImage convertToRGB(BufferedImage image) {
		if (image.getType() == BufferedImage.TYPE_INT_RGB)
			return image;
		BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics g = rgbImage.getGraphics();
		g.drawImage(image, 0, 0, java.awt.Color.WHITE, null);
		g.dispose();
		return rgbImage;
	}

	/**
	 * Draw the image.
	 */
	private void paint(GC gc) {
		Point renderPanelSize = getSize();
		gc.setBackground(getBackground());
		gc.fillRectangle(0, 0, renderPanelSize.x, renderPanelSize.y);
		if (this.currentImage == null) {
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			gc.drawString(Messages.getString("common.working"), Math.max(0, renderPanelSize.x / 2 - 30), Math.max(0, renderPanelSize.y / 2), true);
		} else {
			Rectangle imageBounds = this.currentImage.getBounds();
			this.imageToScreenScale = Math.min(
				renderPanelSize.x / (double)imageBounds.width,
				renderPanelSize.y / (double)imageBounds.height);
			int actualRenderWidth = (int)(imageBounds.width * this.imageToScreenScale);
			int actualRenderHeight = (int)(imageBounds.height * this.imageToScreenScale);

			this.offX = (renderPanelSize.x - actualRenderWidth) / 2;
			this.offY = (renderPanelSize.y - actualRenderHeight) / 2;

			gc.drawImage(this.currentImage, 0, 0, imageBounds.width, imageBounds.height, this.offX, this.offY, actualRenderWidth, actualRenderHeight);

			if (this.sigPagePos == null)
				return;

			int sigX = (int) this.scale(this.sigPagePos.getX(), U.PAGE_ABS, U.SCREEN_ABS, Dim.X);
			int sigY = (int) this.scale(this.sigPagePos.getY(), U.PAGE_ABS, U.SCREEN_ABS, Dim.Y);
			if (this.sigPlaceholder == null) {
				gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
				gc.drawRectangle(sigX, sigY, 100, 40);
			}
			else {
				Rectangle placeholderBounds = this.sigPlaceholder.getBounds();
				int sigScreenWidth = (int)this.scale(this.sigPageWidth, U.PAGE_REL, U.SCREEN_REL, Dim.X);
				int sigScreenHeight = (int)this.scale(this.sigPageHeight, U.PAGE_REL, U.SCREEN_REL, Dim.Y);
				gc.drawImage(this.sigPlaceholder, 0, 0, placeholderBounds.width, placeholderBounds.height, sigX, sigY, sigScreenWidth, sigScreenHeight);
				gc.setForeground(this.sigPlaceholderBorderColor != null ? this.sigPlaceholderBorderColor : getDisplay().getSystemColor(SWT.COLOR_BLUE));
				gc.drawRectangle(sigX, sigY, sigScreenWidth-1, sigScreenHeight-1);
			}
		}
	}

	private MouseMoveListener mouseMoveListener = evt -> {
		if (this.doDrag) {
			updateSigPosDrag(evt);
			return;
		}
		try {
			boolean onSig = isOnSignature(evt);
			setCursor(onSig ? Cursors.HAND : Cursors.DEFAULT);
		} catch (NullPointerException e) {
			// do nothing
		}
	};

	private MouseAdapter mouseListener = new MouseAdapter() {
		/** Handles a mousePressed event */
		@Override
		public void mouseDown(MouseEvent evt) {
			if (evt.button == 1)
			{
				setFocus();
				SignaturePanel.this.doDrag = true;
				if (isOnSignature(evt)) {
					SignaturePanel.this.dragXOffset = (int)(evt.x - SignaturePanel.this.scale(SignaturePanel.this.sigPagePos.getX(), U.PAGE_ABS, U.SCREEN_ABS, Dim.X));
					SignaturePanel.this.dragYOffset = (int)(evt.y - SignaturePanel.this.scale(SignaturePanel.this.sigPagePos.getY(), U.PAGE_ABS, U.SCREEN_ABS, Dim.Y));
				} else {
					SignaturePanel.this.dragXOffset = 0;
					SignaturePanel.this.dragYOffset = 0;
				}
				updateSigPosDrag(evt);
				setCursor(Cursors.MOVE);
			}
		}

		/** Handles a mouseReleased event */
		@Override
		public void mouseUp(MouseEvent evt) {
			SignaturePanel.this.doDrag = false;
			boolean onSig = isOnSignature(evt);
			setCursor(onSig ? Cursors.HAND : Cursors.DEFAULT);
		}
	};

	private void updateSigPosDrag(MouseEvent evt) {
		setSignaturePosition(
			scale(evt.x - this.dragXOffset, U.SCREEN_ABS, U.PAGE_ABS, Dim.X),
			scale(evt.y - this.dragYOffset, U.SCREEN_ABS, U.PAGE_ABS, Dim.Y)
		);
	}

	/**
	 * Sets the mouse cursor
	 * @param cursor cursor to set
	 */
	void setCursor(Cursors cursor)
	{
		if (this.currentCursor == cursor)
			return;
		this.currentCursor = cursor;
		switch (cursor) {
			case DEFAULT:
				setCursor(getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
				break;
			case HAND:
				setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));
				break;
			case MOVE:
				setCursor(getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL));
				break;
		}
	}

	/**
	 * Check whether given point is on signature placeholder
	 * @return true if given point is on signature placeholder
	 */
	private boolean isOnSignature(MouseEvent evt)
	{
		if (this.sigPagePos == null)
			return false;

		double sigX = this.scale(this.sigPagePos.getX(), U.PAGE_ABS, U.SCREEN_ABS, Dim.X);
		double sigY = this.scale(this.sigPagePos.getY(), U.PAGE_ABS, U.SCREEN_ABS, Dim.Y);
		double sigWidth = this.scale(this.sigPageWidth, U.PAGE_REL, U.SCREEN_REL, Dim.X);
		double sigHeight = this.scale(this.sigPageHeight, U.PAGE_REL, U.SCREEN_REL, Dim.Y);
		return (evt.x >= sigX) && (evt.x <= (sigX + sigWidth)) && (evt.y >= sigY) && (evt.y <= (sigY + sigHeight));
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
