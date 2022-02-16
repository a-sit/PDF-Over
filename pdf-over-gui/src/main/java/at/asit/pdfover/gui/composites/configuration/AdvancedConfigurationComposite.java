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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
public class AdvancedConfigurationComposite extends ConfigurationCompositeBase {

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
		ConfigurationCompositeBase.anchor(grpSignatur).top(0,5).right(100,-5).left(0,5).set();
		ConfigurationCompositeBase.setFontHeight(grpSignatur, Constants.TEXT_SIZE_NORMAL);

		this.btnAutomatischePositionierung = new Button(this.grpSignatur, SWT.CHECK);
		ConfigurationCompositeBase.anchor(btnAutomatischePositionierung).right(100,-5).top(0).left(0,5).set();
		ConfigurationCompositeBase.setFontHeight(btnAutomatischePositionierung, Constants.TEXT_SIZE_BUTTON);

		this.btnAutomatischePositionierung.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.performPositionSelection(
						AdvancedConfigurationComposite.this.btnAutomatischePositionierung.getSelection());
			}
		});

		this.btnPdfACompat = new Button(this.grpSignatur, SWT.CHECK);
		ConfigurationCompositeBase.anchor(btnPdfACompat).right(100,-5).top(btnAutomatischePositionierung, 5).left(0,5).set();
		ConfigurationCompositeBase.setFontHeight(btnPdfACompat, Constants.TEXT_SIZE_BUTTON);

		this.btnPdfACompat.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this
						.performPdfACompatSelection(AdvancedConfigurationComposite.this.btnPdfACompat.getSelection());
			}
		});

		this.lblTransparenz = new Label(this.grpSignatur, SWT.HORIZONTAL);
		ConfigurationCompositeBase.anchor(lblTransparenz).top(btnPdfACompat, 5).left(0,5).set();
		ConfigurationCompositeBase.setFontHeight(lblTransparenz, Constants.TEXT_SIZE_NORMAL);

		this.lblTransparenzLinks = new Label(this.grpSignatur, SWT.HORIZONTAL);
		ConfigurationCompositeBase.anchor(lblTransparenzLinks).top(lblTransparenz, 5).left(0,15).set();
		ConfigurationCompositeBase.setFontHeight(lblTransparenzLinks, Constants.TEXT_SIZE_NORMAL);

		this.lblTransparenzRechts = new Label(this.grpSignatur, SWT.HORIZONTAL);
		ConfigurationCompositeBase.anchor(lblTransparenzRechts).top(lblTransparenz, 5).right(100,-5).set();
		ConfigurationCompositeBase.setFontHeight(lblTransparenzRechts, Constants.TEXT_SIZE_NORMAL);

		this.sclTransparenz = new Scale(this.grpSignatur, SWT.HORIZONTAL);
		ConfigurationCompositeBase.anchor(sclTransparenz).right(lblTransparenzRechts, -5).top(lblTransparenz, 5).left(lblTransparenzLinks, 5).set();
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
		ConfigurationCompositeBase.anchor(grpPlaceholder).top(grpSignatur, 5).left(0,5).right(100,-5).set();
		ConfigurationCompositeBase.setFontHeight(grpPlaceholder, Constants.TEXT_SIZE_NORMAL);

		this.btnEnablePlaceholderUsage = new Button(this.grpPlaceholder, SWT.CHECK);
		ConfigurationCompositeBase.anchor(btnEnablePlaceholderUsage).top(0,5).left(0,5).right(100,-5).set();
		ConfigurationCompositeBase.setFontHeight(btnEnablePlaceholderUsage, Constants.TEXT_SIZE_BUTTON);

		this.btnEnablePlaceholderUsage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.performEnableUsePlaceholder(
						AdvancedConfigurationComposite.this.btnEnablePlaceholderUsage.getSelection());

			}
		});

		this.btnPlatzhalterVerwenden = new Button(this.grpPlaceholder, SWT.RADIO);
		ConfigurationCompositeBase.anchor(btnPlatzhalterVerwenden).right(100,-5).top(btnEnablePlaceholderUsage,5).left(0,5).set();
		ConfigurationCompositeBase.setFontHeight(btnPlatzhalterVerwenden, Constants.TEXT_SIZE_BUTTON);

		this.btnPlatzhalterVerwenden.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.performUseMarkerSelection(
						AdvancedConfigurationComposite.this.btnPlatzhalterVerwenden.getSelection());
			}
		});

		this.btnSignatureFieldsUsage = new Button(this.grpPlaceholder, SWT.RADIO);
		ConfigurationCompositeBase.anchor(btnSignatureFieldsUsage).right(100,-5).top(btnPlatzhalterVerwenden, 5).left(0,5).set();
		ConfigurationCompositeBase.setFontHeight(btnSignatureFieldsUsage, Constants.TEXT_SIZE_BUTTON);

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
		ConfigurationCompositeBase.anchor(grpBkuAuswahl).top(grpPlaceholder, 5).left(0,5).right(100,-5).set();
		ConfigurationCompositeBase.setFontHeight(grpBkuAuswahl, Constants.TEXT_SIZE_NORMAL);

		this.cmbBKUAuswahl = new Combo(this.grpBkuAuswahl, SWT.READ_ONLY);
		ConfigurationCompositeBase.anchor(cmbBKUAuswahl).right(100,-5).top(0).left(0,5).set();
		ConfigurationCompositeBase.setFontHeight(cmbBKUAuswahl, Constants.TEXT_SIZE_NORMAL);

		this.bkuStrings = Arrays.stream(BKUs.values()).map(s -> Messages.getString("BKU."+s)).collect(Collectors.toList());
		this.cmbBKUAuswahl.setItems(bkuStrings.toArray(new String[0]));
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
		ConfigurationCompositeBase.anchor(btnKeystoreEnabled).right(100,-5).top(cmbBKUAuswahl,5).left(0,5).set();
		ConfigurationCompositeBase.setFontHeight(btnKeystoreEnabled, Constants.TEXT_SIZE_BUTTON);

		this.btnKeystoreEnabled.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.performKeystoreEnabledSelection(
						AdvancedConfigurationComposite.this.btnKeystoreEnabled.getSelection());
			}
		});

		this.grpSpeicherort = new Group(this, SWT.NONE);
		grpSpeicherort.setLayout(new GridLayout(3, false));
		ConfigurationCompositeBase.anchor(grpSpeicherort).left(0,5).top(grpBkuAuswahl, 5).right(100,-5).set();
		ConfigurationCompositeBase.setFontHeight(grpSpeicherort, Constants.TEXT_SIZE_NORMAL);

		this.lblDefaultOutputFolder = new Label(this.grpSpeicherort, SWT.NONE);
		ConfigurationCompositeBase.setFontHeight(lblDefaultOutputFolder, Constants.TEXT_SIZE_NORMAL);

		this.txtOutputFolder = new Text(this.grpSpeicherort, SWT.BORDER);
		txtOutputFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		ConfigurationCompositeBase.setFontHeight(txtOutputFolder, Constants.TEXT_SIZE_NORMAL);

		this.txtOutputFolder.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				performOutputFolderChanged(AdvancedConfigurationComposite.this.txtOutputFolder.getText());
			}
		});

		this.btnBrowse = new Button(this.grpSpeicherort, SWT.NONE);
		btnBrowse.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		ConfigurationCompositeBase.setFontHeight(btnBrowse, Constants.TEXT_SIZE_BUTTON);

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
		ConfigurationCompositeBase.setFontHeight(lblSaveFilePostFix, Constants.TEXT_SIZE_NORMAL);

		this.txtSaveFilePostFix = new Text(this.grpSpeicherort, SWT.BORDER);
		txtSaveFilePostFix.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		ConfigurationCompositeBase.setFontHeight(txtSaveFilePostFix, Constants.TEXT_SIZE_NORMAL);

		this.txtSaveFilePostFix.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				performPostFixChanged(AdvancedConfigurationComposite.this.txtSaveFilePostFix.getText());
			}
		});

		this.grpLocaleAuswahl = new Group(this, SWT.NONE);
		FormLayout layout_grpLocaleAuswahl = new FormLayout();
		layout_grpLocaleAuswahl.marginHeight = 10;
		layout_grpLocaleAuswahl.marginWidth = 5;
		this.grpLocaleAuswahl.setLayout(layout_grpLocaleAuswahl);
		ConfigurationCompositeBase.anchor(grpLocaleAuswahl).top(grpSpeicherort, 5).left(0,5).right(100,-5).set();
		ConfigurationCompositeBase.setFontHeight(grpLocaleAuswahl, Constants.TEXT_SIZE_NORMAL);

		this.cmbLocaleAuswahl = new Combo(this.grpLocaleAuswahl, SWT.READ_ONLY);
		ConfigurationCompositeBase.anchor(cmbLocaleAuswahl).right(100,-5).top(0).left(0,5).set();
		ConfigurationCompositeBase.setFontHeight(cmbLocaleAuswahl, Constants.TEXT_SIZE_NORMAL);;
		this.cmbLocaleAuswahl.setItems(Arrays.stream(Constants.SUPPORTED_LOCALES).map(l -> l.getDisplayLanguage(l)).toArray(String[]::new));

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
		ConfigurationCompositeBase.anchor(grpUpdateCheck).top(grpLocaleAuswahl, 5).left(0,5).right(100,-5).set();
		ConfigurationCompositeBase.setFontHeight(grpUpdateCheck, Constants.TEXT_SIZE_NORMAL);

		this.btnUpdateCheck = new Button(this.grpUpdateCheck, SWT.CHECK);
		ConfigurationCompositeBase.anchor(btnUpdateCheck).right(100,-5).top(0).left(0,5).set();
		ConfigurationCompositeBase.setFontHeight(btnUpdateCheck, Constants.TEXT_SIZE_BUTTON);

		this.btnUpdateCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.performUpdateCheckSelection(AdvancedConfigurationComposite.this.btnUpdateCheck.getSelection());
			}
		});

		this.grpProxy = new Group(this, SWT.NONE);
		ConfigurationCompositeBase.anchor(grpProxy).right(100,-5).top(grpUpdateCheck, 5).left(0,5).set();
		this.grpProxy.setLayout(new GridLayout(2, false));
		ConfigurationCompositeBase.setFontHeight(grpProxy, Constants.TEXT_SIZE_NORMAL);

		this.lblProxyHost = new Label(this.grpProxy, SWT.NONE);
		do { /* grid positioning */
			GridData gd_lblProxyHost = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_lblProxyHost.widthHint = 66;
			this.lblProxyHost.setLayoutData(gd_lblProxyHost);
			this.lblProxyHost.setBounds(0, 0, 57, 15);
		} while (false);
		ConfigurationCompositeBase.setFontHeight(lblProxyHost, Constants.TEXT_SIZE_NORMAL);

		Composite compProxyHostContainer = new Composite(this.grpProxy, SWT.NONE);
		compProxyHostContainer.setLayout(new FormLayout());
		compProxyHostContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		this.txtProxyHost = new Text(compProxyHostContainer, SWT.BORDER);
		ConfigurationCompositeBase.anchor(txtProxyHost).right(100,-42).top(0).left(0,5).set();
		ConfigurationCompositeBase.setFontHeight(txtProxyHost, Constants.TEXT_SIZE_NORMAL);

		this.proxyHostErrorMarker = new ErrorMarker(compProxyHostContainer, SWT.NONE, ""); //$NON-NLS-1$
		ConfigurationCompositeBase.anchor(proxyHostErrorMarker).left(100,-32).right(100).top(0).bottom(0,32).set();
		this.proxyHostErrorMarker.setVisible(false);

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
		ConfigurationCompositeBase.setFontHeight(lblProxyPort, Constants.TEXT_SIZE_NORMAL);

		Composite compProxyPortContainer = new Composite(this.grpProxy, SWT.NONE);
		compProxyPortContainer.setLayout(new FormLayout());
		compProxyPortContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		this.txtProxyPort = new Text(compProxyPortContainer, SWT.BORDER);
		ConfigurationCompositeBase.anchor(txtProxyPort).top(0,0).left(0,5).right(100,-42).set();
		ConfigurationCompositeBase.setFontHeight(txtProxyPort, Constants.TEXT_SIZE_NORMAL);

		this.txtProxyPort.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				processProxyPortChanged();
			}
		});

		this.txtProxyPortErrorMarker = new ErrorMarker(compProxyPortContainer, SWT.NONE, ""); //$NON-NLS-1$
		ConfigurationCompositeBase.anchor(txtProxyPortErrorMarker).left(100,-32).right(100).top(0).bottom(0,32).set();
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
