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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
// Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Messages;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Label;


/**
 *
 */
public class PlaceholderSelectionGui extends Dialog {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(PlaceholderSelectionGui.class);
	protected Object result;
	protected Shell shlInfo;
	protected Combo placeholderNameDropDown;
	protected String lblString;
	protected List<String> placeholderList;
	private Button btnCancel;
	private Button btnOk;
	protected int returnValue;


	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 * @param text
	 * @param lblString
	 * @param placeholderList
	 */
	public PlaceholderSelectionGui(Shell parent, int style, String text, String lblString, List<String> placeholderList) {
		super(parent, style);
		setText(text);
		this.lblString = lblString;
		this.placeholderList = placeholderList;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */

	public int open() {
		createContents();
		this.shlInfo.open();
		this.shlInfo.layout();
		Display display = getParent().getDisplay();
		while (!this.shlInfo.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return this.returnValue;
	}

	/**
	 * @return
	 */
	public int getUserSelection() {
		return this.returnValue;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		this.shlInfo = new Shell(getParent(), getStyle());
		this.shlInfo.setSize(290, 195);
		this.shlInfo.setText("Info"); //
		this.shlInfo.setLayout(null);

		this.placeholderNameDropDown = new Combo(this.shlInfo, SWT.NONE);
		this.placeholderNameDropDown.setBounds(27, 77, 223, 23);
		addDropDownEntries(this.placeholderList);
		this.placeholderNameDropDown.select(0);

		this.btnCancel = new Button(this.shlInfo, SWT.NONE);
		this.btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {


				PlaceholderSelectionGui.this.returnValue = -1;
				PlaceholderSelectionGui.this.shlInfo.dispose();

			}
		});
		this.btnCancel.setBounds(175, 118, 75, 25);
		this.btnCancel.setText("Cancel"); //

		this.btnOk = new Button(this.shlInfo, SWT.NONE);
		this.btnOk.setBounds(94, 118, 75, 25);
		this.btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				PlaceholderSelectionGui.this.returnValue = PlaceholderSelectionGui.this.placeholderNameDropDown.getSelectionIndex();
				PlaceholderSelectionGui.this.shlInfo.dispose();
			}
		});
		this.btnOk.setText("Ok"); //

		Label lbLabel = new Label(this.shlInfo, SWT.WRAP);
		lbLabel.setBounds(27, 10, 223, 41);
		lbLabel.setText(Messages.getString("positioning.placeholder")); //

	}

	/**
	 * @param list
	 * @param Filling up the placeholder drop down list
	 */
	public void addDropDownEntries(List<String> list) {
		for (String name : list) {
			this.placeholderNameDropDown.add(name);
		}
	}

	/**
	 *
	 */
	protected void close() {
		this.close();
	}

	@Override
	protected void checkSubclass() {
	    //  allow subclass
	}
}
