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

/**
 * Logical state for signing process, usually show BKU Dialog during this state.
 */
public class SigningState extends State {

	/**
	 * @param stateMachine
	 */
	public SigningState(StateMachine stateMachine) {
		super(stateMachine);
	}

	/**
	 * SFL4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SigningState.class);
	
	@Override
	public void run() {
		// TODO Wait until output ready and set output
		
		this.setNextState(new OutputState(this.stateMachine));
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#cleanUp()
	 */
	@Override
	public void cleanUp() {
		// No composite - no cleanup necessary
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#setMainWindowBehavior()
	 */
	@Override
	public void updateMainWindowBehavior() {
		//no change of behavior necessary
	}

	@Override
	public String toString()  {
		return this.getClass().getName();
	}
}
