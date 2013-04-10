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
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.workflow.states.PositioningState;

/**
 * Displays a PDF document
 */
public class PDFViewerComposite extends Composite {
	/**
	 * SFL4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(PDFViewerComposite.class);

	/**
	 * PDF document
	 */
	protected Document document;

	/**
	 * Currently selected page in the document
	 */
	protected int page;

	/**
	 * AWT Canvas displaying the document
	 */
	private Canvas canvas = null;

	/**
	 * Dimension of each page at default zoom
	 */
	Dimension[] base_dimensions;

	/**
	 * Set the document to be displayed
	 * @param document PDF document to be displayed
	 * @throws PDFException Error parsing PDF document
	 * @throws PDFSecurityException Error decrypting PDF document (not supported)
	 * @throws IOException I/O Error
	 */
	public void setDocument(File document) throws PDFException, PDFSecurityException, IOException {
		this.document = new Document();
		this.document.setFile(document.getPath());
		int pages = this.document.getNumberOfPages();

		this.base_dimensions = new Dimension[pages];
		for (int page = 0; page < pages; ++page)
			this.base_dimensions[page] = this.document.getPageDimension(page, 0f, 1.0f).toDimension();

		this.page = pages - 1;

		if (this.canvas != null)
			this.canvas.repaint();
	}

	/**
	 * Get the currently selected page in the document
	 * @return current page
	 */
	public int getPage() {
		return this.page;
	}

	/**
	 * Set the visible page in the document
	 * @param page new active page
	 */
	public void setPage(int page) {
		this.page = page;
		this.canvas.repaint();
	}

	/**
	 * Create the PDF Viewer composite.
	 * Displays a PDF document.
	 * Starts on the last page.
	 * @param parent parent Composite
	 * @param style 
	 * @param document
	 * @throws PDFException Error parsing PDF document
	 * @throws PDFSecurityException Error decrypting PDF document (not supported)
	 * @throws IOException I/O Error
	 */
	public PDFViewerComposite(Composite parent, int style, File document) throws PDFException, PDFSecurityException, IOException {
		super(parent, style);

		setDocument(document);

		Frame frame = SWT_AWT.new_Frame(this);
		this.canvas = new Canvas() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				if (g == null || g.getClip() == null)
					return;
				int page = getPage();
				// Make page always fit to window
				Dimension d = getSize();
				double h_zoom = d.getWidth() / PDFViewerComposite.this.base_dimensions[page].width;
				double v_zoom = d.getHeight() / PDFViewerComposite.this.base_dimensions[page].height;
				float zoom = (float) (h_zoom < v_zoom ? h_zoom : v_zoom);
				if (v_zoom < h_zoom)
				{
					// Page is narrower than window, center it
					g.translate((int) ((d.width - (PDFViewerComposite.this.base_dimensions[page].width * zoom)) / 2), 0);
				}

				log.debug("Repainting " + g.getClipBounds().width + "x" + g.getClipBounds().height + " - " + d.width + "x" + d.height);
				PDFViewerComposite.this.document.paintPage(page, g, GraphicsRenderingHints.SCREEN, Page.BOUNDARY_CROPBOX, 0f, zoom);
			}
		};
		frame.add(this.canvas);
		frame.pack();
		frame.setVisible(true);

		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int page = getPage();
				int old_page = page;

				switch (e.keyCode)
				{
					case SWT.PAGE_DOWN:
						if (page < (PDFViewerComposite.this.document.getNumberOfPages() - 1))
							++page;
						break;

					case SWT.PAGE_UP:
						if (page > 0)
							--page;
						break;

					case SWT.END:
						page = (PDFViewerComposite.this.document.getNumberOfPages() - 1);
						break;

					case SWT.HOME:
						page = 0;
						break;
				}

				if (page != old_page)
					setPage(page);
			}
		});
	}
}
