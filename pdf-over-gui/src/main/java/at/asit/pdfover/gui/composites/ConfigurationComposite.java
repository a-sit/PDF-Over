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

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.composites.configuration.AboutComposite;
import at.asit.pdfover.gui.composites.configuration.AdvancedConfigurationComposite;
import at.asit.pdfover.gui.composites.configuration.ConfigurationCompositeBase;
import at.asit.pdfover.gui.composites.configuration.KeystoreConfigurationComposite;
import at.asit.pdfover.gui.composites.configuration.SimpleConfigurationComposite;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.exceptions.ResumableException;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;
import at.asit.pdfover.gui.workflow.config.ConfigurationDataInMemory;
import at.asit.pdfover.gui.workflow.states.State;

/**
 * Composite for hosting configuration composites
 */
public class ConfigurationComposite extends StateComposite {

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(ConfigurationComposite.class);

	/**
	 * configuration provider
	 */
	ConfigurationManager configProvider = null;

	/**
	 * simple configuration composite
	 */
	ConfigurationCompositeBase simpleConfigComposite;

	/**
	 * advanced configuration composite
	 */
	ConfigurationCompositeBase advancedConfigComposite;

	/**
	 * advanced configuration composite
	 */
	ConfigurationCompositeBase keystoreConfigComposite = null;

	/**
	 * The TabFolder
	 */
	TabFolder tabFolder;

	/**
	 * configuration container Keeps state for current configuration changes
	 */
	ConfigurationDataInMemory configurationContainer = new ConfigurationDataInMemory();

	/**
	 * The stack layout
	 */
	StackLayout compositeStack = new StackLayout();

	/**
	 * SWT style
	 */
	int style;

	/**
	 * base configuration container
	 */
	Composite containerComposite;

	/**
	 * checks whether the user is done
	 */
	boolean userDone = false;

	private TabItem simpleTabItem;

	private TabItem advancedTabItem;

	private TabItem keystoreTabItem;

	private TabItem aboutTabItem;

	private Button btnSpeichern;

	private Button btnAbbrechen;

