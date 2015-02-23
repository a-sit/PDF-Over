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
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
import at.knowcenter.wag.egov.egiz.exceptions.PDFDocumentException;

/**
 * Implementation of SignatureParameter specific for PDF - AS Library
 */
public class PdfAsSignatureParameter extends SignatureParameter {

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(PdfAsSignatureParameter.class);

	/** The profile ID for the german signature block */
	private static final String PROFILE_ID_DE = "SIGNATURBLOCK_SMALL_DE";
	/** The profile ID for the german signature block if a signature note is set */
	private static final String PROFILE_ID_DE_NOTE = "SIGNATURBLOCK_SMALL_DE_NOTE";
	/** The profile ID for the english signature block */
	private static final String PROFILE_ID_EN = "SIGNATURBLOCK_SMALL_EN";
	/** The profile ID for the english signature block if a signature note is set */
	private static final String PROFILE_ID_EN_NOTE = "SIGNATURBLOCK_SMALL_EN_NOTE";

	private HashMap<String, String> genericProperties = new HashMap<String, String>();

//	private static final int PLACEHOLDER_SCALE = 4;
//
//	private int height = -1;
//
//	private float perUnitHeight = 0;

	/**
	 * Gets the PDFAS Positioning
	 * 
	 * @return SignaturePositioning
	 * @throws PDFDocumentException
	 */
	public SignaturePositioning getPDFASPositioning()
			throws PDFDocumentException {
		SignaturePosition position = this.getSignaturePosition();

		SignaturePositioning positioning = null;
		if (!position.useAutoPositioning()) {
			if (position.getPage() < 1) {
				positioning = new SignaturePositioning(String.format(
						(Locale) null,
						"p:new;x:%f;y:%f;w:276",  position.getX(),
						position.getY()));
			} else {
				positioning = new SignaturePositioning(String.format(
						(Locale) null,
						"p:%d;x:%f;y:%f;w:276", position.getPage(), position.getX(),
						position.getY()));
			}
		} else {
			positioning = new SignaturePositioning("p:auto;x:auto;y:auto;w:276");
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
//		this.height = -1;
	}

	@Override
	public String getProperty(String key) {
		return this.genericProperties.get(key);
	}

	@Override
	public SignatureDimension getPlaceholderDimension() {
		// return new SignatureDimension(487, 206);
		return new SignatureDimension(276, 126);
		//return new SignatureDimension(getWidth(), getHeight());
	}

//	private static int getWidth() {
//		return 276;
//	}
//
//	private int getHeight() {
//		if (this.height < 0)
//		{
//			BufferedImage timage = new BufferedImage(1, 1,
//					BufferedImage.TYPE_INT_RGB);
//			try {
//				this.height = (int) (getTableHeight(getSignatureTable(), null, getWidth() * PLACEHOLDER_SCALE, timage.getGraphics()) / PLACEHOLDER_SCALE);
//			} catch (SignatureException e) {
//				log.error("getTableHeight failed ...", e);
//			} catch (SignatureTypesException e) {
//				log.error("getTableHeight failed ...", e);
//			}
//			timage.flush();
//		}
//		if (this.height < 0)
//			return 95;
//
//		return this.height;
//	}

	/**
	 * Get the Signature Profile ID for this set of parameters
	 * @return the Signature Profile ID
	 */
	public String getSignatureProfileID() {
		String lang = getSignatureLanguage();
		boolean useNote = (getProperty("SIG_NOTE") != null);

		if (lang != null && lang.equals("en"))
			return useNote ? PROFILE_ID_EN_NOTE : PROFILE_ID_EN;

		return useNote ? PROFILE_ID_DE_NOTE : PROFILE_ID_DE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.signator.SignatureParameter#getPlaceholder()
	 */
	@Override
	public Image getPlaceholder() {

		try {
			Image logo = null; 
			try {
			if (this.getEmblem() != null
					&& this.getEmblem().getFileName() != null
					&& new File(this.getEmblem().getFileName())
							.exists()) {
				logo = ImageIO.read(new File(this.getEmblem()
						.getFileName()));
				
			} 
			}
			catch(Exception e) {
				log.error("Failed to get emblem ...", e);
			}
			
			Image img = null;
			String lang = getSignatureLanguage();
			if (lang != null && lang.equals("en")) {
				img = ImageIO.read(PdfAsSignatureParameter.class
						.getResourceAsStream("/img/sign_prev_en.png"));
				
				if(logo != null) {
					logo = logo.getScaledInstance(141, 140,
							Image.SCALE_SMOOTH);
					img.getGraphics().drawImage(logo, 6, 115, null);
				}
				
			} else {
				img = ImageIO.read(PdfAsSignatureParameter.class
						.getResourceAsStream("/img/sign_prev_de.png"));
				
				if(logo != null) {
					logo = logo.getScaledInstance(141, 140,
							Image.SCALE_SMOOTH);
					img.getGraphics().drawImage(logo, 6, 115, null);
				}
			}
			return img;
		} catch (IOException e) {
			return new BufferedImage(getPlaceholderDimension().getWidth(),
					getPlaceholderDimension().getHeight(),
					BufferedImage.TYPE_INT_RGB);
		}
		
//		Try to render signature block - disabled for now (just use images)
//
//		try {
//			PDFASHelper.getPdfAs();
//
//			
//			float width = getPlaceholderDimension().getWidth() * PLACEHOLDER_SCALE;
//			float height = getPlaceholderDimension().getHeight() * PLACEHOLDER_SCALE;
//
//			Table table = this.getSignatureTable();
//
//			//log.info(table.toString());
//
//			BufferedImage timage = new BufferedImage(1, 1,
//					BufferedImage.TYPE_INT_RGB);
//			float[] heights = this.getTableHeights(table, table.getStyle(),
//					height, (int)width, timage.getGraphics());
//			timage.flush();
//			float mheight = 0;
//			
//			for(int i = 0; i < heights.length; i++) {
//				mheight += heights[i];
//			}
//		
//			this.height = (int) (mheight / PLACEHOLDER_SCALE);
//			
//			log.info("Width: " + width + " Height: " + height + " HShould: " + mheight);
//			BufferedImage image = new BufferedImage((int) width, (int) mheight,
//					BufferedImage.TYPE_INT_RGB);
//			Graphics g = image.getGraphics();
//
//			g.setColor(table.getStyle().getBgColor());
//			g.fillRect(0, 0, (int) width, (int) mheight);
//
//			g.setColor(Color.black);
//			g.drawRect(0, 0, (int) width, (int) mheight);
//
//			this.drawTable(0, 0, (int) width, (int) mheight, table,
//					table.getStyle(), g, heights);
//
//			g.dispose();
//
//			//save(image, "png");
//
//			return image;
//		} catch (Exception ex) {
//			try {
//				return ImageIO.read(PdfAsSignatureParameter.class
//						.getResourceAsStream("/img/fallbackPlaceholder.png"));
//			} catch (IOException e) {
//				return new BufferedImage(getPlaceholderDimension().getWidth(),
//						getPlaceholderDimension().getHeight(),
//						BufferedImage.TYPE_INT_RGB);
//			}
//		}
	}

//	private Table getSignatureTable() throws SignatureException, SignatureTypesException {
//		SignatureObject sign_obj = at.knowcenter.wag.egov.egiz.PdfAS
//				.createSignatureObjectFromType(getSignatureProfileID());
//
//		sign_obj.fillValues(' ', true, false);
//		sign_obj.setKZ(BinarySignator_1_1_0.MY_ID);
//		return sign_obj.getAbstractTable();
//	}
//	
//	/**
//	 * used for debugging ..
//	 * 
//	 * @param image
//	 * @param ext
//	 */
//	@SuppressWarnings("unused")
//	@Deprecated
//	private static void save(BufferedImage image, String ext) {
//		String fileName = "savingAnImage";
//		File file = new File(fileName + "." + ext);
//		try {
//			ImageIO.write(image, ext, file); // ignore returned boolean
//			log.debug("Saved as: " + file.getAbsolutePath());
//		} catch (IOException e) {
//			System.out.println("Write error for " + file.getPath() + ": "
//					+ e.getMessage());
//		}
//	}
//
//	@SuppressWarnings("rawtypes")
//	private int drawTable(int xoff, int yoff, int width, int height,
//			Table table, Style parentstyle, Graphics g, float[] heights) {
//		Style style = parentstyle;
//		if (table.getStyle() != null) {
//			style = table.getStyle();
//		}
//
//		log.debug(String.format((Locale) null, "Table@ %dx%d", xoff, yoff));
//
//		Font oldFont = g.getFont();
//		Font font = PdfAsSignatureParameter.getFont(style);
//		g.setFont(font);
//		// draw background
//		// graphic.setColor(style.getBgColor());
//		// graphic.fillRect(xoff, yoff, width, height);
//
//		g.setColor(Color.black);
//
//		// draw border
//		if (style.getBorder() > 0) {
//			g.setColor(Color.black);
//			g.drawRect(xoff, yoff, width, height);
//		}
//		float[] colWidths = table.getColsRelativeWith();
//		float sum = 0;
//
//		for (int i = 0; i < colWidths.length; i++) {
//			sum += colWidths[i];
//		}
//
//		float perUnit = width / sum;
//
//		int padding = (int) (style.getPadding() * this.perUnitHeight * PLACEHOLDER_SCALE);
//
//		ArrayList rows = table.getRows();
//		float roffset = 0;
//		for (int rowidx = 0; rowidx < rows.size(); rowidx++) {
//			ArrayList cols = (ArrayList) rows.get(rowidx);
//			int rsize = (int) heights[rowidx];
//			for (int j = 0; j < cols.size(); j++) {
//				Entry entry = (Entry) cols.get(j);
//				float offset = 0;
//				for (int k = 0; k < j; k++) {
//					offset += colWidths[k] * perUnit;
//				}
//				if (entry.getType() == 0 || entry.getType() == 1) {
//					// Text
//					g.drawRect((int) (xoff + offset),
//							(int) (yoff + roffset),
//							(int) (colWidths[j] * perUnit), rsize);
//
//					String[] lines = getLines(entry.getValue().toString(), (int)(colWidths[j] * perUnit), g.getFontMetrics(), (int) style.getPadding()  * PLACEHOLDER_SCALE);
//					
//					for(int i = 0; i < lines.length; i++) {
//						g.drawString(lines[i].toString(), (int) (xoff
//								+ offset + padding / PLACEHOLDER_SCALE), (int) (yoff + padding
//								+ roffset + (i + 1) * g.getFontMetrics().getHeight() * this.perUnitHeight));
//					}
//					//g.drawString(entry.getValue().toString(), (int) (xoff
//					//		+ offset + padding / PLACEHOLDER_SCALE), (int) (yoff + padding
//					//		+ roffset + font.getSize() * this.perUnitHeight));
//				} else if (entry.getType() == 2) {
//					// Image ...
//					BufferedImage image;
//					try {
//						if (this.getEmblem() != null
//								&& this.getEmblem().getFileName() != null
//								&& new File(this.getEmblem().getFileName())
//										.exists()) {
//							image = ImageIO.read(new File(this.getEmblem()
//									.getFileName()));
//						} else {
//							image = ImageIO.read(new File(PDFASHelper
//									.getWorkDir()
//									+ File.separator
//									+ entry.getValue().toString()));
//						}
//						int imgWidth = 30 * PLACEHOLDER_SCALE;
//						int imgHeight = 30 * PLACEHOLDER_SCALE;
//						Image img = image.getScaledInstance(imgWidth, imgHeight,
//								Image.SCALE_SMOOTH);
//
//						g.drawImage(
//								img,
//								(int) (xoff + offset + padding + (((colWidths[j] * perUnit) - imgWidth - 2* padding)) / 2),
//								(int) (yoff + roffset + padding + ((rsize - imgHeight - 2* padding) / 2)),
//								null);
//					} catch (IOException e) {
//						log.warn("Failed to paint emblem to placeholder image");
//					}
//				} else {
//					// Table
//
//					int colWidth = (int) (colWidths[j] * perUnit);
//					
//					float[] cheights = this.getTableHeights(
//							(Table) entry.getValue(), style, rsize, colWidth, g);
//
//					this.drawTable(
//							(int) (xoff + offset),
//							(int) (yoff + roffset),
//							(int) (colWidths[j] * perUnit),
//							// (int)this.getTableHeight((Table)
//							// entry.getValue(), style),
//							rsize, (Table) entry.getValue(), style, g,
//							cheights);
//					/*
//					 * if (rsize < tsize) { rsize = tsize; }
//					 */
//				}
//			}
//			roffset += rsize;
//		}
//
//		g.setFont(oldFont);
//
//		return (int) roffset;
//	}
//
//	private static Font getFont(Style style) {
//		String def = "COURIER-NORMAL-8";
//		String fontString = style.getFont();
//		String[] font_arr = fontString.split(",");
//		if (font_arr.length != 3) {
//			return Font.decode(def);
//		}
//		Font font = Font.decode(String.format("%s-%s-%s", font_arr[0], font_arr[2],
//				font_arr[1]));
//		return font.deriveFont((float) font.getSize() * PLACEHOLDER_SCALE);
//	}
//
//	/**
//	 * extracts the value font
//	 * 
//	 * @param style
//	 *            the table style
//	 * @return the value font
//	 */
//	@SuppressWarnings("unused")
//	private static Font getValueFont(Style style) {
//		String def = "COURIER-NORMAL-8";
//		String fontString = style.getValueFont();
//		String[] font_arr = fontString.split(",");
//		if (font_arr.length != 3) {
//			return Font.decode(def);
//		}
//		Font font = Font.decode(String.format("%s-%s-%s", font_arr[0], font_arr[2],
//				font_arr[1]));
//		return font.deriveFont((float) font.getSize() * PLACEHOLDER_SCALE);
//	}
//
//	private static String[] getLines(String text, int width, FontMetrics fmetric, int padding) {
//		String currentline = text;
//		int averageCharWi = fmetric.charWidth('c');
//		
//		int max_line_chars = (width - padding) / (averageCharWi);
//		ArrayList<String> lines = new ArrayList<String>();
//		
//		while(currentline.length() > max_line_chars) {
//			int cutidx = currentline.substring(0, max_line_chars).lastIndexOf(' ');
//			if(cutidx < 1)  {
//				cutidx = max_line_chars - 1;
//			} else {
//				cutidx++;
//			}
//			String tmpLine = currentline.substring(0, cutidx);
//			lines.add(tmpLine);
//			currentline = currentline.substring(cutidx);
//		}
//		lines.add(currentline);
//		
//		
//		String[] arrline = new String[lines.size()];
//		for(int i = 0; i < lines.size(); i++) {
//			arrline[i] = lines.get(i);
//		}
//		
//		//log.debug(text + " needs " + lines.size() + " lines");
//		
//		return arrline;
//	}
//	
//	@SuppressWarnings("rawtypes")
//	private float[] getTableHeights(Table table, Style parentstyle, float height, int width, Graphics g) {
//		ArrayList rows = table.getRows();
//		float[] sizes = new float[rows.size()];
//		Style style = parentstyle;
//		if (table.getStyle() != null) {
//			style = table.getStyle(); 
//		}
//		Font font = PdfAsSignatureParameter.getFont(style);
//		g.setFont(font);
//		
//		float total_height = this.getTableHeight(table, parentstyle, width, g);
//
//		float perUnit = height / total_height;
//		
//		this.perUnitHeight = perUnit;
//
//		float[] colWidths = table.getColsRelativeWith();
//		float sum = 0;
//
//		for (int i = 0; i < colWidths.length; i++) {
//			sum += colWidths[i];
//		}
//
//		float perUnitWidth = width / sum;		
//
//		for (int i = 0; i < rows.size(); i++) {
//			Object robj = rows.get(i);
//			ArrayList cols = (ArrayList) robj;
//			float tsize = 0;
//			float rsize = 0;
//			for (int j = 0; j < cols.size(); j++) {
//				Entry entry = (Entry) cols.get(j);
//				if (entry.getType() == 0 || entry.getType() == 1) {
//					int colWidth = (int) (colWidths[j] * perUnitWidth);
//					
//					float trsize = getLines(entry.getValue().toString(), colWidth, g.getFontMetrics(), (int) style.getPadding()  * PLACEHOLDER_SCALE).length * g.getFontMetrics().getHeight() + (style.getPadding()  * PLACEHOLDER_SCALE * 2);
//					
//					if (rsize < trsize) {
//						rsize = trsize;
//					}
//				} else if (entry.getType() == 3) {
//					
//					int colWidth = (int) (colWidths[j] * perUnitWidth);
//					
//					tsize = this
//							.getTableHeight((Table) entry.getValue(), style, colWidth, g);
//					if (rsize < tsize) {
//						rsize = tsize;
//					}
//				}
//			}
//			sizes[i] = perUnit * rsize;
//		}
//
//		return sizes;
//	}
//
//	@SuppressWarnings("rawtypes")
//	private float getTableHeight(Table table, Style parentstyle, int width, Graphics g) {
//		ArrayList rows = table.getRows();
//		Style style = parentstyle;
//		if (table.getStyle() != null) {
//			style = table.getStyle();
//		}
//		float size = 0;
//		
//		float[] colWidths = table.getColsRelativeWith();
//		float sum = 0;
//
//		for (int i = 0; i < colWidths.length; i++) {
//			sum += colWidths[i];
//		}
//
//		float perUnitWidth = width / sum;
//		
//		for (int i = 0; i < rows.size(); i++) {
//			Object robj = rows.get(i);
//			ArrayList cols = (ArrayList) robj;
//			float tsize = 0;
//			float rsize = 0;
//			for (int j = 0; j < cols.size(); j++) {
//				Entry entry = (Entry) cols.get(j);
//				if (entry.getType() == 0 || entry.getType() == 1) {
//					int colWidth = (int) (colWidths[j] * perUnitWidth);
//					
//					float trsize = getLines(entry.getValue().toString(), colWidth, g.getFontMetrics(), (int) style.getPadding()  * PLACEHOLDER_SCALE).length * g.getFontMetrics().getHeight() + (style.getPadding()  * PLACEHOLDER_SCALE * 2);
//					
//					if (rsize < trsize) {
//						rsize = trsize;
//					}
//					
//					/*if (rsize < ((style.getPadding() * PLACEHOLDER_SCALE * 2) + fontSize)) {
//						rsize = ((style.getPadding() * PLACEHOLDER_SCALE * 2) + fontSize);
//					}*/
//				} else if (entry.getType() == 3) {
//					int colWidth = (int) (colWidths[j] * perUnitWidth);
//					tsize = this
//							.getTableHeight((Table) entry.getValue(), style, colWidth, g);
//					if (rsize < tsize) {
//						rsize = tsize;
//					}
//				}
//			}
//			size += rsize;
//		}
//
//		return size;
//	}
}
