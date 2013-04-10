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
public abstract class WorkflowState {

	/**
	 * SFL4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(WorkflowState.class);
	
	private WorkflowState _next = null;
	
	/**
	 * Default Workflow State constructor
	 */
	public WorkflowState() {
		this._next = this;
	}
	
	/**
	 * Gets the next logical state or null if this their is no state transition
	 * @return the next state (or null)
	 */
	public WorkflowState nextState() {
		return this._next;
	}
	
	/**
	 * Sets the next logical state
	 * @param state
	 */
	protected void setNextState(WorkflowState state) {
		this._next = state;
	}
	
	/**
	 * Perform main logic for this state
	 * @param workflow
	 */
	public abstract void update(Workflow workflow);
}
