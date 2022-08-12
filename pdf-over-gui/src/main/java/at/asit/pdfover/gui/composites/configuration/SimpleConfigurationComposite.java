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
package at.asit.pdfover.gui.composites.configuration;

// Imports
import java.io.File;
import java.util.Arrays;
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.commons.Profile;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.controls.ErrorMarker;
import at.asit.pdfover.gui.exceptions.InvalidEmblemFile;
import at.asit.pdfover.gui.utils.SWTUtils;
import at.asit.pdfover.gui.workflow.config.ConfigurationDataInMemory;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.Emblem;
import at.asit.pdfover.signer.pdfas.PdfAs4SignatureParameter;
import at.asit.pdfover.signer.pdfas.PdfAs4SignaturePlaceholder;

/**
 *
 */
public class SimpleConfigurationComposite extends ConfigurationCompositeBase {

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(SimpleConfigurationComposite.class);

	private Group grpHandySignatur;
	private Label lblMobileNumber;
	protected Text txtMobileNumber;
	protected ErrorMarker txtMobileNumberErrorMarker;

	private Group grpPreview;
	protected Canvas cSigPreview;
	protected Button btnClearImage;
	private Button btnBrowseLogo;

	private Group grpSignatureNote;
	private Label lblSignatureNote;
	protected Text txtSignatureNote;
	private Button btnSignatureNoteDefault;

	protected final Group grpSignatureLang;
	protected final Combo cmbSignatureLang;

	final Group grpLogoOnlyTargetSize;
	final Label lblLogoOnlyTargetSizeCurrentValue;
	/** in millimeters */
	final Scale sclLogoOnlyTargetSize;
	private void updateLogoOnlyTargetSizeCurrentValueLabel() {
		lblLogoOnlyTargetSizeCurrentValue.setText(String.format("%4.1fcm", configurationContainer.logoOnlyTargetSize / 10.0));
	}

	protected String logoFile = null;

	protected final Group grpSignatureProfile;
	protected final Combo cmbSignatureProfiles;




