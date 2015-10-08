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
import java.io.File;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.controls.ErrorMarker;
import at.asit.pdfover.gui.exceptions.InvalidEmblemFile;
import at.asit.pdfover.gui.exceptions.InvalidNumberException;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.utils.SignaturePlaceholderCache;
import at.asit.pdfover.gui.workflow.config.ConfigManipulator;
import at.asit.pdfover.gui.workflow.config.ConfigurationContainer;
import at.asit.pdfover.gui.workflow.config.PersistentConfigProvider;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.FileNameEmblem;
import at.asit.pdfover.signator.SignatureParameter;

/**
 * 
 */
public class SimpleConfigurationComposite extends BaseConfigurationComposite {

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(SimpleConfigurationComposite.class);

	ConfigurationComposite configurationComposite;

	private Group grpHandySignatur;
	private Label lblMobileNumber;
	Text txtMobileNumber;
	FormData fd_txtMobileNumber;
	ErrorMarker txtMobileNumberErrorMarker;
	FormData fd_txtMobileNumberErrorMarker;

	private Group grpLogo;
	private Canvas cLogo;
	private Label lblDropLogo;
	Button btnClearImage;
	private Button btnBrowseLogo;
	Canvas cSigPreview;

	private Group grpSignatureNote;
	private Label lblSignatureNote;
	Text txtSignatureNote;
	private Button btnSignatureNoteDefault;

	private Group grpSignatureLang;
	Combo cmbSignatureLang;

	String logoFile;
	Image sigPreview = null;
	Image logo = null;

	/**
	 * @param parent
	 * @param style
	 * @param state
	 * @param container
	 */
	public SimpleConfigurationComposite(
			org.eclipse.swt.widgets.Composite parent, int style, State state,
			ConfigurationContainer container) {
		super(parent, style, state, container);
		setLayout(new FormLayout());

		this.grpHandySignatur = new Group(this, SWT.NONE | SWT.RESIZE);
		FormData fd_grpHandySignatur = new FormData();
		fd_grpHandySignatur.right = new FormAttachment(100, -5);
		fd_grpHandySignatur.left = new FormAttachment(0, 5);
		fd_grpHandySignatur.top = new FormAttachment(0, 5);
		this.grpHandySignatur.setLayoutData(fd_grpHandySignatur);
		this.grpHandySignatur.setLayout(new GridLayout(2, false));

		FontData[] fD_grpHandySignatur = this.grpHandySignatur.getFont()
				.getFontData();
		fD_grpHandySignatur[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpHandySignatur.setFont(new Font(Display.getCurrent(),
				fD_grpHandySignatur[0]));

		this.lblMobileNumber = new Label(this.grpHandySignatur, SWT.NONE
				| SWT.RESIZE);
		this.lblMobileNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				false, false, 1, 1));

