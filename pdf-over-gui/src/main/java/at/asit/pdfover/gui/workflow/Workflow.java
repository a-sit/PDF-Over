package at.asit.pdfover.gui.workflow;

import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import at.asit.pdfover.gui.workflow.states.PrepareConfigurationState;

public class Workflow {
	public Workflow(String[] cmdLineArgs) {
		setCmdLineAargs(cmdLineArgs);
	}

	/**
	 * @uml.property name="state"
	 * @uml.associationEnd multiplicity="(1 1)" aggregation="shared"
	 *                     inverse="workflow1:at.asit.pdfover.gui.workflow.WorkflowState"
	 */
	private WorkflowState state = new PrepareConfigurationState();

	/**
	 * Getter of the property <tt>state</tt>
	 * 
	 * @return Returns the state.
	 * @uml.property name="state"
	 */
	public WorkflowState getState() {
		return this.state;
	}

	private String[] cmdLineArgs = new String[] {};

	/**
	 * sets the command line arguments
	 * 
	 * @param cmdLineArgs
	 */
	private void setCmdLineAargs(String[] cmdLineArgs) {
		this.cmdLineArgs = cmdLineArgs;
	}

	/**
	 * Gets the command line arguments
	 * 
	 * @return the command line arguments
	 */
	public String[] GetCmdArgs() {
		return this.cmdLineArgs;
	}

	private Properties persistentState = new Properties();

	/**
	 * Gets the persistent state
	 * 
	 * @return the persistent state
	 */
	public Properties GetPersistentState() {
		return this.persistentState;
	}

	/**
	 * Update Workflow logic and let state machine do its job...
	 */
	public void Update() {
		WorkflowState next = null;
		do {
			this.state.update(this);
			next = this.state.nextState();
		} while (next != null);
	}

	private Display display = null;
	
	private Shell shell = null;
	
	private Composite container = null;
	
	private void CreateMainWindow() {
		//TODO: Instantiate Main Window Class
		this.display = Display.getDefault();
		this.shell = new Shell();
		this.shell.setSize(608, 340);
		this.shell.setText("PDFOver 4.0!! :)");
		
		this.container = new Composite(this.shell, SWT.NONE);
		this.container.setBounds(20, 44, 572, 257);
		
		this.shell.open();
		this.shell.layout();
	}
	
	/**
	 * Gets the Shell for drawing the ui
	 * 
	 * @return Composite
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
