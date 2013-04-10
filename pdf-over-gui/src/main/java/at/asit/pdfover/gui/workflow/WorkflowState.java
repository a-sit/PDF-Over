package at.asit.pdfover.gui.workflow;

/**
 * Base state class 
 * @author afitzek
 */
public abstract class WorkflowState {

	private WorkflowState _next = null;
	
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
