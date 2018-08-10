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
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.exceptions.ResumableException;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.PDFSigner;
import at.asit.pdfover.gui.workflow.config.ConfigManipulator;
import at.asit.pdfover.gui.workflow.config.ConfigurationContainer;
import at.asit.pdfover.gui.workflow.config.ConfigurationContainerImpl;
import at.asit.pdfover.gui.workflow.config.PersistentConfigProvider;
import at.asit.pdfover.gui.workflow.states.State;

/**
 * Composite for hosting configuration composites
 */
public class ConfigurationComposite extends StateComposite {

	/**
	 * The PDF Signer used to produce signature block preview
	 */
	protected PDFSigner signer;

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(ConfigurationComposite.class);

	/**
	 * configuration manipulator
	 */
	ConfigManipulator configManipulator = null;

	/**
	 * configuration provider
	 */
	PersistentConfigProvider configProvider = null;

	/**
	 * simple configuration composite
	 */
	BaseConfigurationComposite simpleConfigComposite;

	/**
	 * advanced configuration composite
	 */
	BaseConfigurationComposite advancedConfigComposite;

	/**
	 * advanced configuration composite
	 */
	BaseConfigurationComposite keystoreConfigComposite = null;

	/**
	 * The TabFolder
	 */
	TabFolder tabFolder;

	/**
	 * configuration container Keeps state for current configuration changes
	 */
	ConfigurationContainer configurationContainer = new ConfigurationContainerImpl();

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
	 * @return the signer
	 */
	public PDFSigner getSigner() {
		return this.signer;
	}

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
		this.simpleTabItem.setText(Messages.getString("config.Simple")); //$NON-NLS-1$

		ScrolledComposite simpleCompositeScr = new ScrolledComposite(this.tabFolder,
				SWT.H_SCROLL | SWT.V_SCROLL);
		this.simpleTabItem.setControl(simpleCompositeScr);
		this.simpleConfigComposite = new SimpleConfigurationComposite(
				simpleCompositeScr, SWT.NONE, state,
				this.configurationContainer);
		simpleCompositeScr.setContent(this.simpleConfigComposite);
		simpleCompositeScr.setExpandHorizontal(true);
		simpleCompositeScr.setExpandVertical(true);
		simpleCompositeScr.setMinSize(this.simpleConfigComposite.computeSize(
				SWT.DEFAULT, SWT.DEFAULT));

		this.advancedTabItem = new TabItem(this.tabFolder, SWT.NONE);
		this.advancedTabItem.setText(Messages.getString("config.Advanced")); //$NON-NLS-1$

		ScrolledComposite advancedCompositeScr = new ScrolledComposite(
				this.tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
		this.advancedTabItem.setControl(advancedCompositeScr);
		this.advancedConfigComposite = new AdvancedConfigurationComposite(
				advancedCompositeScr, SWT.NONE, state,
				this.configurationContainer, this);
		advancedCompositeScr.setContent(this.advancedConfigComposite);
		advancedCompositeScr.setExpandHorizontal(true);
		advancedCompositeScr.setExpandVertical(true);
		advancedCompositeScr.setMinSize(this.advancedConfigComposite
				.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		this.aboutTabItem = new TabItem(this.tabFolder, SWT.NONE);
		this.aboutTabItem.setText(String.format(Messages.getString("config.About"), Constants.APP_NAME)); //$NON-NLS-1$

		ScrolledComposite aboutCompositeScr = new ScrolledComposite(
				this.tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
		this.aboutTabItem.setControl(aboutCompositeScr);
		AboutComposite aboutConfigComposite = new AboutComposite(
				aboutCompositeScr, SWT.NONE);
		aboutCompositeScr.setContent(aboutConfigComposite);
		aboutCompositeScr.setExpandHorizontal(true);
		aboutCompositeScr.setExpandVertical(true);
		aboutCompositeScr.setMinSize(aboutConfigComposite
				.computeSize(SWT.DEFAULT, SWT.DEFAULT));

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
		this.btnSpeichern.setText(Messages.getString("common.Save")); //$NON-NLS-1$
		getShell().setDefaultButton(this.btnSpeichern);

		FontData[] fD_btnSpeichern = this.btnSpeichern.getFont().getFontData();
		fD_btnSpeichern[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnSpeichern
				.setFont(new Font(Display.getCurrent(), fD_btnSpeichern[0]));

		this.btnAbbrechen = new Button(this, SWT.NONE);
		FormData fd_btnAbrechen = new FormData();
		fd_btnAbrechen.right = new FormAttachment(this.btnSpeichern, -10);
		fd_btnAbrechen.bottom = new FormAttachment(this.btnSpeichern, 0, SWT.BOTTOM);
		this.btnAbbrechen.setLayoutData(fd_btnAbrechen);
		this.btnAbbrechen.setText(Messages.getString("common.Cancel")); //$NON-NLS-1$
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
		this.btnAbbrechen
				.setFont(new Font(Display.getCurrent(), fD_btnAbbrechen[0]));

		FormData fd_composite = new FormData();
		fd_composite.top = new FormAttachment(0, 5);
		fd_composite.bottom = new FormAttachment(this.btnSpeichern, -10);
		fd_composite.left = new FormAttachment(0, 5);
		fd_composite.right = new FormAttachment(100, -5);
		this.containerComposite.setLayoutData(fd_composite);
		this.containerComposite.setLayout(this.compositeStack);
		this.compositeStack.topControl = this.tabFolder;

		getShell().setText(Constants.APP_NAME_VERSION);

		this.doLayout();
	}

	/**
	 * @param signer
	 *            the signer to set
	 */
	public void setSigner(PDFSigner signer) {
		this.signer = signer;
		if (this.simpleConfigComposite != null) {
			this.simpleConfigComposite.setSigner(getSigner());
		}
		if (this.advancedConfigComposite != null) {
			// not needed at the moment
			this.advancedConfigComposite.setSigner(getSigner());
		}
		if (this.keystoreConfigComposite != null) {
			// not needed at the moment
			this.keystoreConfigComposite.setSigner(getSigner());
		}
	}

	private class AboutComposite extends StateComposite {
		private Link lnkAbout;
		/**
	 * @param parent
	 * @param style
		 */
		public AboutComposite(Composite parent, int style) {
			super(parent, style, null);

			setLayout(new FormLayout());

			this.lnkAbout = new Link(this, SWT.WRAP);

			FormData fd_lnkAbout = new FormData();
			fd_lnkAbout.right = new FormAttachment(100, -5);
			fd_lnkAbout.left = new FormAttachment(0, 5);
			fd_lnkAbout.top = new FormAttachment(0, 5);
			fd_lnkAbout.width = 100;
			this.lnkAbout.setLayoutData(fd_lnkAbout);

			FontData[] fD_lnkAbout = this.lnkAbout.getFont().getFontData();
			fD_lnkAbout[0].setHeight(Constants.TEXT_SIZE_NORMAL);
			this.lnkAbout.setFont(new Font(Display.getCurrent(),
					fD_lnkAbout[0]));

			this.lnkAbout.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						URI url = new URI("https://technology.a-sit.at/lizenzbedingungen/"); //$NON-NLS-1$
						log.debug("Trying to open " + url.toString()); //$NON-NLS-1$
						if (Desktop.isDesktopSupported()) {
							Desktop.getDesktop().browse(url);
						} else {
							log.info("AWT Desktop is not supported on this platform"); //$NON-NLS-1$
							Program.launch(url.toString());
						}
					} catch (IOException ex) {
						log.error("AboutComposite: ", ex); //$NON-NLS-1$
					} catch (URISyntaxException ex) {
						log.error("AboutComposite: ", ex); //$NON-NLS-1$
					}
				}
			});

			// Load localized strings
			reloadResources();
		}

