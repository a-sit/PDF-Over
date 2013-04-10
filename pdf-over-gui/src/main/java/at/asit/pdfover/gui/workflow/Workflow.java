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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import at.asit.pdfover.gui.workflow.states.PrepareConfigurationState;

/**
 * Workflow holds logical state of signing process and updates the current logical state
 */
public class Workflow {
	
	/**
	 * SFL4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Workflow.class);
	
	/**
	 * Default constructor
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
		do {
			this.state.update(this);
			next = this.state.nextState();
		} while (next != null);
	}

	private Display display = null;
	
	private Shell shell = null;
	
	private Composite container = null;
	
	private void createMainWindow() {
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
	public Composite getComposite() {
		// TODO: implement
		// Main window will be build on first call
		// returns SWT Composite container for states to draw their GUI
		
		if(this.container == null) {
			this.createMainWindow();
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
