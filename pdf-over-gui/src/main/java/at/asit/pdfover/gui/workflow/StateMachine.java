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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.MainWindow;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;
import at.asit.pdfover.gui.workflow.states.PrepareConfigurationState;
import at.asit.pdfover.gui.workflow.states.State;

/**
 * Workflow holds logical state of signing process and updates the current
 * logical state
 */
public class StateMachine implements GUIProvider {

	private static final Logger log = LoggerFactory.getLogger(StateMachine.class);

	public final Status status;
	public final PDFSigner pdfSigner;
	public final ConfigurationManager configProvider;
	public final String[] cmdLineArgs;

	/**
	 * Default constructor
	 *
	 * @param cmdLineArgs
	 */
	public StateMachine(String[] cmdLineArgs) {
		this.status = new Status();
		this.status.setCurrentState(new PrepareConfigurationState(this));
		this.pdfSigner = new PDFSigner();
		this.configProvider = new ConfigurationManager();
		this.cmdLineArgs = cmdLineArgs;
	}

	/**
	 * Sets the workflow state
	 * This method should be used to let the user jump
	 * around between states. This Method also resets certain properties defined
	 * by later states then state
	 *
	 * @param state
	 */
	public void jumpToState(State state) {
		this.status.setCurrentState(state);
		this.invokeUpdate();
	}

	/**
	 * Update workflow logic and let state machine do its job...
	 */
	public synchronized void update() {
		State next = null;
		while (this.status.getCurrentState() != null) {
			State current = this.status.getCurrentState();
			try {
				current.run();
			} catch (Exception e) {
				log.error("StateMachine update: ", e);
				ErrorDialog errorState = new ErrorDialog(this.getMainShell(),
						Messages.getString("error.Unexpected"), BUTTONS.OK);
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

				if (this.mainWindow != null && !this.mainWindow.getShell().isDisposed()) {
					log.debug("Allowing MainWindow to update its state for " + current);
					current.updateMainWindowBehavior();
					this.mainWindow.applyBehavior();
					this.mainWindow.doLayout();
				}
				
				// TODO: i really want this to be a return value from run()
				next = current.nextState();
				if (next == current) {
					break;
				}

				if (next == null) {
					log.info("Next state is null -> exit");
					this.status.setCurrentState(next);
					break;
				}

				log.debug("Changing state from: "
						+ current + " to "
						+ next.toString());
				this.status.setCurrentState(next);
			}
		}
	}

	/**
	 * Invoke Update in UI (Main) Thread
	 */
	public void invokeUpdate() {
		if (this.display != null) {
			this.display.asyncExec(() -> {
				this.update();
			});
		}
	}

	private Display display = null;

	private Shell shell = null;

	private Composite container = null;

	private MainWindow mainWindow = null;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * at.asit.pdfover.gui.workflow.StateMachine#display(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void display(Composite composite) {
		this.mainWindow.setTopControl(composite);
	}

	private void createMainWindow() {
		try {

			this.display = Display.getDefault();

			this.mainWindow = new MainWindow(this);
			this.mainWindow.open();

			this.shell = this.mainWindow.getShell();

			this.container = this.mainWindow.getContainer();

			this.shell.open();
			this.shell.layout();
		} catch (Exception e) {
			log.warn("Main-Window creation FAILED. Reason: " + e.getMessage());
			this.display = null;
			this.mainWindow = null;
			this.shell = null;
			this.container = null;
			throw e;
		}
	}

	/**
	 * Gets the Shell for drawing the ui
	 *
	 * @return Composite
	 */
	public synchronized Composite getComposite() {
		// Main window will be built on first call
		// returns SWT Composite container for states to draw their GUI

		if (this.container == null) {
			this.createMainWindow();
		}

		return this.container;
	}

	public <T> T createComposite(Class<T> compositeClass, int style, State state) {
		T composite = null;
		try {
			Constructor<T> constructor = compositeClass.getDeclaredConstructor(
					Composite.class, int.class, State.class);
			composite = constructor.newInstance(getComposite(), style, state);
		} catch (Exception e) {
			log.error("Could not create Composite for Class "
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
		update();

		// if a user interaction is required we have a shell ...
		Shell shell = nonCreatingGetShell();
		Display display = nonCreatingGetDisplay();

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


	public synchronized Shell getMainShell() {
		if(this.shell == null) {
			this.createMainWindow();
		}

		return this.shell;
	}

	public void reloadResources() {
		this.mainWindow.reloadLocalization();
	}
}
