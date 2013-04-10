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
package at.asit.pdfover.gui.workflow;

import at.asit.pdfover.gui.workflow.states.State;

/**
 * 
 */
public interface StateMachine {
	/**
	 * Get the ConfigProvider
	 * @return the ConfigProvider
	 */
	public ConfigProvider getConfigProvider();

	/**
	 * Get the PersistentConfigProvider
	 * @return the PersistentConfigProvider
	 */
	public PersistentConfigProvider getPersistentConfigProvider();

	/**
	 * Gets the Config Manipulator
	 * @return the config manipulator
	 */
	public ConfigManipulator getConfigManipulator();

	/**
	 * Gets the Config Overlay Manipulator
	 * @return the config overlay manipulator
	 */
	public ConfigOverlayManipulator getConfigOverlayManipulator();

	/**
	 * Get the PDF Signer
	 * @return the PDF Signer
	 */
	public PDFSigner getPDFSigner();
	
	/**
	 * Get the Status
	 * @return the Status
	 */
	public Status getStatus();
	
	/**
	 * Gets the GUI provider
	 * @return the GUI provider
	 */
	public GUIProvider getGUIProvider();
	
	/**
	 * Jump to specific state
	 * 
	 * Sets the state machine state this method should be used to let the user jump
	 * around between states. This Method also resets certain properties defined
	 * by later states then the target state.
	 * 
	 * Example: Usually the MainWindow allows the user to jump to the states:
	 * DataSourceSelectionState, PositioningState and BKUSelectionState
	 * 
	 * @param state the state to jump to
	 */
	public void jumpToState(State state);

	/**
	 * Update state machine
	 * Calls the next state.
	 */
	public void update();
	
	/**
	 * Update state machine from other thread
	 * Calls the next state within the main thread
	 */
	public void invokeUpdate();

	/**
	 * Exit state machine execution
	 */
	public void exit();
	
	/**
	 * Gets the command line arguments
	 * 
	 * @return the command line arguments
	 */
	public String[] getCmdArgs();
}
