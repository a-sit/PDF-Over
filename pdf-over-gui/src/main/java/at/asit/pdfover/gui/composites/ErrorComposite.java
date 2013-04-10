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
package at.asit.pdfover.gui.composites;

// Imports
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import at.asit.pdfover.gui.workflow.states.State;

/**
 * 
 */
public class ErrorComposite extends StateComposite {
	/**
	 * 
	 */
	private final class OkSelectionListener extends SelectionAdapter {
		/**
		 * Empty constructor
		 */
		public OkSelectionListener() {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			ErrorComposite.this.userOk = true;
			ErrorComposite.this.shouldTryToRecover = false;
			ErrorComposite.this.state.updateStateMachine();
		}
	}
	
	/**
	 * 
	 */
	private final class RetrySelectionListener extends SelectionAdapter {
		/**
		 * Empty constructor
		 */
		public RetrySelectionListener() {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			ErrorComposite.this.userOk = true;
			ErrorComposite.this.shouldTryToRecover = true;
			ErrorComposite.this.state.updateStateMachine();
		}
	}

	boolean userOk = false;
	
	boolean canTryToRecover = false;
	
	boolean shouldTryToRecover = false;
	
	/**
	 * Checks if we should try to recover form the error
	 * @return the shouldTryToRecover
	 */
	public boolean getShouldTryToRecover() {
		return this.shouldTryToRecover;
	}

	/**
	 * Gets try to recover
	 * @return can try to recover
	 */
	public boolean getCanTryToRecover() {
		return this.canTryToRecover;
	}
	
	/**
	 * Sets try to recover
	 * @param value
	 */
	public void setCanTryToRecover(boolean value) {
		this.canTryToRecover = value;
		
		if(this.canTryToRecover) {
			this.btn_ok.setVisible(false);
			this.btn_retry.setVisible(true);
			this.btn_cancel.setVisible(true);
			this.lbl_title.setText("Recoverable error ocurred");
		} else {
			this.btn_ok.setVisible(true);
			this.btn_retry.setVisible(false);
			this.btn_cancel.setVisible(false);
			this.lbl_title.setText("Fatal error ocurred");
		}
	}
	
	/**
	 * Checks if the user has clicked OK
	 * @return whether the user has clicked OK
	 */
	public boolean isUserOk() {
		return this.userOk;
	}

	private Exception exception;
	private Label lbl_message;

	private Button btn_ok;

	private Button btn_retry;

	private Button btn_cancel;

	private Label lbl_title;
	
	
	/**
	 * Sets the Exception to present
	 * @param exception the exception
	 */
	public void setException(Exception exception) {
		this.exception = exception;
		this.lbl_message.setText(this.exception.getMessage());
	}

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 * @param state 
	 */
	public ErrorComposite(Composite parent, int style, State state) {
		super(parent, style, state);

		this.setLayout(new FormLayout());

		this.lbl_message = new Label(this, SWT.WRAP | SWT.NATIVE | SWT.RESIZE);
		FormData fd_lbl_message = new FormData();
		fd_lbl_message.left = new FormAttachment(10, 0);
		fd_lbl_message.right = new FormAttachment(90, 0);
		fd_lbl_message.top = new FormAttachment(15, 5);
		fd_lbl_message.bottom = new FormAttachment(80, 0);
		this.lbl_message.setLayoutData(fd_lbl_message);
		//lbl_message.setText(this.exception.getMessage());
		
		this.btn_ok = new Button(this, SWT.NATIVE | SWT.RESIZE);
		this.btn_ok.setText("OK");
		// Point mobile_size = btn_mobile.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		FormData fd_btn_ok = new FormData();
		fd_btn_ok.left = new FormAttachment(45, 0);
		fd_btn_ok.right = new FormAttachment(55, 0);
		fd_btn_ok.top = new FormAttachment(85, 0);
		fd_btn_ok.bottom = new FormAttachment(95, 0);
		this.btn_ok.setLayoutData(fd_btn_ok);
		this.btn_ok.addSelectionListener(new OkSelectionListener());
		
		this.btn_retry = new Button(this, SWT.NATIVE | SWT.RESIZE);
		this.btn_retry.setText("OK");
		// Point mobile_size = btn_mobile.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		FormData fd_btn_retry = new FormData();
		fd_btn_retry.left = new FormAttachment(30, 0);
		fd_btn_retry.right = new FormAttachment(50, -5);
		fd_btn_retry.top = new FormAttachment(85, 0);
		fd_btn_retry.bottom = new FormAttachment(95, 0);
		this.btn_retry.setLayoutData(fd_btn_retry);
		this.btn_retry.addSelectionListener(new RetrySelectionListener());
		this.btn_retry.setVisible(false);
		
		this.btn_cancel = new Button(this, SWT.NATIVE | SWT.RESIZE);
		this.btn_cancel.setText("Cancel");
		// Point mobile_size = btn_mobile.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		FormData fd_btn_cancel = new FormData();
		fd_btn_cancel.left = new FormAttachment(50, 5);
		fd_btn_cancel.right = new FormAttachment(80, 0);
		fd_btn_cancel.top = new FormAttachment(85, 0);
		fd_btn_cancel.bottom = new FormAttachment(95, 0);
		this.btn_cancel.setLayoutData(fd_btn_cancel);
		this.btn_cancel.addSelectionListener(new OkSelectionListener());
		this.btn_cancel.setVisible(false);
		
		this.lbl_title = new Label(this, SWT.NONE);
		FormData fd_lbl_title = new FormData();
		fd_lbl_title.left = new FormAttachment(10, 0);
		fd_lbl_title.right = new FormAttachment(90, 0);
		fd_lbl_title.top = new FormAttachment(0, 5);
		fd_lbl_title.bottom = new FormAttachment(15, -5);
		this.lbl_title.setLayoutData(fd_lbl_title);
		this.lbl_title.setText(""); //$NON-NLS-1$
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		// Nothing to do
	}
}
