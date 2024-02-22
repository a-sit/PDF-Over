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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.bku.mobile.MobileBKUValidator;
import at.asit.pdfover.gui.composites.StateComposite;
import at.asit.pdfover.gui.exceptions.InvalidPasswordException;
import at.asit.pdfover.gui.utils.SWTUtils;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.states.State;
import lombok.extern.slf4j.Slf4j;

/**
 * Composite for entering the phone number for the mobile BKU
 */
@Slf4j
public class MobileBKUEnterNumberComposite extends StateComposite {

	/**
	 *
	 */
	private final SelectionListener okListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (!MobileBKUEnterNumberComposite.this.btn_ok.isEnabled()) {
				return;
			}

			try {
				String number = MobileBKUEnterNumberComposite.this.txt_number.getText();

				MobileBKUEnterNumberComposite.this.setMobileNumber(number);

				String password = MobileBKUEnterNumberComposite.this.txt_password.getText();

				MobileBKUValidator.validatePassword(password);

				MobileBKUEnterNumberComposite.this.mobilePassword = password;
				MobileBKUEnterNumberComposite.this.userAck = true;

				MobileBKUEnterNumberComposite.this.btn_ok.setEnabled(false);

			} catch(InvalidPasswordException ex) {
				log.info("Validating input for Mobile BKU failed!", ex);
				MobileBKUEnterNumberComposite.this.setErrorMessage(ex.getMessage());
				MobileBKUEnterNumberComposite.this.txt_password.setFocus();
			} catch (Exception ex) {
				log.info("Validating input for Mobile BKU failed!", ex);
				MobileBKUEnterNumberComposite.this.setErrorMessage(Messages.getString("error.InvalidPhoneNumber"));
				MobileBKUEnterNumberComposite.this.txt_number.setFocus();
				return;
			}
		}
	};

	String mobileNumber;

	String mobilePassword;

	Text txt_number;

	Text txt_password;

	String errorMessage = null;

	public boolean userAck = false;
	public boolean userCancel = false;


	private Label lbl_error;
	private Label lbl_password;
	private Label lbl_number;

	Button btn_ok;
	Button btn_cancel;
	Button btn_remember;

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}

	/**
	 * @param errorMessage
	 *            the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;

		if (this.errorMessage != null) {
			this.lbl_error.setText(this.errorMessage);
		} else {
			this.lbl_error.setText("");
		}
	}

	public boolean isRememberPassword() { return this.btn_remember.getSelection(); }
	public void setRememberPassword(boolean state) { this.btn_remember.setSelection(state); }

	/**
	 * Create the composite.
	 *
	 * @param parent
	 * @param style
	 * @param state
	 */
	public MobileBKUEnterNumberComposite(Composite parent, int style, State state) {
		super(parent, style, state);
		setLayout(new FormLayout());

		final Composite containerComposite = new Composite(this, SWT.NATIVE);
		containerComposite.addPaintListener(e -> {
			Rectangle clientArea = containerComposite.getClientArea();

			//e.gc.setForeground();
			e.gc.setForeground(Constants.MAINBAR_ACTIVE_BACK_DARK);
			e.gc.setLineWidth(3);
			e.gc.setLineStyle(SWT.LINE_SOLID);
			e.gc.drawRoundRectangle(clientArea.x,
					clientArea.y, clientArea.width - 2, clientArea.height - 2,
					10, 10);
		});
		containerComposite.setLayout(new FormLayout());
		SWTUtils.anchor(containerComposite).top(50, -120).bottom(50, 120).left(50, -200).right(50, 200);

		this.lbl_number = new Label(containerComposite, SWT.NATIVE);
		this.lbl_number.setAlignment(SWT.RIGHT);
		SWTUtils.anchor(lbl_number).bottom(50, -10).right(50, -10);

		this.txt_number = new Text(containerComposite, SWT.SINGLE | SWT.NATIVE | SWT.BORDER);
		SWTUtils.anchor(txt_number).bottom(50, -10).left(50, 10).right(100, -20);
		this.txt_number.setEditable(true);
		SWTUtils.addFocusGainedListener(txt_number, () -> { txt_number.selectAll(); });

		ImageData mobileIconData = new ImageData(this.getClass().getResourceAsStream(Constants.RES_IMG_MOBILE)).scaledTo(90, 90);
		Image mobileIcon = new Image(getDisplay(), mobileIconData);

		Label lbl_image = new Label(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(lbl_image).top(20, -1 * (mobileIconData.width / 2)).bottom(20, mobileIconData.width / 2).left(0, 10).width(mobileIconData.width);
		lbl_image.setImage(mobileIcon);

		this.lbl_password = new Label(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(lbl_password).top(50, 10).right(50, -10);
		this.lbl_password.setAlignment(SWT.RIGHT);
		
		this.txt_password = new Text(containerComposite, SWT.SINGLE | SWT.PASSWORD | SWT.BORDER | SWT.NATIVE);
		SWTUtils.anchor(txt_password).top(50, 10).left(50, 10).right(100, -20);
		this.txt_password.setEditable(true);
		SWTUtils.addFocusGainedListener(txt_password, () -> { txt_password.selectAll(); });

		this.btn_ok = new Button(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(btn_ok).bottom(100, -20).right(100, -20);
		this.btn_ok.addSelectionListener(this.okListener);

		this.btn_cancel = new Button(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(btn_cancel).bottom(100, -20).right(btn_ok, -10);
		SWTUtils.addSelectionListener(btn_cancel, () -> { this.userCancel = true; });

		this.lbl_error = new Label(containerComposite, SWT.WRAP | SWT.NATIVE );
		SWTUtils.anchor(lbl_error).bottom(103, -20).left(5, 0).right(btn_cancel, -10);

		this.btn_remember = new Button(containerComposite, SWT.CHECK);
		SWTUtils.anchor(btn_remember).right(100, -10).top(0, 5);
	}

	@Override public void onDisplay() {
		getShell().setDefaultButton(this.btn_ok);
		if (this.txt_number.getText().isEmpty()) {
			this.txt_number.setFocus();
		} else {
			this.txt_password.setFocus();
		}
	}

	/**
	 * enables submit button
	 */
	public void enableButton() {
		this.btn_ok.setEnabled(true);
	}

	/**
	 * @return the mobileNumber
	 */
	public String getMobileNumber() {
		return this.mobileNumber;
	}

	/**
	 * @param mobileNumber
	 *            the mobileNumber to set
	 */
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;

		if (this.mobileNumber != null && !this.mobileNumber.isEmpty()) {
			this.txt_number.setText(this.mobileNumber);
		} else {
			this.txt_number.setText("");
		}
	}

	/**
	 * @return the mobilePassword
	 */
	public String getMobilePassword() {
		return this.mobilePassword;
	}



	/**
	 * @param mobilePassword
	 *            the mobilePassword to set
	 */
	public void setMobilePassword(String mobilePassword) {
		this.mobilePassword = mobilePassword;

		if (this.mobilePassword != null) {
			this.txt_password.setText(this.mobilePassword);
		} else {
			this.txt_password.setText("");
		}
	}

	/** (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		SWTUtils.setLocalizedText(lbl_number, "mobileBKU.number");
		SWTUtils.setLocalizedText(lbl_password, "mobileBKU.password");
		SWTUtils.setLocalizedText(btn_remember, "mobileBKU.rememberPassword");
		SWTUtils.setLocalizedText(btn_ok, "common.Ok");
		SWTUtils.setLocalizedText(btn_cancel, "common.Cancel");

		SWTUtils.setLocalizedToolTipText(btn_remember, "mobileBKU.rememberPasswordNote");
	}

}
