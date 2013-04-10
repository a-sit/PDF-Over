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
package at.asit.pdfover.signer.pdfas;

//Imports
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.signator.SignatureDimension;
import at.asit.pdfover.signator.SignatureParameter;
import at.asit.pdfover.signator.SignaturePosition;
import at.gv.egiz.pdfas.api.io.DataSource;
import at.gv.egiz.pdfas.api.sign.pos.SignaturePositioning;
import at.gv.egiz.pdfas.impl.signator.binary.BinarySignator_1_1_0;
import at.knowcenter.wag.egov.egiz.exceptions.PDFDocumentException;
import at.knowcenter.wag.egov.egiz.sig.SignatureObject;
import at.knowcenter.wag.egov.egiz.table.Entry;
import at.knowcenter.wag.egov.egiz.table.Style;
import at.knowcenter.wag.egov.egiz.table.Table;

/**
 * Implementation of SignatureParameter specific for PDF - AS Library
 */
public class PdfAsSignatureParameter extends SignatureParameter {

	/**
	 * SFL4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(PdfAsSignatureParameter.class);

	private HashMap<String, String> genericProperties = new HashMap<String, String>();

	/**
	 * Gets the PDFAS Positioning
	 * 
	 * @return SignaturePositioning
	 * @throws PDFDocumentException
	 */
	public SignaturePositioning getPDFASPositioning()
			throws PDFDocumentException {
		SignaturePosition position = this.getSignaturePosition();
		position.useAutoPositioning();

		SignaturePositioning positioning = null;
		if (!position.useAutoPositioning()) {
			if (position.getPage() < 1) {
				positioning = new SignaturePositioning(String.format(
						(Locale) null,
						"p:new;x:%f;y:%f;w:262",  position.getX(),
						position.getY()));
			} else {
				positioning = new SignaturePositioning(String.format(
						(Locale) null,
						"p:%d;x:%f;y:%f;w:262", position.getPage(), position.getX(),
						position.getY()));
			}
		} else {
			positioning = new SignaturePositioning();
		}

		return positioning;
	}

	/**
	 * Gets PDF - AS specific data source
	 * 
	 * @return ByteArrayPDFASDataSource
	 */
	public DataSource getPDFASDataSource() {
		return new ByteArrayPDFASDataSource(this.getInputDocument()
				.getByteArray());
	}

	@Override
	public void setProperty(String key, String value) {
		this.genericProperties.put(key, value);
	}

	@Override
	public String getProperty(String key) {
		return this.genericProperties.get(key);
	}

