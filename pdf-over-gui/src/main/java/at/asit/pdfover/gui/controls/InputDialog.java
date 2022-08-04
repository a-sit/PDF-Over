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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import at.asit.pdfover.gui.utils.SWTUtils;

/**
 *
 */
public class InputDialog extends org.eclipse.swt.widgets.Dialog {
	/**
	 * SLF4J Logger instance
	 **/
//	private static final Logger log = LoggerFactory
//			.getLogger(InputDialog.class);

	private String prompt;
	String input;

	/**
	 * SWT flags of the input box
	 */
	protected static int TEXT_FLAGS = SWT.BORDER;

	/**
	 * Create a password input dialog
	 * @param parent parent
	 * @param title title
	 * @param prompt prompt message
	 */
	public InputDialog(Shell parent, String title, String prompt) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		setText(title);
		setMessage(prompt);
	}

	/**
	 * Get the prompt message
	 * @return the prompt message
	 */
	public String getPrompt() {
		return this.prompt;
	}

	/**
	 * set the prompt message
	 * @param message the prompt message
	 */
	public void setMessage(String message) {
		this.prompt = message;
	}

	/**
	 * Get the input
	 * @return the input
	 */
	public String getInput() {
		return this.input;
	}

	/**
	 * Get the input
	 * @param input the input
	 */
	public void setInput(String input) {
		this.input = input;
	}

	/**
	 * Open the dialog
	 * @return the input
	 */
	public String open() {
		Shell parent = getParent();
		Shell shell = new Shell(parent, getStyle());
		shell.setText(getText());
		createContents(shell);
		shell.pack();
		shell.open();
		Display display = parent.getDisplay();
		Rectangle bounds = parent.getBounds();
		Rectangle main = shell.getBounds();
		shell.setLocation(
				bounds.x + (bounds.width - main.width) / 2,
				bounds.y + (bounds.height - main.height) / 2);
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return getInput();
	}

	private void createContents(final Shell shell) {
		GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 10;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.marginTop = 5;
		shell.setLayout(layout);
		Label label = new Label(shell, SWT.NONE);
		label.setText(this.prompt);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		final Text text = new Text(shell, TEXT_FLAGS);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		text.setLayoutData(data);
		Button ok = new Button(shell, SWT.PUSH);
		SWTUtils.setLocalizedText(ok, "common.Ok");
		data = new GridData(GridData.FILL_HORIZONTAL);
		ok.setLayoutData(data);
		ok.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				InputDialog.this.input = text.getText();
				shell.close();
			}
		});
		Button cancel = new Button(shell, SWT.PUSH);
		SWTUtils.setLocalizedText(cancel, "common.Cancel");
		data = new GridData(GridData.FILL_HORIZONTAL);
		cancel.setLayoutData(data);
		cancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				InputDialog.this.input = null;
				shell.close();
			}
		});
		shell.setDefaultButton(ok);
	}
}
