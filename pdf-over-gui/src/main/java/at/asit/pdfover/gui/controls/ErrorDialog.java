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
package at.asit.pdfover.gui.controls;

// Imports
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class ErrorDialog extends Dialog {
	/**
	 * @param parent
	 * @param style
	 * @param message
	 * @param exception
	 */
	public ErrorDialog(Shell parent, int style, String message,
			Throwable exception) {
		super(parent, style);
		this.message = message;

		final StringBuilder result = new StringBuilder();
		result.append(exception.getLocalizedMessage());
		final String NEW_LINE = System.getProperty("line.separator"); //$NON-NLS-1$
		result.append(NEW_LINE);
		result.append(NEW_LINE);
		result.append(NEW_LINE);

		// add each element of the stack trace
		for (StackTraceElement element : exception.getStackTrace()) {
			result.append(element);
			result.append(NEW_LINE);
		}
		this.details = result.toString();
	}

	/**
	 * @param parent
	 * @param style
	 * @param message
	 * @param details
	 */
	public ErrorDialog(Shell parent, int style, String message, String details) {
		super(parent, style);
		this.message = message;
		this.details = details;
	}

	private String message = null;

	private String details = null;

	/**
	 * SLF4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(ErrorDialog.class);

	/**
	 * Open error dialog
	 */
	public void open() {
		Shell parent = getParent();
		final Shell shell = new Shell(parent, SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL);
		shell.setText(getText());
		shell.setLayout(new FormLayout());

		Label lblErrorImage = new Label(shell, SWT.NONE);
		FormData fd_lblErrorImage = new FormData();
		fd_lblErrorImage.top = new FormAttachment(50, -16);
		fd_lblErrorImage.left = new FormAttachment(0, 5);
		lblErrorImage.setLayoutData(fd_lblErrorImage);
		lblErrorImage.setText(""); //$NON-NLS-1$

		String imgPath = "/img/error.png"; //$NON-NLS-1$

		InputStream stream = this.getClass().getResourceAsStream(imgPath);

		Image orig = new Image(Display.getCurrent(),
				new ImageData(stream).scaledTo(32, 32));

		lblErrorImage.setImage(orig);

		Label lblerrorMessage = new Label(shell, SWT.NONE);
		FormData fd_lblerrorMessage = new FormData();
		fd_lblerrorMessage.top = new FormAttachment(0, 5);
		fd_lblerrorMessage.left = new FormAttachment(0, 42);
		lblerrorMessage.setLayoutData(fd_lblerrorMessage);
		lblerrorMessage.setText(this.message);

		Group group = new Group(shell, SWT.NONE);
		group.setLayout(new FormLayout());
		FormData fd_group = new FormData();

		fd_group.right = new FormAttachment(100, -5);
		fd_group.top = new FormAttachment(lblerrorMessage, 5);
		fd_group.left = new FormAttachment(lblErrorImage, 5);
		group.setLayoutData(fd_group);
		group.setText("Details");
		Button btnOk = new Button(shell, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});
		fd_group.bottom = new FormAttachment(btnOk, -5);

		ScrolledComposite scrolledComposite = new ScrolledComposite(group,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		FormData fd_scrolledComposite = new FormData();
		fd_scrolledComposite.top = new FormAttachment(0, 5);
		fd_scrolledComposite.left = new FormAttachment(0, 5);
		fd_scrolledComposite.bottom = new FormAttachment(100, -5);
		fd_scrolledComposite.right = new FormAttachment(100, -5);
		scrolledComposite.setLayoutData(fd_scrolledComposite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Label lblDetails = new Label(scrolledComposite, SWT.NONE);

		lblDetails.setText(this.details);

		scrolledComposite.setContent(lblDetails);
		scrolledComposite.setMinSize(lblDetails.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));
		FormData fd_btnOk = new FormData();
		fd_btnOk.bottom = new FormAttachment(100, -5);
		fd_btnOk.right = new FormAttachment(100, -5);
		btnOk.setLayoutData(fd_btnOk);
		btnOk.setText("Ok");
		
		shell.pack();
		shell.open();
		shell.pack();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
}
