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
package at.asit.pdfover.gui.composites.mobilebku;


// Imports
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import at.asit.pdfover.gui.composites.StateComposite;
import at.asit.pdfover.gui.utils.SWTUtils;
import at.asit.pdfover.gui.workflow.states.State;
import org.eclipse.swt.widgets.Button;

/**
 *
 */
public class WaitingForAppComposite extends StateComposite {
	private Label lbl_description;
	private Button btn_sms;
	private Button btn_cancel;
	private Button btn_fido2;
	private boolean userSMSClicked = false;
	private boolean userCancelClicked = false;
	private boolean userFIDO2Clicked = false;
	private boolean pollingDone = false;

	/**
	 * @return the isDone
	 */
	public Boolean getIsDone() {
		return this.pollingDone;
	}

	public void signalPollingDone() { this.pollingDone = true; getDisplay().wake(); }
	public boolean isDone() { return (this.userCancelClicked || this.userSMSClicked || this.userFIDO2Clicked || this.pollingDone); }
	public boolean wasCancelClicked() { return this.userCancelClicked; }
	public boolean wasSMSClicked() { return this.userSMSClicked; }
	public boolean wasFIDO2Clicked() { return this.userFIDO2Clicked; }
	public void reset() { this.userCancelClicked = this.userSMSClicked = this.userFIDO2Clicked = this.pollingDone = false; }

	public void setSMSEnabled(boolean state) {
		this.btn_sms.setEnabled(state);
	}

	public void setFIDO2Enabled(boolean state) {
		this.btn_fido2.setEnabled(state);
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
		SWTUtils.anchor(lbl_description).bottom(50, -1).left(0, 10).right(100, -10);
		this.lbl_description.setAlignment(SWT.CENTER);
		SWTUtils.setLocalizedText(lbl_description, "waiting_for_app.message");

		ProgressBar progressBar = new ProgressBar(this, SWT.HORIZONTAL | SWT.INDETERMINATE);
		SWTUtils.anchor(progressBar).top(50, 10).bottom(50, 40).left(50,-100).right(50,100);

		this.btn_sms = new Button(this, SWT.NONE);
		SWTUtils.anchor(btn_sms).top(progressBar, 24).right(progressBar, 0, SWT.RIGHT);
		SWTUtils.addSelectionListener(btn_sms, (e) -> { this.userSMSClicked = true; });

		this.btn_cancel = new Button(this, SWT.NONE);
		SWTUtils.anchor(btn_cancel).top(btn_sms, 0, SWT.TOP).right(btn_sms, -6);
		SWTUtils.addSelectionListener(btn_cancel, (e) -> { this.userCancelClicked = true; });

		this.btn_fido2 = new Button(this, SWT.NONE);
		SWTUtils.anchor(btn_fido2).top(btn_cancel, 0, SWT.TOP).right(btn_cancel, -6);
		SWTUtils.addSelectionListener(btn_fido2, (e) -> { this.userFIDO2Clicked = true; });

		reloadResources();

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
		SWTUtils.setLocalizedText(btn_fido2, "tanEnter.FIDO2");
		SWTUtils.setLocalizedText(btn_cancel, "common.Cancel");
	}
}
