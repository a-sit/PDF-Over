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
package at.asit.pdfover.gui.components;

// Imports
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import java.lang.Class;
import java.util.HashMap;
import java.util.Map;

import at.asit.pdfover.gui.components.main_behavior.ConfigOpenEnabled;
import at.asit.pdfover.gui.components.main_behavior.ConfigOpenPositionEnabled;
import at.asit.pdfover.gui.components.main_behavior.MainWindowAllDisabled;
import at.asit.pdfover.gui.components.main_behavior.MainWindowBehavior;
import at.asit.pdfover.gui.components.main_behavior.OnlyConfigEnabled;
import at.asit.pdfover.gui.workflow.Workflow;
import at.asit.pdfover.gui.workflow.WorkflowState;
import at.asit.pdfover.gui.workflow.states.BKUSelectionState;
import at.asit.pdfover.gui.workflow.states.DataSourceSelectionState;
import at.asit.pdfover.gui.workflow.states.LocalBKUState;
import at.asit.pdfover.gui.workflow.states.MobileBKUState;
import at.asit.pdfover.gui.workflow.states.OutputState;
import at.asit.pdfover.gui.workflow.states.PositioningState;
import at.asit.pdfover.gui.workflow.states.PrepareConfigurationState;
import at.asit.pdfover.gui.workflow.states.PrepareSigningState;
import at.asit.pdfover.gui.workflow.states.SigningState;

/**
 * The Main Window of PDFOver 4.0
 */
public class MainWindow {

	/**
	 * 
	 */
	private final class DataSourceSelectionListener implements
			SelectionListener {
		/**
		 * 
		 */
		public DataSourceSelectionListener() {
			// Nothing to do here
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			MainWindow.this.workflow.setWorkflowState(new DataSourceSelectionState());
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// Nothing to do here
		}
	}

	/**
	 * SFL4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
	private Shell shell;
	private CLabel lbl_status;
	private Composite container;
	private StackLayout stack;
	private Workflow workflow;
	private Button btn_sign;
	
	/**
	 * Gets the sign button
	 * @return the sign button
	 */
	public Button getBtn_sign() {
		return this.btn_sign;
	}

	private Button btn_position;
	
	/**
	 * Gets the position button
	 * @return the position button
	 */
	public Button getBtn_position() {
		return this.btn_position;
	}

	private Button btn_open;
	
	/**
	 * Gets the open button
	 * @return the open button
	 */
	public Button getBtn_open() {
		return this.btn_open;
	}

	private Button btn_config;
	
	/**
	 * Gets the config button
	 * @return the config button
	 */
	public Button getBtn_config() {
		return this.btn_config;
	}

	private Map<Class, MainWindowBehavior> behavior = new HashMap<Class, MainWindowBehavior>();
	
	/**
	 * Default contsructor
	 * @param workflow The main workflow
	 */
	public MainWindow(Workflow workflow) {
		super();
		
		this.behavior.put(PrepareConfigurationState.class, new MainWindowAllDisabled());
		this.behavior.put(PrepareSigningState.class, new MainWindowAllDisabled());
		this.behavior.put(SigningState.class, new MainWindowAllDisabled());
		this.behavior.put(LocalBKUState.class, new MainWindowAllDisabled());
		this.behavior.put(MobileBKUState.class, new MainWindowAllDisabled());
		
		this.behavior.put(OutputState.class, new MainWindowAllDisabled());
		
		this.behavior.put(DataSourceSelectionState.class, new OnlyConfigEnabled());
		
		this.behavior.put(PositioningState.class, new ConfigOpenEnabled());
		
		this.behavior.put(BKUSelectionState.class, new ConfigOpenPositionEnabled());
		
		this.workflow = workflow;
	}

	/**
	 * Set current status (may be removed in production release)
	 * @param value
	 */
	public void setStatus(String value) {
		if(this.getShell().isDisposed()) {
			return;
		}
		this.lbl_status.setText("[DEBUG]: Current workflow state: " + value);
	}

	/**
	 * Sets top level composite for stack layout
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
		if(ctrl instanceof StateComposite) {
			((StateComposite)ctrl).doLayout();
		}
	}
	
	/**
	 * Gets the container composite
	 * @return
	 */
	public Composite getContainer() {
		return this.container;
	}
	
	/**
	 * Entrance point for swt designer
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
		getShell().setSize(450, 329);
		getShell().setText("PDF OVER 4.0! :)");
		
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
		
		this.btn_open = new Button(composite, SWT.NONE);
		FormData fd_open = new FormData();
		fd_open.left = new FormAttachment(25, 0);
		fd_open.right = new FormAttachment(50, 0);
		fd_open.top = new FormAttachment(0, 0);
		fd_open.bottom = new FormAttachment(100, 0);
		this.btn_open.setLayoutData(fd_open);
		this.btn_open.setText("Open ...");
		this.btn_open.addSelectionListener(new DataSourceSelectionListener());
		
		this.btn_position = new Button(composite, SWT.NONE);
		FormData fd_position = new FormData();
		fd_position.left = new FormAttachment(50, 0);
		fd_position.right = new FormAttachment(75, 0);
		fd_position.top = new FormAttachment(0, 0);
		fd_position.bottom = new FormAttachment(100, 0);
		this.btn_position.setLayoutData(fd_position);
		this.btn_position.setText("Positon ...");
		
		this.btn_sign = new Button(composite, SWT.NONE);
		FormData fd_sign = new FormData();
		fd_sign.left = new FormAttachment(75, 0);
		fd_sign.right = new FormAttachment(100, 0);
		fd_sign.top = new FormAttachment(0, 0);
		fd_sign.bottom = new FormAttachment(100, 0);
		this.btn_sign.setLayoutData(fd_sign);
		this.btn_sign.setText("Sign ...");
		
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
	public void UpdateNewState() {
		WorkflowState state = this.workflow.getState();
		
		log.debug("Updating MainWindow state for : " + state.toString());
		
		if(this.behavior.containsKey(state.getClass())) {
			this.behavior.get(state.getClass()).SetState(this);
		}
	}
	
	/**
	 * @return the shell
	 */
	public Shell getShell() {
		return this.shell;
	}
}
