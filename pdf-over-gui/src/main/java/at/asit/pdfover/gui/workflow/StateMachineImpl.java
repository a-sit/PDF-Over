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

import java.io.File;
import java.util.Properties;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import at.asit.pdfover.gui.components.MainWindow;
import at.asit.pdfover.gui.workflow.states.BKUSelectionState;
import at.asit.pdfover.gui.workflow.states.DataSourceSelectionState;
import at.asit.pdfover.gui.workflow.states.PositioningState;
import at.asit.pdfover.gui.workflow.states.PrepareConfigurationState;
import at.asit.pdfover.gui.workflow.states.BKUSelectionState.BKUs;
import at.asit.pdfover.signator.Signator;
import at.asit.pdfover.signator.SignatureParameter;
import at.asit.pdfover.signator.Signer;

/**
 * Workflow holds logical state of signing process and updates the current
 * logical state
 */
public class StateMachineImpl implements StateMachine {

	/**
	 * SFL4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(StateMachineImpl.class);

	/**
	 * Default constructor
	 * 
	 * @param cmdLineArgs
	 */
	public StateMachineImpl(String[] cmdLineArgs) {
		setCmdLineAargs(cmdLineArgs);
	}

	/**
	 * @uml.property name="state"
	 * @uml.associationEnd multiplicity="(1 1)" aggregation="shared"
	 *                     inverse="workflow1:at.asit.pdfover.gui.workflow.WorkflowState"
	 */
	private State state = new PrepareConfigurationState();

	/**
	 * Getter of the property <tt>state</tt>
	 * 
	 * @return Returns the state.
	 * @uml.property name="state"
	 */
	public State getState() {
		return this.state;
	}

	/**
	 * Sets the workflow state This method should be used to let the user jump
	 * around between states. This Method also resets certain properties defined
	 * by later states then state
	 * 
	 * @param state
	 */
	public void setWorkflowState(State state) {
		if (this.state != state && state != null) {
			this.state = state;

			if (state instanceof PositioningState) {
				// User jumps to positioning state !
				// restore possible default for bku selection / forget BKU
				// selection
				this.setSelectedBKU(PrepareConfigurationState
						.readSelectedBKU(this.getConfigurationValues()));
				// forget position
				this.getParameter().setSignaturePosition(null);
			} else if (state instanceof BKUSelectionState) {
				// User jumps to bku selection state !
				// forget bku selection
				this.setSelectedBKU(BKUs.NONE);
			} else if (state instanceof DataSourceSelectionState) {
				// User jumps to data source selection state !
				// forget bku selection / restore possible default for bku
				// selection
				this.setSelectedBKU(PrepareConfigurationState
						.readSelectedBKU(this.getConfigurationValues()));
				// forget position / restore possible default for position
				this.getParameter().setSignaturePosition(
						PrepareConfigurationState.readDefinedPosition(this
								.getConfigurationValues()));
				// forget data source selection
				this.setDataSource(null);
			}

			this.update();
		}
	}

	/**
	 * Update Workflow logic and let state machine do its job...
	 */
	@Override
	public void update() {
		State next = null;
		while (this.state != null) {
			this.state.run();
			if (this.mainWindow != null && !this.mainWindow.getShell().isDisposed()) {
				log.debug("Allowing MainWindow to update its state for " + this.state.toString());
				this.mainWindow.UpdateNewState();
				this.mainWindow.doLayout();
			}
			next = this.state.nextState();
			if (next == this.state) {
				break;
			}
			// this.state.hideGUI();
			log.debug("Changing state from: " + this.state.toString() + " to "
					+ next.toString());
			this.state = next;
		}
		if (this.state != null) {
			this.setCurrentStateMessage(this.state.toString());
		} else {
			this.setCurrentStateMessage("");
		}
	}

	/**
	 * Invoke Update in UI (Main) Thread
	 */
	public void InvokeUpdate() {
		if(this.display != null) {
			this.display.asyncExec(new Runnable() {
				
				@Override
				public void run() {
					StateMachineImpl.this.update();
				}
			});
		}
	}
	
	private Display display = null;

	private Shell shell = null;

	private Composite container = null;

	private MainWindow mainWindow = null;

	/**
	 * Helper method for developing
	 * 
	 * @param value
	 */
	public void setCurrentStateMessage(String value) {
		if (this.mainWindow != null) {
			this.mainWindow.setStatus(value);
		}
	}

	/**
	 * Used by active workflow state to show its own gui component
	 * 
	 * @param ctrl
	 */
	public void setTopControl(final Control ctrl) {
		this.mainWindow.setTopControl(ctrl);
	}

	private void createMainWindow() {
		this.display = Display.getDefault();

		this.mainWindow = new MainWindow(this);

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
	@Override
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
	 * Exists the Workflow
	 */
	@Override
	public void exit() {
		this.shell.dispose();
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

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.StateMachine#getConfigProvider()
	 */
	@Override
	public ConfigProvider getConfigProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.StateMachine#getStatus()
	 */
	@Override
	public Status getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	// Data Section
	// =============================================================================

	// Command Line Arguments
	// -------------------------------------------------------
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
	public String[] getCmdArgs() {
		return this.cmdLineArgs;
	}

	// Key Value String properties
	// -------------------------------------------------------
	private Properties configurationValues = new Properties();

	/**
	 * Gets the persistent state
	 * 
	 * @return the persistent state
	 */
	public Properties getConfigurationValues() {
		return this.configurationValues;
	}

	// Data source
	// -------------------------------------------------------
	private File dataSource = null;

	/**
	 * Gets the DataSource
	 * 
	 * @return The data source file
	 */
	public File getDataSource() {
		return this.dataSource;
	}

	/**
	 * Sets the DataSource
	 * 
	 * @param file
	 */
	public void setDataSource(File file) {
		this.dataSource = file;
	}

	// Selected BKU
	// -------------------------------------------------------

	/**
	 * The selected BKU
	 */
	private BKUs selectedBKU = BKUs.NONE;

	/**
	 * Gets the selected BKU
	 * 
	 * @return the selectedBKU
	 */
	public BKUs getSelectedBKU() {
		return this.selectedBKU;
	}

	/**
	 * Sets the selected BKU
	 * 
	 * @param selectedBKU
	 *            the selectedBKU to set
	 */
	public void setSelectedBKU(BKUs selectedBKU) {
		this.selectedBKU = selectedBKU;
	}

	private Signator.Signers usedSignerLib = Signator.Signers.PDFAS;

	/**
	 * The PDF Signer
	 */
	private Signer pdfSigner = null;

	/**
	 * @return the pdfSigner
	 */
	public Signer getPdfSigner() {
		return this.pdfSigner;
	}

	/**
	 * @param pdfSigner
	 *            the pdfSigner to set
	 */
	public void setPdfSigner(Signer pdfSigner) {
		this.pdfSigner = pdfSigner;
	}

	private SignatureParameter parameter = null;

	/**
	 * @return the parameter
	 */
	public SignatureParameter getParameter() {
		return this.parameter;
	}

	/**
	 * @param parameter
	 *            the parameter to set
	 */
	public void setParameter(SignatureParameter parameter) {
		this.parameter = parameter;
	}

	/**
	 * @return the usedSignerLib
	 */
	public Signator.Signers getUsedSignerLib() {
		return this.usedSignerLib;
	}

	/**
	 * @param usedSignerLib
	 *            the usedSignerLib to set
	 */
	public void setUsedSignerLib(Signator.Signers usedSignerLib) {
		this.usedSignerLib = usedSignerLib;
	}
}
