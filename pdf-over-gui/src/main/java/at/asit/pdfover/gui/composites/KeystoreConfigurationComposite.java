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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
import at.asit.pdfover.gui.exceptions.CantLoadKeystoreException;
import at.asit.pdfover.gui.exceptions.KeystoreAliasDoesntExistException;
import at.asit.pdfover.gui.exceptions.KeystoreDoesntExistException;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.config.ConfigManipulator;
import at.asit.pdfover.gui.workflow.config.ConfigurationContainer;
import at.asit.pdfover.gui.workflow.config.PersistentConfigProvider;
import at.asit.pdfover.gui.workflow.states.State;

/**
 * 
 */
public class KeystoreConfigurationComposite extends BaseConfigurationComposite {

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(KeystoreConfigurationComposite.class);

	private Group grpKeystore;
	private Label lblKeystoreFile;
	Text txtKeystoreFile;
	private Button btnBrowse;
	private Label lblKeystoreType;
	Combo cmbKeystoreType;
	private Label lblKeystoreStorePass;
	Text txtKeystoreStorePass;
	private Button btnLoad;
	private Label lblKeystoreAlias;
	Combo cmbKeystoreAlias;
	private Label lblKeystoreKeyPass;
	Text txtKeystoreKeyPass;

	private Map<String, String> keystoreTypes;
	private Map<String, String> keystoreTypes_i;

	private KeyStore ks;

