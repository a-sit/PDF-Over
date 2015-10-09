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
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
import at.asit.pdfover.gui.controls.ErrorMarker;
import at.asit.pdfover.gui.exceptions.InvalidEmblemFile;
import at.asit.pdfover.gui.exceptions.InvalidNumberException;
import at.asit.pdfover.gui.exceptions.PDFOverGUIException;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.utils.SignaturePlaceholderCache;
import at.asit.pdfover.gui.workflow.config.ConfigManipulator;
import at.asit.pdfover.gui.workflow.config.ConfigurationContainer;
import at.asit.pdfover.gui.workflow.config.PersistentConfigProvider;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.FileNameEmblem;
import at.asit.pdfover.signator.SignatureParameter;

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
		FormData fd_grpKeystore = new FormData();
		fd_grpKeystore.right = new FormAttachment(100, -5);
		fd_grpKeystore.left = new FormAttachment(0, 5);
		fd_grpKeystore.top = new FormAttachment(0, 5);
		this.grpKeystore.setLayoutData(fd_grpKeystore);
		this.grpKeystore.setLayout(new GridLayout(2, false));

		FontData[] fD_grpKeystore = this.grpKeystore.getFont()
				.getFontData();
		fD_grpKeystore[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpKeystore.setFont(new Font(Display.getCurrent(),
				fD_grpKeystore[0]));

		// Load localized strings
		reloadResources();
	}

	/**
	 * 
	 */
	private final class KeystoreFileBrowser extends SelectionAdapter {
		/**
		 * 
		 */
		public KeystoreFileBrowser() {
			// Nothing to do
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			FileDialog dialog = new FileDialog(
					KeystoreConfigurationComposite.this.getShell(), SWT.OPEN);
			dialog.setFilterExtensions(new String[] {
					"*.p12;*.pkcs12;*.ks;*.jks", "*.p12;*.pkcs12", "*.ks;*.jks*.", "*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
					//processEmblemChanged(fileName);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#signerChanged()
	 */
	@Override
	protected void signerChanged() {
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
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.BaseConfigurationComposite#storeConfiguration(at.asit.pdfover.gui.workflow.config.ConfigManipulator, at.asit.pdfover.gui.workflow.config.PersistentConfigProvider)
	 */
	@Override
	public void storeConfiguration(ConfigManipulator store,
			PersistentConfigProvider provider) {
		store.setKeyStoreFile(this.configurationContainer.getKeyStoreFile());
		store.setKeyStoreType(this.configurationContainer.getKeyStoreType());
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
	}
}
