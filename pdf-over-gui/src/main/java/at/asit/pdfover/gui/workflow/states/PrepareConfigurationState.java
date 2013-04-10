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

//Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.signator.Signator;


/**
 * Starting state of workflow proccess
 * 
 * Reads configuration, command arguments and initializes configured variables
 */
public class PrepareConfigurationState extends State {

	/**
	 * @param stateMachine
	 */
	public PrepareConfigurationState(StateMachine stateMachine) {
		super(stateMachine);
	}

	/**
	 * SFL4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(PrepareConfigurationState.class);

	@Override
	public void run() {
		// TODO: Read config file and command line arguments
		// Set usedSignerLib ...
		this.stateMachine.getPDFSigner().setUsedPDFSignerLibrary(Signator.Signers.PDFAS);
		
		// Create PDF Signer
		this.stateMachine.getStatus().setBKU(this.stateMachine.getConfigProvider().getDefaultBKU());
		
		this.stateMachine.getStatus().setSignaturePosition(this.stateMachine.getConfigProvider().getDefaultSignaturePosition());
		
		this.setNextState(new OpenState(this.stateMachine));
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#setMainWindowBehavior()
	 */
	@Override
	public void updateMainWindowBehavior() {
		//no behavior necessary yet
	}

	@Override
	public String toString()  {
		return this.getClass().getName();
	}
}
