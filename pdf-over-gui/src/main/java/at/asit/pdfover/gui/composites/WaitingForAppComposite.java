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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import at.asit.pdfover.gui.utils.SWTUtils;
import at.asit.pdfover.gui.workflow.states.State;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

/**
 *
 */
public class WaitingForAppComposite extends StateComposite {
	private Label lbl_description;
	private Button btn_sms;
	private Button btn_cancel;
	private Boolean isUserSMS = false;
	private Boolean userCancel = false;
	private Boolean isDone = false;

	/**
	 * @return the isDone
	 */
	public Boolean getIsDone() {
		return this.isDone;
	}

	/**
	 * @param isDone the isDone to set
	 */
	public void setIsDone(Boolean isDone) {
		this.isDone = isDone;
	}

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 * @param state
	 */
	public WaitingForAppComposite(Composite parent, int style, State state) {
		super(parent, style, state);
		setLayout(new FormLayout());

		this.lbl_description = new Label(this, SWT.NATIVE);
		FormData fd_lbl_description = new FormData();
		fd_lbl_description.bottom = new FormAttachment(50, -10);
		fd_lbl_description.left = new FormAttachment(0, +10);
		fd_lbl_description.right = new FormAttachment(100, -10);
		this.lbl_description.setLayoutData(fd_lbl_description);
		this.lbl_description.setAlignment(SWT.CENTER);
		SWTUtils.setLocalizedText(lbl_description, "waiting_for_app.message");

		ProgressBar progressBar = new ProgressBar(this, SWT.HORIZONTAL | SWT.INDETERMINATE);
		FormData fd_progressBar = new FormData();
		fd_progressBar.top = new FormAttachment(50, +10);
		fd_progressBar.bottom = new FormAttachment(50, +40);
		fd_progressBar.left = new FormAttachment(50, -100);
		fd_progressBar.right = new FormAttachment(50, +100);
		progressBar.setLayoutData(fd_progressBar);

		this.btn_sms = new Button(this, SWT.NONE);
		this.btn_sms.addSelectionListener(new SMSSelectionListener());

		FormData fd_btnSMS = new FormData();
		fd_btnSMS.top = new FormAttachment(progressBar, 24);
		fd_btnSMS.right = new FormAttachment(progressBar, 0, SWT.RIGHT);
		this.btn_sms.setLayoutData(fd_btnSMS);
		SWTUtils.setLocalizedText(btn_sms, "SMS tan");

		this.btn_cancel = new Button(this, SWT.NONE);
		this.btn_cancel.addSelectionListener(new CancelSelectionListener());
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.top = new FormAttachment(btn_sms, 0, SWT.TOP);
		fd_btnCancel.right = new FormAttachment(btn_sms, -6);
		this.btn_cancel.setLayoutData(fd_btnCancel);
		SWTUtils.setLocalizedText(btn_cancel, "WaitingForAppComposite.btnCancel.text");

		reloadResources();

	}

	/**
	 *
	 */
	private final class CancelSelectionListener extends SelectionAdapter {
		/**
		 * Empty constructor
		 */
		public CancelSelectionListener() {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			WaitingForAppComposite.this.setUserCancel(true);
			WaitingForAppComposite.this.btn_cancel.setEnabled(false);
			WaitingForAppComposite.this.btn_sms.setEnabled(false);
		}
	}

	private final class SMSSelectionListener extends SelectionAdapter {
		/**
		 * Empty constructor
		 */
		public SMSSelectionListener() {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if(!WaitingForAppComposite.this.btn_sms.getEnabled()) {
				return;
			}

			WaitingForAppComposite.this.setUserSMS(true);
			WaitingForAppComposite.this.btn_sms.setEnabled(false);
			WaitingForAppComposite.this.btn_cancel.setEnabled(false);
		}
	}

	public void setUserSMS(boolean b) {
		this.isUserSMS = b;
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
		// Nothing to do here
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		SWTUtils.setLocalizedText(lbl_description, "waiting_for_app.message");
		SWTUtils.setLocalizedText(btn_sms, "tanEnter.SMS");
		SWTUtils.setLocalizedText(btn_cancel, "common.Cancel");
		SWTUtils.setLocalizedText(btn_sms, "tanEnter.SMS");
	}

	/**
	 * @return
	 */
	public boolean getUserCancel() {
		return this.userCancel;
	}

	/**
	 * @param b
	 */
	public void setUserCancel(boolean b) {
		this.userCancel = b;

	}

	/**
	 * @return
	 */
	public boolean getUserSMS() {
		return this.isUserSMS;
	}
}
