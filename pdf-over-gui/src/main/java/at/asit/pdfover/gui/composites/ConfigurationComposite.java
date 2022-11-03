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
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.composites.configuration.AboutComposite;
import at.asit.pdfover.gui.composites.configuration.AdvancedConfigurationComposite;
import at.asit.pdfover.gui.composites.configuration.ConfigurationCompositeBase;
import at.asit.pdfover.gui.composites.configuration.Fido2ConfigurationComposite;
import at.asit.pdfover.gui.composites.configuration.KeystoreConfigurationComposite;
import at.asit.pdfover.gui.composites.configuration.SimpleConfigurationComposite;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.exceptions.ResumableException;
import at.asit.pdfover.gui.utils.SWTUtils;
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

	ConfigurationCompositeBase fido2ConfigComposite;

	/**
	 * about page composite
	 */
	ConfigurationCompositeBase aboutConfigComposite;

	private final Iterable<ConfigurationCompositeBase> _compositeIterable = new Iterable<>() {
		@Override
		public Iterator<ConfigurationCompositeBase> iterator() {
			return new Iterator<>() {
				int i = 0;
				@Override
				public boolean hasNext() {
					return i < 5;
				}

				@Override
				public ConfigurationCompositeBase next() {
					if ((i == 2) && (keystoreConfigComposite == null)) ++i;
					switch (++i) {
						case 1: return simpleConfigComposite;
						case 2: return advancedConfigComposite;
						case 3: return keystoreConfigComposite;
						case 4: return fido2ConfigComposite;
						case 5: return aboutConfigComposite;
					}
					throw new NoSuchElementException();
				}
			};
		}
	};
	private Iterable<ConfigurationCompositeBase> composites() { return _compositeIterable; }

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

	private TabItem fido2TabItem;

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
		SWTUtils.anchor(tabFolder).bottom(100, -5).right(100, -5).top(0, 5).left(0, 5);
		SWTUtils.setFontHeight(tabFolder, Constants.TEXT_SIZE_NORMAL);

		this.simpleTabItem = new TabItem(this.tabFolder, SWT.NONE);
		ScrolledComposite simpleCompositeScr = new ScrolledComposite(this.tabFolder, (SWT.H_SCROLL | SWT.V_SCROLL));
		this.simpleTabItem.setControl(simpleCompositeScr);
		this.simpleConfigComposite = new SimpleConfigurationComposite(simpleCompositeScr, SWT.NONE, state, configurationContainer);
		simpleCompositeScr.setContent(simpleConfigComposite);
		simpleCompositeScr.setExpandHorizontal(true);
		simpleCompositeScr.setExpandVertical(true);
		simpleCompositeScr.setMinSize(simpleConfigComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		this.advancedTabItem = new TabItem(this.tabFolder, SWT.NONE);
		ScrolledComposite advancedCompositeScr = new ScrolledComposite(this.tabFolder, (SWT.H_SCROLL | SWT.V_SCROLL));
		this.advancedTabItem.setControl(advancedCompositeScr);
		this.advancedConfigComposite = new AdvancedConfigurationComposite(advancedCompositeScr, SWT.NONE, state, configurationContainer, this);
		advancedCompositeScr.setContent(advancedConfigComposite);
		advancedCompositeScr.setExpandHorizontal(true);
		advancedCompositeScr.setExpandVertical(true);
		advancedCompositeScr.setMinSize(advancedConfigComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		this.fido2TabItem = new TabItem(this.tabFolder, SWT.NONE);
		ScrolledComposite fido2CompositeScr = new ScrolledComposite(this.tabFolder, (SWT.H_SCROLL | SWT.V_SCROLL));
		this.fido2TabItem.setControl(fido2CompositeScr);
		this.fido2ConfigComposite = new Fido2ConfigurationComposite(fido2CompositeScr, SWT.NONE, state, configurationContainer);
		fido2CompositeScr.setContent(fido2ConfigComposite);
		fido2CompositeScr.setExpandHorizontal(true);
		fido2CompositeScr.setExpandVertical(true);
		fido2CompositeScr.addListener(SWT.Resize, (e) -> {
			int widthHint = fido2CompositeScr.getClientArea().width - 50; /* offset for scroll bar */
			fido2CompositeScr.setMinSize(null);
			fido2CompositeScr.setMinSize(fido2CompositeScr.computeSize(widthHint, SWT.DEFAULT));
		});

		this.aboutTabItem = new TabItem(this.tabFolder, SWT.NONE);
		ScrolledComposite aboutCompositeScr = new ScrolledComposite(this.tabFolder, (SWT.H_SCROLL | SWT.V_SCROLL));
		this.aboutTabItem.setControl(aboutCompositeScr);
		this.aboutConfigComposite = new AboutComposite(aboutCompositeScr, SWT.NONE, state, configurationContainer);
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
		SWTUtils.anchor(btnSpeichern).right(100, -5).bottom(100);
		SWTUtils.setFontHeight(btnSpeichern, Constants.TEXT_SIZE_BUTTON);
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

		this.btnAbbrechen = new Button(this, SWT.NONE);
		SWTUtils.anchor(btnAbbrechen).right(btnSpeichern, -10).bottom(btnSpeichern, 0, SWT.BOTTOM);
		SWTUtils.setFontHeight(btnAbbrechen, Constants.TEXT_SIZE_BUTTON);
		this.btnAbbrechen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getShell().setText(Constants.APP_NAME);
				ConfigurationComposite.this.userDone = true;
				ConfigurationComposite.this.state.updateStateMachine();
			}
		});

		SWTUtils.anchor(containerComposite).top(0, 5).bottom(btnSpeichern, -10).left(0, 5).right(100, -5);
		this.containerComposite.setLayout(this.compositeStack);
		this.compositeStack.topControl = this.tabFolder;

		getShell().setText(Constants.APP_NAME_VERSION + " [" + System.getProperty("java.vendor") + " Java " + System.getProperty("java.version") + "]");

		reloadResources();
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
			SWTUtils.setLocalizedText(keystoreTabItem, "config.Keystore");

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

			reloadResources();
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
		if (provider.crashOnConfig) /* for testing initialization error handlers */
			throw new RuntimeException("A robot must protect its own existence as long as such protection does not conflict with the First or Second Law.\n(CRASH=config is set.)");

		this.configProvider = provider;
		if (this.configProvider != null) {
			for (ConfigurationCompositeBase c : composites())
				c.initConfiguration(this.configProvider);

			for (ConfigurationCompositeBase c : composites())
				c.loadConfiguration();
		}
	}

	boolean storeConfiguration() {
		try {
			for (ConfigurationCompositeBase c : composites()) {
				int resumeIndex = 0;
				while (true) {
					try {
						c.validateSettings(resumeIndex);
						break;
					} catch (ResumableException e) {
						log.info("Settings validation failed!", e);
						ErrorDialog dialog = new ErrorDialog(getShell(),
							e.getMessage(), BUTTONS.ABORT_RETRY_IGNORE);
						int rc = dialog.open();

						if (rc == SWT.ABORT)
							return false;
						if (rc == SWT.IGNORE)
							resumeIndex = e.getResumeIndex();
					}
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

		// Write current Configuration
		for (ConfigurationCompositeBase c : composites())
			c.storeConfiguration(this.configProvider);

		while (true) {
			// Save current config to file
			try {
				this.configProvider.saveToDisk();
				return true;
			} catch (IOException e) {
				log.error("Failed to save configuration to file!", e);
				ErrorDialog dialog = new ErrorDialog(getShell(),
						Messages.getString("error.FailedToSaveSettings"), BUTTONS.RETRY_CANCEL);
				int rc = dialog.open();
				if (rc == SWT.CANCEL)
					return false;
			}
		}
	}

	/**
	 * Checks if the user has finished working with the configuration composite
	 *
	 * @return if the user is done
	 */
	public boolean isUserDone() {
		return this.userDone;
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
		SWTUtils.setLocalizedText(simpleTabItem, "config.Simple");
		SWTUtils.setLocalizedText(advancedTabItem, "config.Advanced");
		SWTUtils.setLocalizedText(fido2TabItem, "config.FIDO2");
		SWTUtils.setLocalizedText(aboutTabItem, "config.About", Constants.APP_NAME);
		if (this.keystoreConfigComposite != null)
			SWTUtils.setLocalizedText(keystoreTabItem, "config.Keystore");

		SWTUtils.setLocalizedText(btnSpeichern, "common.Save");
		SWTUtils.setLocalizedText(btnAbbrechen, "common.Cancel");
	}
}
