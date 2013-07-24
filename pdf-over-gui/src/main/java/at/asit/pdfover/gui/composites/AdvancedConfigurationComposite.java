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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.controls.ErrorMarker;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.exceptions.InvalidPortException;
import at.asit.pdfover.gui.exceptions.OutputfolderDoesntExistException;
import at.asit.pdfover.gui.exceptions.OutputfolderNotADirectoryException;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.config.ConfigurationContainer;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.BKUs;
import at.asit.pdfover.signator.SignaturePosition;

/**
 * Composite for advanced configuration
 * 
 * Contains the simple configuration composite
 */
public class AdvancedConfigurationComposite extends BaseConfigurationComposite {

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(AdvancedConfigurationComposite.class);
	Text txtOutputFolder;
	Combo cmbBKUAuswahl;
	Combo cmbLocaleAuswahl;
	String[] bkuStrings;
	Button btnAutomatischePositionierung;
	Scale sclTransparenz;
	private Group grpSignatur;
	private Group grpLocaleAuswahl;
	private Button btnBrowse;
	private Label lblDefaultOutputFolder;
	private Group grpSpeicherort;
	private Group grpBkuAuswahl;
	private Label lblTransparenzRechts;
	private Label lblTransparenzLinks;
	private Label lblTransparenz;

	private Group grpProxy;
	private Label lblProxyHost;
	private Text txtProxyHost;
	private ErrorMarker proxyHostErrorMarker;
	private Label lblProxyPort;
	Text txtProxyPort;
	FormData fd_txtProxyPort;
	ErrorMarker txtProxyPortErrorMarker;
	FormData fd_txtProxyPortErrorMarker;


