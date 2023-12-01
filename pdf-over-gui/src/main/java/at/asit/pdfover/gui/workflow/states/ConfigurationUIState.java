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

import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.composites.ConfigurationComposite;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
public class ConfigurationUIState extends State {

	private ConfigurationComposite configurationComposite = null;

	private ConfigurationComposite getConfigurationComposite() {
		if (this.configurationComposite == null) {
			this.configurationComposite =
					getStateMachine().createComposite(ConfigurationComposite.class, SWT.RESIZE, this);
			this.configurationComposite.setConfigProvider(getStateMachine().configProvider);
		}

		return this.configurationComposite;
	}

	/**
	 * @param stateMachine
	 */
	public ConfigurationUIState(StateMachine stateMachine) {
		super(stateMachine);
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#run()
	 */
	@Override
	public void run() {
		Status status = getStateMachine().status;
		
		ConfigurationComposite config;
		try {
			config = this.getConfigurationComposite();
			getStateMachine().display(config);
		} catch (Exception e) {
			log.error("Failed to initialize config UI", e);
			ErrorDialog error = new ErrorDialog(
				getStateMachine().getMainShell(),
				Messages.getString("error.ConfigInitialization"),
				BUTTONS.YES_NO
			);

			int selection = error.open();
			if (selection == SWT.YES)
			{
				ConfigurationManager.factoryResetPersistentConfig();
				getStateMachine().exit();
				return;
			}

			throw e;
		}

		if(config.isUserDone())
		{
			this.reloadResources();
			State previousState = status.getPreviousState();
			if (previousState instanceof OutputState)
				this.setNextState(new OpenState(getStateMachine()));
			else
				this.setNextState(previousState); // TODO do we need to tell a previous state to refresh from config settings? (positioning preview)
		}
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#cleanUp()
	 */
	@Override
	public void cleanUp() {
		if(this.configurationComposite != null)
			this.configurationComposite.dispose();
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#updateMainWindowBehavior()
	 */
	@Override
	public void updateMainWindowBehavior() {
		// Leave the state as it is
		MainWindowBehavior behavior = getStateMachine().status.behavior;
		behavior.setEnabled(Buttons.CONFIG, false);
		behavior.setMainBarVisible(false);
	}

	/**
	 * Triggers to reload the resources
	 */
	public void reloadResources() {
		getStateMachine().reloadResources();
	}
}