	/**
	 * Create the composite.
	 *
	 * @param parent
	 * @param style
	 * @param state
	 */
	public ConfigurationComposite(Composite parent, int style, State state) {
		super(parent, SWT.FILL | style, state);
		this.style = SWT.FILL | style;

		this.setLayout(new FormLayout());

		this.containerComposite = new Composite(this, SWT.FILL | SWT.RESIZE);

		this.tabFolder = new TabFolder(this.containerComposite, SWT.NONE);
		FormData fd_tabFolder = new FormData();
		fd_tabFolder.bottom = new FormAttachment(100, -5);
		fd_tabFolder.right = new FormAttachment(100, -5);
		fd_tabFolder.top = new FormAttachment(0, 5);
		fd_tabFolder.left = new FormAttachment(0, 5);
		this.tabFolder.setLayoutData(fd_tabFolder);

		FontData[] fD_tabFolder = this.tabFolder.getFont().getFontData();
		fD_tabFolder[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.tabFolder.setFont(new Font(Display.getCurrent(), fD_tabFolder[0]));

		this.simpleTabItem = new TabItem(this.tabFolder, SWT.NONE);
		this.simpleTabItem.setText(Messages.getString("config.Simple"));

		ScrolledComposite simpleCompositeScr = new ScrolledComposite(this.tabFolder, (SWT.H_SCROLL | SWT.V_SCROLL));
		this.simpleTabItem.setControl(simpleCompositeScr);
		this.simpleConfigComposite = new SimpleConfigurationComposite(simpleCompositeScr, SWT.NONE, state, configurationContainer);
		simpleCompositeScr.setContent(simpleConfigComposite);
		simpleCompositeScr.setExpandHorizontal(true);
		simpleCompositeScr.setExpandVertical(true);
		simpleCompositeScr.setMinSize(simpleConfigComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		this.advancedTabItem = new TabItem(this.tabFolder, SWT.NONE);
		this.advancedTabItem.setText(Messages.getString("config.Advanced"));

		ScrolledComposite advancedCompositeScr = new ScrolledComposite(this.tabFolder, (SWT.H_SCROLL | SWT.V_SCROLL));
		this.advancedTabItem.setControl(advancedCompositeScr);
		this.advancedConfigComposite = new AdvancedConfigurationComposite(advancedCompositeScr, SWT.NONE, state, configurationContainer, this);
		advancedCompositeScr.setContent(advancedConfigComposite);
		advancedCompositeScr.setExpandHorizontal(true);
		advancedCompositeScr.setExpandVertical(true);
		advancedCompositeScr.setMinSize(advancedConfigComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		this.aboutTabItem = new TabItem(this.tabFolder, SWT.NONE);
		this.aboutTabItem.setText(String.format(Messages.getString("config.About"), Constants.APP_NAME));

		ScrolledComposite aboutCompositeScr = new ScrolledComposite(this.tabFolder, (SWT.H_SCROLL | SWT.V_SCROLL));
		this.aboutTabItem.setControl(aboutCompositeScr);
		AboutComposite aboutConfigComposite = new AboutComposite(aboutCompositeScr, SWT.NONE);
		aboutCompositeScr.setContent(aboutConfigComposite);
		aboutCompositeScr.setExpandHorizontal(true);
		aboutCompositeScr.setExpandVertical(true);
		aboutCompositeScr.addListener(SWT.Resize, (event) -> {
			int widthHint = aboutCompositeScr.getClientArea().width - 50; /* offset for scroll bar */
			aboutCompositeScr.setMinSize(null);
			aboutCompositeScr.setMinSize(aboutCompositeScr.computeSize(widthHint, SWT.DEFAULT));
		});

		this.tabFolder.setSelection(this.simpleTabItem);

		this.btnSpeichern = new Button(this, SWT.NONE);
		FormData fd_btnSpeichern = new FormData();
		fd_btnSpeichern.right = new FormAttachment(100, -5);
		fd_btnSpeichern.bottom = new FormAttachment(100);
		this.btnSpeichern.setLayoutData(fd_btnSpeichern);
		this.btnSpeichern.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getShell().setText(Constants.APP_NAME);
				if (ConfigurationComposite.this.storeConfiguration()) {
					ConfigurationComposite.this.userDone = true;
					ConfigurationComposite.this.state.updateStateMachine();
				}
			}
		});
		this.btnSpeichern.setText(Messages.getString("common.Save"));
		getShell().setDefaultButton(this.btnSpeichern);

		FontData[] fD_btnSpeichern = this.btnSpeichern.getFont().getFontData();
		fD_btnSpeichern[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnSpeichern.setFont(new Font(Display.getCurrent(), fD_btnSpeichern[0]));

		this.btnAbbrechen = new Button(this, SWT.NONE);
		FormData fd_btnAbrechen = new FormData();
		fd_btnAbrechen.right = new FormAttachment(this.btnSpeichern, -10);
		fd_btnAbrechen.bottom = new FormAttachment(this.btnSpeichern, 0, SWT.BOTTOM);
		this.btnAbbrechen.setLayoutData(fd_btnAbrechen);
		this.btnAbbrechen.setText(Messages.getString("common.Cancel"));
		this.btnAbbrechen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getShell().setText(Constants.APP_NAME);
				ConfigurationComposite.this.userDone = true;
				ConfigurationComposite.this.state.updateStateMachine();
			}
		});

		FontData[] fD_btnAbbrechen = this.btnAbbrechen.getFont().getFontData();
		fD_btnAbbrechen[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnAbbrechen.setFont(new Font(Display.getCurrent(), fD_btnAbbrechen[0]));

		FormData fd_composite = new FormData();
		fd_composite.top = new FormAttachment(0, 5);
		fd_composite.bottom = new FormAttachment(this.btnSpeichern, -10);
		fd_composite.left = new FormAttachment(0, 5);
		fd_composite.right = new FormAttachment(100, -5);
		this.containerComposite.setLayoutData(fd_composite);
		this.containerComposite.setLayout(this.compositeStack);
		this.compositeStack.topControl = this.tabFolder;

		getShell().setText(Constants.APP_NAME_VERSION + " [" + System.getProperty("java.vendor") + " Java " + System.getProperty("java.version") + "]");

		this.doLayout();
	}

	private boolean keystoreInitialized = false;

	/**
	 * Set whether keystore tab is enabled
	 * @param enabled whether keystore tab is enabled
	 */
	public void keystoreEnabled(boolean enabled) {
		if (enabled && this.keystoreConfigComposite == null) {
			this.keystoreTabItem = new TabItem(this.tabFolder, SWT.NONE, 2);
			this.keystoreTabItem.setText(Messages.getString("config.Keystore"));

			ScrolledComposite keystoreCompositeScr = new ScrolledComposite(
					this.tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
			this.keystoreTabItem.setControl(keystoreCompositeScr);
			this.keystoreConfigComposite = new KeystoreConfigurationComposite(
					keystoreCompositeScr, SWT.NONE, this.state,
					this.configurationContainer);
			keystoreCompositeScr.setContent(this.keystoreConfigComposite);
			keystoreCompositeScr.setExpandHorizontal(true);
			keystoreCompositeScr.setExpandVertical(true);
			keystoreCompositeScr.setMinSize(this.keystoreConfigComposite
					.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			if (!this.keystoreInitialized) {
				this.keystoreConfigComposite.initConfiguration(this.configProvider);
				this.keystoreInitialized = true;
			}
			this.keystoreConfigComposite.loadConfiguration();
		} else if (!enabled && this.keystoreConfigComposite != null){
			this.keystoreTabItem.dispose();
			this.keystoreConfigComposite = null;
		}
	}

	/**
	 * Sets the configuration provider
	 *
	 * @param provider
	 */
	public void setConfigProvider(ConfigurationManager provider) {
		this.configProvider = provider;
		if (this.configProvider != null) {
			// Initialize Configuration Container
			this.simpleConfigComposite.initConfiguration(this.configProvider);
			this.advancedConfigComposite.initConfiguration(this.configProvider);

			this.simpleConfigComposite.loadConfiguration();
			this.advancedConfigComposite.loadConfiguration();
			if (this.keystoreConfigComposite != null)
				this.keystoreConfigComposite.loadConfiguration();
		}
	}

	boolean storeConfiguration() {
		boolean status = false;
		boolean redo = false;
		int resumeIndex = 0;
		try {
			do {
				try {
					this.simpleConfigComposite.validateSettings(resumeIndex);

					redo = false;
					status = true;
				} catch (ResumableException e) {
					log.error("Settings validation failed!", e);
					ErrorDialog dialog = new ErrorDialog(getShell(),
							e.getMessage(), BUTTONS.ABORT_RETRY_IGNORE);
					int rc = dialog.open();

					redo = (rc == SWT.RETRY);
					if (rc == SWT.IGNORE)
					{
						resumeIndex = e.getResumeIndex();
						redo = true;
					}
				}
			} while (redo);

			if (!status) {
				return false;
			}

			status = false;
			redo = false;
			resumeIndex = 0;

			do {
				try {
					this.advancedConfigComposite.validateSettings(resumeIndex);

					redo = false;
					status = true;
				} catch (ResumableException e) {
					log.error("Settings validation failed!", e);
					ErrorDialog dialog = new ErrorDialog(getShell(),
							e.getMessage(), BUTTONS.ABORT_RETRY_IGNORE);
					int rc = dialog.open();

					redo = (rc == SWT.RETRY);
					if (rc == SWT.IGNORE)
					{
						resumeIndex = e.getResumeIndex();
						redo = true;
					}
				}
			} while (redo);

			if (!status) {
				return false;
			}

			if (this.keystoreConfigComposite != null) {
				status = false;
				redo = false;
				resumeIndex = 0;

				do {
					try {
						this.keystoreConfigComposite.validateSettings(resumeIndex);

						redo = false;
						status = true;
					} catch (ResumableException e) {
						log.error("Settings validation failed!", e);
						ErrorDialog dialog = new ErrorDialog(getShell(),
								e.getMessage(), BUTTONS.ABORT_RETRY_IGNORE);
						int rc = dialog.open();

						redo = (rc == SWT.RETRY);
						if (rc == SWT.IGNORE)
						{
							resumeIndex = e.getResumeIndex();
							redo = true;
						}
					}
				} while (redo);

				if (!status) {
					return false;
				}
			}
		} catch (Exception e) {
			log.error("Settings validation failed!", e);
			String message = e.getMessage();
			if (message == null)
				message = Messages.getString("error.Unexpected");
			ErrorDialog dialog = new ErrorDialog(getShell(), message,
					BUTTONS.OK);
			dialog.open();
			return false;
		}

		if (!status) {
			return false;
		}

		// Write current Configuration
		this.simpleConfigComposite.storeConfiguration(this.configProvider);
		this.advancedConfigComposite.storeConfiguration(this.configProvider);
		if (this.keystoreConfigComposite != null)
			this.keystoreConfigComposite.storeConfiguration(this.configProvider);

		status = false;
		redo = false;
		do {
			// Save current config to file
			try {
				this.configProvider.saveToDisk();
				redo = false;
				status = true;
			} catch (IOException e) {
				log.error("Failed to save configuration to file!", e);
				ErrorDialog dialog = new ErrorDialog(getShell(),
						Messages.getString("error.FailedToSaveSettings"), BUTTONS.RETRY_CANCEL);
				redo = (dialog.open() == SWT.RETRY);

				// return false;
			}
		} while (redo);

		return status;
	}

	/**
	 * Checks if the user has finished working with the configuration composite
	 *
	 * @return if the user is done
	 */
	public boolean isUserDone() {
		return this.userDone;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.composites.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		Control ctrl = this.compositeStack.topControl;
		this.containerComposite.layout(true, true);
		getShell().layout(true, true);
		// Note: SWT only layouts children! No grandchildren!
		if (ctrl instanceof StateComposite) {
			((StateComposite) ctrl).doLayout();
		}
		setFocus();
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		this.simpleTabItem.setText(Messages.getString("config.Simple"));
		this.advancedTabItem.setText(Messages.getString("config.Advanced"));
		this.aboutTabItem.setText(String.format(Messages.getString("config.About"), Constants.APP_NAME));
		this.btnSpeichern.setText(Messages.getString("common.Save"));
		this.btnAbbrechen.setText(Messages.getString("common.Cancel"));
	}
}