	/**
	 * @param parent
	 * @param style
	 * @param state
	 * @param container
	 */
	public AdvancedConfigurationComposite(Composite parent, int style,
			State state, ConfigurationContainer container) {
		super(parent, style, state, container);
		setLayout(new FormLayout());

		this.grpSignatur = new Group(this, SWT.NONE);
		FormLayout layout = new FormLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 5;
		this.grpSignatur.setLayout(layout);
		FormData fd_grpSignatur = new FormData();
		fd_grpSignatur.top = new FormAttachment(0, 5);
		fd_grpSignatur.right = new FormAttachment(100, -5);
		fd_grpSignatur.left = new FormAttachment(0, 5);
		this.grpSignatur.setLayoutData(fd_grpSignatur);

		FontData[] fD_grpSignaturPosition = this.grpSignatur.getFont().getFontData();
		fD_grpSignaturPosition[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpSignatur.setFont(new Font(Display.getCurrent(),
				fD_grpSignaturPosition[0]));

		this.btnAutomatischePositionierung = new Button(this.grpSignatur, SWT.CHECK);
		FormData fd_btnAutomatischePositionierung = new FormData();
		fd_btnAutomatischePositionierung.right = new FormAttachment(100, -5);
		fd_btnAutomatischePositionierung.top = new FormAttachment(0);
		fd_btnAutomatischePositionierung.left = new FormAttachment(0, 5);
		this.btnAutomatischePositionierung
				.setLayoutData(fd_btnAutomatischePositionierung);

		FontData[] fD_btnAutomatischePositionierung = this.btnAutomatischePositionierung
				.getFont().getFontData();
		fD_btnAutomatischePositionierung[0]
				.setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnAutomatischePositionierung.setFont(new Font(Display
				.getCurrent(), fD_btnAutomatischePositionierung[0]));

		this.btnAutomatischePositionierung
				.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						AdvancedConfigurationComposite.this
								.performPositionSelection(AdvancedConfigurationComposite.this.btnAutomatischePositionierung
										.getSelection());
					}
				});
		log.debug(this.btnAutomatischePositionierung.getBounds().toString());

		this.lblTransparenz = new Label(this.grpSignatur, SWT.HORIZONTAL);
		FormData fd_lblTransparenz = new FormData();
		fd_lblTransparenz.top = new FormAttachment(
				this.btnAutomatischePositionierung, 5);
		fd_lblTransparenz.left = new FormAttachment(0, 5);
		this.lblTransparenz.setLayoutData(fd_lblTransparenz);

		FontData[] fD_lblTransparenz = this.lblTransparenz.getFont().getFontData();
		fD_lblTransparenz[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblTransparenz.setFont(new Font(Display.getCurrent(),
				fD_lblTransparenz[0]));

		this.lblTransparenzLinks = new Label(this.grpSignatur, SWT.HORIZONTAL);
		FormData fd_lblTransparenzLinks = new FormData();
		fd_lblTransparenzLinks.top = new FormAttachment(this.lblTransparenz, 5);
		fd_lblTransparenzLinks.left = new FormAttachment(0, 15);
		this.lblTransparenzLinks.setLayoutData(fd_lblTransparenzLinks);

		FontData[] fD_lblTransparenzLinks = this.lblTransparenzLinks.getFont()
				.getFontData();
		fD_lblTransparenzLinks[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblTransparenzLinks.setFont(new Font(Display.getCurrent(),
				fD_lblTransparenzLinks[0]));

		this.lblTransparenzRechts = new Label(this.grpSignatur, SWT.HORIZONTAL);
		FormData fd_lblTransparenzRechts = new FormData();
		fd_lblTransparenzRechts.top = new FormAttachment(this.lblTransparenz, 5);
		fd_lblTransparenzRechts.right = new FormAttachment(100, -5);
		this.lblTransparenzRechts.setLayoutData(fd_lblTransparenzRechts);

		FontData[] fD_lblTransparenzRechts = this.lblTransparenzRechts.getFont()
				.getFontData();
		fD_lblTransparenzRechts[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblTransparenzRechts.setFont(new Font(Display.getCurrent(),
				fD_lblTransparenzRechts[0]));

		this.sclTransparenz = new Scale(this.grpSignatur, SWT.HORIZONTAL);
		FormData fd_sldTransparenz = new FormData();
		fd_sldTransparenz.right = new FormAttachment(this.lblTransparenzRechts, -5);
		fd_sldTransparenz.top = new FormAttachment(this.lblTransparenz, 5);
		fd_sldTransparenz.left = new FormAttachment(this.lblTransparenzLinks, 5);
		this.sclTransparenz.setLayoutData(fd_sldTransparenz);
		this.sclTransparenz.setMinimum(0);
		this.sclTransparenz.setMaximum(255);
		this.sclTransparenz.setIncrement(1);
		this.sclTransparenz.setPageIncrement(10);
		this.sclTransparenz.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performPlaceholderTransparency(AdvancedConfigurationComposite.this.sclTransparenz
						.getSelection());
			}
		});

		this.grpBkuAuswahl = new Group(this, SWT.NONE);
		layout = new FormLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 5;
		this.grpBkuAuswahl.setLayout(layout);
		FormData fd_grpBkuAuswahl = new FormData();
		fd_grpBkuAuswahl.top = new FormAttachment(this.grpSignatur, 5);
		fd_grpBkuAuswahl.left = new FormAttachment(0, 5);
		fd_grpBkuAuswahl.right = new FormAttachment(100, -5);
		this.grpBkuAuswahl.setLayoutData(fd_grpBkuAuswahl);

		FontData[] fD_grpBkuAuswahl = this.grpBkuAuswahl.getFont().getFontData();
		fD_grpBkuAuswahl[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpBkuAuswahl.setFont(new Font(Display.getCurrent(),
				fD_grpBkuAuswahl[0]));

		this.cmbBKUAuswahl = new Combo(this.grpBkuAuswahl, SWT.READ_ONLY);
		FormData fd_cmbBKUAuswahl = new FormData();
		fd_cmbBKUAuswahl.right = new FormAttachment(100, -5);
		fd_cmbBKUAuswahl.top = new FormAttachment(0);
		fd_cmbBKUAuswahl.left = new FormAttachment(0, 5);
		this.cmbBKUAuswahl.setLayoutData(fd_cmbBKUAuswahl);

		FontData[] fD_cmbBKUAuswahl = this.cmbBKUAuswahl.getFont()
				.getFontData();
		fD_cmbBKUAuswahl[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.cmbBKUAuswahl.setFont(new Font(Display.getCurrent(),
				fD_cmbBKUAuswahl[0]));

		int blen = BKUs.values().length;
		this.bkuStrings = new String[blen];
		for (int i = 0; i < blen; i++) {
			String lookup = "BKU." + BKUs.values()[i].toString(); //$NON-NLS-1$
			this.bkuStrings[i] = Messages.getString(lookup);
		}
		this.cmbBKUAuswahl.setItems(this.bkuStrings);
		this.cmbBKUAuswahl.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = getBKUElementIndex(AdvancedConfigurationComposite.this.configurationContainer
						.getDefaultBKU());
				if (AdvancedConfigurationComposite.this.cmbBKUAuswahl
						.getSelectionIndex() != selectionIndex) {
					selectionIndex = AdvancedConfigurationComposite.this.cmbBKUAuswahl
							.getSelectionIndex();
					performBKUSelectionChanged(AdvancedConfigurationComposite.this.cmbBKUAuswahl
							.getItem(selectionIndex));
				}
			}
		});

		this.grpSpeicherort = new Group(this, SWT.NONE);
		layout = new FormLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 5;
		this.grpSpeicherort.setLayout(layout);
		FormData fd_grpSpeicherort = new FormData();
		fd_grpSpeicherort.top = new FormAttachment(this.grpBkuAuswahl, 5);
		fd_grpSpeicherort.left = new FormAttachment(0, 5);
		fd_grpSpeicherort.right = new FormAttachment(100, -5);
		this.grpSpeicherort.setLayoutData(fd_grpSpeicherort);

		FontData[] fD_grpSpeicherort = this.grpSpeicherort.getFont().getFontData();
		fD_grpSpeicherort[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpSpeicherort.setFont(new Font(Display.getCurrent(),
				fD_grpSpeicherort[0]));

		this.lblDefaultOutputFolder = new Label(this.grpSpeicherort, SWT.NONE);
		FormData fd_lblDefaultOutputFolder = new FormData();
		fd_lblDefaultOutputFolder.top = new FormAttachment(0);
		fd_lblDefaultOutputFolder.left = new FormAttachment(0, 5);
		this.lblDefaultOutputFolder.setLayoutData(fd_lblDefaultOutputFolder);

		FontData[] fD_lblDefaultOutputFolder = this.lblDefaultOutputFolder.getFont()
				.getFontData();
		fD_lblDefaultOutputFolder[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblDefaultOutputFolder.setFont(new Font(Display.getCurrent(),
				fD_lblDefaultOutputFolder[0]));

		this.txtOutputFolder = new Text(this.grpSpeicherort, SWT.BORDER);
		FormData fd_text = new FormData();
		fd_text.top = new FormAttachment(this.lblDefaultOutputFolder, 5);
		fd_text.left = new FormAttachment(0, 15);
		this.txtOutputFolder.setLayoutData(fd_text);

		FontData[] fD_txtOutputFolder = this.txtOutputFolder.getFont()
				.getFontData();
		fD_txtOutputFolder[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.txtOutputFolder.setFont(new Font(Display.getCurrent(),
				fD_txtOutputFolder[0]));

		this.txtOutputFolder.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				performOutputFolderChanged(AdvancedConfigurationComposite.this.txtOutputFolder
						.getText());
			}
		});

		this.btnBrowse = new Button(this.grpSpeicherort, SWT.NONE);
		fd_text.right = new FormAttachment(this.btnBrowse, -5);

		FontData[] fD_btnBrowse = this.btnBrowse.getFont().getFontData();
		fD_btnBrowse[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnBrowse.setFont(new Font(Display.getCurrent(), fD_btnBrowse[0]));

		FormData fd_btnBrowse = new FormData();
		fd_btnBrowse.top = new FormAttachment(this.lblDefaultOutputFolder, 5);
		fd_btnBrowse.right = new FormAttachment(100, -5);
		this.btnBrowse.setLayoutData(fd_btnBrowse);

		this.btnBrowse.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(
						AdvancedConfigurationComposite.this.getShell());

				// Set the initial filter path according
				// to anything they've selected or typed in
				dlg.setFilterPath(AdvancedConfigurationComposite.this.txtOutputFolder
						.getText());

				// Change the title bar text
				dlg.setText(Messages
						.getString("advanced_config.OutputFolder.Dialog_Title")); //$NON-NLS-1$

				// Customizable message displayed in the dialog
				dlg.setMessage(Messages
						.getString("advanced_config.OutputFolder.Dialog")); //$NON-NLS-1$

				// Calling open() will open and run the dialog.
				// It will return the selected directory, or
				// null if user cancels
				String dir = dlg.open();
				if (dir != null) {
					// Set the text box to the new selection
					performOutputFolderChanged(dir);
				}
			}
		});
		
		this.grpLocaleAuswahl = new Group(this, SWT.NONE);
		FormLayout layout_grpLocaleAuswahl = new FormLayout();
		layout_grpLocaleAuswahl.marginHeight = 10;
		layout_grpLocaleAuswahl.marginWidth = 5;
		this.grpLocaleAuswahl.setLayout(layout_grpLocaleAuswahl);
		FormData fd_grpLocaleAuswahl = new FormData();
		fd_grpLocaleAuswahl.top = new FormAttachment(this.grpSpeicherort, 5);
		fd_grpLocaleAuswahl.left = new FormAttachment(0, 5);
		fd_grpLocaleAuswahl.right = new FormAttachment(100, -5);
		this.grpLocaleAuswahl.setLayoutData(fd_grpLocaleAuswahl);

		FontData[] fD_grpLocaleAuswahl = this.grpLocaleAuswahl.getFont().getFontData();
		fD_grpLocaleAuswahl[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpLocaleAuswahl.setFont(new Font(Display.getCurrent(),
				fD_grpLocaleAuswahl[0]));

		this.cmbLocaleAuswahl = new Combo(this.grpLocaleAuswahl, SWT.READ_ONLY);
		FormData fd_cmbLocaleAuswahl = new FormData();
		fd_cmbLocaleAuswahl.right = new FormAttachment(100, -5);
		fd_cmbLocaleAuswahl.top = new FormAttachment(0);
		fd_cmbLocaleAuswahl.left = new FormAttachment(0, 5);
		this.cmbLocaleAuswahl.setLayoutData(fd_cmbLocaleAuswahl);

		FontData[] fD_cmbLocaleAuswahl = this.cmbLocaleAuswahl.getFont()
				.getFontData();
		fD_cmbLocaleAuswahl[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.cmbLocaleAuswahl.setFont(new Font(Display.getCurrent(),
				fD_cmbLocaleAuswahl[0]));

		String[] localeStrings = new String[Constants.SUPPORTED_LOCALES.length];
		for (int i = 0; i < Constants.SUPPORTED_LOCALES.length; ++i) {
			localeStrings[i] = Constants.SUPPORTED_LOCALES[i].getDisplayLanguage(Constants.SUPPORTED_LOCALES[i]);
		}
		this.cmbLocaleAuswahl.setItems(localeStrings);
		this.cmbLocaleAuswahl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Locale currentLocale = AdvancedConfigurationComposite.this.configurationContainer
						.getLocale();
				Locale selectedLocale = Constants.
						SUPPORTED_LOCALES[AdvancedConfigurationComposite.this.cmbLocaleAuswahl
						                  .getSelectionIndex()];
				if (!currentLocale.equals(selectedLocale)) {
					performLocaleSelectionChanged(selectedLocale);
				}
			}
		});

		this.grpProxy = new Group(this, SWT.NONE);
		FormData fd_grpProxy = new FormData();
		fd_grpProxy.right = new FormAttachment(100, -5);
		fd_grpProxy.top = new FormAttachment(this.grpLocaleAuswahl, 5);
		fd_grpProxy.left = new FormAttachment(0, 5);
		this.grpProxy.setLayoutData(fd_grpProxy);
		this.grpProxy.setLayout(new GridLayout(2, false));

		FontData[] fD_grpProxy = this.grpProxy.getFont().getFontData();
		fD_grpProxy[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpProxy.setFont(new Font(Display.getCurrent(), fD_grpProxy[0]));

		this.lblProxyHost = new Label(this.grpProxy, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_lblNewLabel.widthHint = 66;
		this.lblProxyHost.setLayoutData(gd_lblNewLabel);
		this.lblProxyHost.setBounds(0, 0, 57, 15);

		FontData[] fD_lblNewLabel = this.lblProxyHost.getFont().getFontData();
		fD_lblNewLabel[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblProxyHost.setFont(new Font(Display.getCurrent(),
				fD_lblNewLabel[0]));

		Composite compProxyHostContainer = new Composite(this.grpProxy, SWT.NONE);
		compProxyHostContainer.setLayout(new FormLayout());
		compProxyHostContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));
		this.txtProxyHost = new Text(compProxyHostContainer, SWT.BORDER);
		FormData fd_txtProxyHost = new FormData();
		fd_txtProxyHost.right = new FormAttachment(100, -42);
		fd_txtProxyHost.top = new FormAttachment(0);
		fd_txtProxyHost.left = new FormAttachment(0, 5);

		FontData[] fD_txtProxyHost = this.txtProxyHost.getFont().getFontData();
		fD_txtProxyHost[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.txtProxyHost.setFont(new Font(Display.getCurrent(),
				fD_txtProxyHost[0]));

		this.proxyHostErrorMarker = new ErrorMarker(compProxyHostContainer, SWT.NONE, ""); //$NON-NLS-1$

		FormData fd_proxyHostErrorMarker = new FormData();
		fd_proxyHostErrorMarker.left = new FormAttachment(100, -32);
		fd_proxyHostErrorMarker.right = new FormAttachment(100);
		fd_proxyHostErrorMarker.top = new FormAttachment(0);
		fd_proxyHostErrorMarker.bottom = new FormAttachment(0, 32);

		this.proxyHostErrorMarker.setLayoutData(fd_proxyHostErrorMarker);
		this.proxyHostErrorMarker.setVisible(false);
		this.txtProxyHost.setLayoutData(fd_txtProxyHost);

		this.txtProxyHost.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				processProxyHostChanged();
			}
		});

		this.txtProxyHost.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					processProxyHostChanged();
				}
			}
		});

		this.lblProxyPort = new Label(this.grpProxy, SWT.NONE);
		this.lblProxyPort.setBounds(0, 0, 57, 15);

		FontData[] fD_lblProxyPort = this.lblProxyPort.getFont()
				.getFontData();
		fD_lblProxyPort[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblProxyPort.setFont(new Font(Display.getCurrent(),
				fD_lblProxyPort[0]));

		Composite compProxyPortContainer = new Composite(this.grpProxy, SWT.NONE);
		compProxyPortContainer.setLayout(new FormLayout());
		compProxyPortContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));

		this.txtProxyPort = new Text(compProxyPortContainer, SWT.BORDER);
		this.fd_txtProxyPort = new FormData();
		this.fd_txtProxyPort.top = new FormAttachment(0, 0);
		this.fd_txtProxyPort.left = new FormAttachment(0, 5);
		this.fd_txtProxyPort.right = new FormAttachment(100, -42);
		this.txtProxyPort.setLayoutData(this.fd_txtProxyPort);

		FontData[] fD_txtProxyPort = this.txtProxyPort.getFont().getFontData();
		fD_txtProxyPort[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.txtProxyPort.setFont(new Font(Display.getCurrent(),
				fD_txtProxyPort[0]));

		this.txtProxyPort.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					processProxyPortChanged();
				}
			}
		});

		this.txtProxyPortErrorMarker = new ErrorMarker(compProxyPortContainer, SWT.NONE, ""); //$NON-NLS-1$
		this.fd_txtProxyPortErrorMarker = new FormData();
		this.fd_txtProxyPortErrorMarker.left = new FormAttachment(100, -32);
		this.fd_txtProxyPortErrorMarker.right = new FormAttachment(100);
		this.fd_txtProxyPortErrorMarker.top = new FormAttachment(0);
		this.fd_txtProxyPortErrorMarker.bottom = new FormAttachment(0, 32);
		this.txtProxyPortErrorMarker
				.setLayoutData(this.fd_txtProxyPortErrorMarker);
		this.txtProxyPortErrorMarker.setVisible(false);

		this.txtProxyPort.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				processProxyPortChanged();
			}
		});

		reloadResources();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#signerChanged()
	 */
	@Override
	protected void signerChanged() {
		// Nothing to do here (yet)
	}

	void performOutputFolderChanged(String foldername) {
		log.debug("Selected Output folder: " + foldername); //$NON-NLS-1$
		this.configurationContainer.setOutputFolder(foldername);
		AdvancedConfigurationComposite.this.txtOutputFolder.setText(foldername);
	}

	int getBKUElementIndex(BKUs bku) {
		String lookup = "BKU." + bku.toString(); //$NON-NLS-1$
		String bkuName = Messages.getString(lookup);

		for (int i = 0; i < this.bkuStrings.length; i++) {
			if (this.bkuStrings[i].equals(bkuName)) {
				log.debug("BKU: " + bkuName + " IDX: " + i); //$NON-NLS-1$ //$NON-NLS-2$
				return i;
			}
		}

		log.warn("NO BKU match for " + bkuName); //$NON-NLS-1$
		return 0;
	}

	void performBKUSelectionChanged(BKUs selected) {
		log.debug("Selected BKU: " + selected.toString()); //$NON-NLS-1$
		this.configurationContainer.setDefaultBKU(selected);
		this.cmbBKUAuswahl.select(this.getBKUElementIndex(selected));
	}

	void performBKUSelectionChanged(String selected) {
		try {
			BKUs bkuvalue = resolveBKU(selected);
			this.performBKUSelectionChanged(bkuvalue);
		} catch (Exception ex) {
			log.error("Failed to parse BKU value: " + selected, ex); //$NON-NLS-1$
			ErrorDialog dialog = new ErrorDialog(getShell(),
					Messages.getString("error.InvalidBKU"), BUTTONS.OK); //$NON-NLS-1$
			dialog.open();
		}
	}

	BKUs resolveBKU(String localizedBKU) {
		int blen = BKUs.values().length;

		for (int i = 0; i < blen; i++) {
			String lookup = "BKU." + BKUs.values()[i].toString(); //$NON-NLS-1$
			if (Messages.getString(lookup).equals(localizedBKU)) {
				return BKUs.values()[i];
			}
		}

		return BKUs.NONE;
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
	
	void performLocaleSelectionChanged(Locale selected) {
		log.debug("Selected Locale: " + selected); //$NON-NLS-1$
		this.configurationContainer.setLocale(selected);
		this.cmbLocaleAuswahl.select(this.getLocaleElementIndex(selected));
	}

	void performPositionSelection(boolean automatic) {
		log.debug("Selected Position: " + automatic); //$NON-NLS-1$
		SignaturePosition pos = automatic ? new SignaturePosition() : null;
		this.configurationContainer.setDefaultSignaturePosition(pos);
		this.btnAutomatischePositionierung.setSelection(automatic);
	}

	void performPlaceholderTransparency(int transparency) {
		this.configurationContainer.setPlaceholderTransparency(transparency);
	}

	void processProxyHostChanged() {
		try {
			this.proxyHostErrorMarker.setVisible(false);
			plainProxyHostSetter();
		} catch (Exception ex) {
			this.proxyHostErrorMarker.setVisible(true);
			this.proxyHostErrorMarker.setToolTipText(ex.getMessage());
			log.error("processProxyHost: ", ex); //$NON-NLS-1$
		}
	}

	/**
	 *
	 */
	private void plainProxyHostSetter() {
		String host = this.txtProxyHost.getText();
		this.configurationContainer.setProxyHost(host);
	}

	void processProxyPortChanged() {
		try {
			this.txtProxyPortErrorMarker.setVisible(false);
			plainProxyPortSetter();
		} catch (Exception ex) {
			this.txtProxyPortErrorMarker.setVisible(true);
			this.txtProxyPortErrorMarker.setToolTipText(ex.getMessage());
			log.error("processProxyPort: ", ex); //$NON-NLS-1$
		}
	}

	/**
	 * @throws InvalidPortException
	 */
	private void plainProxyPortSetter() throws InvalidPortException {
		String portString = this.txtProxyPort.getText();
		int port = -1;
		if (portString == null || portString.trim().isEmpty()) {
			port = -1;
		} else {
			try {
				port = Integer.parseInt(portString);
			} catch (NumberFormatException e) {
				throw new InvalidPortException(portString, e);
			}
		}
		this.configurationContainer.setProxyPort(port);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.composites.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		// Nothing to do here
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#loadConfiguration
	 * ()
	 */
	@Override
	public void loadConfiguration() {
		// load advanced settings
		this.performBKUSelectionChanged(this.configurationContainer
				.getDefaultBKU());
		String outputFolder = this.configurationContainer.getOutputFolder();
		if (outputFolder != null) {
			this.performOutputFolderChanged(outputFolder);
		}
		SignaturePosition pos = this.configurationContainer
				.getDefaultSignaturePosition();
		this.performPositionSelection(pos != null && pos.useAutoPositioning());
		this.sclTransparenz.setSelection(this.configurationContainer
				.getPlaceholderTransparency());
		this.performLocaleSelectionChanged(this.configurationContainer.getLocale());

		int port = this.configurationContainer.getProxyPort();
		if (port > 0) {
			this.txtProxyPort.setText(Integer.toString(port));
		}

		String host = this.configurationContainer.getProxyHost();
		if (host != null) {
			this.txtProxyHost.setText(host);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#validateSettings
	 * ()
	 */
	@Override
	public void validateSettings(int resumeIndex) throws Exception {
		
		String foldername = this.configurationContainer.getOutputFolder();
		
		switch (resumeIndex) {
			case 0:
				if (foldername != null && !foldername.isEmpty()) {
					File outputFolder = new File(foldername);
					if (!outputFolder.exists()) {
						throw new OutputfolderDoesntExistException(outputFolder, 1);
					}
					if (!outputFolder.isDirectory()) {
						throw new OutputfolderNotADirectoryException(outputFolder);
					}
				}
				// Fall through
			case 1:
				this.plainProxyHostSetter();
				// Fall through
			case 2:
				this.plainProxyPortSetter();
				// Fall through
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		this.grpSignatur.setText(Messages
				.getString("advanced_config.Signature_Title")); //$NON-NLS-1$
		this.btnAutomatischePositionierung.setText(Messages
				.getString("advanced_config.AutoPosition")); //$NON-NLS-1$
		this.btnAutomatischePositionierung.setToolTipText(Messages
				.getString("advanced_config.AutoPosition_ToolTip")); //$NON-NLS-1$
		this.lblTransparenz.setText(Messages
				.getString("advanced_config.SigPHTransparency")); //$NON-NLS-1$
		this.lblTransparenzLinks.setText(Messages
				.getString("advanced_config.SigPHTransparencyMin")); //$NON-NLS-1$
		this.lblTransparenzRechts.setText(Messages
				.getString("advanced_config.SigPHTransparencyMax")); //$NON-NLS-1$
		this.sclTransparenz.setToolTipText(Messages
				.getString("advanced_config.SigPHTransparencyTooltip")); //$NON-NLS-1$

		this.grpBkuAuswahl.setText(Messages
				.getString("advanced_config.BKUSelection_Title")); //$NON-NLS-1$
		this.cmbBKUAuswahl.setToolTipText(Messages
				.getString("advanced_config.BKUSelection_ToolTip")); //$NON-NLS-1$

		this.grpSpeicherort.setText(Messages
				.getString("advanced_config.OutputFolder_Title")); //$NON-NLS-1$
		this.lblDefaultOutputFolder.setText(Messages
				.getString("advanced_config.OutputFolder")); //$NON-NLS-1$
		this.txtOutputFolder.setToolTipText(Messages
				.getString("advanced_config.OutputFolder_ToolTip")); //$NON-NLS-1$
		this.btnBrowse.setText(Messages.getString("common.browse")); //$NON-NLS-1$

		this.grpLocaleAuswahl.setText(Messages
				.getString("advanced_config.LocaleSelection_Title")); //$NON-NLS-1$
		this.cmbLocaleAuswahl.setToolTipText(Messages
				.getString("advanced_config.LocaleSelection_ToolTip")); //$NON-NLS-1$

		this.grpProxy.setText(Messages.getString("simple_config.Proxy_Title")); //$NON-NLS-1$
		this.lblProxyHost.setText(Messages.getString("simple_config.ProxyHost")); //$NON-NLS-1$
		this.txtProxyHost.setToolTipText(Messages
				.getString("simple_config.ProxyHost_ToolTip")); //$NON-NLS-1$
		this.txtProxyHost.setMessage(Messages
				.getString("simple_config.ProxyHostTemplate")); //$NON-NLS-1$
		this.lblProxyPort.setText(Messages
				.getString("simple_config.ProxyPort")); //$NON-NLS-1$
		this.txtProxyPort.setToolTipText(Messages
				.getString("simple_config.ProxyPort_ToolTip")); //$NON-NLS-1$
		this.txtProxyPort.setMessage(Messages
				.getString("simple_config.ProxyPortTemplate")); //$NON-NLS-1$
	}
}
