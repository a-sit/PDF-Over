package at.asit.pdfover.gui.workflow;

import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Workflow {
	/**
	 * @uml.property name="state"
	 * @uml.associationEnd multiplicity="(1 1)" aggregation="shared"
	 *                     inverse="workflow1:at.asit.pdfover.gui.workflow.WorkflowState"
	 */
	private WorkflowState state = new at.asit.pdfover.gui.workflow.states.PrepareConfigurationState();

	/**
	 * Getter of the property <tt>state</tt>
	 * 
	 * @return Returns the state.
	 * @uml.property name="state"
	 */
	public WorkflowState getState() {
		return state;
	}

	private String[] CmdArgs = new String[] {};

	/**
	 * Sets the Cmd Arguments
	 * 
	 * @param args
	 */
	public void SetCmdArgs(String[] args) {
		this.CmdArgs = args;
	}

	/**
	 * Gets the Cmd Arguments
	 * 
	 * @return
	 */
	public String[] GetCmdArgs() {
		return this.CmdArgs;
	}

	private Properties persistent_state = new Properties();

	/**
	 * Gets the Persistent State
	 * 
	 * @return
	 */
	public Properties GetPersistentState() {
		return this.persistent_state;
	}

	/**
	 * Update Workflow logic and let state machine do its job...
	 */
	public void Update() {
		WorkflowState next = null;
		do {
			this.state.Update(this);
			next = this.state.NextState();
		} while (next != null);
	}

	private Display display = null;
	
	private Shell shell = null;
	
	private Composite container = null;
	
	private void CreateMainWindow() {
		//TODO: Instanciate Main Window Class
		this.display = Display.getDefault();
		shell = new Shell();
		shell.setSize(608, 340);
		shell.setText("PDFOver 4.0!! :)");
		
		container = new Composite(shell, SWT.NONE);
		container.setBounds(20, 44, 572, 257);
		
		shell.open();
        shell.layout();
	}
	
	/**
	 * Gets the Shell for drawing the ui
	 * 
	 * @return
	 */
	public Composite GetComposite() {
		// TODO: implement
		// Main window will be build on first call
		// returns SWT Composite container for states to draw their GUI
		
		if(this.container == null) {
			this.CreateMainWindow();
		}
		
		if(this.container == null) {
			// TODO throw Exception...
		}
		
		return this.container;
	}

	/**
	 * Only returns a shell if one was already created ...
	 * 
	 * @return
	 */
	private Shell NonCreatingGetShell() {
		return this.shell;
	}

	/**
	 * Only returns a shell if one was already created ...
	 * 
	 * @return
	 */
	private Display NonCreatingGetDisplay() {
		return this.display;
	}

	/**
	 * Workflow main entrance point
	 */
	public void Start() {
		// Call update to start processing ...
		this.Update();

		// if a user interaction is required we have a shell ...
		Shell shell = this.NonCreatingGetShell();
		Display display = this.NonCreatingGetDisplay();

		if (shell != null && display != null) {
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			display.dispose();
		}		
	}

}
