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
package at.asit.pdfover.gui;

// Imports
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.composites.StateComposite;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.states.ConfigurationUIState;
import at.asit.pdfover.gui.workflow.states.OpenState;
import at.asit.pdfover.gui.workflow.states.PositioningState;

/**
 * The Main Window of PDFOver 4.0
 */
public class MainWindow {

	/**
	 * 
	 */
	private final class ConfigSelectionListener implements SelectionListener {
		/**
		 * 
		 */
		public ConfigSelectionListener() {
			// Nothing to do here
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			MainWindow.this.stateMachine.jumpToState(new ConfigurationUIState(
					MainWindow.this.stateMachine));
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// Nothing to do here
		}
	}

	/**
	 * Selection Listener for Position Button
	 */
	private final class PositionSelectionListener extends SelectionAdapter {
		/**
		 * Empty constructor
		 */
		public PositionSelectionListener() {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			MainWindow.this.stateMachine.jumpToState(new PositioningState(
					MainWindow.this.stateMachine));
		}
	}

	/**
	 * Selection Listener for Open Button
	 */
	private final class DataSourceSelectionListener extends SelectionAdapter {
		/**
		 * Empty constructor
		 */
		public DataSourceSelectionListener() {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			MainWindow.this.stateMachine
					.jumpToState(new OpenState(
							MainWindow.this.stateMachine));
		}
	}

	/**
	 * SFL4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(MainWindow.class);

	private Shell shell;
	private CLabel lbl_status;
	private Composite container;
	private StackLayout stack;
	StateMachine stateMachine;
	private Button btn_sign;
	private Button btn_position;
	private Button btn_open;
	private Button btn_config;

	/**
	 * Main bar Buttons
	 */
	public enum Buttons {
		CONFIG, OPEN, POSITION, SIGN, FINAL
	}

	private Map<Buttons, Button> buttonMap;

	/**
	 * Default constructor
	 * 
	 * @param stateMachine
	 *            The main workflow
	 */
	public MainWindow(StateMachine stateMachine) {
		super();

		this.stateMachine = stateMachine;

		this.buttonMap = new EnumMap<MainWindow.Buttons, Button>(Buttons.class);
	}

	/**
	 * Set current status (may be removed in production release)
	 * 
	 * @param value
	 */
	public void setStatus(String value) {
		if (this.getShell().isDisposed()) {
			return;
		}
		this.lbl_status.setText("[DEBUG]: Current workflow state: " + value);
	}

	/**
	 * Sets top level composite for stack layout
	 * 
	 * @param ctrl
	 */
	public void setTopControl(Control ctrl) {
		log.debug("Top control: " + ctrl.toString());
		this.stack.topControl = ctrl;
		this.doLayout();
	}

	/**
	 * Layout the Main Window
	 */
	public void doLayout() {
		Control ctrl = this.stack.topControl;
		this.container.layout(true, true);
		this.shell.layout(true, true);
		// Note: SWT only layouts children! No grandchildren!
		if (ctrl instanceof StateComposite) {
			((StateComposite) ctrl).doLayout();
		}
	}

	/**
	 * Gets the container composite
	 * 
	 * @return the container composite
	 */
	public Composite getContainer() {
		return this.container;
	}

