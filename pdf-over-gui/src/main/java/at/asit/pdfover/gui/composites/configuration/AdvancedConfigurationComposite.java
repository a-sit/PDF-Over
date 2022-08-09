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
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.commons.Profile;
import at.asit.pdfover.gui.composites.ConfigurationComposite;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.controls.ErrorMarker;
import at.asit.pdfover.gui.exceptions.InvalidPortException;
import at.asit.pdfover.gui.exceptions.OutputfolderDoesntExistException;
import at.asit.pdfover.gui.exceptions.OutputfolderNotADirectoryException;
import at.asit.pdfover.gui.utils.SWTUtils;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;
import at.asit.pdfover.gui.workflow.config.ConfigurationDataInMemory;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.BKUs;

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
	public AdvancedConfigurationComposite(Composite parent, int style, State state, ConfigurationDataInMemory container,
			ConfigurationComposite config) {
		super(parent, style, state, container);
		this.configurationComposite = config;
		setLayout(new FormLayout());

		this.grpSignatur = new Group(this, SWT.NONE);
		FormLayout layout = new FormLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 5;
		this.grpSignatur.setLayout(layout);
		SWTUtils.anchor(grpSignatur).top(0,5).right(100,-5).left(0,5).set();
		SWTUtils.setFontHeight(grpSignatur, Constants.TEXT_SIZE_NORMAL);

		this.btnAutomatischePositionierung = new Button(this.grpSignatur, SWT.CHECK);
		SWTUtils.anchor(btnAutomatischePositionierung).right(100,-5).top(0).left(0,5).set();
		SWTUtils.setFontHeight(btnAutomatischePositionierung, Constants.TEXT_SIZE_BUTTON);

		this.btnAutomatischePositionierung.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.performPositionSelection(
						AdvancedConfigurationComposite.this.btnAutomatischePositionierung.getSelection());
			}
		});

		this.btnPdfACompat = new Button(this.grpSignatur, SWT.CHECK);
		SWTUtils.anchor(btnPdfACompat).right(100,-5).top(btnAutomatischePositionierung, 5).left(0,5).set();
		SWTUtils.setFontHeight(btnPdfACompat, Constants.TEXT_SIZE_BUTTON);

		this.btnPdfACompat.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this
						.performPdfACompatSelection(AdvancedConfigurationComposite.this.btnPdfACompat.getSelection());
			}
		});

		this.grpPlaceholder = new Group(this, SWT.NONE);
		FormLayout layout_grpPlaceholder = new FormLayout();
		layout_grpPlaceholder.marginHeight = 10;
		layout_grpPlaceholder.marginWidth = 5;
		this.grpPlaceholder.setLayout(layout_grpPlaceholder);
		SWTUtils.anchor(grpPlaceholder).top(grpSignatur, 5).left(0,5).right(100,-5).set();
		SWTUtils.setFontHeight(grpPlaceholder, Constants.TEXT_SIZE_NORMAL);

		this.btnEnablePlaceholderUsage = new Button(this.grpPlaceholder, SWT.CHECK);
		SWTUtils.anchor(btnEnablePlaceholderUsage).top(0,5).left(0,5).right(100,-5).set();
		SWTUtils.setFontHeight(btnEnablePlaceholderUsage, Constants.TEXT_SIZE_BUTTON);

		this.btnEnablePlaceholderUsage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.performEnableUsePlaceholder(
						AdvancedConfigurationComposite.this.btnEnablePlaceholderUsage.getSelection());

			}
		});

		this.btnPlatzhalterVerwenden = new Button(this.grpPlaceholder, SWT.RADIO);
		SWTUtils.anchor(btnPlatzhalterVerwenden).right(100,-5).top(btnEnablePlaceholderUsage,5).left(0,5).set();
		SWTUtils.setFontHeight(btnPlatzhalterVerwenden, Constants.TEXT_SIZE_BUTTON);

		this.btnPlatzhalterVerwenden.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.performUseMarkerSelection(
						AdvancedConfigurationComposite.this.btnPlatzhalterVerwenden.getSelection());
			}
		});

		this.btnSignatureFieldsUsage = new Button(this.grpPlaceholder, SWT.RADIO);
		SWTUtils.anchor(btnSignatureFieldsUsage).right(100,-5).top(btnPlatzhalterVerwenden, 5).left(0,5).set();
		SWTUtils.setFontHeight(btnSignatureFieldsUsage, Constants.TEXT_SIZE_BUTTON);

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
		SWTUtils.anchor(grpBkuAuswahl).top(grpPlaceholder, 5).left(0,5).right(100,-5).set();
		SWTUtils.setFontHeight(grpBkuAuswahl, Constants.TEXT_SIZE_NORMAL);

		this.cmbBKUAuswahl = new Combo(this.grpBkuAuswahl, SWT.READ_ONLY);
		SWTUtils.anchor(cmbBKUAuswahl).right(100,-5).top(0).left(0,5).set();
		SWTUtils.setFontHeight(cmbBKUAuswahl, Constants.TEXT_SIZE_NORMAL);
		SWTUtils.disableEventDefault(cmbBKUAuswahl, SWT.MouseVerticalWheel);

		this.bkuStrings = Arrays.stream(BKUs.values()).map(s -> Messages.getString("BKU."+s)).collect(Collectors.toList());
		this.cmbBKUAuswahl.setItems(bkuStrings.toArray(new String[0]));
		this.cmbBKUAuswahl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = getBKUElementIndex(
						AdvancedConfigurationComposite.this.configurationContainer.defaultBKU);
				if (AdvancedConfigurationComposite.this.cmbBKUAuswahl.getSelectionIndex() != selectionIndex) {
					selectionIndex = AdvancedConfigurationComposite.this.cmbBKUAuswahl.getSelectionIndex();
					performBKUSelectionChanged(
							AdvancedConfigurationComposite.this.cmbBKUAuswahl.getItem(selectionIndex));
				}
			}
		});

		this.btnKeystoreEnabled = new Button(this.grpBkuAuswahl, SWT.CHECK);
		SWTUtils.anchor(btnKeystoreEnabled).right(100,-5).top(cmbBKUAuswahl,5).left(0,5).set();
		SWTUtils.setFontHeight(btnKeystoreEnabled, Constants.TEXT_SIZE_BUTTON);

		this.btnKeystoreEnabled.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.performKeystoreEnabledSelection(
						AdvancedConfigurationComposite.this.btnKeystoreEnabled.getSelection());
			}
		});

		this.grpSpeicherort = new Group(this, SWT.NONE);
		grpSpeicherort.setLayout(new GridLayout(3, false));
		SWTUtils.anchor(grpSpeicherort).left(0,5).top(grpBkuAuswahl, 5).right(100,-5).set();
		SWTUtils.setFontHeight(grpSpeicherort, Constants.TEXT_SIZE_NORMAL);

		this.lblDefaultOutputFolder = new Label(this.grpSpeicherort, SWT.NONE);
		SWTUtils.setFontHeight(lblDefaultOutputFolder, Constants.TEXT_SIZE_NORMAL);

		this.txtOutputFolder = new Text(this.grpSpeicherort, SWT.BORDER);
		txtOutputFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		SWTUtils.setFontHeight(txtOutputFolder, Constants.TEXT_SIZE_NORMAL);

		this.txtOutputFolder.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				performOutputFolderChanged(AdvancedConfigurationComposite.this.txtOutputFolder.getText());
			}
		});

		this.btnBrowse = new Button(this.grpSpeicherort, SWT.NONE);
		btnBrowse.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		SWTUtils.setFontHeight(btnBrowse, Constants.TEXT_SIZE_BUTTON);

		this.btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(AdvancedConfigurationComposite.this.getShell());

				// Set the initial filter path according
				// to anything they've selected or typed in
				dlg.setFilterPath(AdvancedConfigurationComposite.this.txtOutputFolder.getText());
				
				// Change the title bar text
				SWTUtils.setLocalizedText(dlg, "advanced_config.OutputFolder.Dialog_Title");

				// Customizable message displayed in the dialog
				dlg.setMessage(Messages.getString("advanced_config.OutputFolder.Dialog"));

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
		SWTUtils.setLocalizedText(lblSaveFilePostFix, "AdvancedConfigurationComposite.lblSaveFilePostFix.text");
		SWTUtils.setFontHeight(lblSaveFilePostFix, Constants.TEXT_SIZE_NORMAL);

		this.txtSaveFilePostFix = new Text(this.grpSpeicherort, SWT.BORDER);
		txtSaveFilePostFix.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		SWTUtils.setFontHeight(txtSaveFilePostFix, Constants.TEXT_SIZE_NORMAL);

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
		SWTUtils.anchor(grpLocaleAuswahl).top(grpSpeicherort, 5).left(0,5).right(100,-5).set();
		SWTUtils.setFontHeight(grpLocaleAuswahl, Constants.TEXT_SIZE_NORMAL);

		this.cmbLocaleAuswahl = new Combo(this.grpLocaleAuswahl, SWT.READ_ONLY);
		SWTUtils.anchor(cmbLocaleAuswahl).right(100,-5).top(0).left(0,5).set();
		SWTUtils.setFontHeight(cmbLocaleAuswahl, Constants.TEXT_SIZE_NORMAL);;
		this.cmbLocaleAuswahl.setItems(Arrays.stream(Constants.SUPPORTED_LOCALES).map(l -> l.getDisplayLanguage()).toArray(String[]::new));
		SWTUtils.disableEventDefault(cmbLocaleAuswahl, SWT.MouseVerticalWheel);

		this.cmbLocaleAuswahl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Locale currentLocale = AdvancedConfigurationComposite.this.configurationContainer.interfaceLocale;
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
		SWTUtils.anchor(grpUpdateCheck).top(grpLocaleAuswahl, 5).left(0,5).right(100,-5).set();
		SWTUtils.setFontHeight(grpUpdateCheck, Constants.TEXT_SIZE_NORMAL);

		this.btnUpdateCheck = new Button(this.grpUpdateCheck, SWT.CHECK);
		SWTUtils.anchor(btnUpdateCheck).right(100,-5).top(0).left(0,5).set();
		SWTUtils.setFontHeight(btnUpdateCheck, Constants.TEXT_SIZE_BUTTON);

		this.btnUpdateCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.performUpdateCheckSelection(AdvancedConfigurationComposite.this.btnUpdateCheck.getSelection());
			}
		});

		this.grpProxy = new Group(this, SWT.NONE);
		SWTUtils.anchor(grpProxy).right(100,-5).top(grpUpdateCheck, 5).left(0,5).set();
		this.grpProxy.setLayout(new GridLayout(2, false));
		SWTUtils.setFontHeight(grpProxy, Constants.TEXT_SIZE_NORMAL);

		this.lblProxyHost = new Label(this.grpProxy, SWT.NONE);
		do { /* grid positioning */
			GridData gd_lblProxyHost = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_lblProxyHost.widthHint = 66;
			this.lblProxyHost.setLayoutData(gd_lblProxyHost);
			this.lblProxyHost.setBounds(0, 0, 57, 15);
		} while (false);
		SWTUtils.setFontHeight(lblProxyHost, Constants.TEXT_SIZE_NORMAL);

		Composite compProxyHostContainer = new Composite(this.grpProxy, SWT.NONE);
		compProxyHostContainer.setLayout(new FormLayout());
		compProxyHostContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		this.txtProxyHost = new Text(compProxyHostContainer, SWT.BORDER);
		SWTUtils.anchor(txtProxyHost).right(100,-42).top(0).left(0,5).set();
		SWTUtils.setFontHeight(txtProxyHost, Constants.TEXT_SIZE_NORMAL);

		this.proxyHostErrorMarker = new ErrorMarker(compProxyHostContainer, SWT.NONE, "");
		SWTUtils.anchor(proxyHostErrorMarker).left(100,-32).right(100).top(0).bottom(0,32).set();
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
		SWTUtils.setFontHeight(lblProxyPort, Constants.TEXT_SIZE_NORMAL);

		Composite compProxyPortContainer = new Composite(this.grpProxy, SWT.NONE);
		compProxyPortContainer.setLayout(new FormLayout());
		compProxyPortContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		this.txtProxyPort = new Text(compProxyPortContainer, SWT.BORDER);
		SWTUtils.anchor(txtProxyPort).top(0,0).left(0,5).right(100,-42).set();
		SWTUtils.setFontHeight(txtProxyPort, Constants.TEXT_SIZE_NORMAL);

		this.txtProxyPort.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				processProxyPortChanged();
			}
		});

		this.txtProxyPortErrorMarker = new ErrorMarker(compProxyPortContainer, SWT.NONE, "");
		SWTUtils.anchor(txtProxyPortErrorMarker).left(100,-32).right(100).top(0).bottom(0,32).set();
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

		log.debug("Save file postfix changed to : {}", postfix);
		this.configurationContainer.saveFilePostFix = postfix;
		AdvancedConfigurationComposite.this.txtSaveFilePostFix.setText(postfix);
	}

	void performOutputFolderChanged(String foldername) {
		log.debug("Selected Output folder: {}", foldername);
		this.configurationContainer.outputFolder = foldername;
		AdvancedConfigurationComposite.this.txtOutputFolder.setText(foldername);
	}

	int getBKUElementIndex(BKUs bku) {
		String lookup = "BKU." + bku.toString();
		String bkuName = Messages.getString(lookup);

		int i = this.bkuStrings.indexOf(bkuName);
		if (i == -1) {
			log.warn("NO BKU match for {}", bkuName);
			return 0;
		}
		return i;
	}

	void performBKUSelectionChanged(BKUs selected) {
		log.debug("Selected BKU: {}", selected);
		this.configurationContainer.defaultBKU = selected;
		this.cmbBKUAuswahl.select(this.getBKUElementIndex(selected));
	}

	void performBKUSelectionChanged(String selected) {
		try {
			BKUs bkuvalue = resolveBKU(selected);
			this.performBKUSelectionChanged(bkuvalue);
		} catch (Exception ex) {
			log.error("Failed to parse BKU value: {} {}", selected, ex);
			ErrorDialog dialog = new ErrorDialog(getShell(), Messages.getString("error.InvalidBKU"), BUTTONS.OK);
			dialog.open();
		}
	}

	BKUs resolveBKU(String localizedBKU) {
		int blen = BKUs.values().length;

		for (int i = 0; i < blen; i++) {
			String lookup = "BKU." + BKUs.values()[i].toString();
			if (Messages.getString(lookup).equals(localizedBKU)) {
				return BKUs.values()[i];
			}
		}

		return BKUs.NONE;
	}

	int getLocaleElementIndex(Locale locale) {
		for (int i = 0; i < Constants.SUPPORTED_LOCALES.length; i++) {
			if (Constants.SUPPORTED_LOCALES[i].equals(locale)) {
				log.debug("Locale: {} IDX: {}", locale, i);
				return i;
			}
		}

		log.warn("NO Locale match for {}", locale);
		return 0;
	}

	void performLocaleSelectionChanged(Locale selected) {
		log.debug("Selected Locale: {}", selected);
		this.configurationContainer.interfaceLocale = selected;
		this.cmbLocaleAuswahl.select(this.getLocaleElementIndex(selected));
	}

	void performPositionSelection(boolean automatic) {
		log.debug("Selected Position: {}", automatic);
		this.configurationContainer.autoPositionSignature = automatic;
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
		this.configurationContainer.enabledPlaceholderUsage = enable;
		this.btnEnablePlaceholderUsage.setSelection(enable);
	}

	void performPdfACompatSelection(boolean compat) {
		this.configurationContainer.signaturePDFACompat = compat;
		this.btnPdfACompat.setSelection(compat);
	}

	void performKeystoreEnabledSelection(boolean enabled) {
		this.configurationContainer.keystoreEnabled = enabled;
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

	void performUpdateCheckSelection(boolean checkUpdate) {
		this.configurationContainer.updateCheck = checkUpdate;
		this.btnUpdateCheck.setSelection(checkUpdate);
	}

	void processProxyHostChanged() {
		try {
			this.proxyHostErrorMarker.setVisible(false);
			plainProxyHostSetter();
		} catch (Exception ex) {
			this.proxyHostErrorMarker.setVisible(true);
			this.proxyHostErrorMarker.setToolTipText(ex.getMessage());
			log.error("processProxyHost: ", ex);
		}
	}

	/**
	 *
	 */
	private void plainProxyHostSetter() {
		String host = this.txtProxyHost.getText();
		this.configurationContainer.proxyHost = host;
	}

	void processProxyPortChanged() {
		try {
			this.txtProxyPortErrorMarker.setVisible(false);
			plainProxyPortSetter();
		} catch (Exception ex) {
			this.txtProxyPortErrorMarker.setVisible(true);
			this.txtProxyPortErrorMarker.setToolTipText(ex.getMessage());
			log.error("processProxyPort: ", ex);
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

	@Override
	public void initConfiguration(ConfigurationManager provider) {
		this.configurationContainer.autoPositionSignature = provider.getAutoPositionSignaturePersistent();
		this.configurationContainer.setUseMarker(provider.getUseMarker());
		this.configurationContainer.setUseSignatureFields(provider.getUseSignatureFields());
		this.configurationContainer.enabledPlaceholderUsage = provider.getEnablePlaceholderUsage();
		this.configurationContainer.signaturePDFACompat = provider.getSignaturePdfACompat();

		this.configurationContainer.defaultBKU = provider.getDefaultBKUPersistent();
		this.configurationContainer.keystoreEnabled = provider.getKeyStoreEnabledPersistent();

		this.configurationContainer.outputFolder = provider.getDefaultOutputFolderPersistent();
		this.configurationContainer.saveFilePostFix = provider.getSaveFilePostFix();

		this.configurationContainer.interfaceLocale = provider.getInterfaceLocale();

		this.configurationContainer.updateCheck = provider.getUpdateCheck();

		this.configurationContainer.proxyHost = provider.getProxyHostPersistent();
		try {
			this.configurationContainer.setProxyPort(provider.getProxyPortPersistent());
		} catch (InvalidPortException e) {
			log.error("Failed to set proxy port!", e);
		}
		this.configurationContainer.proxyUser = provider.getProxyUserPersistent();
		this.configurationContainer.proxyPass = provider.getProxyPassPersistent();
		this.configurationContainer.setSignatureProfile(provider.getSignatureProfile());
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
		performBKUSelectionChanged(this.configurationContainer.defaultBKU);
		String outputFolder = this.configurationContainer.outputFolder;
		if (outputFolder != null) {
			performOutputFolderChanged(outputFolder);
		}
		String postFix = this.configurationContainer.saveFilePostFix;
		if (postFix != null) {
			performPostFixChanged(postFix);
		} else {
			performPostFixChanged(Constants.DEFAULT_POSTFIX);
		}
		performPositionSelection(this.configurationContainer.autoPositionSignature);
		performUseMarkerSelection(this.configurationContainer.getUseMarker());
		performUseSignatureFieldsSelection(this.configurationContainer.getUseSignatureFields());
		performEnableUsePlaceholder(this.configurationContainer.enabledPlaceholderUsage);
		performLocaleSelectionChanged(this.configurationContainer.interfaceLocale);
		performPdfACompatSelection(this.configurationContainer.signaturePDFACompat);
		performKeystoreEnabledSelection(this.configurationContainer.keystoreEnabled);
		performUpdateCheckSelection(this.configurationContainer.updateCheck);
		performSetSignatureProfile(this.configurationContainer.getSignatureProfile());

		int port = this.configurationContainer.getProxyPort();
		if (port > 0) {
			this.txtProxyPort.setText(Integer.toString(port));
		}

		String host = this.configurationContainer.proxyHost;
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

	@Override
	public void storeConfiguration(ConfigurationManager store) {
		store.setAutoPositionSignaturePersistent(this.configurationContainer.autoPositionSignature);
		store.setUseMarkerPersistent(this.configurationContainer.getUseMarker());
		store.setUseSignatureFieldsPersistent(this.configurationContainer.getUseSignatureFields());
		store.setEnablePlaceholderUsagePersistent(this.configurationContainer.enabledPlaceholderUsage);
		store.setSignaturePdfACompatPersistent(this.configurationContainer.signaturePDFACompat);

		store.setDefaultBKUPersistent(this.configurationContainer.defaultBKU);
		store.setKeyStoreEnabledPersistent(this.configurationContainer.keystoreEnabled);

		store.setDefaultOutputFolderPersistent(this.configurationContainer.outputFolder);
		store.setSaveFilePostFixPersistent(this.configurationContainer.saveFilePostFix);
		store.setInterfaceLocalePersistent(this.configurationContainer.interfaceLocale);

		store.setUpdateCheckPersistent(this.configurationContainer.updateCheck);

		store.setSignatureProfilePersistent(this.configurationContainer.getSignatureProfile());

		String hostOld = store.getProxyHostPersistent();
		String hostNew = this.configurationContainer.proxyHost;
		if (hostOld != null && !hostOld.isEmpty() && (hostNew == null || hostNew.isEmpty())) {
			// Proxy has been removed, let's clear the system properties
			// Otherwise, the proxy settings wouldn't get removed
			System.clearProperty("http.proxyHost");
			System.clearProperty("https.proxyHost");
		}
		store.setProxyHostPersistent(hostNew);

		int portOld = store.getProxyPortPersistent();
		int portNew = this.configurationContainer.getProxyPort();
		if (portOld != -1 && portNew == -1) {
			// cf. above
			System.clearProperty("http.proxyPort");
			System.clearProperty("https.proxyPort");
		}
		store.setProxyPortPersistent(portNew);

		String userOld = store.getProxyUserPersistent();
		String userNew = this.configurationContainer.proxyUser;
		if (userOld != null && !userOld.isEmpty() && (userNew == null || userNew.isEmpty())) {
			// cf. above
			System.clearProperty("http.proxyUser");
			System.clearProperty("https.proxyUser");
		}
		store.setProxyUserPersistent(userNew);

		String passOld = store.getProxyPassPersistent();
		String passNew = this.configurationContainer.proxyPass;
		if (passOld != null && passNew == null) {
			// cf. above
			System.clearProperty("http.proxyPassword");
			System.clearProperty("https.proxyPassword");
		}
		store.setProxyPassPersistent(passNew);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#validateSettings()
	 */
	@Override
	public void validateSettings(int resumeIndex) throws Exception {

		String foldername = this.configurationContainer.outputFolder;

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
		SWTUtils.setLocalizedText(grpSignatur, "advanced_config.Signature_Title");
		SWTUtils.setLocalizedText(btnAutomatischePositionierung, "advanced_config.AutoPosition");
		SWTUtils.setLocalizedToolTipText(btnAutomatischePositionierung, "advanced_config.AutoPosition_ToolTip");
		SWTUtils.setLocalizedText(grpPlaceholder, "advanced_config.Placeholder_Title");
		SWTUtils.setLocalizedText(btnPlatzhalterVerwenden, "advanced_config.UseMarker");
		SWTUtils.setLocalizedToolTipText(btnPlatzhalterVerwenden, "advanced_config.UseMarker_ToolTip");
		SWTUtils.setLocalizedText(btnSignatureFieldsUsage, "advanced_config.UseSignatureFields");
		SWTUtils.setLocalizedToolTipText(btnSignatureFieldsUsage, "advanced_config.UseSignatureFields_ToolTip");
		SWTUtils.setLocalizedText(btnEnablePlaceholderUsage, "advanced_config.Placeholder_Enabled");
		SWTUtils.setLocalizedText(btnPdfACompat, "advanced_config.PdfACompat");
		SWTUtils.setLocalizedToolTipText(btnPdfACompat, "advanced_config.PdfACompat_ToolTip");

		SWTUtils.setLocalizedText(grpBkuAuswahl, "advanced_config.BKUSelection_Title");
		SWTUtils.setLocalizedToolTipText(cmbBKUAuswahl, "advanced_config.BKUSelection_ToolTip");
		SWTUtils.setLocalizedText(btnKeystoreEnabled, "advanced_config.KeystoreEnabled");
		SWTUtils.setLocalizedToolTipText(btnKeystoreEnabled, "advanced_config.KeystoreEnabled_ToolTip");

		SWTUtils.setLocalizedText(grpSpeicherort, "advanced_config.OutputFolder_Title");
		SWTUtils.setLocalizedText(lblDefaultOutputFolder, "advanced_config.OutputFolder");
		SWTUtils.setLocalizedToolTipText(txtOutputFolder, "advanced_config.OutputFolder_ToolTip");
		SWTUtils.setLocalizedText(btnBrowse, "common.browse");

		SWTUtils.setLocalizedText(grpLocaleAuswahl, "advanced_config.LocaleSelection_Title");
		SWTUtils.setLocalizedToolTipText(cmbLocaleAuswahl, "advanced_config.LocaleSelection_ToolTip");

		SWTUtils.setLocalizedText(grpUpdateCheck, "advanced_config.UpdateCheck_Title");
		SWTUtils.setLocalizedText(btnUpdateCheck, "advanced_config.UpdateCheck");
		SWTUtils.setLocalizedToolTipText(btnUpdateCheck, "advanced_config.UpdateCheck_ToolTip");

		SWTUtils.setLocalizedText(grpProxy, "advanced_config.Proxy_Title");
		SWTUtils.setLocalizedText(lblProxyHost, "advanced_config.ProxyHost");
		SWTUtils.setLocalizedToolTipText(txtProxyHost, "advanced_config.ProxyHost_ToolTip");
		this.txtProxyHost.setMessage(Messages.getString("advanced_config.ProxyHost_Template"));
		SWTUtils.setLocalizedText(lblProxyPort, "advanced_config.ProxyPort");
		SWTUtils.setLocalizedToolTipText(txtProxyPort, "advanced_config.ProxyPort_ToolTip");
		this.txtProxyPort.setMessage(Messages.getString("advanced_config.ProxyPort_Template"));
		// StateComposite.setLocalizedText(lblProxyUser, "advanced_config.ProxyUser");

		// this.txtProxyUser.setToolTipText(Messages
		// .getString("advanced_config.ProxyUser_ToolTip"));
		// this.txtProxyUser.setMessage(Messages
		// .getString("advanced_config.ProxyUser_Template"));
		// StateComposite.setLocalizedText(lblProxyPass, "advanced_config.ProxyPass");

		// this.txtProxyPass.setToolTipText(Messages
		// .getString("advanced_config.ProxyPass_ToolTip"));
		// this.txtProxyPass.setMessage(Messages
		// .getString("advanced_config.ProxyPass_Template"));
	}
}