		FontData[] fD_lblMobileNumber = this.lblMobileNumber.getFont()
				.getFontData();
		fD_lblMobileNumber[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblMobileNumber.setFont(new Font(Display.getCurrent(),
				fD_lblMobileNumber[0]));

		Composite compMobileNumerContainer = new Composite(this.grpHandySignatur, SWT.NONE);
		compMobileNumerContainer.setLayout(new FormLayout());
		compMobileNumerContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));

		this.txtMobileNumber = new Text(compMobileNumerContainer, SWT.BORDER | SWT.RESIZE);
		this.fd_txtMobileNumber = new FormData();
		this.fd_txtMobileNumber.left = new FormAttachment(0, 5);
		this.fd_txtMobileNumber.right = new FormAttachment(100, -42);
		this.fd_txtMobileNumber.top = new FormAttachment(0);
		this.txtMobileNumber.setLayoutData(this.fd_txtMobileNumber);


		this.txtMobileNumberErrorMarker = new ErrorMarker(compMobileNumerContainer,
				SWT.NONE, ""); //$NON-NLS-1$
		this.txtMobileNumberErrorMarker.setVisible(false);
		this.fd_txtMobileNumberErrorMarker = new FormData();
		this.fd_txtMobileNumberErrorMarker.left = new FormAttachment(100, -32);
		this.fd_txtMobileNumberErrorMarker.right = new FormAttachment(100);
		this.fd_txtMobileNumberErrorMarker.top = new FormAttachment(0);
		this.fd_txtMobileNumberErrorMarker.bottom = new FormAttachment(0, 32);
		this.txtMobileNumberErrorMarker
				.setLayoutData(this.fd_txtMobileNumberErrorMarker);

		FontData[] fD_txtMobileNumber = this.txtMobileNumber.getFont()
				.getFontData();
		fD_txtMobileNumber[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.txtMobileNumber.setFont(new Font(Display.getCurrent(),
				fD_txtMobileNumber[0]));

		this.txtMobileNumber.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					processNumberChanged();
				}
			}
		});

		this.txtMobileNumber.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				processNumberChanged();
			}
		});

		this.grpLogo = new Group(this, SWT.NONE);
		FormData fd_grpBildmarke = new FormData();
		fd_grpBildmarke.left = new FormAttachment(0, 5);
		fd_grpBildmarke.right = new FormAttachment(100, -5);
		fd_grpBildmarke.top = new FormAttachment(this.grpHandySignatur, 5);
		this.grpLogo.setLayoutData(fd_grpBildmarke);
		this.grpLogo.setLayout(new FormLayout());

		FontData[] fD_grpBildmarke = this.grpLogo.getFont().getFontData();
		fD_grpBildmarke[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpLogo.setFont(new Font(Display.getCurrent(),
				fD_grpBildmarke[0]));

		Composite containerComposite = new Composite(this.grpLogo,
				SWT.NONE);
		containerComposite.setLayout(new FormLayout());
		FormData fd_containerComposite = new FormData();
		fd_containerComposite.left = new FormAttachment(0);
		fd_containerComposite.right = new FormAttachment(100);
		fd_containerComposite.top = new FormAttachment(0);
		fd_containerComposite.bottom = new FormAttachment(100);
		containerComposite.setLayoutData(fd_containerComposite);

		final Composite controlComposite = new Composite(containerComposite, SWT.NONE);
		controlComposite.setLayout(new FormLayout());
		FormData fd_controlComposite = new FormData();
		fd_controlComposite.left = new FormAttachment(0, 20);
		fd_controlComposite.right = new FormAttachment(0, 300);
		fd_controlComposite.top = new FormAttachment(0, 20);
		fd_controlComposite.bottom = new FormAttachment(100, -20);
		controlComposite.setLayoutData(fd_controlComposite);
		controlComposite.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				e.gc.setForeground(Constants.DROP_BORDER_COLOR);
				e.gc.setLineWidth(3);
				e.gc.setLineStyle(SWT.LINE_DASH);
				Point size = controlComposite.getSize();
				e.gc.drawRoundRectangle(0, 0, size.x - 2, size.y - 2,
						10, 10);
			}
		});

		this.cSigPreview = new Canvas(containerComposite, SWT.RESIZE);

		this.btnBrowseLogo = new Button(controlComposite, SWT.NONE);

		this.lblDropLogo = new Label(controlComposite, SWT.NATIVE | SWT.CENTER);

		this.cLogo = new Canvas(controlComposite, SWT.NONE);
		FormData fd_cLogo = new FormData();
		fd_cLogo.left = new FormAttachment(0, 20);
		fd_cLogo.right = new FormAttachment(100, -20);
		fd_cLogo.top = new FormAttachment(0, 20);
		fd_cLogo.bottom = new FormAttachment(this.lblDropLogo, -20);
		fd_cLogo.height = 40;
		fd_cLogo.width = 40;
		this.cLogo.setLayoutData(fd_cLogo);
		this.cLogo.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				imagePaintControl(e, SimpleConfigurationComposite.this.logo);
			}
		});

		this.btnClearImage = new Button(controlComposite, SWT.NATIVE);

		FormData fd_lbl_drop = new FormData();
		fd_lbl_drop.left = new FormAttachment(0, 20);
		fd_lbl_drop.right = new FormAttachment(100, -20);
		// fd_lbl_drop.top = new FormAttachment(50, -20);
		fd_lbl_drop.bottom = new FormAttachment(this.btnBrowseLogo, -20);

		this.lblDropLogo.setLayoutData(fd_lbl_drop);

		FormData fd_cSigPreview = new FormData();
		fd_cSigPreview.left = new FormAttachment(controlComposite, 20);
		fd_cSigPreview.right = new FormAttachment(100, -20);
		fd_cSigPreview.top = new FormAttachment(0, 20);
		fd_cSigPreview.bottom = new FormAttachment(100, -20);

		this.cSigPreview.setLayoutData(fd_cSigPreview);
		this.cSigPreview.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				imagePaintControl(e, SimpleConfigurationComposite.this.sigPreview);
			}
		});

		FontData[] fD_cSigPreview = this.cSigPreview.getFont().getFontData();
		fD_cSigPreview[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.cSigPreview.setFont(new Font(Display.getCurrent(), fD_cSigPreview[0]));

		DropTarget dnd_target = new DropTarget(controlComposite,
				DND.DROP_DEFAULT | DND.DROP_COPY);
		final FileTransfer fileTransfer = FileTransfer.getInstance();
		Transfer[] types = new Transfer[] { fileTransfer };
		dnd_target.setTransfer(types);

		dnd_target.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				if (fileTransfer.isSupportedType(event.currentDataType)) {
					String[] files = (String[]) event.data;
					if (files.length > 0) {
						// Only taking first file ...
						File file = new File(files[0]);
						if (!file.exists()) {
							log.error("File: " + files[0] + " does not exist!"); //$NON-NLS-1$//$NON-NLS-2$
							return;
						}
						processEmblemChanged(file.getAbsolutePath());
					}
				}
			}

			@Override
			public void dragOperationChanged(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_COPY;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
			}

			@Override
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_COPY;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
				// Only drop one item!
				if (event.dataTypes.length > 1) {
					event.detail = DND.DROP_NONE;
					return;
				}
				// will accept text but prefer to have files dropped
				for (int i = 0; i < event.dataTypes.length; i++) {
					if (fileTransfer.isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
						// files should only be copied
						if (event.detail != DND.DROP_COPY) {
							event.detail = DND.DROP_NONE;
						}
						break;
					}
				}
			}
		});

		this.btnClearImage.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				SimpleConfigurationComposite.this.processEmblemChanged(null);
			}
		});

		FontData[] fD_btnUseImage = this.btnClearImage.getFont().getFontData();
		fD_btnUseImage[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnClearImage.setFont(new Font(Display.getCurrent(),
				fD_btnUseImage[0]));

		FormData fd_btnUseImage = new FormData();

		fd_btnUseImage.bottom = new FormAttachment(100, -20);
		fd_btnUseImage.right = new FormAttachment(this.btnBrowseLogo, -10);

		this.btnClearImage.setLayoutData(fd_btnUseImage);

		FormData fd_btnBrowseLogo = new FormData();

		fd_btnBrowseLogo.bottom = new FormAttachment(100, -20);
		fd_btnBrowseLogo.right = new FormAttachment(100, -20);

		this.btnBrowseLogo.setLayoutData(fd_btnBrowseLogo);
		this.btnBrowseLogo.addSelectionListener(new ImageFileBrowser());

		FontData[] fD_btnBrowseLogo = this.btnBrowseLogo.getFont()
				.getFontData();
		fD_btnBrowseLogo[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnBrowseLogo.setFont(new Font(Display.getCurrent(),
				fD_btnBrowseLogo[0]));


		this.grpSignatureLang = new Group(this, SWT.NONE);
		FormData fd_grpSignatureLang = new FormData();
		fd_grpSignatureLang.right = new FormAttachment(100, -5);
		fd_grpSignatureLang.top = new FormAttachment(this.grpLogo, 5);
		fd_grpSignatureLang.left = new FormAttachment(0, 5);
		this.grpSignatureLang.setLayoutData(fd_grpSignatureLang);
		this.grpSignatureLang.setLayout(new FormLayout());

		FontData[] fD_grpSignatureLang = this.grpSignatureLang.getFont()
				.getFontData();
		fD_grpSignatureLang[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpSignatureLang.setFont(new Font(Display.getCurrent(),
				fD_grpSignatureLang[0]));

		this.cmbSignatureLang = new Combo(this.grpSignatureLang, SWT.READ_ONLY);
		FormData fd_cmbSignatureLang = new FormData();
		fd_cmbSignatureLang.left = new FormAttachment(0, 10);
		fd_cmbSignatureLang.right = new FormAttachment(100, -10);
		fd_cmbSignatureLang.top = new FormAttachment(0, 10);
		fd_cmbSignatureLang.bottom = new FormAttachment(100, -10);
		this.cmbSignatureLang.setLayoutData(fd_cmbSignatureLang);

		FontData[] fD_cmbSignatureLang = this.cmbSignatureLang.getFont()
				.getFontData();
		fD_cmbSignatureLang[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.cmbSignatureLang.setFont(new Font(Display.getCurrent(),
				fD_cmbSignatureLang[0]));

		String[] localeSignStrings = new String[Constants.SUPPORTED_LOCALES.length];
		for (int i = 0; i < Constants.SUPPORTED_LOCALES.length; ++i) {
			localeSignStrings[i] = Constants.SUPPORTED_LOCALES[i].getDisplayLanguage();
		}
		this.cmbSignatureLang.setItems(localeSignStrings);
		this.cmbSignatureLang.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Locale currentLocale = SimpleConfigurationComposite.this.configurationContainer
						.getSignatureLocale();
				Locale selectedLocale = Constants.
						SUPPORTED_LOCALES[SimpleConfigurationComposite.this.cmbSignatureLang
						                  .getSelectionIndex()];
				if (!currentLocale.equals(selectedLocale)) {
					performSignatureLangSelectionChanged(selectedLocale);
				}
			}
		});


		this.grpSignatureNote = new Group(this, SWT.NONE);
		FormData fd_grpSignatureNote = new FormData();
		fd_grpSignatureNote.right = new FormAttachment(100, -5);
		fd_grpSignatureNote.top = new FormAttachment(this.grpSignatureLang, 5);
		fd_grpSignatureNote.left = new FormAttachment(0, 5);
		this.grpSignatureNote.setLayoutData(fd_grpSignatureNote);
		this.grpSignatureNote.setLayout(new GridLayout(2, false));

		FontData[] fD_grpSignatureNote = this.grpSignatureNote.getFont()
				.getFontData();
		fD_grpSignatureNote[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpSignatureNote.setFont(new Font(Display.getCurrent(),
				fD_grpSignatureNote[0]));

		this.lblSignatureNote = new Label(this.grpSignatureNote, SWT.NONE);
		GridData gd_lblSignatureNote = new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1);
		gd_lblSignatureNote.widthHint = 66;
		this.lblSignatureNote.setLayoutData(gd_lblSignatureNote);
		this.lblSignatureNote.setBounds(0, 0, 57, 15);

		FontData[] fD_lblSignatureNote = this.lblSignatureNote.getFont()
				.getFontData();
		fD_lblSignatureNote[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblSignatureNote.setFont(new Font(Display.getCurrent(),
				fD_lblSignatureNote[0]));

		Composite compSignatureNoteContainer = new Composite(this.grpSignatureNote, SWT.NONE);
		compSignatureNoteContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));
		compSignatureNoteContainer.setLayout(new FormLayout());

		this.txtSignatureNote = new Text(compSignatureNoteContainer, SWT.BORDER);
		FormData fd_txtSignatureNote = new FormData();
		fd_txtSignatureNote.top = new FormAttachment(0, 0);
		fd_txtSignatureNote.left = new FormAttachment(0, 5);
		fd_txtSignatureNote.right = new FormAttachment(100, -42);
		this.txtSignatureNote.setLayoutData(fd_txtSignatureNote);

		FontData[] fD_txtSignatureNote = this.txtSignatureNote.getFont()
				.getFontData();
		fD_txtSignatureNote[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.txtSignatureNote.setFont(new Font(Display.getCurrent(),
				fD_txtSignatureNote[0]));

		this.txtSignatureNote.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				processSignatureNoteChanged();
			}
		});

		this.txtSignatureNote.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					processSignatureNoteChanged();
				}
			}
		});

		Composite compSignatureNoteButtonContainer = new Composite(this.grpSignatureNote, SWT.NONE);
		compSignatureNoteButtonContainer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				false, false, 2, 1));
		compSignatureNoteButtonContainer.setLayout(new FormLayout());

		this.btnSignatureNoteDefault = new Button(compSignatureNoteButtonContainer, SWT.NONE);
		FormData fd_btnSignatureNoteDefault = new FormData();
		fd_btnSignatureNoteDefault.top = new FormAttachment(0, 0);
		fd_btnSignatureNoteDefault.right = new FormAttachment(100, -42);
		this.btnSignatureNoteDefault.setLayoutData(fd_btnSignatureNoteDefault);
		FontData[] fD_btnSignatureNoteDefault = this.btnSignatureNoteDefault.getFont()
				.getFontData();
		fD_btnSignatureNoteDefault[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnSignatureNoteDefault.setFont(new Font(Display.getCurrent(),
				fD_btnSignatureNoteDefault[0]));
		this.btnSignatureNoteDefault.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SimpleConfigurationComposite.this.txtSignatureNote.setText(
						Messages.getString("simple_config.Note_Default", //$NON-NLS-1$
						SimpleConfigurationComposite.this.configurationContainer.getSignatureLocale()));
			}
		});

		// Load localized strings
		reloadResources();
	}

	static void imagePaintControl(PaintEvent e, Image i) {
		if (i == null)
			return;

		Rectangle r = i.getBounds();
		int srcW = r.width;
		int srcH = r.height;
		Point p = ((Control) e.widget).getSize();
		float dstW = p.x;
		float dstH = p.y;

		float scale = dstW / srcW;
		if (srcH * scale > dstH)
			scale = dstH / srcH;

		float w = srcW * scale;
		float h = srcH * scale;

		int x = (int) ((dstW / 2) - (w / 2));
		int y = (int) ((dstH / 2) - (h / 2));
		e.gc.drawImage(i, 0, 0, srcW, srcH, x, y, (int) w, (int) h);
	}

	/**
	 * 
	 */
	private final class ImageFileBrowser extends SelectionAdapter {
		/**
		 * 
		 */
		public ImageFileBrowser() {
			// Nothing to do
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			FileDialog dialog = new FileDialog(
					SimpleConfigurationComposite.this.getShell(), SWT.OPEN);
			dialog.setFilterExtensions(new String[] {
					"*.jpg;*.png;*.gif", "*.jpg", "*.png", "*.gif", "*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			dialog.setFilterNames(new String[] {
					Messages.getString("common.ImageExtension_Description"), //$NON-NLS-1$
					Messages.getString("common.JPGExtension_Description"), //$NON-NLS-1$
					Messages.getString("common.PNGExtension_Description"), //$NON-NLS-1$
					Messages.getString("common.GIFExtension_Description"), //$NON-NLS-1$
					Messages.getString("common.AllExtension_Description") }); //$NON-NLS-1$
			String fileName = dialog.open();
			File file = null;
			if (fileName != null) {
				file = new File(fileName);
				if (file.exists()) {
					/*
					 * SimpleConfigurationComposite.this.txtEmblemFile
					 * .setText(fileName);
					 */
					processEmblemChanged(fileName);
				}
			}
		}
	}

	private void setEmblemFile(final String filename) throws Exception {
		setEmblemFileInternal(filename, false);
	}

	private void setEmblemFileInternal(final String filename, boolean force)
			throws Exception {
		if (!force) {
			if (this.configurationContainer.getEmblem() != null) {
				if (this.configurationContainer.getEmblem().equals(filename)) {
					return; // Ignore ...
				}
			}
		}

		this.configurationContainer.setEmblem(filename);
		this.setVisibleImage();
		this.doLayout();
	}

	void setVisibleImage() {
		String image = this.configurationContainer.getEmblem();
		ImageData img = null;
		ImageData logo = null;

		try {
			if (this.signer != null) {
				SignatureParameter param = this.signer.getPDFSigner()
						.newParameter();
				if(this.configurationContainer.getSignatureNote() != null && !this.configurationContainer.getSignatureNote().isEmpty()) {
					param.setProperty("SIG_NOTE", this.configurationContainer.getSignatureNote()); //$NON-NLS-1$
				}
	
				param.setSignatureLanguage(this.configurationContainer.getSignatureLocale().getLanguage());
				param.setSignaturePdfACompat(this.configurationContainer.getSignaturePdfACompat());
				if (image != null && !image.trim().isEmpty()) {
					logo = new ImageData(image);
					param.setEmblem(new FileNameEmblem(image));
				}
	
				img = SignaturePlaceholderCache.getSWTPlaceholder(param);
			}
		} catch (Exception e) {
			log.error("Failed to load image for display...", e); //$NON-NLS-1$
		}

		if (img != null) {
			this.sigPreview = new Image(this.getDisplay(), img);
		} else {
			this.sigPreview = null;
		}

		if (logo != null) {
			this.logo = new Image(this.getDisplay(), logo);
		} else {
			this.logo = null;
		}

		this.cSigPreview.redraw();
		this.cLogo.redraw();
	}

	void processEmblemChanged(String filename) {
		try {
			setEmblemFile(filename);
		} catch (Exception ex) {
			log.error("processEmblemChanged: ", ex); //$NON-NLS-1$
			ErrorDialog dialog = new ErrorDialog(
					getShell(),
					Messages.getString("error.FailedToLoadEmblem"), BUTTONS.OK); //$NON-NLS-1$
			dialog.open();
		}
	}

	void processNumberChanged() {
		try {
			this.txtMobileNumberErrorMarker.setVisible(false);
			plainMobileNumberSetter();
		} catch (Exception ex) {
			this.txtMobileNumberErrorMarker.setVisible(true);
			this.txtMobileNumberErrorMarker.setToolTipText(Messages
					.getString("error.InvalidPhoneNumber")); //$NON-NLS-1$
			log.error("processNumberChanged: ", ex); //$NON-NLS-1$
			this.redraw();
			this.doLayout();
		}
	}

	int getLocaleElementIndex(Locale locale) {
		for (int i = 0; i < Constants.SUPPORTED_LOCALES.length; i++) {
			if (Constants.SUPPORTED_LOCALES[i].equals(locale)) {
				log.debug("Locale: " + locale + " IDX: " + i); //$NON-NLS-1$ //$NON-NLS-2$
				return i;
			}
		}

		log.warn("NO Locale match for " + locale); //$NON-NLS-1$
		return 0;
	}

	void performSignatureLangSelectionChanged(Locale selected) {
		log.debug("Selected Sign Locale: " + selected); //$NON-NLS-1$
		this.configurationContainer.setSignatureLocale(selected);
		this.cmbSignatureLang.select(this.getLocaleElementIndex(selected));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#signerChanged()
	 */
	@Override
	protected void signerChanged() {
		this.setVisibleImage();
	}

	/**
	 * @throws InvalidNumberException
	 */
	private void plainMobileNumberSetter() throws InvalidNumberException {
		String number = this.txtMobileNumber.getText();
		this.configurationContainer.setMobileNumber(number);
		number = this.configurationContainer.getMobileNumber();
		if (number == null) {
			this.txtMobileNumber.setText(""); //$NON-NLS-1$
			return;
		}
		this.txtMobileNumber.setText(number);
	}

	void processSignatureNoteChanged() {
		String note = this.txtSignatureNote.getText();
		this.configurationContainer.setSignatureNote(note);
	}

	/**
	 * @return the configurationComposite
	 */
	public ConfigurationComposite getConfigurationComposite() {
		return this.configurationComposite;
	}

	/**
	 * @param configurationComposite
	 *            the configurationComposite to set
	 */
	public void setConfigurationComposite(
			ConfigurationComposite configurationComposite) {
		this.configurationComposite = configurationComposite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.composites.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		layout(true, true);
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.BaseConfigurationComposite#initConfiguration(at.asit.pdfover.gui.workflow.config.PersistentConfigProvider)
	 */
	@Override
	public void initConfiguration(PersistentConfigProvider provider) {
		try {
			this.configurationContainer.setMobileNumber(
					provider.getDefaultMobileNumberPersistent());
		} catch (InvalidNumberException e) {
			log.error("Failed to set mobile phone number!", e); //$NON-NLS-1$
		}

		try {
			this.configurationContainer.setEmblem(
					provider.getDefaultEmblemPersistent());
		} catch (InvalidEmblemFile e) {
			log.error("Failed to set emblem!", e); //$NON-NLS-1$
		}

		this.configurationContainer.setSignatureLocale(
				provider.getSignatureLocale());

		this.configurationContainer.setSignatureNote(
				provider.getSignatureNote());
	}

	/*
	 * (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.BaseConfigurationComposite#loadConfiguration
	 * ()
	 */
	@Override
	public void loadConfiguration() {
		// Initialize form fields from configuration Container
		String number = this.configurationContainer.getMobileNumber();
		if (number != null) {
			this.txtMobileNumber.setText(number);
		}

		String emblemFile = this.configurationContainer.getEmblem();
		if (emblemFile != null && !emblemFile.trim().isEmpty()) {
			// this.txtEmblemFile.setText(emblemFile);
			this.logoFile = emblemFile;
			try {
				setEmblemFileInternal(emblemFile, true);
				this.btnClearImage.setSelection(true);
			} catch (Exception e1) {
				log.error("Failed to load emblem: ", e1); //$NON-NLS-1$
				ErrorDialog dialog = new ErrorDialog(
						getShell(),
						Messages.getString("error.FailedToLoadEmblem"), BUTTONS.OK); //$NON-NLS-1$
				dialog.open();
			}
		}

		String note = this.configurationContainer.getSignatureNote();

		if (note != null) {
			this.txtSignatureNote.setText(note);
		}

		this.setVisibleImage();

		this.performSignatureLangSelectionChanged(this.configurationContainer.getSignatureLocale());
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.BaseConfigurationComposite#storeConfiguration(at.asit.pdfover.gui.workflow.config.ConfigManipulator, at.asit.pdfover.gui.workflow.config.PersistentConfigProvider)
	 */
	@Override
	public void storeConfiguration(ConfigManipulator store,
			PersistentConfigProvider provider) {
		store.setDefaultMobileNumber(this.configurationContainer.getMobileNumber());

		store.setDefaultEmblem(this.configurationContainer.getEmblem());

		store.setSignatureLocale(this.configurationContainer.getSignatureLocale());

		store.setSignatureNote(this.configurationContainer.getSignatureNote());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#validateSettings
	 * ()
	 */
	@Override
	public void validateSettings(int resumeFrom) throws Exception {
		switch (resumeFrom) {
		case 0:
			this.plainMobileNumberSetter();
			// Fall through
		case 1:
			this.processSignatureNoteChanged();
		}
		// this.plainEmblemSetter(this.emblemFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		this.grpHandySignatur.setText(Messages
				.getString("simple_config.MobileBKU_Title")); //$NON-NLS-1$
		this.lblMobileNumber.setText(Messages
				.getString("simple_config.PhoneNumber")); //$NON-NLS-1$
		this.txtMobileNumber.setToolTipText(Messages
				.getString("simple_config.ExampleNumber_ToolTip")); //$NON-NLS-1$
		this.txtMobileNumber.setMessage(Messages
				.getString("simple_config.ExampleNumber")); //$NON-NLS-1$

		this.grpLogo.setText(Messages
				.getString("simple_config.Emblem_Title")); //$NON-NLS-1$
		this.lblDropLogo.setText(Messages.getString("simple_config.EmblemEmpty")); //$NON-NLS-1$
		this.btnClearImage.setText(Messages
				.getString("simple_config.ClearEmblem")); //$NON-NLS-1$
		this.btnBrowseLogo.setText(Messages.getString("common.browse")); //$NON-NLS-1$

		this.grpSignatureNote.setText(Messages
				.getString("simple_config.Note_Title")); //$NON-NLS-1$
		this.lblSignatureNote.setText(Messages.getString("simple_config.Note")); //$NON-NLS-1$
		this.txtSignatureNote.setToolTipText(Messages
				.getString("simple_config.Note_Tooltip")); //$NON-NLS-1$
		this.btnSignatureNoteDefault.setText(Messages
				.getString("simple_config.Note_SetDefault")); //$NON-NLS-1$

		this.grpSignatureLang.setText(Messages.getString("simple_config.SigBlockLang_Title")); //$NON-NLS-1$
		this.cmbSignatureLang.setToolTipText(Messages.getString("simple_config.SigBlockLang_ToolTip")); //$NON-NLS-1$
	}
}
