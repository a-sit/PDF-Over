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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Messages;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.workflow.ConfigurationContainer;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.BKUs;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

/**
 * Composite for advanced configuration
 */
public class AdvancedConfigurationComposite extends BaseConfigurationComposite {
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
		
		TabFolder tabFolder = new TabFolder(this, SWT.NONE);
		FormData fd_tabFolder = new FormData();
		fd_tabFolder.bottom = new FormAttachment(100, -5);
		fd_tabFolder.right = new FormAttachment(100, -5);
		fd_tabFolder.top = new FormAttachment(0, 5);
		fd_tabFolder.left = new FormAttachment(0, 5);
		tabFolder.setLayoutData(fd_tabFolder);

		tabFolder.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.simpleComposite.signerChanged();
			}
		});
		
		TabItem simpleTabItem = new TabItem(tabFolder, SWT.NULL);
		simpleTabItem.setText(Messages.getString("config.Simple")); //$NON-NLS-1$

		FontData[] fD_tabFolder = tabFolder.getFont().getFontData();
		fD_tabFolder[0].setHeight(TEXT_SIZE_NORMAL);
		tabFolder.setFont(new Font(Display.getCurrent(), fD_tabFolder[0]));
		
		this.simpleComposite = new SimpleConfigurationComposite(tabFolder,
				SWT.NONE, state, container);

		simpleTabItem.setControl(this.simpleComposite);

		TabItem advancedTabItem = new TabItem(tabFolder, SWT.NULL);
		advancedTabItem.setText(Messages.getString("config.Advanced")); //$NON-NLS-1$

		Composite advancedComposite = new Composite(tabFolder, SWT.NONE);

		advancedTabItem.setControl(advancedComposite);
		advancedComposite.setLayout(new FormLayout());

		Group grpSignaturPosition = new Group(advancedComposite, SWT.NONE);
		grpSignaturPosition.setText(Messages.getString("advanced_config.AutoPosition_Title")); //$NON-NLS-1$
		grpSignaturPosition.setLayout(new FormLayout());
		FormData fd_grpSignaturPosition = new FormData();
		fd_grpSignaturPosition.top = new FormAttachment(0, 5);
		fd_grpSignaturPosition.bottom = new FormAttachment(33, -5);
		fd_grpSignaturPosition.right = new FormAttachment(100, -5);
		fd_grpSignaturPosition.left = new FormAttachment(0, 5);
		grpSignaturPosition.setLayoutData(fd_grpSignaturPosition);

		FontData[] fD_grpSignaturPosition = grpSignaturPosition.getFont().getFontData();
		fD_grpSignaturPosition[0].setHeight(TEXT_SIZE_NORMAL);
		grpSignaturPosition.setFont(new Font(Display.getCurrent(), fD_grpSignaturPosition[0]));
		
		this.btnAutomatischePositionierung = new Button(grpSignaturPosition,
				SWT.CHECK);
		FormData fd_btnAutomatischePositionierung = new FormData();
		fd_btnAutomatischePositionierung.right = new FormAttachment(100, -5);
		fd_btnAutomatischePositionierung.top = new FormAttachment(0, 5);
		fd_btnAutomatischePositionierung.left = new FormAttachment(0, 5);
		this.btnAutomatischePositionierung
				.setLayoutData(fd_btnAutomatischePositionierung);
		this.btnAutomatischePositionierung.setText(Messages.getString("advanced_config.AutoPosition")); //$NON-NLS-1$
		
		FontData[] fD_btnAutomatischePositionierung = this.btnAutomatischePositionierung.getFont().getFontData();
		fD_btnAutomatischePositionierung[0].setHeight(TEXT_SIZE_BUTTON);
		this.btnAutomatischePositionierung.setFont(new Font(Display.getCurrent(), fD_btnAutomatischePositionierung[0]));
		
		this.btnAutomatischePositionierung.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdvancedConfigurationComposite.this.performPositionSelection(
						AdvancedConfigurationComposite.this.btnAutomatischePositionierung.getSelection());
			}
		});

		Group grpBkuAuswahl = new Group(advancedComposite, SWT.NONE);
		grpBkuAuswahl.setText(Messages.getString("advanced_config.BKUSelection_Title")); //$NON-NLS-1$
		grpBkuAuswahl.setLayout(new FormLayout());
		FormData fd_grpBkuAuswahl = new FormData();
		fd_grpBkuAuswahl.top = new FormAttachment(33, 5);
		fd_grpBkuAuswahl.left = new FormAttachment(0, 5);
		fd_grpBkuAuswahl.right = new FormAttachment(100, -5);
		fd_grpBkuAuswahl.bottom = new FormAttachment(66, -5);
		grpBkuAuswahl.setLayoutData(fd_grpBkuAuswahl);

		FontData[] fD_grpBkuAuswahl = grpBkuAuswahl.getFont().getFontData();
		fD_grpBkuAuswahl[0].setHeight(TEXT_SIZE_NORMAL);
		grpBkuAuswahl.setFont(new Font(Display.getCurrent(), fD_grpBkuAuswahl[0]));
		
		this.cmbBKUAuswahl = new Combo(grpBkuAuswahl, SWT.NONE);
		FormData fd_cmbBKUAuswahl = new FormData();
		fd_cmbBKUAuswahl.right = new FormAttachment(100, -5);
		fd_cmbBKUAuswahl.top = new FormAttachment(0, 5);
		fd_cmbBKUAuswahl.left = new FormAttachment(0, 5);

		FontData[] fD_cmbBKUAuswahl = this.cmbBKUAuswahl.getFont().getFontData();
		fD_cmbBKUAuswahl[0].setHeight(TEXT_SIZE_NORMAL);
		this.cmbBKUAuswahl.setFont(new Font(Display.getCurrent(), fD_cmbBKUAuswahl[0]));
		
		int blen = BKUs.values().length;

		this.bkuStrings = new String[blen];

		for (int i = 0; i < blen; i++) {
			String lookup = "BKU." + BKUs.values()[i].toString(); //$NON-NLS-1$
			this.bkuStrings[i] = Messages.getString(lookup);
		}

		this.cmbBKUAuswahl.setItems(this.bkuStrings);

		this.cmbBKUAuswahl.setLayoutData(fd_cmbBKUAuswahl);
		
		this.cmbBKUAuswahl.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = getBKUElementIndex(AdvancedConfigurationComposite.this.configurationContainer.getBKUSelection());
				if(AdvancedConfigurationComposite.this.cmbBKUAuswahl.getSelectionIndex() != selectionIndex) {
					selectionIndex = AdvancedConfigurationComposite.this.cmbBKUAuswahl.getSelectionIndex();
					performBKUSelectionChanged(AdvancedConfigurationComposite.this.cmbBKUAuswahl.getItem(selectionIndex));
				}
			}
		});

		Group grpSpeicherort = new Group(advancedComposite, SWT.NONE);
		grpSpeicherort.setText(Messages.getString("advanced_config.OutputFolder_Title")); //$NON-NLS-1$
		grpSpeicherort.setLayout(new FormLayout());
		FormData fd_grpSpeicherort = new FormData();
		fd_grpSpeicherort.top = new FormAttachment(66, 5);
		fd_grpSpeicherort.left = new FormAttachment(0, 5);
		fd_grpSpeicherort.right = new FormAttachment(100, -5);
		fd_grpSpeicherort.bottom = new FormAttachment(100, -5);
		grpSpeicherort.setLayoutData(fd_grpSpeicherort);

		FontData[] fD_grpSpeicherort = grpSpeicherort.getFont().getFontData();
		fD_grpSpeicherort[0].setHeight(TEXT_SIZE_NORMAL);
		grpSpeicherort.setFont(new Font(Display.getCurrent(), fD_grpSpeicherort[0]));
		
		Label lblDefaultOutputFolder = new Label(grpSpeicherort, SWT.NONE);
		FormData fd_lblDefaultOutputFolder = new FormData();
		fd_lblDefaultOutputFolder.top = new FormAttachment(0, 5);
		fd_lblDefaultOutputFolder.left = new FormAttachment(0, 5);
		lblDefaultOutputFolder.setLayoutData(fd_lblDefaultOutputFolder);
		lblDefaultOutputFolder.setText(Messages.getString("advanced_config.OutputFolder")); //$NON-NLS-1$

		FontData[] fD_lblDefaultOutputFolder = lblDefaultOutputFolder.getFont().getFontData();
		fD_lblDefaultOutputFolder[0].setHeight(TEXT_SIZE_NORMAL);
		lblDefaultOutputFolder.setFont(new Font(Display.getCurrent(), fD_lblDefaultOutputFolder[0]));
		
		this.txtOutputFolder = new Text(grpSpeicherort, SWT.BORDER);
		FormData fd_text = new FormData();
		fd_text.top = new FormAttachment(lblDefaultOutputFolder, 5);
		fd_text.left = new FormAttachment(0, 5);
		this.txtOutputFolder.setLayoutData(fd_text);

		FontData[] fD_txtOutputFolder = this.txtOutputFolder.getFont().getFontData();
		fD_txtOutputFolder[0].setHeight(TEXT_SIZE_NORMAL);
		this.txtOutputFolder.setFont(new Font(Display.getCurrent(), fD_txtOutputFolder[0]));
		
		this.txtOutputFolder.addFocusListener(new FocusAdapter() {
			
			@Override
			public void focusLost(FocusEvent e) {
				performOutputFolderChanged(AdvancedConfigurationComposite.this.txtOutputFolder.getText());
			}
		});
		
		Button btnBrowse = new Button(grpSpeicherort, SWT.NONE);
		fd_text.right = new FormAttachment(btnBrowse, -5);

		FontData[] fD_btnBrowse = btnBrowse.getFont().getFontData();
		fD_btnBrowse[0].setHeight(TEXT_SIZE_BUTTON);
		btnBrowse.setFont(new Font(Display.getCurrent(), fD_btnBrowse[0]));
		
		FormData fd_btnBrowse = new FormData();
		fd_btnBrowse.top = new FormAttachment(lblDefaultOutputFolder, 5);
		fd_btnBrowse.right = new FormAttachment(100, -5);
		btnBrowse.setLayoutData(fd_btnBrowse);
		btnBrowse.setText(Messages.getString("common.browse")); //$NON-NLS-1$

		btnBrowse.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(
						AdvancedConfigurationComposite.this.getShell());

				// Set the initial filter path according
				// to anything they've selected or typed in
				dlg.setFilterPath(AdvancedConfigurationComposite.this.txtOutputFolder
						.getText());

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
		
		tabFolder.setSelection(advancedTabItem);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.BaseConfigurationComposite#signerChanged()
	 */
	@Override
	protected void signerChanged() {
		this.simpleComposite.setSigner(this.getSigner());
	}
	
	void performOutputFolderChanged(String foldername) {
		log.debug("Selected Output folder: " + foldername); //$NON-NLS-1$
		this.configurationContainer.setOutputFolder(foldername);
		AdvancedConfigurationComposite.this.txtOutputFolder.setText(foldername);
	}

	int getBKUElementIndex(BKUs bku) {
		String lookup = "BKU." + bku.toString(); //$NON-NLS-1$
		String bkuName = Messages.getString(lookup);
		
		for(int i = 0; i < this.bkuStrings.length; i++) {
			if(this.bkuStrings[i].equals(bkuName)) {
				log.debug("BKU: " + bkuName + " IDX: " + i); //$NON-NLS-1$ //$NON-NLS-2$
				return i;
			}
		}
		
		log.warn("NO BKU match for " + bkuName); //$NON-NLS-1$
		return 0;
	}
	
	void performBKUSelectionChanged(BKUs selected) {
		log.debug("Selected BKU: " + selected.toString()); //$NON-NLS-1$
		this.configurationContainer.setBKUSelection(selected);
		this.cmbBKUAuswahl.select(this.getBKUElementIndex(selected));
	}

	void performBKUSelectionChanged(String selected) {
		try {
			BKUs bkuvalue = resolvBKU(selected);
			this.performBKUSelectionChanged(bkuvalue);
		} catch (Exception ex) {
			log.error("Failed to parse BKU value: " + selected, ex); //$NON-NLS-1$
			ErrorDialog dialog = new ErrorDialog(getShell(), SWT.NONE, Messages.getString("error.InvalidBKU"), ex, false); //$NON-NLS-1$
			dialog.open();
		}
	}
	
	BKUs resolvBKU(String localizedBKU) {
		int blen = BKUs.values().length;

		for (int i = 0; i < blen; i++) {
			String lookup = "BKU." + BKUs.values()[i].toString(); //$NON-NLS-1$
			if(Messages.getString(lookup).equals(localizedBKU)) {
				return BKUs.values()[i];
			}
		}

		return BKUs.NONE;
	}
	
	void performPositionSelection(boolean automatic) {
		log.debug("Selected Position: " + automatic); //$NON-NLS-1$
		this.configurationContainer.setAutomaticPosition(automatic);
		this.btnAutomatischePositionierung.setSelection(automatic);
	}

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(AdvancedConfigurationComposite.class);
	SimpleConfigurationComposite simpleComposite;
	Text txtOutputFolder;
	Combo cmbBKUAuswahl;
	String[] bkuStrings;
	Button btnAutomatischePositionierung;

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
		this.simpleComposite.loadConfiguration();

		// load advanced settings
		this.performBKUSelectionChanged(this.configurationContainer.getBKUSelection());
		String outputFolder = this.configurationContainer.getOutputFolder();
		if(outputFolder != null) {
			this.performOutputFolderChanged(outputFolder);
		}
		this.performPositionSelection(this.configurationContainer.getAutomaticPosition());
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.BaseConfigurationComposite#validateSettings()
	 */
	@Override
	public void validateSettings() throws Exception {
		this.simpleComposite.validateSettings();
	}
}
