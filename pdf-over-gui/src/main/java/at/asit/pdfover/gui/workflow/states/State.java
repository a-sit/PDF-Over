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
import at.asit.pdfover.gui.workflow.StateMachine;

/**
 * Base state class
 */
public abstract class State {

	/**
	 * The StateMachine
	 */
	private StateMachine stateMachine;

	private State nextState = null;

	/**
	 * Default Workflow State constructor
	 * @param stateMachine the State Machine
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
	 */
	public abstract void run();

	/**
	 * Perform status cleanup
	 */
	public abstract void cleanUp();

	/**
	 * Update the state machine
	 */
	public void updateStateMachine()
	{
		this.stateMachine.invokeUpdate();
	}

	/**
	 * Get the state machine
	 * @return the StateMachine
	 */
	protected StateMachine getStateMachine()
	{
		return this.stateMachine;
	}

	/**
	 * Update the main window behavior of this state if necessary
	 * Should update this.stateMachine.status.getBehavior()
	 */
	public abstract void updateMainWindowBehavior();
}
