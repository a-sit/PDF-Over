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
import javax.swing.GroupLayout;

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
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

/**
 * The Main Window of PDFOver 4.0
 */
public class MainWindow {

	/**
	 * SFL4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
	private Shell shell;
	private CLabel lbl_status;
	private Composite container;
	private StackLayout stack;

	/**
	 * Set current status (may be removed in production release)
	 * @param value
	 */
	public void setStatus(String value) {
		this.lbl_status.setText("[DEBUG]: Current workflow state: " + value);
	}

	/**
	 * Sets top level composite for stack layout
	 * @param ctrl
	 */
	public void setTopControl(Control ctrl) {
		log.debug("Top control: " + ctrl.toString());
		this.stack.topControl = ctrl;
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
		
		MainWindow window = new MainWindow();
		
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
		
		Button btn_config = new Button(composite, SWT.NONE);
		FormData fd_config = new FormData();
		fd_config.left = new FormAttachment(0, 0);
		fd_config.right = new FormAttachment(25, 0);
		fd_config.top = new FormAttachment(0, 0);
		fd_config.bottom = new FormAttachment(100, 0);
		btn_config.setLayoutData(fd_config);
		btn_config.setText("Config ...");
		
		Button btn_open = new Button(composite, SWT.NONE);
		FormData fd_open = new FormData();
		fd_open.left = new FormAttachment(25, 0);
		fd_open.right = new FormAttachment(50, 0);
		fd_open.top = new FormAttachment(0, 0);
		fd_open.bottom = new FormAttachment(100, 0);
		btn_open.setLayoutData(fd_open);
		btn_open.setText("Open ...");
		
		Button btn_position = new Button(composite, SWT.NONE);
		FormData fd_position = new FormData();
		fd_position.left = new FormAttachment(50, 0);
		fd_position.right = new FormAttachment(75, 0);
		fd_position.top = new FormAttachment(0, 0);
		fd_position.bottom = new FormAttachment(100, 0);
		btn_position.setLayoutData(fd_position);
		btn_position.setText("Positon ...");
		
		Button btn_sign = new Button(composite, SWT.NONE);
		FormData fd_sign = new FormData();
		fd_sign.left = new FormAttachment(75, 0);
		fd_sign.right = new FormAttachment(100, 0);
		fd_sign.top = new FormAttachment(0, 0);
		fd_sign.bottom = new FormAttachment(100, 0);
		btn_sign.setLayoutData(fd_sign);
		btn_sign.setText("Sign ...");
		
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
	 * @return the shell
	 */
	public Shell getShell() {
		return this.shell;
	}
}