		/* (non-Javadoc)
		 * @see at.asit.pdfover.gui.composites.StateComposite#doLayout()
		 */
		@Override
		public void doLayout() {
			// Nothing to do here
		}

		/* (non-Javadoc)
		 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
		 */
		@Override
		public void reloadResources() {
			this.lnkAbout.setText(Messages.getString("config.AboutText")); //$NON-NLS-1$
		}
	}

	private boolean keystoreInitialized = false;

	/**
	 * Set whether keystore tab is enabled
	 * @param enabled whether keystore tab is enabled
	 */
	public void keystoreEnabled(boolean enabled) {
		if (enabled && this.keystoreConfigComposite == null) {
			this.keystoreTabItem = new TabItem(this.tabFolder, SWT.NONE, 2);
			this.keystoreTabItem.setText(Messages.getString("config.Keystore")); //$NON-NLS-1$
	
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
	 * Sets the configuration manipulator
	 * 
	 * @param manipulator
	 */
	public void setConfigManipulator(ConfigManipulator manipulator) {
		this.configManipulator = manipulator;
	}

	/**
	 * Sets the configuration provider
	 * 
	 * @param provider
	 */
	public void setConfigProvider(PersistentConfigProvider provider) {
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
					log.error("Settings validation failed!", e); //$NON-NLS-1$
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
					log.error("Settings validation failed!", e); //$NON-NLS-1$
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
						log.error("Settings validation failed!", e); //$NON-NLS-1$
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
			log.error("Settings validation failed!", e); //$NON-NLS-1$
			String message = e.getMessage();
			if (message == null)
				message = Messages.getString("error.Unexpected"); //$NON-NLS-1$
			ErrorDialog dialog = new ErrorDialog(getShell(), message,
					BUTTONS.OK);
			dialog.open();
			return false;
		}

		if (!status) {
			return false;
		}

		// Write current Configuration
		this.simpleConfigComposite.storeConfiguration(
				this.configManipulator, this.configProvider);
		this.advancedConfigComposite.storeConfiguration(
				this.configManipulator, this.configProvider);
		if (this.keystoreConfigComposite != null)
			this.keystoreConfigComposite.storeConfiguration(
					this.configManipulator, this.configProvider);

		status = false;
		redo = false;
		do {
			// Save current config to file
			try {
				this.configManipulator.saveCurrentConfiguration();
				redo = false;
				status = true;
			} catch (IOException e) {
				log.error("Failed to save configuration to file!", e); //$NON-NLS-1$
				ErrorDialog dialog = new ErrorDialog(getShell(),
						Messages.getString("error.FailedToSaveSettings"), BUTTONS.RETRY_CANCEL); //$NON-NLS-1$
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
		this.simpleTabItem.setText(Messages.getString("config.Simple")); //$NON-NLS-1$
		this.advancedTabItem.setText(Messages.getString("config.Advanced")); //$NON-NLS-1$
		this.aboutTabItem.setText(String.format(Messages.getString("config.About"), Constants.APP_NAME)); //$NON-NLS-1$
		this.btnSpeichern.setText(Messages.getString("common.Save")); //$NON-NLS-1$
		this.btnAbbrechen.setText(Messages.getString("common.Cancel")); //$NON-NLS-1$
	}
}
