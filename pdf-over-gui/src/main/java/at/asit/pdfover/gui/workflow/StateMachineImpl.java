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

import java.lang.reflect.Constructor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import at.asit.pdfover.gui.MainWindow;
import at.asit.pdfover.gui.workflow.states.BKUSelectionState;
import at.asit.pdfover.gui.workflow.states.OpenState;
import at.asit.pdfover.gui.workflow.states.PositioningState;
import at.asit.pdfover.gui.workflow.states.PrepareConfigurationState;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.gui.workflow.states.BKUSelectionState.BKUs;

/**
 * Workflow holds logical state of signing process and updates the current
 * logical state
 */
public class StateMachineImpl implements StateMachine, GUIProvider {

	/**
	 * SFL4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(StateMachineImpl.class);

	private StatusImpl status;

	private PDFSignerImpl pdfSigner;

	private ConfigProviderImpl configProvider;

	/**
	 * Default constructor
	 * 
	 * @param cmdLineArgs
	 */
	public StateMachineImpl(String[] cmdLineArgs) {
		this.status = new StatusImpl();
		this.status.setCurrentState(new PrepareConfigurationState(this));
		this.pdfSigner = new PDFSignerImpl();
		this.configProvider = new ConfigProviderImpl();
		setCmdLineAargs(cmdLineArgs);
	}

	/**
	 * Sets the workflow state this method should be used to let the user jump
	 * around between states. This Method also resets certain properties defined
	 * by later states then state
	 * 
	 * @param state
	 */
	@Override
	public void jumpToState(State state) {
		if (this.status.getCurrentState() != state && state != null) {
			this.status.setCurrentState(state);

			// TODO rewrite when Config is done ...
			if (state instanceof PositioningState) {
				// User jumps to positioning state !
				// restore possible default for bku selection / forget BKU
				// selection
				this.getStatus().setBKU(
						this.getConfigProvider().getDefaultBKU());
				// forget position
				this.getStatus().setSignaturePosition(null);
			} else if (state instanceof BKUSelectionState) {
				// User jumps to bku selection state !
				// forget bku selection
				this.getStatus().setBKU(BKUs.NONE);
			} else if (state instanceof OpenState) {
				// User jumps to data source selection state !
				// forget bku selection / restore possible default for bku
				// selection
				this.getStatus().setBKU(
						this.getConfigProvider().getDefaultBKU());
				// forget position / restore possible default for position
				this.getStatus().setSignaturePosition(
						this.getConfigProvider().getDefaultSignaturePosition());
				// forget data source selection
				this.getStatus().setDocument(null);
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
		while (this.status.getCurrentState() != null) {
			State current = this.status.getCurrentState();
			current.run();
			if (this.mainWindow != null
					&& !this.mainWindow.getShell().isDisposed()) {
				log.debug("Allowing MainWindow to update its state for "
						+ current);
				current.updateMainWindowBehavior();
				this.mainWindow.applyBehavior();
				this.mainWindow.doLayout();
			}
			next = current.nextState();
			if (next == current) {
				break;
			}
			log.debug("Changing state from: "
					+ current + " to "
					+ next.toString());
			this.status.setCurrentState(next);
		}
		if (this.status.getCurrentState() != null) {
			this.setCurrentStateMessage(this.status.getCurrentState()
					.toString());
		} else {
			this.setCurrentStateMessage("");
		}
	}

	/**
	 * Invoke Update in UI (Main) Thread
	 */
	@Override
	public void InvokeUpdate() {
		if (this.display != null) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.workflow.StateMachine#display(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void display(Composite composite) {
		this.mainWindow.setTopControl(composite);
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

	@Override
	public <T> T createComposite(Class<T> compositeClass, int style) {
		T composite = null;
		try {
			Constructor<T> constructor = compositeClass.getDeclaredConstructor(
					Composite.class, int.class, BKUSelectionState.class);
			composite = constructor.newInstance(getComposite(), style, this);
		} catch (Exception e) {
			log.error(
					"Could not create Composite for Class "
							+ compositeClass.getName(), e);
		}
		return composite;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.StateMachine#getConfigProvider()
	 */
	@Override
	public ConfigProvider getConfigProvider() {
		return this.configProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.StateMachine#getStatus()
	 */
	@Override
	public Status getStatus() {
		return this.status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.StateMachine#getPDFSigner()
	 */
	@Override
	public PDFSigner getPDFSigner() {
		return this.pdfSigner;
	}
	
	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.StateMachine#getGUIProvider()
	 */
	@Override
	public GUIProvider getGUIProvider() {
		return this;
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
}
