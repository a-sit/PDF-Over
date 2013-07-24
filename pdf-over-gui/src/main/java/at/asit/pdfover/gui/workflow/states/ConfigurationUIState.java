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

import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.composites.ConfigurationComposite;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;

/**
 * 
 */
public class ConfigurationUIState extends State {
	/**
	 * SLF4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(ConfigurationUIState.class);

	
	
	private ConfigurationComposite configurationComposite = null;

	private ConfigurationComposite getConfigurationComposite() {
		if (this.configurationComposite == null) {
			this.configurationComposite =
					getStateMachine().getGUIProvider().createComposite(ConfigurationComposite.class, SWT.RESIZE, this);
			this.configurationComposite.setConfigManipulator(getStateMachine().getConfigManipulator());
			this.configurationComposite.setConfigProvider(getStateMachine().getPersistentConfigProvider());
			this.configurationComposite.setSigner(getStateMachine().getPDFSigner());
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
		Status status = getStateMachine().getStatus();

		ConfigurationComposite config = this.getConfigurationComposite();
		
		getStateMachine().getGUIProvider().display(config);

		if(config.isUserDone())
		{
			this.reloadResources();
			this.setNextState(status.getPreviousState());
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
		MainWindowBehavior behavior = getStateMachine().getStatus().getBehavior();
		behavior.setMainBarVisible(false);
	}

	/**
	 * Triggers to reload the resources
	 */
	public void reloadResources() {
		getStateMachine().getGUIProvider().reloadResources();
	}
}