	@Override
	public SignatureDimension getPlaceholderDimension() {
		// return new SignatureDimension(487, 206);

		return new SignatureDimension(262, 88);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.signator.SignatureParameter#getPlaceholder()
	 */
	@Override
	public Image getPlaceholder() {

		try {
			PDFASHelper.getPdfAs();

			SignatureObject sign_obj = at.knowcenter.wag.egov.egiz.PdfAS
					.createSignatureObjectFromType(PDFASSigner.PROFILE_ID);

			sign_obj.fillValues(' ', true, false);
			sign_obj.setKZ(BinarySignator_1_1_0.MY_ID);

			float width = getPlaceholderDimension().getWidth();
			float height = getPlaceholderDimension().getHeight();

			Table table = sign_obj.getAbstractTable();

			table.getStyle().getBgColor();

			log.info(table.toString());

			float[] heights = this.getTableHeights(table, table.getStyle(),
					height);

			log.info("Width: " + width + " Height: " + height);
			BufferedImage image = new BufferedImage((int) width, (int) height,
					BufferedImage.TYPE_INT_RGB);
			Graphics g = image.getGraphics();

			g.setColor(table.getStyle().getBgColor());
			g.fillRect(0, 0, (int) width, (int) height);

			g.setColor(Color.black);
			g.drawRect(0, 0, (int) width, (int) height);

			this.drawTable(0, 0, (int) width, (int) height, table,
					table.getStyle(), g, heights);

			g.dispose();

			// save(image, "png");

			return image;
		} catch (Exception ex) {
			try {
				return ImageIO.read(PdfAsSignatureParameter.class
						.getResourceAsStream("/img/fallbackPlaceholder.png"));
			} catch (IOException e) {
				return new BufferedImage(getPlaceholderDimension().getWidth(),
						getPlaceholderDimension().getHeight(),
						BufferedImage.TYPE_INT_RGB);
			}
		}
	}

	/**
	 * used for debugging ..
	 * 
	 * @param image
	 * @param ext
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private static void save(BufferedImage image, String ext) {
		String fileName = "savingAnImage";
		File file = new File(fileName + "." + ext);
		try {
			ImageIO.write(image, ext, file); // ignore returned boolean
			log.debug("Saved as: " + file.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("Write error for " + file.getPath() + ": "
					+ e.getMessage());
		}
	}

	private static Font getFont(Style style) {
		String def = "COURIER-NORMAL-8";
		String fontString = style.getFont();
		String[] font_arr = fontString.split(",");
		if (font_arr.length != 3) {
			return Font.decode(def);
		}
		return Font.decode(String.format("%s-%s-%s", font_arr[0], font_arr[2],
				font_arr[1]));

	}

	/**
	 * extracts the value font
	 * 
	 * @param style
	 *            the table style
	 * @return the value font
	 */
	@SuppressWarnings("unused")
	private static Font getValueFont(Style style) {
		String def = "COURIER-NORMAL-8";
		String fontString = style.getValueFont();
		String[] font_arr = fontString.split(",");
		if (font_arr.length != 3) {
			return Font.decode(def);
		}
		return Font.decode(String.format("%s-%s-%s", font_arr[0], font_arr[2],
				font_arr[1]));
	}

	@SuppressWarnings("rawtypes")
	private int drawTable(int xoff, int yoff, int width, int height,
			Table table, Style parentstyle, Graphics g, float[] heights) {
		Style style = parentstyle;
		if (table.getStyle() != null) {
			style = table.getStyle();
		}

		log.debug(String.format((Locale) null, "Table@ %dx%d", xoff, yoff));

		Font oldFont = g.getFont();
		Font font = PdfAsSignatureParameter.getFont(style);
		g.setFont(font);

		// draw background
		// graphic.setColor(style.getBgColor());
		// graphic.fillRect(xoff, yoff, width, height);

		g.setColor(Color.black);

		// draw border
		if (style.getBorder() > 0) {
			g.setColor(Color.black);
			g.drawRect(xoff, yoff, width, height);
		}
		float[] colWidths = table.getColsRelativeWith();
		float sum = 0;

		for (int i = 0; i < colWidths.length; i++) {
			sum += colWidths[i];
		}

		float perUnit = width / sum;

		int padding = (int) (style.getPadding() * this.perUnitHeight);

		ArrayList rows = table.getRows();
		float roffset = 0;
		for (int rowidx = 0; rowidx < rows.size(); rowidx++) {
			ArrayList cols = (ArrayList) rows.get(rowidx);
			int rsize = (int) heights[rowidx];
			for (int j = 0; j < cols.size(); j++) {
				Entry entry = (Entry) cols.get(j);
				float offset = 0;
				for (int k = 0; k < j; k++) {
					offset += colWidths[k] * perUnit;
				}
				if (entry.getType() == 0 || entry.getType() == 1) {
					// Text
					g.drawRect((int) (xoff + offset),
							(int) (yoff + roffset),
							(int) (colWidths[j] * perUnit), rsize);

					g.drawString(entry.getValue().toString(), (int) (xoff
							+ offset + padding), (int) (yoff + padding
							+ roffset + font.getSize() * this.perUnitHeight));
				} else if (entry.getType() == 2) {
					// Image ...
					BufferedImage image;
					try {
						if (this.getEmblem() != null
								&& this.getEmblem().getFileName() != null
								&& new File(this.getEmblem().getFileName())
										.exists()) {
							image = ImageIO.read(new File(this.getEmblem()
									.getFileName()));
						} else {
							image = ImageIO.read(new File(PDFASHelper
									.getWorkDir()
									+ "/"
									+ entry.getValue().toString()));
						}
						int imgWidth = 40;
						int imgHeight = 40;
						Image img = image.getScaledInstance(imgWidth, imgHeight,
								Image.SCALE_SMOOTH);

						g.drawImage(
								img,
								(int) (xoff + offset + padding + (((colWidths[j] * perUnit) - imgWidth - padding)) / 2),
								(int) (yoff + roffset + padding + ((rsize - imgHeight - padding) / 2)),
								null);
					} catch (IOException e) {
						log.warn("Failed to paint emblem to placeholder image");
					}
				} else {
					// Table

					float[] cheights = this.getTableHeights(
							(Table) entry.getValue(), style, rsize);

					this.drawTable(
							(int) (xoff + offset),
							(int) (yoff + roffset),
							(int) (colWidths[j] * perUnit),
							// (int)this.getTableHeight((Table)
							// entry.getValue(), style),
							rsize, (Table) entry.getValue(), style, g,
							cheights);
					/*
					 * if (rsize < tsize) { rsize = tsize; }
					 */
				}
			}
			roffset += rsize;
		}

		g.setFont(oldFont);

		return (int) roffset;
	}

	private float perUnitHeight = 0;

	@SuppressWarnings("rawtypes")
	private float[] getTableHeights(Table table, Style parentstyle, float height) {
		ArrayList rows = table.getRows();
		float[] sizes = new float[rows.size()];

		float total_height = this.getTableHeight(table, parentstyle);

		float perUnit = height / total_height;

		this.perUnitHeight = perUnit;

		Style style = parentstyle;
		if (table.getStyle() != null) {
			style = table.getStyle();
		}

		for (int i = 0; i < rows.size(); i++) {
			Object robj = rows.get(i);
			ArrayList cols = (ArrayList) robj;
			float tsize = 0;
			float rsize = 0;
			for (int j = 0; j < cols.size(); j++) {
				Entry entry = (Entry) cols.get(j);
				if (entry.getType() == 0 || entry.getType() == 1) {
					String fontString = style.getFont();
					String[] font_arr = fontString.split(",");
					int fontSize = 8;

					if (font_arr.length == 3) {
						fontSize = Integer.parseInt(font_arr[1]);
					}

					if (rsize < ((style.getPadding() * 2) + fontSize)) {
						rsize = ((style.getPadding() * 2) + fontSize);
					}
				} else if (entry.getType() == 3) {
					tsize = this
							.getTableHeight((Table) entry.getValue(), style);
					if (rsize < tsize) {
						rsize = tsize;
					}
				}
			}
			sizes[i] = perUnit * rsize;
		}

		return sizes;
	}

	@SuppressWarnings("rawtypes")
	private float getTableHeight(Table table, Style parentstyle) {
		ArrayList rows = table.getRows();
		Style style = parentstyle;
		if (table.getStyle() != null) {
			style = table.getStyle();
		}
		float size = 0;
		for (int i = 0; i < rows.size(); i++) {
			Object robj = rows.get(i);
			ArrayList cols = (ArrayList) robj;
			float tsize = 0;
			float rsize = 0;
			for (int j = 0; j < cols.size(); j++) {
				Entry entry = (Entry) cols.get(j);
				if (entry.getType() == 0 || entry.getType() == 1) {
					String fontString = style.getFont();
					String[] font_arr = fontString.split(",");
					int fontSize = 8;

					if (font_arr.length == 3) {
						fontSize = Integer.parseInt(font_arr[1]);
					}

					if (rsize < ((style.getPadding() * 2) + fontSize)) {
						rsize = ((style.getPadding() * 2) + fontSize);
					}
				} else if (entry.getType() == 3) {
					tsize = this
							.getTableHeight((Table) entry.getValue(), style);
					if (rsize < tsize) {
						rsize = tsize;
					}
				}
			}
			size += rsize;
		}

		return size;
	}
}
