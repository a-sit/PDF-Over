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
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import at.asit.pdfover.gui.components.MainWindow;
import at.asit.pdfover.gui.workflow.states.PrepareConfigurationState;

/**
 * Workflow holds logical state of signing process and updates the current
 * logical state
 */
public class Workflow {

	/**
	 * SFL4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Workflow.class);

	/**
	 * Default constructor
	 * 
	 * @param cmdLineArgs
	 */
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
	public String[] cetCmdArgs() {
		return this.cmdLineArgs;
	}

	private Properties persistentState = new Properties();

	/**
	 * Gets the persistent state
	 * 
	 * @return the persistent state
	 */
	public Properties getPersistentState() {
		return this.persistentState;
	}

	/**
	 * Update Workflow logic and let state machine do its job...
	 */
	public void update() {
		WorkflowState next = null;
		while (this.state != null) {
			this.state.update(this);
			next = this.state.nextState();
			if (next == this.state) {
				break;
			}
			//this.state.hideGUI();
			this.state = next;
		}
		if (this.state != null) {
			this.setCurrentStateMessage(this.state.toString());
		} else {
			this.setCurrentStateMessage("");
		}
	}

	private Display display = null;

	private Shell shell = null;

	private Composite container = null;

	private MainWindow mainWindow = null;
	
	private final StackLayout stack = new StackLayout();

	/**
	 * Helper method for developing
	 * @param value
	 */
	public void setCurrentStateMessage(String value) {
		if(this.mainWindow != null) {
			this.mainWindow.setStatus(value);
		}
	}
	
	/**
	 * Used by active workflow state to show its own gui component
	 * @param ctrl
	 */
	public void setTopControl(final Control ctrl) {
		this.mainWindow.setTopControl(ctrl);
	}

	private void createMainWindow() {
		this.display = Display.getDefault();
		
		this.mainWindow = new MainWindow();
		
		this.mainWindow.open();
		
		this.shell = this.mainWindow.getShell();
		
		this.container = this.mainWindow.getContainer();
		
		this.shell.open();
		this.shell.layout();
	}

	/**
	 * Gets the Shell for drawing the ui
	 * 
	 * @return Composite
	 */
	public Composite getComposite() {
		// Main window will be build on first call
		// returns SWT Composite container for states to draw their GUI

		if (this.container == null) {
			this.createMainWindow();
		}

		if (this.container == null) {
			// TODO throw Exception...
		}

		return this.container;
	}

	/**
	 * Only returns a shell if one was already created ...
	 * 
	 * @return
	 */
	private Shell nonCreatingGetShell() {
		return this.shell;
	}

	/**
	 * Only returns a shell if one was already created ...
	 * 
	 * @return
	 */
	private Display nonCreatingGetDisplay() {
		return this.display;
	}

	/**
	 * Workflow main entrance point
	 */
	public void start() {
		// Call update to start processing ...
		this.update();

		// if a user interaction is required we have a shell ...
		Shell shell = this.nonCreatingGetShell();
		Display display = this.nonCreatingGetDisplay();

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
