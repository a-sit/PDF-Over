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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.workflow.states.State;

/**
 * 
 */
public class MobileBKUEnterNumberComposite extends StateComposite {
	/**
	 * 
	 */
	private final class OkSelectionListener extends SelectionAdapter {
		/**
		 * Regular expression for mobile phone numbers:
		 * this allows the entrance of mobile numbers in the following formats:
		 * 
		 * +(countryCode)99999999999
		 * 00(countryCode)99999999999
		 * 099999999999
		 * 1030199999999999 (A-Trust Test bku)
		 */
		private static final String NUMBER_REGEX = "^((\\+[\\d]{2})|(00[\\d]{2})|(0)|(10301))([1-9][\\d]+)$"; //$NON-NLS-1$

		/**
		 * Empty constructor
		 */
		public OkSelectionListener() {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				String number = MobileBKUEnterNumberComposite.this.txt_number
						.getText();

				// Verify number and normalize

				// Compile and use regular expression
				Pattern pattern = Pattern.compile(NUMBER_REGEX);
				Matcher matcher = pattern.matcher(number);

				if (!matcher.find()) {
					MobileBKUEnterNumberComposite.this
							.setErrorMessage("Given phone number is invalid! Example: +43664123456789");
					return;
				}

				if (matcher.groupCount() != 6) {
					MobileBKUEnterNumberComposite.this
							.setErrorMessage("Given phone number is invalid! Example: +43664123456789");
					return;
				}

				String countryCode = matcher.group(1);

				String normalNumber = matcher.group(6);

				if (countryCode.equals("10301")) { //$NON-NLS-1$
					// A-Trust Testnumber!
				} else {

					countryCode = countryCode.replace("00", "+"); //$NON-NLS-1$ //$NON-NLS-2$

					if (countryCode.equals("0")) { //$NON-NLS-1$
						countryCode = "+43"; //$NON-NLS-1$
					}

					number = countryCode + normalNumber;
				}
				MobileBKUEnterNumberComposite.this.setMobileNumber(number);

				MobileBKUEnterNumberComposite.this.mobileNumber = number;

				String password = MobileBKUEnterNumberComposite.this.txt_password
						.getText();

				// TODO: Logic to verify password

				if (password.length() < 6 || password.length() > 20) {
					if (password.length() < 6) {
						MobileBKUEnterNumberComposite.this
								.setErrorMessage("Given password is too short!");
					} else {
						MobileBKUEnterNumberComposite.this
								.setErrorMessage("Given password is too long!");
					}
					return;
				}

				MobileBKUEnterNumberComposite.this.mobilePassword = password;
				MobileBKUEnterNumberComposite.this.setUserAck(true);
			} catch (Exception ex) {
				log.error("Validating input for Mobile BKU failed!", ex); //$NON-NLS-1$
				// TODO: NOT VALID
				MobileBKUEnterNumberComposite.this
						.setErrorMessage("Given phone number is invalid! Example: +43664123456789");
				return;
			}
			MobileBKUEnterNumberComposite.this.state.updateStateMachine();
		}
	}

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(MobileBKUEnterNumberComposite.class);

	String mobileNumber;

	String mobilePassword;

	Text txt_number;

	Text txt_password;

	String errorMessage = null;

	boolean userAck = false;

	/**
	 * @return the userAck
	 */
	public boolean isUserAck() {
		return this.userAck;
	}

	/**
	 * @param userAck
	 *            the userAck to set
	 */
	public void setUserAck(boolean userAck) {
		this.userAck = userAck;
	}

	private Label lbl_error;

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
			this.lbl_error.setText(""); //$NON-NLS-1$
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

		this.txt_number = new Text(this, SWT.SINGLE | SWT.NATIVE | SWT.BORDER);
		FormData fd_number = new FormData();
		fd_number.top = new FormAttachment(30, -15);
		fd_number.bottom = new FormAttachment(30, 15);
		fd_number.left = new FormAttachment(50, 0);
		fd_number.right = new FormAttachment(85, 0);
		this.txt_number.setLayoutData(fd_number);
		this.txt_number.setEditable(true);

		Label lbl_number = new Label(this, SWT.NATIVE);
		lbl_number.setText("Nummer:");
		FormData fd_lbl_number = new FormData();
		fd_lbl_number.top = new FormAttachment(30, -15);
		fd_lbl_number.bottom = new FormAttachment(30, 15);
		fd_lbl_number.left = new FormAttachment(15, 0);
		fd_lbl_number.right = new FormAttachment(45, 0);
		lbl_number.setLayoutData(fd_lbl_number);

		this.txt_password = new Text(this, SWT.SINGLE | SWT.PASSWORD
				| SWT.BORDER | SWT.NATIVE);
		FormData fd_password = new FormData();
		fd_password.top = new FormAttachment(50, -15);
		fd_password.bottom = new FormAttachment(50, 15);
		fd_password.left = new FormAttachment(50, 0);
		fd_password.right = new FormAttachment(85, 0);
		this.txt_password.setLayoutData(fd_password);
		this.txt_password.setEditable(true);

		Label lbl_password = new Label(this, SWT.NATIVE);
		lbl_password.setText("Passwort:");
		FormData fd_lbl_password = new FormData();
		fd_lbl_password.top = new FormAttachment(50, -15);
		fd_lbl_password.bottom = new FormAttachment(50, 15);
		fd_lbl_password.left = new FormAttachment(15, 0);
		fd_lbl_password.right = new FormAttachment(45, 0);
		lbl_password.setLayoutData(fd_lbl_password);

		this.lbl_error = new Label(this, SWT.WRAP | SWT.NATIVE);
		FormData fd_lbl_error = new FormData();
		fd_lbl_error.top = new FormAttachment(70, -15);
		fd_lbl_error.bottom = new FormAttachment(70, 15);
		fd_lbl_error.left = new FormAttachment(15, 0);
		fd_lbl_error.right = new FormAttachment(85, 0);
		this.lbl_error.setLayoutData(fd_lbl_error);

		Button btn_ok = new Button(this, SWT.NATIVE);
		btn_ok.setText("Ok");
		FormData fd_btn_ok = new FormData();
		fd_btn_ok.top = new FormAttachment(87, 0);
		fd_btn_ok.bottom = new FormAttachment(95, 0);
		fd_btn_ok.left = new FormAttachment(75, 0);
		fd_btn_ok.right = new FormAttachment(95, 0);
		btn_ok.setLayoutData(fd_btn_ok);
		btn_ok.addSelectionListener(new OkSelectionListener());
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.composites.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		// Nothing to do here till now
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

		if (this.mobileNumber != null) {
			this.txt_number.setText(this.mobileNumber);
		} else {
			this.txt_number.setText(""); //$NON-NLS-1$
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
			this.txt_password.setText(""); //$NON-NLS-1$
		}
	}

}
