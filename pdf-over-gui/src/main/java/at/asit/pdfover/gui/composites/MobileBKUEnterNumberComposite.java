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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.bku.mobile.MobileBKUHelper;
import at.asit.pdfover.gui.exceptions.InvalidPasswordException;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.states.State;

/**
 * Composite for entering the phone number for the mobile BKU
 */
public class MobileBKUEnterNumberComposite extends StateComposite {
	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(MobileBKUEnterNumberComposite.class);

	/**
	 *
	 */
	private final SelectionListener okListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if(!MobileBKUEnterNumberComposite.this.btn_ok.isEnabled()) {
				return;
			}

			try {
				String number = MobileBKUEnterNumberComposite.this.txt_number
						.getText();

				number = number.replaceAll("\\s","");

				MobileBKUEnterNumberComposite.this.setMobileNumber(number);

				MobileBKUEnterNumberComposite.this.mobileNumber = number;

				String password = MobileBKUEnterNumberComposite.this.txt_password
						.getText();

				MobileBKUHelper.validatePassword(password);

				MobileBKUEnterNumberComposite.this.mobilePassword = password;
				MobileBKUEnterNumberComposite.this.userAck = true;

				MobileBKUEnterNumberComposite.this.btn_ok.setEnabled(false);

			} catch(InvalidPasswordException ex) {
				log.error("Validating input for Mobile BKU failed!", ex);
				MobileBKUEnterNumberComposite.this
				.setErrorMessage(ex.getMessage());
				MobileBKUEnterNumberComposite.this.txt_password.setFocus();
			}
			catch (Exception ex) {
				log.error("Validating input for Mobile BKU failed!", ex);
				MobileBKUEnterNumberComposite.this
						.setErrorMessage(Messages.getString("error.InvalidPhoneNumber"));
				MobileBKUEnterNumberComposite.this.txt_number.setFocus();
				return;
			}
		}
	};

	/**
	 *
	 */
	private final SelectionListener cancelListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			MobileBKUEnterNumberComposite.this.userCancel = true;
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

	/**
	 * Create the composite.
	 *
	 * @param parent
	 * @param style
	 * @param state
	 */
	public MobileBKUEnterNumberComposite(Composite parent, int style,
			State state) {
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
		StateComposite.anchor(containerComposite).top(50, -120).bottom(50, 120).left(50, -200).right(50, 200).set();

		this.txt_number = new Text(containerComposite, SWT.SINGLE | SWT.NATIVE | SWT.BORDER);
		StateComposite.anchor(txt_number).bottom(50, -10).left(50, 10).right(100, -20).set();
		this.txt_number.setEditable(true);

		this.lbl_number = new Label(containerComposite, SWT.NATIVE);
		this.lbl_number.setAlignment(SWT.RIGHT);
		this.lbl_number.setText(Messages.getString("mobileBKU.number"));
		StateComposite.anchor(lbl_number).bottom(50, -10).right(50, -10).set();

		ImageData mobileIconData = new ImageData(this.getClass().getResourceAsStream(Constants.RES_IMG_MOBILE));
		Image mobileIcon = new Image(getDisplay(), mobileIconData);

		Label lbl_image = new Label(containerComposite, SWT.NATIVE);
		StateComposite.anchor(lbl_image).top(20, -1 * (mobileIconData.width / 2)).bottom(20, mobileIconData.width / 2).left(0, 10).width(mobileIconData.width).set();
		lbl_image.setImage(mobileIcon);

		this.txt_password = new Text(containerComposite, SWT.SINGLE | SWT.PASSWORD | SWT.BORDER | SWT.NATIVE);
		StateComposite.anchor(txt_password).top(50, 10).left(50, 10).right(100, -20).set();
		this.txt_password.setEditable(true);

		this.lbl_password = new Label(containerComposite, SWT.NATIVE);
		StateComposite.anchor(lbl_password).top(50, 10).right(50, -10).set();
		this.lbl_password.setAlignment(SWT.RIGHT);
		this.lbl_password.setText(Messages.getString("mobileBKU.password"));

		this.btn_ok = new Button(containerComposite, SWT.NATIVE);
		StateComposite.anchor(btn_ok).bottom(100, -20).right(100, -20).set();
		this.btn_ok.setText(Messages.getString("common.Ok"));
		this.btn_ok.addSelectionListener(this.okListener);

		this.btn_cancel = new Button(containerComposite, SWT.NATIVE);
		StateComposite.anchor(btn_cancel).bottom(100, -20).right(btn_ok, -10).set();
		this.btn_cancel.setText(Messages.getString("common.Cancel"));
		this.btn_cancel.addSelectionListener(this.cancelListener);

		this.lbl_error = new Label(containerComposite, SWT.WRAP | SWT.NATIVE );
		StateComposite.anchor(lbl_error).top(87, -15).bottom(103, -20).left(5, 0).right(btn_cancel, -10).set();

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/**
	 * enables submit button
	 */
	public void enableButton() {
		this.btn_ok.setEnabled(true);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.composites.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		getShell().setDefaultButton(this.btn_ok);
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
			this.txt_password.setFocus();
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
		this.lbl_number.setText(Messages.getString("mobileBKU.number"));
		this.lbl_password.setText(Messages.getString("mobileBKU.password"));
		this.btn_ok.setText(Messages.getString("common.Ok"));
	}

}
