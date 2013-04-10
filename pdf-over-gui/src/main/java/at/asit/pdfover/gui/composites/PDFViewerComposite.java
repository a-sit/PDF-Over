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

/**
 * Displays a PDF document
 */
public class PDFViewerComposite extends Composite {

	/**
	 * PDF document
	 */
	protected Document document;

	/**
	 * Currently selected page in the document
	 */
	protected int page;

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
	 * AWT Canvas displaying the document
	 */
	private Canvas canvas;

	/**
	 * Create the PDF Viewer composite.
	 * @param parent parent Composite
	 * @param style 
	 * @param document
	 * @throws PDFException Error parsing PDF document
	 * @throws PDFSecurityException Error decrypting PDF document (not supported)
	 * @throws IOException I/O Error
	 */
	public PDFViewerComposite(Composite parent, int style, File document) throws PDFException, PDFSecurityException, IOException {
		super(parent, style);
		this.document = new Document();
		this.document.setFile(document.getPath());
		int pages = this.document.getNumberOfPages();

		final Dimension[] base_dimensions = new Dimension[pages];
		for (int page = 0; page < pages; ++page)
			base_dimensions[page] = this.document.getPageDimension(page, 0f, 1.0f).toDimension();

		this.page = pages - 1;

		Frame frame = SWT_AWT.new_Frame(this);
		this.canvas = new Canvas() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				if (g == null || g.getClip() == null)
					return;
				int page = getPage();
				// Make page always fit to window
				Rectangle2D clip = g.getClip().getBounds2D();
				double h_zoom = clip.getWidth() / base_dimensions[page].width;
				double v_zoom = clip.getHeight() / base_dimensions[page].height;
				float zoom = (float) (h_zoom < v_zoom ? h_zoom : v_zoom);
				if (v_zoom < h_zoom)
				{
					// Page is narrower than window, center it
					g.translate((int) ((clip.getWidth() - (base_dimensions[page].width * zoom)) / 2), 0);
				}

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
