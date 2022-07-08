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
package at.asit.pdfover.gui.workflow.states;

// Imports
import java.io.File;
import java.security.Key;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;

import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.keystore.KeystoreUtils;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.controls.PasswordInputDialog;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;
import at.asit.pdfover.gui.workflow.config.ConfigurationDataInMemory.KeyStorePassStorageType;
import at.asit.pdfover.signator.SignatureException;
import at.asit.pdfover.signator.SigningState;

/**
 * Logical state for performing the BKU Request to a local BKU
 */
public class KSState extends State {

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(KSState.class);

	/**
	 * Constructor
	 * @param stateMachine the StateMachine
	 */
	public KSState(StateMachine stateMachine) {
		super(stateMachine);
	}

	private void showError(String messageKey, Object... args)
	{
		new ErrorDialog(getStateMachine().getMainShell(), String.format(Messages.getString(messageKey), args), BUTTONS.OK).open();
	}

	private boolean askShouldRetry(String messageKey, Object... args)
	{
		return SWT.RETRY == (new ErrorDialog(getStateMachine().getMainShell(), String.format(Messages.getString(messageKey), args), BUTTONS.RETRY_CANCEL).open());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * at.asit.pdfover.gui.workflow.WorkflowState#update(at.asit.pdfover.gui
	 * .workflow.Workflow)
	 */
	@Override
	public void run() {
		Status status = getStateMachine().status;

		SigningState signingState = status.signingState;
		ConfigurationManager config = getStateMachine().configProvider;

		try {
			String file = config.getKeyStoreFile();
			File f = new File(file);
			if (!f.isFile()) {
				log.error("Keystore not found");
				if (askShouldRetry("error.KeyStoreFileNotExist", f.getName()))
					this.run();
				else
					this.setNextState(new BKUSelectionState(getStateMachine()));
				return;
			}
			String type = config.getKeyStoreType();
			KeyStore keyStore = null;
			String storePass = config.getKeyStoreStorePass();
			while (keyStore == null) {
				if (storePass == null)
				{
					storePass = new PasswordInputDialog(
							getStateMachine().getMainShell(),
							Messages.getString("keystore_config.KeystoreStorePass"),
							Messages.getString("keystore.KeystoreStorePassEntry")).open();

					if (storePass == null)
					{
						this.setNextState(new BKUSelectionState(getStateMachine()));
						return;
					}
				}

				try {
					keyStore = KeystoreUtils.tryLoadKeystore(f, type, storePass);
				} catch (UnrecoverableKeyException e) {
					showError("error.KeyStoreStorePass");
					storePass = null;
				} catch (Exception e) {
					throw new SignatureException("Failed to load keystore", e);
				}
			}

			/* we've successfully unlocked the key store, save the entered password if requested */
			if (config.getKeyStorePassStorageType() == KeyStorePassStorageType.DISK)
			{
				/* only save to disk if the current keystore file is the one saved to disk */
				/* (might not be true if overridden from CLI) */
				if (file.equals(config.getKeyStoreFilePersistent()))
					config.setKeyStoreStorePassPersistent(storePass);
				else
					config.setKeyStoreStorePassOverlay(storePass);
			}
			else if (config.getKeyStorePassStorageType() == KeyStorePassStorageType.MEMORY)
				config.setKeyStoreStorePassOverlay(storePass);

			/* next, try to load the key from the now-unlocked keystore */
			String alias = config.getKeyStoreAlias();
			Key key = null;
			String keyPass = config.getKeyStoreKeyPass();
			while (key == null) {
				if (keyPass == null) {
					keyPass = new PasswordInputDialog(
							getStateMachine().getMainShell(),
							Messages.getString("keystore_config.KeystoreKeyPass"),
							Messages.getString("keystore.KeystoreKeyPassEntry")).open();

					if (keyPass == null)
					{
						this.setNextState(new BKUSelectionState(getStateMachine()));
						return;
					}
				}

				try {
					key = keyStore.getKey(alias, keyPass.toCharArray());
					if (key == null) /* alias does not exist */
					{
						if (!askShouldRetry("error.KeyStoreAliasExist", alias))
						{
							this.setNextState(new BKUSelectionState(getStateMachine()));
							return;
						}
						continue;
					}
				} catch (UnrecoverableKeyException e) {
					showError("error.KeyStoreKeyPass");
					keyPass = null;
				} catch (Exception e) {
					throw new SignatureException("Failed to load key from store", e);
				}
			}

			if (config.getKeyStorePassStorageType() == KeyStorePassStorageType.DISK)
			{
				if (file.equals(config.getKeyStoreFilePersistent()))
					config.setKeyStoreKeyPassPersistent(keyPass);
				else
					config.setKeyStoreKeyPassOverlay(keyPass);
			}
			else if (config.getKeyStorePassStorageType() == KeyStorePassStorageType.MEMORY)
				config.setKeyStoreKeyPassOverlay(keyPass);

			signingState.setKSSigner(file, alias, storePass, keyPass, type);
		} catch (SignatureException e) {
			log.error("Error loading keystore", e);
			if (askShouldRetry("error.KeyStore"))
				this.run(); /* recurse */
			else
				this.setNextState(new BKUSelectionState(getStateMachine()));
			return;
		}

		// OK
		this.setNextState(new at.asit.pdfover.gui.workflow.states.SigningState(getStateMachine()));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.workflow.states.State#cleanUp()
	 */
	@Override
	public void cleanUp() {
		// No composite - no cleanup necessary
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.workflow.states.State#setMainWindowBehavior()
	 */
	@Override
	public void updateMainWindowBehavior() {
		MainWindowBehavior behavior = getStateMachine().status.behavior;
		behavior.reset();
		behavior.setActive(Buttons.OPEN, true);
		behavior.setActive(Buttons.POSITION, true);
		behavior.setActive(Buttons.SIGN, true);
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
