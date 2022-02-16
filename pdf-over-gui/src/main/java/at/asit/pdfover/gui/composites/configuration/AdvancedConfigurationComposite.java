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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Profile;
import at.asit.pdfover.gui.composites.ConfigurationComposite;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.controls.ErrorMarker;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.exceptions.InvalidPortException;
import at.asit.pdfover.gui.exceptions.OutputfolderDoesntExistException;
import at.asit.pdfover.gui.exceptions.OutputfolderNotADirectoryException;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.config.ConfigManipulator;
import at.asit.pdfover.gui.workflow.config.ConfigurationContainer;
import at.asit.pdfover.gui.workflow.config.PersistentConfigProvider;
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
	private static final Logger log = LoggerFactory.getLogger(AdvancedConfigurationComposite.class);

	private ConfigurationComposite configurationComposite;

	private Group grpSignatur;
	private Group grpPlaceholder;
	Button btnAutomatischePositionierung;
	Button btnPdfACompat;
	Button btnPlatzhalterVerwenden;
	Button btnSignatureFieldsUsage;
	Button btnEnablePlaceholderUsage;
	private Label lblTransparenz;
	private Label lblTransparenzLinks;
	private Label lblTransparenzRechts;
	Scale sclTransparenz;

	private Group grpBkuAuswahl;
	Combo cmbBKUAuswahl;
	List<String> bkuStrings;
	Button btnKeystoreEnabled;

	private final Group grpSpeicherort;
	private final Label lblDefaultOutputFolder;
	Text txtOutputFolder;
	private final Button btnBrowse;
	private final Label lblSaveFilePostFix;
	private final Text txtSaveFilePostFix;

	private final Group grpLocaleAuswahl;
	Combo cmbLocaleAuswahl;

	private Group grpUpdateCheck;
	Button btnUpdateCheck;

	private Group grpProxy;
	private Label lblProxyHost;
	private Text txtProxyHost;
	private ErrorMarker proxyHostErrorMarker;
	private Label lblProxyPort;
	private Text txtProxyPort;
	private ErrorMarker txtProxyPortErrorMarker;
	FormData fd_txtProxyPort;
	FormData fd_txtProxyPortErrorMarker;

	/**
	 * @param parent
	 * @param style
	 * @param state
	 * @param container
	 * @param config
	 */
	public AdvancedConfigurationComposite(Composite parent, int style, State state, ConfigurationContainer container,
			ConfigurationComposite config) {
		super(parent, style, state, container);
		this.configurationComposite = config;
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
		this.grpSignatur.setFont(new Font(Display.getCurrent(), fD_grpSignaturPosition[0]));

		this.btnAutomatischePositionierung = new Button(this.grpSignatur, SWT.CHECK);
		FormData fd_btnAutomatischePositionierung = new FormData();
		fd_btnAutomatischePositionierung.right = new FormAttachment(100, -5);
		fd_btnAutomatischePositionierung.top = new FormAttachment(0);
		fd_btnAutomatischePositionierung.left = new FormAttachment(0, 5);
		this.btnAutomatischePositionierung.setLayoutData(fd_btnAutomatischePositionierung);

		FontData[] fD_btnAutomatischePositionierung = this.btnAutomatischePositionierung.getFont().getFontData();
		fD_btnAutomatischePositionierung[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnAutomatischePositionierung.setFont(new Font(Display.getCurrent(), fD_btnAutomatischePositionierung[0]));

		this.btnAutomatischePositionierung.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.performPositionSelection(
						AdvancedConfigurationComposite.this.btnAutomatischePositionierung.getSelection());
			}
		});

		this.btnPdfACompat = new Button(this.grpSignatur, SWT.CHECK);
		FormData fd_btnPdfACompat = new FormData();
		fd_btnPdfACompat.right = new FormAttachment(100, -5);
		fd_btnPdfACompat.top = new FormAttachment(this.btnAutomatischePositionierung, 5);
		fd_btnPdfACompat.left = new FormAttachment(0, 5);
		this.btnPdfACompat.setLayoutData(fd_btnPdfACompat);

		FontData[] fD_btnPdfACompat = this.btnPdfACompat.getFont().getFontData();
		fD_btnPdfACompat[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnPdfACompat.setFont(new Font(Display.getCurrent(), fD_btnPdfACompat[0]));

		this.btnPdfACompat.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this
						.performPdfACompatSelection(AdvancedConfigurationComposite.this.btnPdfACompat.getSelection());
			}
		});

		this.lblTransparenz = new Label(this.grpSignatur, SWT.HORIZONTAL);
		FormData fd_lblTransparenz = new FormData();
		fd_lblTransparenz.top = new FormAttachment(this.btnPdfACompat, 5);
		fd_lblTransparenz.left = new FormAttachment(0, 5);
		this.lblTransparenz.setLayoutData(fd_lblTransparenz);

		FontData[] fD_lblTransparenz = this.lblTransparenz.getFont().getFontData();
		fD_lblTransparenz[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblTransparenz.setFont(new Font(Display.getCurrent(), fD_lblTransparenz[0]));

		this.lblTransparenzLinks = new Label(this.grpSignatur, SWT.HORIZONTAL);
		FormData fd_lblTransparenzLinks = new FormData();
		fd_lblTransparenzLinks.top = new FormAttachment(this.lblTransparenz, 5);
		fd_lblTransparenzLinks.left = new FormAttachment(0, 15);
		this.lblTransparenzLinks.setLayoutData(fd_lblTransparenzLinks);

		FontData[] fD_lblTransparenzLinks = this.lblTransparenzLinks.getFont().getFontData();
		fD_lblTransparenzLinks[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblTransparenzLinks.setFont(new Font(Display.getCurrent(), fD_lblTransparenzLinks[0]));

		this.lblTransparenzRechts = new Label(this.grpSignatur, SWT.HORIZONTAL);
		FormData fd_lblTransparenzRechts = new FormData();
		fd_lblTransparenzRechts.top = new FormAttachment(this.lblTransparenz, 5);
		fd_lblTransparenzRechts.right = new FormAttachment(100, -5);
		this.lblTransparenzRechts.setLayoutData(fd_lblTransparenzRechts);

		FontData[] fD_lblTransparenzRechts = this.lblTransparenzRechts.getFont().getFontData();
		fD_lblTransparenzRechts[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblTransparenzRechts.setFont(new Font(Display.getCurrent(), fD_lblTransparenzRechts[0]));

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
				performPlaceholderTransparency(AdvancedConfigurationComposite.this.sclTransparenz.getSelection());
			}
		});

		this.grpPlaceholder = new Group(this, SWT.NONE);
		FormLayout layout_grpPlaceholder = new FormLayout();
		layout_grpPlaceholder.marginHeight = 10;
		layout_grpPlaceholder.marginWidth = 5;
		this.grpPlaceholder.setLayout(layout_grpPlaceholder);

		FormData fd_grpPlaceholder = new FormData();
		fd_grpPlaceholder.top = new FormAttachment(this.grpSignatur, 5);
		fd_grpPlaceholder.right = new FormAttachment(100, -5);
		fd_grpPlaceholder.left = new FormAttachment(0, 5);
		this.grpPlaceholder.setLayoutData(fd_grpPlaceholder);

		FontData[] fD_grpPlaceholder = this.grpPlaceholder.getFont().getFontData();
		fD_grpPlaceholder[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpPlaceholder.setFont(new Font(Display.getCurrent(), fD_grpPlaceholder[0]));

		this.btnEnablePlaceholderUsage = new Button(this.grpPlaceholder, SWT.CHECK);
		FormData fd_btnEnablePlaceholderUsage = new FormData();
		fd_btnEnablePlaceholderUsage.right = new FormAttachment(100, -5);
		fd_btnEnablePlaceholderUsage.top = new FormAttachment(0, 5);
		fd_btnEnablePlaceholderUsage.left = new FormAttachment(0, 5);
		this.btnEnablePlaceholderUsage.setLayoutData(fd_btnEnablePlaceholderUsage);

		FontData[] fD_btnEnablePlaceholderUsage = this.btnEnablePlaceholderUsage.getFont().getFontData();
		fD_btnEnablePlaceholderUsage[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnEnablePlaceholderUsage.setFont(new Font(Display.getCurrent(), fD_btnEnablePlaceholderUsage[0]));

		this.btnEnablePlaceholderUsage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.performEnableUsePlaceholder(
						AdvancedConfigurationComposite.this.btnEnablePlaceholderUsage.getSelection());

			}
		});

		this.btnPlatzhalterVerwenden = new Button(this.grpPlaceholder, SWT.RADIO);
		FormData fd_btnPlatzhalterVerwenden = new FormData();
		fd_btnPlatzhalterVerwenden.right = new FormAttachment(100, -5);
		fd_btnPlatzhalterVerwenden.top = new FormAttachment(this.btnEnablePlaceholderUsage, 5);
		fd_btnPlatzhalterVerwenden.left = new FormAttachment(0, 5);
		this.btnPlatzhalterVerwenden.setLayoutData(fd_btnPlatzhalterVerwenden);

		FontData[] fD_btnPlatzhalterVerwenden = this.btnPlatzhalterVerwenden.getFont().getFontData();
		fD_btnPlatzhalterVerwenden[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnPlatzhalterVerwenden.setFont(new Font(Display.getCurrent(), fD_btnPlatzhalterVerwenden[0]));

		this.btnPlatzhalterVerwenden.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.performUseMarkerSelection(
						AdvancedConfigurationComposite.this.btnPlatzhalterVerwenden.getSelection());
			}
		});

		this.btnSignatureFieldsUsage = new Button(this.grpPlaceholder, SWT.RADIO);
		FormData fd_btnSignatureFieldsUsage = new FormData();
		fd_btnSignatureFieldsUsage.right = new FormAttachment(100, -5);
		fd_btnSignatureFieldsUsage.top = new FormAttachment(this.btnPlatzhalterVerwenden, 5);
		fd_btnSignatureFieldsUsage.left = new FormAttachment(0, 5);
		this.btnSignatureFieldsUsage.setLayoutData(fd_btnSignatureFieldsUsage);

		FontData[] fD_btnSignatureFieldsUsage = this.btnSignatureFieldsUsage.getFont().getFontData();
		fD_btnSignatureFieldsUsage[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnSignatureFieldsUsage.setFont(new Font(Display.getCurrent(), fD_btnSignatureFieldsUsage[0]));

		this.btnSignatureFieldsUsage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.performUseSignatureFieldsSelection(
						AdvancedConfigurationComposite.this.btnSignatureFieldsUsage.getSelection());
			}
		});

		this.grpBkuAuswahl = new Group(this, SWT.NONE);
		layout = new FormLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 5;
		this.grpBkuAuswahl.setLayout(layout);
		FormData fd_grpBkuAuswahl = new FormData();
		fd_grpBkuAuswahl.top = new FormAttachment(this.grpPlaceholder, 5);
		fd_grpBkuAuswahl.left = new FormAttachment(0, 5);
		fd_grpBkuAuswahl.right = new FormAttachment(100, -5);
		this.grpBkuAuswahl.setLayoutData(fd_grpBkuAuswahl);

		FontData[] fD_grpBkuAuswahl = this.grpBkuAuswahl.getFont().getFontData();
		fD_grpBkuAuswahl[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpBkuAuswahl.setFont(new Font(Display.getCurrent(), fD_grpBkuAuswahl[0]));

		this.cmbBKUAuswahl = new Combo(this.grpBkuAuswahl, SWT.READ_ONLY);
		FormData fd_cmbBKUAuswahl = new FormData();
		fd_cmbBKUAuswahl.right = new FormAttachment(100, -5);
		fd_cmbBKUAuswahl.top = new FormAttachment(0);
		fd_cmbBKUAuswahl.left = new FormAttachment(0, 5);
		this.cmbBKUAuswahl.setLayoutData(fd_cmbBKUAuswahl);

		FontData[] fD_cmbBKUAuswahl = this.cmbBKUAuswahl.getFont().getFontData();
		fD_cmbBKUAuswahl[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.cmbBKUAuswahl.setFont(new Font(Display.getCurrent(), fD_cmbBKUAuswahl[0]));

		int blen = BKUs.values().length;
		this.bkuStrings = new ArrayList<>(blen);
		for (int i = 0; i < blen; i++) {
			String lookup = "BKU." + BKUs.values()[i].toString(); //$NON-NLS-1$
			String text = Messages.getString(lookup);
			this.bkuStrings.add(text);
			this.cmbBKUAuswahl.add(text);
		}
		this.cmbBKUAuswahl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = getBKUElementIndex(
						AdvancedConfigurationComposite.this.configurationContainer.getDefaultBKU());
				if (AdvancedConfigurationComposite.this.cmbBKUAuswahl.getSelectionIndex() != selectionIndex) {
					selectionIndex = AdvancedConfigurationComposite.this.cmbBKUAuswahl.getSelectionIndex();
					performBKUSelectionChanged(
							AdvancedConfigurationComposite.this.cmbBKUAuswahl.getItem(selectionIndex));
				}
			}
		});

		this.btnKeystoreEnabled = new Button(this.grpBkuAuswahl, SWT.CHECK);
		FormData fd_btnKeystoreEnabled = new FormData();
		fd_btnKeystoreEnabled.right = new FormAttachment(100, -5);
		fd_btnKeystoreEnabled.top = new FormAttachment(this.cmbBKUAuswahl, 5);
		fd_btnKeystoreEnabled.left = new FormAttachment(0, 5);
		this.btnKeystoreEnabled.setLayoutData(fd_btnKeystoreEnabled);

		FontData[] fD_btnKeystoreEnabled = this.btnKeystoreEnabled.getFont().getFontData();
		fD_btnKeystoreEnabled[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnKeystoreEnabled.setFont(new Font(Display.getCurrent(), fD_btnKeystoreEnabled[0]));

		this.btnKeystoreEnabled.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.performKeystoreEnabledSelection(
						AdvancedConfigurationComposite.this.btnKeystoreEnabled.getSelection());
			}
		});

		this.grpSpeicherort = new Group(this, SWT.NONE);
		GridLayout gl_grpSpeicherort = new GridLayout(3, false);
		grpSpeicherort.setLayout(gl_grpSpeicherort);
		FormData fd_grpSpeicherort = new FormData();
		fd_grpSpeicherort.left = new FormAttachment(0,5);
		fd_grpSpeicherort.top = new FormAttachment(this.grpBkuAuswahl, 5);
		fd_grpSpeicherort.right = new FormAttachment(100, -5);
		this.grpSpeicherort.setLayoutData(fd_grpSpeicherort);


		FontData[] fD_grpSpeicherort = this.grpSpeicherort.getFont().getFontData();
		fD_grpSpeicherort[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpSpeicherort.setFont(new Font(Display.getCurrent(), fD_grpSpeicherort[0]));

		this.lblDefaultOutputFolder = new Label(this.grpSpeicherort, SWT.NONE);

		FontData[] fD_lblDefaultOutputFolder = this.lblDefaultOutputFolder.getFont().getFontData();
		fD_lblDefaultOutputFolder[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblDefaultOutputFolder.setFont(new Font(Display.getCurrent(), fD_lblDefaultOutputFolder[0]));

		this.txtOutputFolder = new Text(this.grpSpeicherort, SWT.BORDER);
		GridData gd_txtOutputFolder = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		txtOutputFolder.setLayoutData(gd_txtOutputFolder);

		FontData[] fD_txtOutputFolder = this.txtOutputFolder.getFont().getFontData();
		fD_txtOutputFolder[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.txtOutputFolder.setFont(new Font(Display.getCurrent(), fD_txtOutputFolder[0]));

		this.txtOutputFolder.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				performOutputFolderChanged(AdvancedConfigurationComposite.this.txtOutputFolder.getText());
			}
		});
		fD_txtOutputFolder[0].setHeight(Constants.TEXT_SIZE_NORMAL);

		this.btnBrowse = new Button(this.grpSpeicherort, SWT.NONE);
		btnBrowse.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		FontData[] fD_btnBrowse = this.btnBrowse.getFont().getFontData();
		fD_btnBrowse[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnBrowse.setFont(new Font(Display.getCurrent(), fD_btnBrowse[0]));
		this.btnBrowse.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(AdvancedConfigurationComposite.this.getShell());

				// Set the initial filter path according
				// to anything they've selected or typed in
				dlg.setFilterPath(AdvancedConfigurationComposite.this.txtOutputFolder.getText());

				// Change the title bar text
				dlg.setText(Messages.getString("advanced_config.OutputFolder.Dialog_Title")); //$NON-NLS-1$

				// Customizable message displayed in the dialog
				dlg.setMessage(Messages.getString("advanced_config.OutputFolder.Dialog")); //$NON-NLS-1$

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

		this.lblSaveFilePostFix = new Label(this.grpSpeicherort, SWT.NONE);
		lblSaveFilePostFix.setText(Messages.getString("AdvancedConfigurationComposite.lblSaveFilePostFix.text"));

		FontData[] fD_lblSaveFilePostFix = this.lblSaveFilePostFix.getFont().getFontData();
		fD_lblSaveFilePostFix[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblSaveFilePostFix.setFont(new Font(Display.getCurrent(), fD_lblSaveFilePostFix[0]));

		this.txtSaveFilePostFix = new Text(this.grpSpeicherort, SWT.BORDER);
		GridData gd_txtSaveFilePostFix = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);

		txtSaveFilePostFix.setLayoutData(gd_txtSaveFilePostFix);

		FontData[] fD_txtPostFix = this.txtSaveFilePostFix.getFont().getFontData();
		fD_txtPostFix[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.txtSaveFilePostFix.setFont(new Font(Display.getCurrent(), fD_txtPostFix[0]));

		this.txtSaveFilePostFix.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				performPostFixChanged(AdvancedConfigurationComposite.this.txtSaveFilePostFix.getText());
			}
		});
		new Label(grpSpeicherort, SWT.NONE);
		fD_lblSaveFilePostFix[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		fD_txtPostFix[0].setHeight(Constants.TEXT_SIZE_NORMAL);

		this.grpLocaleAuswahl = new Group(this, SWT.NONE);
		FormLayout layout_grpLocaleAuswahl = new FormLayout();
		layout_grpLocaleAuswahl.marginHeight = 10;
		layout_grpLocaleAuswahl.marginWidth = 5;
		this.grpLocaleAuswahl.setLayout(layout_grpLocaleAuswahl);
		FormData fd_grpLocaleAuswahl = new FormData();
		fd_grpLocaleAuswahl.top = new FormAttachment(grpSpeicherort, 5);
		fd_grpLocaleAuswahl.left = new FormAttachment(0, 5);
		fd_grpLocaleAuswahl.right = new FormAttachment(100, -5);
		this.grpLocaleAuswahl.setLayoutData(fd_grpLocaleAuswahl);

		FontData[] fD_grpLocaleAuswahl = this.grpLocaleAuswahl.getFont().getFontData();
		fD_grpLocaleAuswahl[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpLocaleAuswahl.setFont(new Font(Display.getCurrent(), fD_grpLocaleAuswahl[0]));

		this.cmbLocaleAuswahl = new Combo(this.grpLocaleAuswahl, SWT.READ_ONLY);
		FormData fd_cmbLocaleAuswahl = new FormData();
		fd_cmbLocaleAuswahl.right = new FormAttachment(100, -5);
		fd_cmbLocaleAuswahl.top = new FormAttachment(0);
		fd_cmbLocaleAuswahl.left = new FormAttachment(0, 5);
		this.cmbLocaleAuswahl.setLayoutData(fd_cmbLocaleAuswahl);

		FontData[] fD_cmbLocaleAuswahl = this.cmbLocaleAuswahl.getFont().getFontData();
		fD_cmbLocaleAuswahl[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.cmbLocaleAuswahl.setFont(new Font(Display.getCurrent(), fD_cmbLocaleAuswahl[0]));

		String[] localeStrings = new String[Constants.SUPPORTED_LOCALES.length];
		for (int i = 0; i < Constants.SUPPORTED_LOCALES.length; ++i) {
			localeStrings[i] = Constants.SUPPORTED_LOCALES[i].getDisplayLanguage(Constants.SUPPORTED_LOCALES[i]);
		}
		this.cmbLocaleAuswahl.setItems(localeStrings);
		this.cmbLocaleAuswahl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Locale currentLocale = AdvancedConfigurationComposite.this.configurationContainer.getLocale();
				Locale selectedLocale = Constants.SUPPORTED_LOCALES[AdvancedConfigurationComposite.this.cmbLocaleAuswahl
						.getSelectionIndex()];
				if (!currentLocale.equals(selectedLocale)) {
					performLocaleSelectionChanged(selectedLocale);
				}
			}
		});

		this.grpUpdateCheck = new Group(this, SWT.NONE);
		FormLayout layout_grpUpdateCheck = new FormLayout();
		layout_grpUpdateCheck.marginHeight = 10;
		layout_grpUpdateCheck.marginWidth = 5;
		this.grpUpdateCheck.setLayout(layout_grpUpdateCheck);
		FormData fd_grpUpdateCheck = new FormData();
		fd_grpUpdateCheck.top = new FormAttachment(this.grpLocaleAuswahl, 5);
		fd_grpUpdateCheck.left = new FormAttachment(0, 5);
		fd_grpUpdateCheck.right = new FormAttachment(100, -5);
		this.grpUpdateCheck.setLayoutData(fd_grpUpdateCheck);

		FontData[] fD_grpUpdateCheck = this.grpUpdateCheck.getFont().getFontData();
		fD_grpUpdateCheck[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpUpdateCheck.setFont(new Font(Display.getCurrent(), fD_grpUpdateCheck[0]));

		this.btnUpdateCheck = new Button(this.grpUpdateCheck, SWT.CHECK);
		FormData fd_btnUpdateCheck = new FormData();
		fd_btnUpdateCheck.right = new FormAttachment(100, -5);
		fd_btnUpdateCheck.top = new FormAttachment(0);
		fd_btnUpdateCheck.left = new FormAttachment(0, 5);
		this.btnUpdateCheck.setLayoutData(fd_btnUpdateCheck);

		FontData[] fD_btnUpdateCheck = this.btnUpdateCheck.getFont().getFontData();
		fD_btnUpdateCheck[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnUpdateCheck.setFont(new Font(Display.getCurrent(), fD_btnUpdateCheck[0]));

		this.btnUpdateCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this
						.performUpdateCheckSelection(AdvancedConfigurationComposite.this.btnUpdateCheck.getSelection());
			}
		});

		this.grpProxy = new Group(this, SWT.NONE);
		FormData fd_grpProxy = new FormData();
		fd_grpProxy.right = new FormAttachment(100, -5);
		fd_grpProxy.top = new FormAttachment(this.grpUpdateCheck, 5);
		fd_grpProxy.left = new FormAttachment(0, 5);
		this.grpProxy.setLayoutData(fd_grpProxy);
		this.grpProxy.setLayout(new GridLayout(2, false));

		FontData[] fD_grpProxy = this.grpProxy.getFont().getFontData();
		fD_grpProxy[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpProxy.setFont(new Font(Display.getCurrent(), fD_grpProxy[0]));

		this.lblProxyHost = new Label(this.grpProxy, SWT.NONE);
		GridData gd_lblProxyHost = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblProxyHost.widthHint = 66;
		this.lblProxyHost.setLayoutData(gd_lblProxyHost);
		this.lblProxyHost.setBounds(0, 0, 57, 15);

		FontData[] fD_lblProxyHost = this.lblProxyHost.getFont().getFontData();
		fD_lblProxyHost[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblProxyHost.setFont(new Font(Display.getCurrent(), fD_lblProxyHost[0]));

		Composite compProxyHostContainer = new Composite(this.grpProxy, SWT.NONE);
		compProxyHostContainer.setLayout(new FormLayout());
		compProxyHostContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		this.txtProxyHost = new Text(compProxyHostContainer, SWT.BORDER);
		FormData fd_txtProxyHost = new FormData();
		fd_txtProxyHost.right = new FormAttachment(100, -42);
		fd_txtProxyHost.top = new FormAttachment(0);
		fd_txtProxyHost.left = new FormAttachment(0, 5);

		FontData[] fD_txtProxyHost = this.txtProxyHost.getFont().getFontData();
		fD_txtProxyHost[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.txtProxyHost.setFont(new Font(Display.getCurrent(), fD_txtProxyHost[0]));

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

		this.txtProxyHost.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				processProxyHostChanged();
			}
		});

		this.lblProxyPort = new Label(this.grpProxy, SWT.NONE);
		this.lblProxyPort.setBounds(0, 0, 57, 15);

		FontData[] fD_lblProxyPort = this.lblProxyPort.getFont().getFontData();
		fD_lblProxyPort[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblProxyPort.setFont(new Font(Display.getCurrent(), fD_lblProxyPort[0]));

		Composite compProxyPortContainer = new Composite(this.grpProxy, SWT.NONE);
		compProxyPortContainer.setLayout(new FormLayout());
		compProxyPortContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		this.txtProxyPort = new Text(compProxyPortContainer, SWT.BORDER);
		this.fd_txtProxyPort = new FormData();
		this.fd_txtProxyPort.top = new FormAttachment(0, 0);
		this.fd_txtProxyPort.left = new FormAttachment(0, 5);
		this.fd_txtProxyPort.right = new FormAttachment(100, -42);
		this.txtProxyPort.setLayoutData(this.fd_txtProxyPort);

		FontData[] fD_txtProxyPort = this.txtProxyPort.getFont().getFontData();
		fD_txtProxyPort[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.txtProxyPort.setFont(new Font(Display.getCurrent(), fD_txtProxyPort[0]));

		this.txtProxyPort.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				processProxyPortChanged();
			}
		});

		this.txtProxyPortErrorMarker = new ErrorMarker(compProxyPortContainer, SWT.NONE, ""); //$NON-NLS-1$
		this.fd_txtProxyPortErrorMarker = new FormData();
		this.fd_txtProxyPortErrorMarker.left = new FormAttachment(100, -32);
		this.fd_txtProxyPortErrorMarker.right = new FormAttachment(100);
		this.fd_txtProxyPortErrorMarker.top = new FormAttachment(0);
		this.fd_txtProxyPortErrorMarker.bottom = new FormAttachment(0, 32);
		this.txtProxyPortErrorMarker.setLayoutData(this.fd_txtProxyPortErrorMarker);
		this.txtProxyPortErrorMarker.setVisible(false);

		this.txtProxyPort.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				processProxyPortChanged();
			}
		});
		reloadResources();
	}

	private void performPostFixChanged(String postfix) {

		log.debug("Save file postfix changed to : {}", postfix); //$NON-NLS-1$
		this.configurationContainer.setSaveFilePostFix(postfix);
		AdvancedConfigurationComposite.this.txtSaveFilePostFix.setText(postfix);
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
		log.debug("Selected Output folder: {}", foldername); //$NON-NLS-1$
		this.configurationContainer.setOutputFolder(foldername);
		AdvancedConfigurationComposite.this.txtOutputFolder.setText(foldername);
	}

	int getBKUElementIndex(BKUs bku) {
		String lookup = "BKU." + bku.toString(); //$NON-NLS-1$
		String bkuName = Messages.getString(lookup);

		int i = this.bkuStrings.indexOf(bkuName);
		if (i == -1) {
			log.warn("NO BKU match for {}", bkuName); //$NON-NLS-1$
			return 0;
		}
		return i;
	}

	void performBKUSelectionChanged(BKUs selected) {
		log.debug("Selected BKU: {}", selected); //$NON-NLS-1$
		this.configurationContainer.setDefaultBKU(selected);
		this.cmbBKUAuswahl.select(this.getBKUElementIndex(selected));
	}

	void performBKUSelectionChanged(String selected) {
		try {
			BKUs bkuvalue = resolveBKU(selected);
			this.performBKUSelectionChanged(bkuvalue);
		} catch (Exception ex) {
			log.error("Failed to parse BKU value: {} {}", selected, ex); //$NON-NLS-1$
			ErrorDialog dialog = new ErrorDialog(getShell(), Messages.getString("error.InvalidBKU"), BUTTONS.OK); //$NON-NLS-1$
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
				log.debug("Locale: {} IDX: {}", locale, i); //$NON-NLS-1$ //$NON-NLS-2$
				return i;
			}
		}

		log.warn("NO Locale match for {}", locale); //$NON-NLS-1$
		return 0;
	}

	void performLocaleSelectionChanged(Locale selected) {
		log.debug("Selected Locale: {}", selected); //$NON-NLS-1$
		this.configurationContainer.setLocale(selected);
		this.cmbLocaleAuswahl.select(this.getLocaleElementIndex(selected));
	}

	void performPositionSelection(boolean automatic) {
		log.debug("Selected Position: {}", automatic); //$NON-NLS-1$
		SignaturePosition pos = automatic ? new SignaturePosition() : null;
		this.configurationContainer.setDefaultSignaturePosition(pos);
		this.btnAutomatischePositionierung.setSelection(automatic);
	}

	void performUseMarkerSelection(boolean useMarker) {
		this.configurationContainer.setUseMarker(useMarker);
		this.btnPlatzhalterVerwenden.setSelection(useMarker);
	}

	void performUseSignatureFieldsSelection(boolean useFields) {
		this.configurationContainer.setUseSignatureFields(useFields);
		this.btnSignatureFieldsUsage.setSelection(useFields);
	}

	void performEnableUsePlaceholder(boolean enable) {
		this.btnPlatzhalterVerwenden.setEnabled(enable);
		this.btnSignatureFieldsUsage.setEnabled(enable);
		this.configurationContainer.setEnablePlaceholderUsage(enable);
		this.btnEnablePlaceholderUsage.setSelection(enable);
	}

	void performPdfACompatSelection(boolean compat) {
		this.configurationContainer.setSignaturePdfACompat(compat);
		this.btnPdfACompat.setSelection(compat);
	}

	void performKeystoreEnabledSelection(boolean enabled) {
		this.configurationContainer.setKeyStoreEnabled(enabled);
		this.btnKeystoreEnabled.setSelection(enabled);
		this.configurationComposite.keystoreEnabled(enabled);

		int ksIndex = getBKUElementIndex(BKUs.KS);
		String ksText = this.bkuStrings.get(ksIndex);
		if (enabled) {
			if (!this.cmbBKUAuswahl.getItem(ksIndex).equals(ksText))
				this.cmbBKUAuswahl.add(ksText, ksIndex);
		} else {
			int i = this.cmbBKUAuswahl.indexOf(ksText);
			if (i != -1) {
				if (this.cmbBKUAuswahl.getSelectionIndex() == i)
					performBKUSelectionChanged(BKUs.NONE);
				this.cmbBKUAuswahl.remove(i);
			}
		}
	}

	void performPlaceholderTransparency(int transparency) {
		this.configurationContainer.setPlaceholderTransparency(transparency);
	}

	void performUpdateCheckSelection(boolean checkUpdate) {
		this.configurationContainer.setUpdateCheck(checkUpdate);
		this.btnUpdateCheck.setSelection(checkUpdate);
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
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#initConfiguration(
	 * at.asit.pdfover.gui.workflow.config.PersistentConfigProvider)
	 */
	@Override
	public void initConfiguration(PersistentConfigProvider provider) {
		this.configurationContainer.setDefaultSignaturePosition(provider.getDefaultSignaturePositionPersistent());
		this.configurationContainer.setUseMarker(provider.getUseMarker());
		this.configurationContainer.setUseSignatureFields(provider.getUseSignatureFields());
		this.configurationContainer.setEnablePlaceholderUsage(provider.getEnablePlaceholderUsage());
		this.configurationContainer.setSignaturePdfACompat(provider.getSignaturePdfACompat());
		this.configurationContainer.setPlaceholderTransparency(provider.getPlaceholderTransparency());

		this.configurationContainer.setDefaultBKU(provider.getDefaultBKUPersistent());
		this.configurationContainer.setKeyStoreEnabled(provider.getKeyStoreEnabledPersistent());

		this.configurationContainer.setOutputFolder(provider.getDefaultOutputFolderPersistent());
		this.configurationContainer.setSaveFilePostFix(provider.getSaveFilePostFix());

		this.configurationContainer.setLocale(provider.getLocale());

		this.configurationContainer.setUpdateCheck(provider.getUpdateCheck());

		this.configurationContainer.setProxyHost(provider.getProxyHostPersistent());
		try {
			this.configurationContainer.setProxyPort(provider.getProxyPortPersistent());
		} catch (InvalidPortException e) {
			log.error("Failed to set proxy port!", e); //$NON-NLS-1$
		}
		this.configurationContainer.setProxyUser(provider.getProxyUserPersistent());
		this.configurationContainer.setProxyPass(provider.getProxyPassPersistent());
		this.configurationContainer.setSignatureProfile(Profile.getProfile(provider.getSignatureProfile()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#loadConfiguration()
	 */
	@Override
	public void loadConfiguration() {
		// load advanced settings
		performBKUSelectionChanged(this.configurationContainer.getDefaultBKU());
		String outputFolder = this.configurationContainer.getOutputFolder();
		if (outputFolder != null) {
			performOutputFolderChanged(outputFolder);
		}
		String postFix = this.configurationContainer.getSaveFilePostFix();
		if (postFix != null) {
			performPostFixChanged(postFix);
		} else {
			performPostFixChanged(Constants.DEFAULT_POSTFIX);
		}
		SignaturePosition pos = this.configurationContainer.getDefaultSignaturePosition();
		performPositionSelection(pos != null && pos.useAutoPositioning());
		performUseMarkerSelection(this.configurationContainer.getUseMarker());
		performUseSignatureFieldsSelection(this.configurationContainer.getUseSignatureFields());
		performEnableUsePlaceholder(this.configurationContainer.getEnablePlaceholderUsage());
		this.sclTransparenz.setSelection(this.configurationContainer.getPlaceholderTransparency());
		performLocaleSelectionChanged(this.configurationContainer.getLocale());
		performPdfACompatSelection(this.configurationContainer.getSignaturePdfACompat());
		performKeystoreEnabledSelection(this.configurationContainer.getKeyStoreEnabled());
		performUpdateCheckSelection(this.configurationContainer.getUpdateCheck());
		performSetSignatureProfile(this.configurationContainer.getSignatureProfile());

		int port = this.configurationContainer.getProxyPort();
		if (port > 0) {
			this.txtProxyPort.setText(Integer.toString(port));
		}

		String host = this.configurationContainer.getProxyHost();
		if (host != null) {
			this.txtProxyHost.setText(host);
		}

	}

	/**
	 * @param profile
	 * 
	 */
	public void performSetSignatureProfile(Profile profile) {
		switch (profile) {
		case INVISIBLE:
			this.performPositionSelection(true);
			this.btnAutomatischePositionierung.setEnabled(false);
			this.btnEnablePlaceholderUsage.setEnabled(false);
			this.performEnableUsePlaceholder(false);
			break;
		default:
			this.btnAutomatischePositionierung.setEnabled(true);
			this.btnEnablePlaceholderUsage.setEnabled(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#storeConfiguration(
	 * at.asit.pdfover.gui.workflow.config.ConfigManipulator,
	 * at.asit.pdfover.gui.workflow.config.PersistentConfigProvider)
	 */
	@Override
	public void storeConfiguration(ConfigManipulator store, PersistentConfigProvider provider) {
		store.setDefaultSignaturePosition(this.configurationContainer.getDefaultSignaturePosition());
		store.setUseMarker(this.configurationContainer.getUseMarker());
		store.setUseSignatureFields(this.configurationContainer.getUseSignatureFields());
		store.setEnablePlaceholderUsage(this.configurationContainer.getEnablePlaceholderUsage());
		store.setSignaturePdfACompat(this.configurationContainer.getSignaturePdfACompat());
		store.setPlaceholderTransparency(this.configurationContainer.getPlaceholderTransparency());

		store.setDefaultBKU(this.configurationContainer.getDefaultBKU());
		store.setKeyStoreEnabled(this.configurationContainer.getKeyStoreEnabled());

		store.setDefaultOutputFolder(this.configurationContainer.getOutputFolder());
		store.setSaveFilePostFix(this.configurationContainer.getSaveFilePostFix());
		store.setLocale(this.configurationContainer.getLocale());

		store.setUpdateCheck(this.configurationContainer.getUpdateCheck());

		store.setSignatureProfile(this.configurationContainer.getSignatureProfile().name());

		String hostOld = provider.getProxyHostPersistent();
		String hostNew = this.configurationContainer.getProxyHost();
		if (hostOld != null && !hostOld.isEmpty() && (hostNew == null || hostNew.isEmpty())) {
			// Proxy has been removed, let's clear the system properties
			// Otherwise, the proxy settings wouldn't get removed
			System.clearProperty("http.proxyHost"); //$NON-NLS-1$
			System.clearProperty("https.proxyHost"); //$NON-NLS-1$
		}
		store.setProxyHost(hostNew);

		int portOld = provider.getProxyPortPersistent();
		int portNew = this.configurationContainer.getProxyPort();
		if (portOld != -1 && portNew == -1) {
			// cf. above
			System.clearProperty("http.proxyPort"); //$NON-NLS-1$
			System.clearProperty("https.proxyPort"); //$NON-NLS-1$
		}
		store.setProxyPort(portNew);

		String userOld = provider.getProxyUserPersistent();
		String userNew = this.configurationContainer.getProxyUser();
		if (userOld != null && !userOld.isEmpty() && (userNew == null || userNew.isEmpty())) {
			// cf. above
			System.clearProperty("http.proxyUser"); //$NON-NLS-1$
			System.clearProperty("https.proxyUser"); //$NON-NLS-1$
		}
		store.setProxyUser(userNew);

		String passOld = provider.getProxyPassPersistent();
		String passNew = this.configurationContainer.getProxyPass();
		if (passOld != null && passNew == null) {
			// cf. above
			System.clearProperty("http.proxyPassword"); //$NON-NLS-1$
			System.clearProperty("https.proxyPassword"); //$NON-NLS-1$
		}
		store.setProxyPass(passNew);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#validateSettings()
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
			// case 3:
			// this.plainProxyUserSetter();
			// // Fall through
			// case 4:
			// this.plainProxyPassSetter();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		this.grpSignatur.setText(Messages.getString("advanced_config.Signature_Title")); //$NON-NLS-1$
		this.btnAutomatischePositionierung.setText(Messages.getString("advanced_config.AutoPosition")); //$NON-NLS-1$
		this.btnAutomatischePositionierung.setToolTipText(Messages.getString("advanced_config.AutoPosition_ToolTip")); //$NON-NLS-1$
		this.grpPlaceholder.setText(Messages.getString("advanced_config.Placeholder_Title")); //$NON-NLS-1$
		this.btnPlatzhalterVerwenden.setText(Messages.getString("advanced_config.UseMarker")); //$NON-NLS-1$
		this.btnPlatzhalterVerwenden.setToolTipText(Messages.getString("advanced_config.UseMarker_ToolTip")); //$NON-NLS-1$
		this.btnSignatureFieldsUsage.setText(Messages.getString("advanced_config.UseSignatureFields")); //$NON-NLS-1$
		this.btnSignatureFieldsUsage.setToolTipText(Messages.getString("advanced_config.UseSignatureFields_ToolTip")); //$NON-NLS-1$
		this.btnEnablePlaceholderUsage.setText(Messages.getString("advanced_config.Placeholder_Enabled"));
		this.btnPdfACompat.setText(Messages.getString("advanced_config.PdfACompat")); //$NON-NLS-1$
		this.btnPdfACompat.setToolTipText(Messages.getString("advanced_config.PdfACompat_ToolTip")); //$NON-NLS-1$
		this.lblTransparenz.setText(Messages.getString("advanced_config.SigPHTransparency")); //$NON-NLS-1$
		this.lblTransparenzLinks.setText(Messages.getString("advanced_config.SigPHTransparencyMin")); //$NON-NLS-1$
		this.lblTransparenzRechts.setText(Messages.getString("advanced_config.SigPHTransparencyMax")); //$NON-NLS-1$
		this.sclTransparenz.setToolTipText(Messages.getString("advanced_config.SigPHTransparencyTooltip")); //$NON-NLS-1$

		this.grpBkuAuswahl.setText(Messages.getString("advanced_config.BKUSelection_Title")); //$NON-NLS-1$
		this.cmbBKUAuswahl.setToolTipText(Messages.getString("advanced_config.BKUSelection_ToolTip")); //$NON-NLS-1$
		this.btnKeystoreEnabled.setText(Messages.getString("advanced_config.KeystoreEnabled")); //$NON-NLS-1$
		this.btnKeystoreEnabled.setToolTipText(Messages.getString("advanced_config.KeystoreEnabled_ToolTip")); //$NON-NLS-1$

		this.grpSpeicherort.setText(Messages.getString("advanced_config.OutputFolder_Title")); //$NON-NLS-1$
		this.lblDefaultOutputFolder.setText(Messages.getString("advanced_config.OutputFolder")); //$NON-NLS-1$
		this.txtOutputFolder.setToolTipText(Messages.getString("advanced_config.OutputFolder_ToolTip")); //$NON-NLS-1$
		this.btnBrowse.setText(Messages.getString("common.browse")); //$NON-NLS-1$

		this.grpLocaleAuswahl.setText(Messages.getString("advanced_config.LocaleSelection_Title")); //$NON-NLS-1$
		this.cmbLocaleAuswahl.setToolTipText(Messages.getString("advanced_config.LocaleSelection_ToolTip")); //$NON-NLS-1$

		this.grpUpdateCheck.setText(Messages.getString("advanced_config.UpdateCheck_Title")); //$NON-NLS-1$
		this.btnUpdateCheck.setText(Messages.getString("advanced_config.UpdateCheck")); //$NON-NLS-1$
		this.btnUpdateCheck.setToolTipText(Messages.getString("advanced_config.UpdateCheck_ToolTip")); //$NON-NLS-1$

		this.grpProxy.setText(Messages.getString("advanced_config.Proxy_Title")); //$NON-NLS-1$
		this.lblProxyHost.setText(Messages.getString("advanced_config.ProxyHost")); //$NON-NLS-1$
		this.txtProxyHost.setToolTipText(Messages.getString("advanced_config.ProxyHost_ToolTip")); //$NON-NLS-1$
		this.txtProxyHost.setMessage(Messages.getString("advanced_config.ProxyHost_Template")); //$NON-NLS-1$
		this.lblProxyPort.setText(Messages.getString("advanced_config.ProxyPort")); //$NON-NLS-1$
		this.txtProxyPort.setToolTipText(Messages.getString("advanced_config.ProxyPort_ToolTip")); //$NON-NLS-1$
		this.txtProxyPort.setMessage(Messages.getString("advanced_config.ProxyPort_Template")); //$NON-NLS-1$
		// this.lblProxyUser.setText(Messages.getString("advanced_config.ProxyUser"));
		// //$NON-NLS-1$
		// this.txtProxyUser.setToolTipText(Messages
		// .getString("advanced_config.ProxyUser_ToolTip")); //$NON-NLS-1$
		// this.txtProxyUser.setMessage(Messages
		// .getString("advanced_config.ProxyUser_Template")); //$NON-NLS-1$
		// this.lblProxyPass.setText(Messages.getString("advanced_config.ProxyPass"));
		// //$NON-NLS-1$
		// this.txtProxyPass.setToolTipText(Messages
		// .getString("advanced_config.ProxyPass_ToolTip")); //$NON-NLS-1$
		// this.txtProxyPass.setMessage(Messages
		// .getString("advanced_config.ProxyPass_Template")); //$NON-NLS-1$
	}
}
