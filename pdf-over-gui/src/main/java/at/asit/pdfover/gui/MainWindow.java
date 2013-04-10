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
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.composites.StateComposite;
import at.asit.pdfover.gui.controls.MainBarButton;
import at.asit.pdfover.gui.controls.MainBarEndButton;
import at.asit.pdfover.gui.controls.MainBarMiddleButton;
import at.asit.pdfover.gui.controls.MainBarRectangleButton;
import at.asit.pdfover.gui.controls.MainBarStartButton;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.states.ConfigurationUIState;
import at.asit.pdfover.gui.workflow.states.OpenState;
import at.asit.pdfover.gui.workflow.states.PositioningState;

/**
 * The Main Window of PDFOver 4.0
 */
public class MainWindow {

	/**
	 * SFL4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(MainWindow.class);

	private Shell shell;
	private CLabel lbl_status;
	private Composite container;
	private StackLayout stack;
	StateMachine stateMachine;
	private MainBarButton btn_sign;
	private MainBarButton btn_position;
	private MainBarButton btn_open;
	private MainBarButton btn_config;

	/**
	 * Main bar Buttons
	 */
	public enum Buttons {
		CONFIG, OPEN, POSITION, SIGN, FINAL
	}

	private Map<Buttons, MainBarButton> buttonMap;

	private FormData mainBarFormData;

	/**
	 * Default constructor
	 * 
	 * @param stateMachine
	 *            The main workflow
	 */
	public MainWindow(StateMachine stateMachine) {
		super();

		this.stateMachine = stateMachine;

		this.buttonMap = new EnumMap<MainWindow.Buttons, MainBarButton>(Buttons.class);
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
		this.lbl_status.setText("[DEBUG]: Current workflow state: " + value); //$NON-NLS-1$
	}

	/**
	 * Sets top level composite for stack layout
	 * 
	 * @param ctrl
	 */
	public void setTopControl(Control ctrl) {
		log.debug("Top control: " + ctrl.toString()); //$NON-NLS-1$
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
			if (!ctrl.isDisposed()) {
				((StateComposite) ctrl).doLayout();
			}
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
		composite.setLayout(new FormLayout());
		this.mainBarFormData = new FormData();
		this.mainBarFormData.left = new FormAttachment(0, 5);
		this.mainBarFormData.right = new FormAttachment(100, -5);
		this.mainBarFormData.top = new FormAttachment(0, 5);
		this.mainBarFormData.bottom = new FormAttachment(0, 60);
		composite.setLayoutData(this.mainBarFormData);

		this.btn_config = new MainBarRectangleButton(composite, SWT.NONE);
		FormData fd_btn_config = new FormData();
		fd_btn_config.bottom = new FormAttachment(0, 45);
		fd_btn_config.right = new FormAttachment(10,0);
		fd_btn_config.top = new FormAttachment(0);
		fd_btn_config.left = new FormAttachment(0, 2);
		this.btn_config.setLayoutData(fd_btn_config);
		this.btn_config.setText("Config");
		this.btn_config.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				MainWindow.this.stateMachine.jumpToState(new ConfigurationUIState(
						MainWindow.this.stateMachine));
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				// NOTHING TO DO HERE
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// NOTHING TO DO HERE				
			}
		});
		this.buttonMap.put(Buttons.CONFIG, this.btn_config);
		
		InputStream is = this.getClass().getResourceAsStream("/img/config.png");
		
		this.btn_config.setImage(new Image(Display.getDefault(), new ImageData(is)));
		
		this.btn_open = new MainBarStartButton(composite, SWT.NONE);
		FormData fd_btn_open = new FormData();
		fd_btn_open.bottom = new FormAttachment(0, 45);
		fd_btn_open.right = new FormAttachment(35, 5);
		fd_btn_open.top = new FormAttachment(0);
		fd_btn_open.left = new FormAttachment(10, 0);
		this.btn_open.setLayoutData(fd_btn_open);
		this.btn_open.setText("Open");
		this.btn_open.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				MainWindow.this.stateMachine.jumpToState(new OpenState(
						MainWindow.this.stateMachine));
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				// NOTHING TO DO HERE
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// NOTHING TO DO HERE				
			}
		});
		this.buttonMap.put(Buttons.OPEN, this.btn_open);

		this.btn_position = new MainBarMiddleButton(composite, SWT.NONE);
		FormData fd_btn_position = new FormData();
		fd_btn_position.bottom = new FormAttachment(0, 45);
		fd_btn_position.right = new FormAttachment(60, 5);
		fd_btn_position.top = new FormAttachment(0);
		fd_btn_position.left = new FormAttachment(35, -5);
		this.btn_position.setLayoutData(fd_btn_position);
		this.btn_position.setText("Positon ...");
		this.btn_position.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				MainWindow.this.stateMachine.jumpToState(new PositioningState(
						MainWindow.this.stateMachine));
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				// NOTHING TO DO HERE
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// NOTHING TO DO HERE				
			}
		});
		this.buttonMap.put(Buttons.POSITION, this.btn_position);

		this.btn_sign = new MainBarMiddleButton(composite, SWT.NONE);
		FormData fd_btn_sign = new FormData();
		fd_btn_sign.bottom = new FormAttachment(0, 45);
		fd_btn_sign.right = new FormAttachment(85, 5);
		fd_btn_sign.top = new FormAttachment(0);
		fd_btn_sign.left = new FormAttachment(60, -5);
		this.btn_sign.setLayoutData(fd_btn_sign);
		this.btn_sign.setText("Sign ...");
		this.buttonMap.put(Buttons.SIGN, this.btn_sign);

		MainBarEndButton end = new MainBarEndButton(composite, SWT.NONE);
		FormData fd_btn_end = new FormData();
		fd_btn_end.bottom = new FormAttachment(0, 45);
		fd_btn_end.right = new FormAttachment(100, -2);
		fd_btn_end.top = new FormAttachment(0);
		fd_btn_end.left = new FormAttachment(85, -5);
		end.setLayoutData(fd_btn_end);
		end.setText("Done");
		this.buttonMap.put(Buttons.FINAL, end);
		
		this.container = new Composite(getShell(), SWT.RESIZE);
		FormData fd_composite_1 = new FormData();
		fd_composite_1.bottom = new FormAttachment(100, -25);
		fd_composite_1.right = new FormAttachment(100, -5);
		fd_composite_1.top = new FormAttachment(0, 60);
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

		log.debug("Updating MainWindow state for : " //$NON-NLS-1$
				+ this.stateMachine.getStatus().getCurrentState().toString());

		for (Buttons button : Buttons.values()) {
			boolean active = behavior.getActive(button);
			boolean enabled = behavior.getEnabled(button);

			MainBarButton theButton = this.buttonMap.get(button);
			if (theButton != null) {
				theButton.setEnabled(enabled);
				theButton.setActive(active);
			}
		}

		if(behavior.getMainBarVisible()) {
			this.mainBarFormData.bottom = new FormAttachment(0, 60);
		} else {
			this.mainBarFormData.bottom = new FormAttachment(0, 0);
		}
	}

	/**
	 * @return the shell
	 */
	public Shell getShell() {
		return this.shell;
	}
}