	/**
	 * Entrance point for swt designer
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = Display.getDefault();

		MainWindow window = new MainWindow(null);

		window.open();

		window.getShell().open();
		window.getShell().layout();
		while (!window.getShell().isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Open the window.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		createContents();
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		this.shell = new Shell();
		getShell().setSize(500, 800);
		getShell().setText("PDF-Over");

		getShell().setLayout(new FormLayout());

		Composite composite = new Composite(getShell(), SWT.NONE);
		FormData fd_composite = new FormData();
		fd_composite.left = new FormAttachment(0, 5);
		fd_composite.right = new FormAttachment(100, -5);
		fd_composite.top = new FormAttachment(0, 5);
		fd_composite.bottom = new FormAttachment(0, 40);
		composite.setLayoutData(fd_composite);
		composite.setLayout(new FormLayout());

		this.btn_config = new Button(composite, SWT.NONE);
		FormData fd_config = new FormData();
		fd_config.left = new FormAttachment(0, 0);
		fd_config.right = new FormAttachment(25, 0);
		fd_config.top = new FormAttachment(0, 0);
		fd_config.bottom = new FormAttachment(100, 0);
		this.btn_config.setLayoutData(fd_config);
		this.btn_config.setText("Config ...");
		this.btn_config.addSelectionListener(new ConfigSelectionListener());
		this.buttonMap.put(Buttons.CONFIG, this.btn_config);

		this.btn_open = new Button(composite, SWT.NONE);
		FormData fd_open = new FormData();
		fd_open.left = new FormAttachment(25, 0);
		fd_open.right = new FormAttachment(50, 0);
		fd_open.top = new FormAttachment(0, 0);
		fd_open.bottom = new FormAttachment(100, 0);
		this.btn_open.setLayoutData(fd_open);
		this.btn_open.setText("Open ...");
		this.btn_open.addSelectionListener(new DataSourceSelectionListener());
		this.buttonMap.put(Buttons.OPEN, this.btn_open);

		this.btn_position = new Button(composite, SWT.NONE);
		FormData fd_position = new FormData();
		fd_position.left = new FormAttachment(50, 0);
		fd_position.right = new FormAttachment(75, 0);
		fd_position.top = new FormAttachment(0, 0);
		fd_position.bottom = new FormAttachment(100, 0);
		this.btn_position.setLayoutData(fd_position);
		this.btn_position.setText("Positon ...");
		this.btn_position.addSelectionListener(new PositionSelectionListener());
		this.buttonMap.put(Buttons.POSITION, this.btn_position);

		this.btn_sign = new Button(composite, SWT.NONE);
		FormData fd_sign = new FormData();
		fd_sign.left = new FormAttachment(75, 0);
		fd_sign.right = new FormAttachment(100, 0);
		fd_sign.top = new FormAttachment(0, 0);
		fd_sign.bottom = new FormAttachment(100, 0);
		this.btn_sign.setLayoutData(fd_sign);
		this.btn_sign.setText("Sign ...");
		this.buttonMap.put(Buttons.SIGN, this.btn_sign);

		this.container = new Composite(getShell(), SWT.BORDER | SWT.RESIZE);
		FormData fd_composite_1 = new FormData();
		fd_composite_1.bottom = new FormAttachment(100, -25);
		fd_composite_1.right = new FormAttachment(100, -5);
		fd_composite_1.top = new FormAttachment(0, 45);
		fd_composite_1.left = new FormAttachment(0, 5);
		this.container.setLayoutData(fd_composite_1);
		this.stack = new StackLayout();
		this.container.setLayout(this.stack);

		this.lbl_status = new CLabel(getShell(), SWT.NONE);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.right = new FormAttachment(100, -5);
		fd_lblNewLabel.bottom = new FormAttachment(100, -5);
		fd_lblNewLabel.top = new FormAttachment(100, -20);
		fd_lblNewLabel.left = new FormAttachment(0, 5);
		this.lbl_status.setLayoutData(fd_lblNewLabel);
		this.lbl_status.setText("New Label");

	}

	/**
	 * Update MainWindow to fit new status
	 */
	public void applyBehavior() {
		MainWindowBehavior behavior = this.stateMachine.getStatus()
				.getBehavior();

		log.debug("Updating MainWindow state for : "
				+ this.stateMachine.getStatus().getCurrentState().toString());

		for (Buttons button : Buttons.values()) {
			boolean active = behavior.getActive(button);
			boolean enabled = behavior.getEnabled(button);

			Button theButton = this.buttonMap.get(button);
			if (theButton != null)
			{
				theButton.setEnabled(enabled);
			}
		}

		//TODO: Display/Hide main bar
	}

	/**
	 * @return the shell
	 */
	public Shell getShell() {
		return this.shell;
	}
}
