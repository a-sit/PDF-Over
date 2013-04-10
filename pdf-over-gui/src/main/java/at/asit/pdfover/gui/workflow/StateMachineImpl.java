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
import java.lang.reflect.Constructor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.MainWindow;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.workflow.states.PrepareConfigurationState;
import at.asit.pdfover.gui.workflow.states.State;

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
		this.status.setCurrentState(state);
		this.update();
	}

	/**
	 * Update Workflow logic and let state machine do its job...
	 */
	@Override
	public void update() {
		State next = null;
		while (this.status.getCurrentState() != null) {
			State current = this.status.getCurrentState();
			try {
				current.run();
			} catch (Exception e) {
				log.error("StateMachine update: ", e); //$NON-NLS-1$
				ErrorDialog errorState = new ErrorDialog(this.getMainShell(), 
						SWT.NONE, "Unexpected Error", e, false);
				//errorState.setException(e);
				//jumpToState(errorState);
				errorState.open();
				this.exit();
			}

			if (this.exit) {
				// exit request ignore
				next = null;
				this.status.setCurrentState(next);
			} else {

				if (this.mainWindow != null
						&& !this.mainWindow.getShell().isDisposed()) {
					log.debug("Allowing MainWindow to update its state for " //$NON-NLS-1$
							+ current);
					current.updateMainWindowBehavior();
					this.mainWindow.applyBehavior();
					this.mainWindow.doLayout();
				}
				next = current.nextState();
				if (next == current) {
					break;
				}

				if (next == null) {
					log.info("Next state is null -> exit"); //$NON-NLS-1$
					this.status.setCurrentState(next);
					break;
				}

				log.debug("Changing state from: " //$NON-NLS-1$
						+ current + " to " //$NON-NLS-1$
						+ next.toString());
				this.status.setCurrentState(next);
			}
		}

		// TODO: Remove following line when releasing ...
		if (this.status.getCurrentState() != null) {
			this.setCurrentStateMessage(this.status.getCurrentState()
					.toString());
		} else {
			this.setCurrentStateMessage(""); //$NON-NLS-1$
		}
	}

	/**
	 * Invoke Update in UI (Main) Thread
	 */
	@Override
	public void invokeUpdate() {
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
	public Composite getComposite() {
		// Main window will be built on first call
		// returns SWT Composite container for states to draw their GUI

		if (this.container == null) {
			this.createMainWindow();
		}

		return this.container;
	}

	@Override
	public <T> T createComposite(Class<T> compositeClass, int style, State state) {
		T composite = null;
		try {
			Constructor<T> constructor = compositeClass.getDeclaredConstructor(
					Composite.class, int.class, State.class);
			composite = constructor.newInstance(getComposite(), style, state);
		} catch (Exception e) {
			log.error("Could not create Composite for Class " //$NON-NLS-1$
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

	private boolean exit = false;

	/**
	 * Exists the Workflow
	 */
	@Override
	public void exit() {
		this.exit = true;
		if (this.shell != null) {
			this.shell.dispose();
		}
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

		if (this.status.getCurrentState() == null) {
			if (shell != null) {
				this.shell.dispose();
			}
		}

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

	/*
	 * (non-Javadoc)
	 * 
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
	@Override
	public String[] getCmdArgs() {
		return this.cmdLineArgs;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.StateMachine#getConfigManipulator()
	 */
	@Override
	public ConfigManipulator getConfigManipulator() {
		return this.configProvider;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.GUIProvider#getMainShell()
	 */
	@Override
	public Shell getMainShell() {
		if(this.shell == null) {
			this.createMainWindow();
		}
		
		return this.shell;
	}
}