	/**
	 * @param parent
	 * @param style
	 * @param state
	 * @param container
	 */
	public SimpleConfigurationComposite(
			org.eclipse.swt.widgets.Composite parent, int style, State state,
			ConfigurationDataInMemory container) {
		super(parent, style, state, container);
		setLayout(new FormLayout());

		this.grpHandySignatur = new Group(this, SWT.NONE | SWT.RESIZE);
		SWTUtils.anchor(grpHandySignatur).right(100,-5).left(0,5).top(0,5);
		grpHandySignatur.setLayout(new GridLayout(2, false));
		SWTUtils.setFontHeight(grpHandySignatur, Constants.TEXT_SIZE_NORMAL);

		this.lblMobileNumber = new Label(grpHandySignatur, SWT.NONE | SWT.RESIZE);
		this.lblMobileNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		SWTUtils.setFontHeight(lblMobileNumber, Constants.TEXT_SIZE_NORMAL);

		Composite compMobileNumerContainer = new Composite(this.grpHandySignatur, SWT.NONE);
		compMobileNumerContainer.setLayout(new FormLayout());
		compMobileNumerContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		this.txtMobileNumber = new Text(compMobileNumerContainer, SWT.BORDER | SWT.RESIZE);
		SWTUtils.anchor(txtMobileNumber).left(0,5).right(100,-42).top(0);
		SWTUtils.setFontHeight(txtMobileNumber, Constants.TEXT_SIZE_NORMAL);

		this.txtMobileNumberErrorMarker = new ErrorMarker(compMobileNumerContainer, SWT.NONE, "");
		this.txtMobileNumberErrorMarker.setVisible(false);
		SWTUtils.anchor(txtMobileNumberErrorMarker).left(100,-32).right(100).top(0).bottom(0,32);

		this.txtMobileNumber.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				processNumberChanged();
			}
		});

		this.txtMobileNumber.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				processNumberChanged();
			}
		});

		this.grpSignatureProfile = new Group(this, SWT.NONE);
		SWTUtils.anchor(grpSignatureProfile).right(100,-5).left(0,5).top(grpHandySignatur, 5);
		this.grpSignatureProfile.setLayout(new FormLayout());
		SWTUtils.setFontHeight(grpSignatureProfile, Constants.TEXT_SIZE_NORMAL);

		this.cmbSignatureProfiles = new Combo(this.grpSignatureProfile, SWT.READ_ONLY);
		SWTUtils.anchor(cmbSignatureProfiles).left(0,10).right(100,-10).top(0,10).bottom(100,-10);
		SWTUtils.setFontHeight(cmbSignatureProfiles, Constants.TEXT_SIZE_NORMAL);
		SWTUtils.scrollPassthrough(cmbSignatureProfiles);
		this.cmbSignatureProfiles.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Profile current = SimpleConfigurationComposite.this.configurationContainer.getSignatureProfile();
				int index = SimpleConfigurationComposite.this.cmbSignatureProfiles.getSelectionIndex();
				Profile selected = Profile.values()[index];
				if (!current.equals(selected)) {
					performProfileSelectionChanged(selected);
				}
			}
		});

		this.grpSignatureLang = new Group(this, SWT.NONE);
		SWTUtils.anchor(grpSignatureLang).right(100,-5).top(grpSignatureProfile, 5).left(0,5);
		this.grpSignatureLang.setLayout(new FormLayout());
		SWTUtils.setFontHeight(grpSignatureLang, Constants.TEXT_SIZE_NORMAL);

		this.cmbSignatureLang = new Combo(this.grpSignatureLang, SWT.READ_ONLY);
		SWTUtils.anchor(cmbSignatureLang).left(0,10).right(100,-10).top(0,10).bottom(100,-10);
		SWTUtils.setFontHeight(cmbSignatureLang, Constants.TEXT_SIZE_NORMAL);
		SWTUtils.scrollPassthrough(cmbSignatureLang);
		this.cmbSignatureLang.setItems(Arrays.stream(Constants.SUPPORTED_LOCALES).map(l -> l.getDisplayLanguage()).toArray(String[]::new));

		this.cmbSignatureLang.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Locale currentLocale = SimpleConfigurationComposite.this.configurationContainer.signatureLocale;
				Locale selectedLocale = Constants.SUPPORTED_LOCALES[SimpleConfigurationComposite.this.cmbSignatureLang.getSelectionIndex()];
				if (!currentLocale.equals(selectedLocale)) {
					performSignatureLangSelectionChanged(selectedLocale, currentLocale);
				}
			}
		});

		this.grpSignatureNote = new Group(this, SWT.NONE);
		SWTUtils.anchor(grpSignatureNote).right(100,-5).top(grpSignatureLang,5).left(0,5);
		this.grpSignatureNote.setLayout(new GridLayout(2, false));
		SWTUtils.setFontHeight(grpSignatureNote, Constants.TEXT_SIZE_NORMAL);

		this.lblSignatureNote = new Label(this.grpSignatureNote, SWT.NONE);
		do { /* grid positioning */
			GridData gd_lblSignatureNote = new GridData(SWT.LEFT, SWT.CENTER,
					false, false, 1, 1);
			gd_lblSignatureNote.widthHint = 66;
			this.lblSignatureNote.setLayoutData(gd_lblSignatureNote);
			this.lblSignatureNote.setBounds(0, 0, 57, 15);
		} while (false);
		SWTUtils.setFontHeight(lblSignatureNote, Constants.TEXT_SIZE_NORMAL);

		Composite compSignatureNoteContainer = new Composite(this.grpSignatureNote, SWT.NONE);
		compSignatureNoteContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		compSignatureNoteContainer.setLayout(new FormLayout());

		this.txtSignatureNote = new Text(compSignatureNoteContainer, SWT.BORDER);
		SWTUtils.anchor(txtSignatureNote).top(0,0).left(0,5).right(100,-42);
		SWTUtils.setFontHeight(txtSignatureNote, Constants.TEXT_SIZE_NORMAL);

		this.txtSignatureNote.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				processSignatureNoteChanged();
			}
		});

		this.txtSignatureNote.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				processSignatureNoteChanged();
			}
		});

		Composite compSignatureNoteButtonContainer = new Composite(this.grpSignatureNote, SWT.NONE);
		compSignatureNoteButtonContainer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
		compSignatureNoteButtonContainer.setLayout(new FormLayout());

		this.btnSignatureNoteDefault = new Button(compSignatureNoteButtonContainer, SWT.NONE);
		SWTUtils.anchor(btnSignatureNoteDefault).top(0,0).right(100,-42);
		SWTUtils.setFontHeight(btnSignatureNoteDefault, Constants.TEXT_SIZE_BUTTON);
		this.btnSignatureNoteDefault.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SimpleConfigurationComposite.this.txtSignatureNote.setText(getDefaultSignatureBlockNoteTextFor(null, null));
			}
		});

		this.grpLogoOnlyTargetSize = new Group(this, SWT.NONE);
		SWTUtils.anchor(grpLogoOnlyTargetSize).left(0,5).right(100,-5).top(grpSignatureProfile,5);
		this.grpLogoOnlyTargetSize.setLayout(new FormLayout());
		SWTUtils.setFontHeight(grpLogoOnlyTargetSize, Constants.TEXT_SIZE_NORMAL);

		Label lblLOTSLeft = new Label(this.grpLogoOnlyTargetSize, SWT.HORIZONTAL);
		SWTUtils.anchor(lblLOTSLeft).top(0, 5).left(0, 15);
		lblLOTSLeft.setText("1cm");
		SWTUtils.setFontHeight(lblLOTSLeft, Constants.TEXT_SIZE_NORMAL);

		Label lblLOTSRight = new Label(this.grpLogoOnlyTargetSize, SWT.HORIZONTAL);
		SWTUtils.anchor(lblLOTSRight).top(0,5).right(100, -5);
		lblLOTSRight.setText("20cm");
		SWTUtils.setFontHeight(lblLOTSRight, Constants.TEXT_SIZE_NORMAL);

		this.sclLogoOnlyTargetSize = new Scale(this.grpLogoOnlyTargetSize, SWT.HORIZONTAL);
		SWTUtils.anchor(sclLogoOnlyTargetSize).top(0,5).left(lblLOTSLeft,5).right(lblLOTSRight,-5);
		sclLogoOnlyTargetSize.setMinimum(10);
		sclLogoOnlyTargetSize.setMaximum(200);
		sclLogoOnlyTargetSize.setIncrement(1);
		sclLogoOnlyTargetSize.setPageIncrement(1);
		SWTUtils.scrollPassthrough(sclLogoOnlyTargetSize);
		SWTUtils.addSelectionListener(sclLogoOnlyTargetSize, e ->
		{
			configurationContainer.logoOnlyTargetSize = sclLogoOnlyTargetSize.getSelection();
			this.updateLogoOnlyTargetSizeCurrentValueLabel();
		});
		sclLogoOnlyTargetSize.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) { signatureBlockPreviewChanged(); };
		});

		this.lblLogoOnlyTargetSizeCurrentValue = new Label(this.grpLogoOnlyTargetSize, SWT.HORIZONTAL);
		SWTUtils.anchor(lblLogoOnlyTargetSizeCurrentValue).top(sclLogoOnlyTargetSize, 5).left(0,5).right(100,-5);
		lblLogoOnlyTargetSizeCurrentValue.setAlignment(SWT.CENTER);
		SWTUtils.setFontHeight(lblLogoOnlyTargetSizeCurrentValue, Constants.TEXT_SIZE_NORMAL);

		this.grpPreview = new Group(this, SWT.NONE);
		SWTUtils.anchor(grpPreview).left(0,5).right(100,-5).top(grpSignatureNote, 5).height(250);
		this.grpPreview.setLayout(new FormLayout());
		SWTUtils.setFontHeight(grpPreview, Constants.TEXT_SIZE_NORMAL);

		Composite containerComposite = new Composite(this.grpPreview, SWT.NONE);
		SWTUtils.anchor(containerComposite).left(0).right(100).top(0).bottom(100);
		containerComposite.setLayout(new FormLayout());

		this.btnBrowseLogo = new Button(containerComposite, SWT.NONE);
		SWTUtils.anchor(btnBrowseLogo).top(0,5).right(50,-5);
		SWTUtils.setFontHeight(btnBrowseLogo, Constants.TEXT_SIZE_BUTTON);

		this.btnClearImage = new Button(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(btnClearImage).top(0,5).left(50, 5);
		SWTUtils.setFontHeight(btnClearImage, Constants.TEXT_SIZE_BUTTON);
		this.btnClearImage.setEnabled(false);

		this.cSigPreview = new Canvas(containerComposite, SWT.RESIZE);
		SWTUtils.anchor(cSigPreview).left(0, 5).right(100,-5).top(btnBrowseLogo,5).bottom(100,-5);
		SWTUtils.setFontHeight(cSigPreview, Constants.TEXT_SIZE_NORMAL);
		this.cSigPreview.addPaintListener(e -> SimpleConfigurationComposite.this.paintSignaturePreview(e));

		DropTarget dnd_target = new DropTarget(containerComposite, DND.DROP_DEFAULT | DND.DROP_COPY);
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
							log.error("File: {} does not exist!", files[0]); ////
							return;
						}
						performProfileSelectionChanged(Profile.BASE_LOGO);
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
		this.btnBrowseLogo.addSelectionListener(new ImageFileBrowser());

		this.cSigPreview.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				SimpleConfigurationComposite.this.forceFocus();
			}
		});

		// Load localized strings
		reloadResources();
	}

	private PdfAs4SignatureParameter sigPreviewParam = null;
	private Image sigPreview = null;
	void paintSignaturePreview(PaintEvent evt) {
		if (this.sigPreview == null)
			return;
		Rectangle r = this.sigPreview.getBounds();
		int srcW = r.width;
		int srcH = r.height;
		Point p = ((Canvas)evt.widget).getSize();
		float dstW = p.x;
		float dstH = p.y;

		float scale = dstW / srcW;
		if (srcH * scale > dstH)
			scale = dstH / srcH;

		float w = srcW * scale;
		float h = srcH * scale;

		int x = (int) ((dstW / 2) - (w / 2));
		int y = (int) ((dstH / 2) - (h / 2));
		evt.gc.drawImage(this.sigPreview, 0, 0, srcW, srcH, x, y, (int) w, (int) h);
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
					"*.jpg;*.png;*.gif", "*.jpg", "*.png", "*.gif", "*" });
			dialog.setFilterNames(new String[] {
					Messages.getString("common.ImageExtension_Description"),
					Messages.getString("common.JPGExtension_Description"),
					Messages.getString("common.PNGExtension_Description"),
					Messages.getString("common.GIFExtension_Description"),
					Messages.getString("common.AllExtension_Description") });
			String fileName = dialog.open();
			File file = null;
			if (fileName != null) {
				file = new File(fileName);
				if (file.exists()) {
					processEmblemChanged(fileName);
				}
			}
		}
	}

	private void setEmblemFileInternal(final String filename, boolean force)
			throws Exception {
		if (!force && this.configurationContainer.getEmblemPath() != null) {
			if (this.configurationContainer.getEmblemPath().equals(filename)) {
				return; // Ignore ...
			}
		}

		this.configurationContainer.setEmblem(filename);
		this.btnClearImage.setEnabled(filename != null);
		this.signatureBlockPreviewChanged();
		this.doLayout();
	}

	void signatureBlockPreviewChanged() {
		try {
			PdfAs4SignatureParameter param = new PdfAs4SignatureParameter();
			param.signatureProfile = this.configurationContainer.getSignatureProfile();
			if(this.configurationContainer.signatureNote != null && !this.configurationContainer.signatureNote.isEmpty()) {
				param.signatureNote = this.configurationContainer.signatureNote;
			}

			param.signatureLanguage = this.configurationContainer.signatureLocale.getLanguage();
			param.enablePDFACompat = this.configurationContainer.signaturePDFACompat;
			param.targetLogoSize = Math.min(120.0, this.configurationContainer.logoOnlyTargetSize); // TODO WORKAROUND FOR #117
			String image = this.configurationContainer.getEmblemPath();
			if (image != null && !image.trim().isEmpty()) {
				param.emblem = new Emblem(image);
			}

			this.sigPreviewParam = param;
			PdfAs4SignaturePlaceholder.For(param, (p) -> {
				if (this.isDisposed())
					return;

				this.getDisplay().syncExec(() -> {
					if (this.isDisposed())
						return;
					if (this.sigPreviewParam != param)
						return;
					if (this.sigPreview != null)
						this.sigPreview.dispose();
					this.sigPreview = new Image(this.getDisplay(), p.getSWTImage());
					this.cSigPreview.redraw();
				});
			});
		} catch (Exception e) {
			log.error("Failed to load image for display...", e);
		}
	}

	void processEmblemChanged(String filename) {
		try {
			setEmblemFileInternal(filename, false);
		} catch (Exception ex) {
			log.error("processEmblemChanged: ", ex);
			ErrorDialog dialog = new ErrorDialog(
					getShell(),
					Messages.getString("error.FailedToLoadEmblem"), BUTTONS.OK);
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
					.getString("error.InvalidPhoneNumber"));
			log.error("processNumberChanged: ", ex);
			this.redraw();
			this.doLayout();
		}
	}

	int getLocaleElementIndex(Locale locale) {
		for (int i = 0; i < Constants.SUPPORTED_LOCALES.length; i++) {
			if (Constants.SUPPORTED_LOCALES[i].equals(locale)) {
				log.debug("Locale: {} IDX: {}",locale, i);
				return i;
			}
		}

		log.warn("NO Locale match for {}", locale);
		return 0;
	}

	void performSignatureLangSelectionChanged(Locale selected, Locale previous) {
		log.debug("Selected Sign Locale: {}", selected);
		this.configurationContainer.signatureLocale = selected;
		this.cmbSignatureLang.select(this.getLocaleElementIndex(selected));

		if ((previous != null) && (txtSignatureNote.getText().equals(getDefaultSignatureBlockNoteTextFor(null, previous))))
			txtSignatureNote.setText(getDefaultSignatureBlockNoteTextFor(null, selected));
		
		signatureBlockPreviewChanged();
	}



    void performProfileSelectionChanged(Profile newProfile) {
		log.debug("Signature Profile {} was selected", newProfile.name());
		Profile oldProfile = this.configurationContainer.getSignatureProfile();
    	this.configurationContainer.setSignatureProfile(newProfile);
    	this.cmbSignatureProfiles.select(newProfile.ordinal());

    	if (newProfile.equals(Profile.AMTSSIGNATURBLOCK) || newProfile.equals(Profile.INVISIBLE)){
			this.configurationContainer.autoPositionSignature = true;
		}
		if (txtSignatureNote.getText().equals(getDefaultSignatureBlockNoteTextFor(oldProfile, null)))
			txtSignatureNote.setText(getDefaultSignatureBlockNoteTextFor(newProfile, null));

		this.grpSignatureLang.setVisible(newProfile.hasText());
		this.grpSignatureNote.setVisible(newProfile.hasText());
		this.grpLogoOnlyTargetSize.setVisible(newProfile.equals(Profile.BASE_LOGO));

		SWTUtils.reanchor(grpPreview).top(newProfile.hasText() ? grpSignatureNote : grpLogoOnlyTargetSize, 5);
		this.grpPreview.setVisible(newProfile.isVisibleSignature());
		this.grpPreview.requestLayout();
		signatureBlockPreviewChanged();
	}

	String getDefaultSignatureBlockNoteTextFor(Profile profile, Locale locale){
		if (profile == null)
			profile = configurationContainer.getSignatureProfile();
		if (locale == null)
			locale = configurationContainer.signatureLocale;
		return profile.getDefaultSignatureBlockNote(locale);
	}

	private void plainMobileNumberSetter() {
		String number = this.txtMobileNumber.getText();
		this.configurationContainer.setMobileNumber(number);
		number = this.configurationContainer.getMobileNumber();
		if (number == null) {
			this.txtMobileNumber.setText("");
			return;
		}
		this.txtMobileNumber.setText(number);
	}

	void processSignatureNoteChanged() {
		String note = this.txtSignatureNote.getText();
		this.configurationContainer.signatureNote = note;
		signatureBlockPreviewChanged();
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

	@Override
	public void initConfiguration(ConfigurationManager provider) {
		this.configurationContainer.setMobileNumber(provider.getDefaultMobileNumberPersistent());

		try {
			this.configurationContainer.setEmblem(provider.getDefaultEmblemPersistent());
		} catch (InvalidEmblemFile e) {
			log.error("Failed to set emblem!", e);
		}

		this.configurationContainer.signatureLocale = provider.getSignatureLocale();
		this.configurationContainer.signatureNote = provider.getSignatureNote();
		this.configurationContainer.logoOnlyTargetSize = provider.getLogoOnlyTargetSize();
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

		String emblemFile = this.configurationContainer.getEmblemPath();
		if (emblemFile != null && !emblemFile.trim().isEmpty()) {
			this.logoFile = emblemFile;
			try {
				setEmblemFileInternal(emblemFile, true);
				this.btnClearImage.setSelection(true);
			} catch (Exception e1) {
				log.error("Failed to load emblem: ", e1);
				ErrorDialog dialog = new ErrorDialog(
						getShell(),
						Messages.getString("error.FailedToLoadEmblem"), BUTTONS.OK);
				dialog.open();
			}
		}

		String note = this.configurationContainer.signatureNote;

		if (note != null) {
			this.txtSignatureNote.setText(note);
		}

		this.sclLogoOnlyTargetSize.setSelection((int)this.configurationContainer.logoOnlyTargetSize);
		this.updateLogoOnlyTargetSizeCurrentValueLabel();

		this.signatureBlockPreviewChanged();

		this.performSignatureLangSelectionChanged(this.configurationContainer.signatureLocale, null);

		this.performProfileSelectionChanged(this.configurationContainer.getSignatureProfile());

	}

	@Override
	public void storeConfiguration(ConfigurationManager store) {
		store.setDefaultMobileNumberPersistent(this.configurationContainer.getMobileNumber());
		store.setDefaultEmblemPersistent(this.configurationContainer.getEmblemPath());
		store.setSignatureLocalePersistent(this.configurationContainer.signatureLocale);
		store.setSignatureNotePersistent(this.configurationContainer.signatureNote);
		store.setSignatureProfilePersistent(this.configurationContainer.getSignatureProfile());
		store.setLogoOnlyTargetSizePersistent(this.configurationContainer.logoOnlyTargetSize);
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
				break;
			default:
				break;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		SWTUtils.setLocalizedText(grpHandySignatur, "simple_config.MobileBKU_Title");
		SWTUtils.setLocalizedText(lblMobileNumber, "simple_config.PhoneNumber");
		SWTUtils.setLocalizedToolTipText(txtMobileNumber, "simple_config.ExampleNumber_ToolTip");
		this.txtMobileNumber.setMessage(Messages.getString("simple_config.ExampleNumber"));

		SWTUtils.setLocalizedText(grpPreview, "simple_config.Preview_Title");
		SWTUtils.setLocalizedText(btnClearImage, "simple_config.ClearEmblem");
		SWTUtils.setLocalizedText(btnBrowseLogo, "simple_config.ReplaceEmblem");
		SWTUtils.setLocalizedText(grpSignatureNote, "simple_config.Note_Title");
		SWTUtils.setLocalizedText(lblSignatureNote, "simple_config.Note");
		SWTUtils.setLocalizedToolTipText(txtSignatureNote, "simple_config.Note_Tooltip");
		SWTUtils.setLocalizedText(btnSignatureNoteDefault, "simple_config.Note_SetDefault");

		SWTUtils.setLocalizedText(grpSignatureLang, "simple_config.SigBlockLang_Title");
		SWTUtils.setLocalizedToolTipText(cmbSignatureLang, "simple_config.SigBlockLang_ToolTip");

		SWTUtils.setLocalizedText(grpSignatureProfile, "simple_config.SigProfile_Title");
		this.cmbSignatureProfiles.setItems(Arrays.stream(Profile.values()).map(v -> Messages.getString("simple_config."+v.name())).toArray(String[]::new));

		SWTUtils.setLocalizedText(grpLogoOnlyTargetSize, "simple_config.LogoOnlyTargetSize_Title");
	}
}
