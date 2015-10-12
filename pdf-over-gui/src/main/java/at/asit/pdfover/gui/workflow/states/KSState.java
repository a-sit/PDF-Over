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
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.controls.PasswordInputDialog;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.gui.workflow.config.ConfigProvider;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.workflow.WorkflowState#update(at.asit.pdfover.gui
	 * .workflow.Workflow)
	 */
	@Override
	public void run() {
		Status status = getStateMachine().getStatus();

		SigningState signingState = status.getSigningState();
		ConfigProvider config = getStateMachine().getConfigProvider();

		try {
			String file = config.getKeyStoreFile();
			String alias = config.getKeyStoreAlias();
			String storePass = config.getKeyStoreStorePass();
			if (storePass.isEmpty()) {
				PasswordInputDialog pwd = new PasswordInputDialog(
						getStateMachine().getGUIProvider().getMainShell(),
						Messages.getString("keystore_config.KeystoreStorePass"), //$NON-NLS-1$
						Messages.getString("keystore.KeystoreStorePassEntry")); //$NON-NLS-1$
				storePass = pwd.open();
			}
			String keyPass = config.getKeyStoreKeyPass();
			if (keyPass.isEmpty()) {
				PasswordInputDialog pwd = new PasswordInputDialog(
						getStateMachine().getGUIProvider().getMainShell(),
						Messages.getString("keystore_config.KeystoreKeyPass"), //$NON-NLS-1$
						Messages.getString("keystore.KeystoreKeyPassEntry")); //$NON-NLS-1$
				keyPass = pwd.open();
			}
			String type = config.getKeyStoreType();
			signingState.setKSSigner(file, alias, storePass, keyPass, type);
		} catch (SignatureException e) {
			log.error("Error loading keystore", e); //$NON-NLS-1$
			ErrorDialog dialog = new ErrorDialog(
					getStateMachine().getGUIProvider().getMainShell(),
					Messages.getString("error.KeyStore"), //$NON-NLS-1$
					BUTTONS.RETRY_CANCEL);
			if (dialog.open() != SWT.RETRY) {
				//getStateMachine().exit();
				this.setNextState(new BKUSelectionState(getStateMachine()));
				return;
			}
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
		MainWindowBehavior behavior = getStateMachine().getStatus()
				.getBehavior();
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