	/**
	 * @param parent
	 * @param style
	 * @param state
	 * @param container
	 */
	public KeystoreConfigurationComposite(
			org.eclipse.swt.widgets.Composite parent, int style, State state,
			ConfigurationContainer container) {
		super(parent, style, state, container);
		setLayout(new FormLayout());

		this.grpKeystore = new Group(this, SWT.NONE | SWT.RESIZE);
		FormLayout layout = new FormLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 5;
		this.grpKeystore.setLayout(layout);
		FormData fd_grpKeystore = new FormData();
		fd_grpKeystore.top = new FormAttachment(0, 5);
		fd_grpKeystore.left = new FormAttachment(0, 5);
		fd_grpKeystore.right = new FormAttachment(100, -5);
		this.grpKeystore.setLayoutData(fd_grpKeystore);

		FontData[] fD_grpKeystore = this.grpKeystore.getFont().getFontData();
		fD_grpKeystore[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpKeystore.setFont(new Font(Display.getCurrent(),
				fD_grpKeystore[0]));

		this.lblKeystoreFile = new Label(this.grpKeystore, SWT.NONE);
		FormData fd_lblKeystoreFile = new FormData();
		fd_lblKeystoreFile.top = new FormAttachment(0);
		fd_lblKeystoreFile.left = new FormAttachment(0, 5);
		this.lblKeystoreFile.setLayoutData(fd_lblKeystoreFile);

		FontData[] fD_lblKeystoreFile = this.lblKeystoreFile.getFont()
				.getFontData();
		fD_lblKeystoreFile[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblKeystoreFile.setFont(new Font(Display.getCurrent(),
				fD_lblKeystoreFile[0]));

		this.txtKeystoreFile = new Text(this.grpKeystore, SWT.BORDER);
		FormData fd_txtKeystoreFile = new FormData();
		fd_txtKeystoreFile.top = new FormAttachment(this.lblKeystoreFile, 5);
		fd_txtKeystoreFile.left = new FormAttachment(0, 15);
		this.txtKeystoreFile.setLayoutData(fd_txtKeystoreFile);

		FontData[] fD_txtKeystoreFile = this.txtKeystoreFile.getFont()
				.getFontData();
		fD_txtKeystoreFile[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.txtKeystoreFile.setFont(new Font(Display.getCurrent(),
				fD_txtKeystoreFile[0]));

		this.txtKeystoreFile.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				performKeystoreFileChanged(KeystoreConfigurationComposite.this.
						txtKeystoreFile.getText());
			}
		});

		this.btnBrowse = new Button(this.grpKeystore, SWT.NONE);
		fd_txtKeystoreFile.right = new FormAttachment(this.btnBrowse, -5);

		FontData[] fD_btnBrowse = this.btnBrowse.getFont().getFontData();
		fD_btnBrowse[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnBrowse.setFont(new Font(Display.getCurrent(), fD_btnBrowse[0]));

		FormData fd_btnBrowse = new FormData();
		fd_btnBrowse.top = new FormAttachment(this.lblKeystoreFile, 5);
		fd_btnBrowse.right = new FormAttachment(100, -5);
		this.btnBrowse.setLayoutData(fd_btnBrowse);

		this.btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(
						KeystoreConfigurationComposite.this.getShell(), SWT.OPEN);
				dialog.setFilterExtensions(new String[] {
						"*.p12;*.pkcs12;*.pfx;*.ks;*.jks", "*.p12;*.pkcs12;*.pfx;", "*.ks;*.jks*.", "*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				dialog.setFilterNames(new String[] {
						Messages.getString("common.KeystoreExtension_Description"), //$NON-NLS-1$
						Messages.getString("common.PKCS12Extension_Description"), //$NON-NLS-1$
						Messages.getString("common.KSExtension_Description"), //$NON-NLS-1$
						Messages.getString("common.AllExtension_Description") }); //$NON-NLS-1$
				String fileName = dialog.open();
				File file = null;
				if (fileName != null) {
					file = new File(fileName);
					if (file.exists()) {
						performKeystoreFileChanged(fileName);
					}
				}
			}
		});

		this.lblKeystoreType = new Label(this.grpKeystore, SWT.NONE);
		FormData fd_lblKeystoreType = new FormData();
		fd_lblKeystoreType.top = new FormAttachment(this.txtKeystoreFile, 5);
		fd_lblKeystoreType.left = new FormAttachment(0, 5);
		this.lblKeystoreType.setLayoutData(fd_lblKeystoreType);

		FontData[] fD_lblKeystoreType = this.lblKeystoreType.getFont()
				.getFontData();
		fD_lblKeystoreType[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblKeystoreType.setFont(new Font(Display.getCurrent(),
				fD_lblKeystoreType[0]));

		this.cmbKeystoreType = new Combo(this.grpKeystore, SWT.READ_ONLY);
		FormData fd_cmbKeystoreType = new FormData();
		fd_cmbKeystoreType.right = new FormAttachment(100, -5);
		fd_cmbKeystoreType.top = new FormAttachment(this.lblKeystoreType, 5);
		fd_cmbKeystoreType.left = new FormAttachment(0, 15);
		this.cmbKeystoreType.setLayoutData(fd_cmbKeystoreType);

		FontData[] fD_cmbKeystoreType = this.cmbKeystoreType.getFont()
				.getFontData();
		fD_cmbKeystoreType[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.cmbKeystoreType.setFont(new Font(Display.getCurrent(),
				fD_cmbKeystoreType[0]));

		initKeystoreTypes();
		this.cmbKeystoreType.setItems(this.keystoreTypes.keySet().toArray(new String[0]));
		this.cmbKeystoreType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performKeystoreTypeChanged(
						KeystoreConfigurationComposite.this.cmbKeystoreType.getItem(
								KeystoreConfigurationComposite.this.cmbKeystoreType.getSelectionIndex()));
			}
		});

		this.lblKeystoreStorePass = new Label(this.grpKeystore, SWT.NONE);
		FormData fd_lblKeystoreStorePass = new FormData();
		fd_lblKeystoreStorePass.top = new FormAttachment(this.cmbKeystoreType, 5);
		fd_lblKeystoreStorePass.left = new FormAttachment(0, 5);
		this.lblKeystoreStorePass.setLayoutData(fd_lblKeystoreStorePass);

		FontData[] fD_lblKeystoreStorePass = this.lblKeystoreStorePass.getFont()
				.getFontData();
		fD_lblKeystoreStorePass[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblKeystoreStorePass.setFont(new Font(Display.getCurrent(),
				fD_lblKeystoreStorePass[0]));

		this.txtKeystoreStorePass = new Text(this.grpKeystore, SWT.BORDER | SWT.PASSWORD);
		FormData fd_txtKeystoreStorePass = new FormData();
		fd_txtKeystoreStorePass.top = new FormAttachment(this.lblKeystoreStorePass, 5);
		fd_txtKeystoreStorePass.left = new FormAttachment(0, 15);
		this.txtKeystoreStorePass.setLayoutData(fd_txtKeystoreStorePass);

		FontData[] fD_txtKeystoreStorePass = this.txtKeystoreStorePass.getFont()
				.getFontData();
		fD_txtKeystoreStorePass[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.txtKeystoreStorePass.setFont(new Font(Display.getCurrent(),
				fD_txtKeystoreStorePass[0]));

		this.txtKeystoreStorePass.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				performKeystoreStorePassChanged(KeystoreConfigurationComposite.
						this.txtKeystoreStorePass.getText());
			}
		});

		this.btnLoad = new Button(this.grpKeystore, SWT.NONE);
		fd_txtKeystoreStorePass.right = new FormAttachment(this.btnLoad, -5);

		FontData[] fD_btnLoad = this.btnLoad.getFont().getFontData();
		fD_btnLoad[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnLoad.setFont(new Font(Display.getCurrent(), fD_btnLoad[0]));

		FormData fd_btnLoad = new FormData();
		fd_btnLoad.top = new FormAttachment(this.lblKeystoreStorePass, 5);
		fd_btnLoad.right = new FormAttachment(100, -5);
		this.btnLoad.setLayoutData(fd_btnLoad);

		this.btnLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				File f = new File(KeystoreConfigurationComposite.this
						.configurationContainer.getKeyStoreFile());
				try {
					loadKeystore();
				} catch (KeyStoreException ex) {
					log.error("Error loading keystore", ex); //$NON-NLS-1$
					showErrorDialog(Messages.getString("error.KeyStore")); //$NON-NLS-1$
				} catch (FileNotFoundException ex) {
					log.error("Error loading keystore", ex); //$NON-NLS-1$
					showErrorDialog(String.format(Messages.getString(
							"error.FileNotExist"), f.getName())); //$NON-NLS-1$
				} catch (NoSuchAlgorithmException ex) {
					log.error("Error loading keystore", ex); //$NON-NLS-1$
					showErrorDialog(Messages.getString("error.KeyStore")); //$NON-NLS-1$
				} catch (CertificateException ex) {
					log.error("Error loading keystore", ex); //$NON-NLS-1$
					showErrorDialog(Messages.getString("error.KeyStore")); //$NON-NLS-1$
				} catch (IOException ex) {
					log.error("Error loading keystore", ex); //$NON-NLS-1$
					showErrorDialog(Messages.getString("error.KeyStore")); //$NON-NLS-1$
				}
						
			}
		});

		this.lblKeystoreAlias = new Label(this.grpKeystore, SWT.NONE);
		FormData fd_lblKeystoreAlias = new FormData();
		fd_lblKeystoreAlias.top = new FormAttachment(this.txtKeystoreStorePass, 5);
		fd_lblKeystoreAlias.left = new FormAttachment(0, 5);
		this.lblKeystoreAlias.setLayoutData(fd_lblKeystoreAlias);

		FontData[] fD_lblKeystoreAlias = this.lblKeystoreAlias.getFont()
				.getFontData();
		fD_lblKeystoreAlias[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblKeystoreAlias.setFont(new Font(Display.getCurrent(),
				fD_lblKeystoreAlias[0]));

		this.cmbKeystoreAlias = new Combo(this.grpKeystore, SWT.NONE);
		FormData fd_cmbKeystoreAlias = new FormData();
		fd_cmbKeystoreAlias.right = new FormAttachment(100, -5);
		fd_cmbKeystoreAlias.top = new FormAttachment(this.lblKeystoreAlias, 5);
		fd_cmbKeystoreAlias.left = new FormAttachment(0, 15);
		this.cmbKeystoreAlias.setLayoutData(fd_cmbKeystoreAlias);

		FontData[] fD_cmbKeystoreAlias = this.cmbKeystoreAlias.getFont()
				.getFontData();
		fD_cmbKeystoreAlias[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.cmbKeystoreAlias.setFont(new Font(Display.getCurrent(),
				fD_cmbKeystoreAlias[0]));

		this.cmbKeystoreAlias.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performKeystoreAliasChanged(
						KeystoreConfigurationComposite.this.cmbKeystoreAlias.getItem(
								KeystoreConfigurationComposite.this.cmbKeystoreAlias.getSelectionIndex()));
			}
		});
		this.cmbKeystoreAlias.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				performKeystoreAliasChanged(KeystoreConfigurationComposite.
						this.cmbKeystoreAlias.getText());
			}
		});

		this.lblKeystoreKeyPass = new Label(this.grpKeystore, SWT.NONE);
		FormData fd_lblKeystoreKeyPass = new FormData();
		fd_lblKeystoreKeyPass.top = new FormAttachment(this.cmbKeystoreAlias, 5);
		fd_lblKeystoreKeyPass.left = new FormAttachment(0, 5);
		this.lblKeystoreKeyPass.setLayoutData(fd_lblKeystoreKeyPass);

		FontData[] fD_lblKeystoreKeyPass = this.lblKeystoreKeyPass.getFont()
				.getFontData();
		fD_lblKeystoreKeyPass[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblKeystoreKeyPass.setFont(new Font(Display.getCurrent(),
				fD_lblKeystoreKeyPass[0]));

		this.txtKeystoreKeyPass = new Text(this.grpKeystore, SWT.BORDER | SWT.PASSWORD);
		FormData fd_txtKeystoreKeyPass = new FormData();
		fd_txtKeystoreKeyPass.top = new FormAttachment(this.lblKeystoreKeyPass, 5);
		fd_txtKeystoreKeyPass.left = new FormAttachment(0, 15);
		fd_txtKeystoreKeyPass.right = new FormAttachment(100, -5);
		this.txtKeystoreKeyPass.setLayoutData(fd_txtKeystoreKeyPass);

		FontData[] fD_txtKeystoreKeyPass = this.txtKeystoreKeyPass.getFont()
				.getFontData();
		fD_txtKeystoreKeyPass[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.txtKeystoreKeyPass.setFont(new Font(Display.getCurrent(),
				fD_txtKeystoreKeyPass[0]));

		this.txtKeystoreKeyPass.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				performKeystoreKeyPassChanged(KeystoreConfigurationComposite.
						this.txtKeystoreKeyPass.getText());
			}
		});

		// Load localized strings
		reloadResources();
	}

	void showErrorDialog(String error) {
		ErrorDialog e = new ErrorDialog(getShell(), error, BUTTONS.OK);
		e.open();
	}

	void loadKeystore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		ConfigurationContainer config = 
				KeystoreConfigurationComposite.this.configurationContainer;
		File f = new File(config.getKeyStoreFile());
		this.ks = KeyStore.getInstance(config.getKeyStoreType());
		FileInputStream fis = new FileInputStream(f);
		this.ks.load(fis, config.getKeyStoreStorePass().toCharArray());
		this.cmbKeystoreAlias.remove(0, this.cmbKeystoreAlias.getItemCount()-1);
		Enumeration<String> aliases = this.ks.aliases();
		while (aliases.hasMoreElements())
			this.cmbKeystoreAlias.add(aliases.nextElement());
	}

	private void initKeystoreTypes() {
		this.keystoreTypes = new HashMap<String, String>();
		this.keystoreTypes_i = new HashMap<String, String>();
		this.keystoreTypes.put(Messages.getString("keystore_config.KeystoreType_PKCS12"), "PKCS12"); //$NON-NLS-1$ //$NON-NLS-2$
		this.keystoreTypes_i.put("PKCS12", Messages.getString("keystore_config.KeystoreType_PKCS12")); //$NON-NLS-1$ //$NON-NLS-2$
		this.keystoreTypes.put(Messages.getString("keystore_config.KeystoreType_JKS"), "JCEKS"); //$NON-NLS-1$ //$NON-NLS-2$
		this.keystoreTypes_i.put("JCEKS", Messages.getString("keystore_config.KeystoreType_JKS")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @param fileName 
	 */
	protected void performKeystoreFileChanged(String fileName) {
		log.debug("Selected keystore file: " + fileName); //$NON-NLS-1$
		this.configurationContainer.setKeyStoreFile(fileName);
		KeystoreConfigurationComposite.this.txtKeystoreFile.setText(fileName);
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			String ext = fileName.substring(i+1);
			if (
					ext.equalsIgnoreCase("p12") || //$NON-NLS-1$
					ext.equalsIgnoreCase("pkcs12") || //$NON-NLS-1$
					ext.equalsIgnoreCase("pfx")) //$NON-NLS-1$
				performKeystoreTypeChanged(this.keystoreTypes_i.get("PKCS12")); //$NON-NLS-1$
			else if (
					ext.equalsIgnoreCase("ks") || //$NON-NLS-1$
					ext.equalsIgnoreCase("jks")) //$NON-NLS-1$
				performKeystoreTypeChanged(this.keystoreTypes_i.get("JCEKS")); //$NON-NLS-1$
		}
	}

	/**
	 * @param type 
	 */
	protected void performKeystoreTypeChanged(String type) {
		log.debug("Selected keystore type: " + type); //$NON-NLS-1$
		this.configurationContainer.setKeyStoreType(
				this.keystoreTypes.get(type));
		for (int i = 0; i < this.cmbKeystoreType.getItemCount(); ++i) {
			if (this.cmbKeystoreType.getItem(i).equals(type)) {
				this.cmbKeystoreType.select(i);
				break;
			}
		}
	}

	/**
	 * @param storepass 
	 */
	protected void performKeystoreStorePassChanged(String storepass) {
		log.debug("Changed keystore store password"); //$NON-NLS-1$
		this.configurationContainer.setKeyStoreStorePass(storepass);
		this.txtKeystoreStorePass.setText(storepass);
	}

	/**
	 * @param alias
	 */
	protected void performKeystoreAliasChanged(String alias) {
		log.debug("Selected keystore alias: " + alias); //$NON-NLS-1$
		this.configurationContainer.setKeyStoreAlias(alias);
		this.cmbKeystoreAlias.setText(alias);
	}

	/**
	 * @param keypass 
	 */
	protected void performKeystoreKeyPassChanged(String keypass) {
		log.debug("Changed keystore key password"); //$NON-NLS-1$
		this.configurationContainer.setKeyStoreKeyPass(keypass);
		this.txtKeystoreKeyPass.setText(keypass);
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
		this.configurationContainer.setKeyStoreFile(
				provider.getKeyStoreFilePersistent());
		this.configurationContainer.setKeyStoreType(
				provider.getKeyStoreTypePersistent());
		this.configurationContainer.setKeyStoreAlias(
				provider.getKeyStoreAliasPersistent());
		this.configurationContainer.setKeyStoreStorePass(
				provider.getKeyStoreStorePassPersistent());
		this.configurationContainer.setKeyStoreKeyPass(
				provider.getKeyStoreKeyPassPersistent());
	}

	/*
	 * (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.BaseConfigurationComposite#loadConfiguration
	 * ()
	 */
	@Override
	public void loadConfiguration() {
		// Initialize form fields from configuration Container
		String ks = this.configurationContainer.getKeyStoreFile();
		performKeystoreFileChanged(ks);
		performKeystoreTypeChanged(
				this.configurationContainer.getKeyStoreType());
		performKeystoreStorePassChanged(
				this.configurationContainer.getKeyStoreStorePass());
		try {
			File ksf = new File(ks);
			if (ksf.exists())
				loadKeystore();
		} catch (Exception e) {
			log.error("Error loading keystore", e); //$NON-NLS-1$
		}
		performKeystoreAliasChanged(
				this.configurationContainer.getKeyStoreAlias());
		performKeystoreKeyPassChanged(
				this.configurationContainer.getKeyStoreKeyPass());
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.BaseConfigurationComposite#storeConfiguration(at.asit.pdfover.gui.workflow.config.ConfigManipulator, at.asit.pdfover.gui.workflow.config.PersistentConfigProvider)
	 */
	@Override
	public void storeConfiguration(ConfigManipulator store,
			PersistentConfigProvider provider) {
		store.setKeyStoreFile(this.configurationContainer.getKeyStoreFile());
		store.setKeyStoreType(this.configurationContainer.getKeyStoreType());
		store.setKeyStoreAlias(this.configurationContainer.getKeyStoreAlias());
		store.setKeyStoreStorePass(this.configurationContainer.getKeyStoreStorePass());
		store.setKeyStoreKeyPass(this.configurationContainer.getKeyStoreKeyPass());
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
			String fname = this.configurationContainer.getKeyStoreFile();
			if (fname.isEmpty())
				break; //no checks required
			File f = new File(fname);
			if (!f.exists() || !f.isFile())
				throw new KeystoreDoesntExistException(f, 3); //skip next checks
			// Fall through
		case 1:
			try {
				loadKeystore();
			} catch (Exception e) {
				throw new CantLoadKeystoreException(e, 3); //skip next check
			}
			// Fall through
		case 2:
			String alias = this.configurationContainer.getKeyStoreAlias();
			if (!this.ks.containsAlias(alias))
				throw new KeystoreAliasDoesntExistException(alias, 3);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		this.grpKeystore.setText(Messages
				.getString("keystore_config.Keystore_Title")); //$NON-NLS-1$
		this.lblKeystoreFile.setText(Messages
				.getString("keystore_config.KeystoreFile")); //$NON-NLS-1$
		this.btnBrowse.setText(Messages.getString("common.browse")); //$NON-NLS-1$
		this.txtKeystoreFile.setToolTipText(Messages
				.getString("keystore_config.KeystoreFile_ToolTip")); //$NON-NLS-1$
		this.lblKeystoreType.setText(Messages
				.getString("keystore_config.KeystoreType")); //$NON-NLS-1$
		this.lblKeystoreStorePass.setText(Messages
				.getString("keystore_config.KeystoreStorePass")); //$NON-NLS-1$
		this.txtKeystoreStorePass.setToolTipText(Messages
				.getString("keystore_config.KeystoreStorePass_ToolTip")); //$NON-NLS-1$
		this.btnLoad.setText(Messages.getString("keystore_config.Load")); //$NON-NLS-1$
		this.btnLoad.setToolTipText(Messages
				.getString("keystore_config.Load_ToolTip")); //$NON-NLS-1$
		this.lblKeystoreAlias.setText(Messages
				.getString("keystore_config.KeystoreAlias")); //$NON-NLS-1$
		this.lblKeystoreKeyPass.setText(Messages
				.getString("keystore_config.KeystoreKeyPass")); //$NON-NLS-1$
		this.txtKeystoreKeyPass.setToolTipText(Messages
				.getString("keystore_config.KeystoreKeyPass_ToolTip")); //$NON-NLS-1$
	}
}
