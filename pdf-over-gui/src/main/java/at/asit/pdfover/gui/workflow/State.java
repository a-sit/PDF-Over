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

//Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base state class 
 */
public abstract class State {

	/**
	 * The StateMachine
	 */
	protected StateMachine stateMachine;

	/**
	 * SFL4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(State.class);
	
	private State nextState = null;
	
	/**
	 * Default Workflow State constructor
	 */
	public State(StateMachine stateMachine) {
		this.stateMachine = stateMachine;
		this.nextState = this;
	}
	
	/**
	 * Gets the next logical state or null if this their is no state transition
	 * @return the next state (or null)
	 */
	public State nextState() {
		return this.nextState;
	}
	
	/**
	 * Sets the next logical state
	 * @param state
	 */
	protected void setNextState(State state) {
		this.nextState = state;
	}
	
	/**
	 * Perform main logic for this state
	 * @param stateMachine the state machine
	 */
	public abstract void run();

	/**
	 * Update the state machine
	 */
	public void updateStateMachine()
	{
		stateMachine.update();
	}
}
