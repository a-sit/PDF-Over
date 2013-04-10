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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 */
public class GUI {
	/**
	 * Main window
	 */
	protected Shell shell;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			GUI window = new GUI();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		this.shell.open();
		this.shell.layout();
		while (!this.shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		this.shell = new Shell();
		this.shell.setSize(640, 480);
		this.shell.setText("PDF-Over 4.0");
		
		final Composite compOpenFile = new Composite(this.shell, SWT.NONE);
		compOpenFile.setBounds(0, 0, 640, 480);

		final Composite compPosition = new Composite(this.shell, SWT.NONE);
		compPosition.setBounds(0, 0, 640, 480);


		Label lblPage1 = new Label(compOpenFile, SWT.NONE);
		lblPage1.setBounds(20, 20, 70, 17);
		lblPage1.setText("Open file");

		Button btnNext = new Button(compOpenFile, SWT.NONE);
		btnNext.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				compPosition.moveAbove(compOpenFile);
			}
		});
		btnNext.setBounds(20, 50, 91, 29);
		btnNext.setText("Next");

		Label lblPage2 = new Label(compPosition, SWT.NONE);
		lblPage2.setBounds(20, 20, 140, 17);
		lblPage2.setText("Position signature");


	}
}
