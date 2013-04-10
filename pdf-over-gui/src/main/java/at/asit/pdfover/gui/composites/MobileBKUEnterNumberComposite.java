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
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
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

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.exceptions.InvalidNumberException;
import at.asit.pdfover.gui.exceptions.InvalidPasswordException;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHelper;

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
	private final SelectionListener okListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if(!MobileBKUEnterNumberComposite.this.btn_ok.isEnabled()) {
				return;
			}
			
			try {
				String number = MobileBKUEnterNumberComposite.this.txt_number
						.getText();

				
				number = MobileBKUHelper.normalizeMobileNumber(number);
				
				MobileBKUEnterNumberComposite.this.setMobileNumber(number);

				MobileBKUEnterNumberComposite.this.mobileNumber = number;

				String password = MobileBKUEnterNumberComposite.this.txt_password
						.getText();

				MobileBKUHelper.validatePassword(password);

				MobileBKUEnterNumberComposite.this.mobilePassword = password;
				MobileBKUEnterNumberComposite.this.setUserAck(true);
				
				MobileBKUEnterNumberComposite.this.btn_ok.setEnabled(false);
				
			} catch(InvalidNumberException ex) {
				log.error("Validating input for Mobile BKU failed!", ex); //$NON-NLS-1$
				MobileBKUEnterNumberComposite.this
				.setErrorMessage(Messages.getString("error.InvalidPhoneNumber")); //$NON-NLS-1$
				MobileBKUEnterNumberComposite.this.txt_number.setFocus();
			} catch(InvalidPasswordException ex) {
				log.error("Validating input for Mobile BKU failed!", ex); //$NON-NLS-1$
				MobileBKUEnterNumberComposite.this
				.setErrorMessage(ex.getMessage());
				MobileBKUEnterNumberComposite.this.txt_password.setFocus();
			}
			catch (Exception ex) {
				log.error("Validating input for Mobile BKU failed!", ex); //$NON-NLS-1$
				MobileBKUEnterNumberComposite.this
						.setErrorMessage(Messages.getString("error.InvalidPhoneNumber")); //$NON-NLS-1$
				MobileBKUEnterNumberComposite.this.txt_number.setFocus();
				return;
			}
			
			MobileBKUEnterNumberComposite.this.state.updateStateMachine();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// Nothing to do here
		}
	};

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

	private Label lbl_password;

	private Label lbl_number;

	Button btn_ok;

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

		final Composite containerComposite = new Composite(this, SWT.NATIVE);
		containerComposite.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				Rectangle clientArea = containerComposite.getClientArea();
				
				//e.gc.setForeground();
				e.gc.setForeground(Constants.MAINBAR_ACTIVE_BACK_DARK);
				e.gc.setLineWidth(3);
				e.gc.setLineStyle(SWT.LINE_SOLID);
				e.gc.drawRoundRectangle(clientArea.x, 
						clientArea.y, clientArea.width - 2, clientArea.height - 2, 
						10, 10);
			}
		});
		containerComposite.setLayout(new FormLayout());
		FormData fd_containerComposite = new FormData();
		fd_containerComposite.top = new FormAttachment(50, -120);
		fd_containerComposite.bottom = new FormAttachment(50, 120);
		fd_containerComposite.left = new FormAttachment(50, -200);
		fd_containerComposite.right = new FormAttachment(50, 200);
		containerComposite.setLayoutData(fd_containerComposite);
		
		
		this.txt_number = new Text(containerComposite, SWT.SINGLE | SWT.NATIVE | SWT.BORDER);
		FormData fd_number = new FormData();
		//fd_number.top = new FormAttachment(0, 20);
		fd_number.bottom = new FormAttachment(50, -10);
		fd_number.left = new FormAttachment(50, 10);
		fd_number.right = new FormAttachment(100, -20);
		this.txt_number.setLayoutData(fd_number);
		this.txt_number.setEditable(true);

		
		this.lbl_number = new Label(containerComposite, SWT.NATIVE);
		this.lbl_number.setAlignment(SWT.RIGHT);
		this.lbl_number.setText(Messages.getString("mobileBKU.number")); //$NON-NLS-1$
		FormData fd_lbl_number = new FormData();
		//fd_lbl_number.top = new FormAttachment(30, -15);
		fd_lbl_number.bottom = new FormAttachment(50, -10);
		//fd_lbl_number.left = new FormAttachment(0, 20);
		fd_lbl_number.right = new FormAttachment(50, -10);
		this.lbl_number.setLayoutData(fd_lbl_number);

		Label lbl_image = new Label(containerComposite, SWT.NATIVE);
		
		ImageData data = new ImageData(this.getClass().getResourceAsStream("/img/handy.gif"));//$NON-NLS-1$
		Image mobile = new Image(getDisplay(), data); 
		
		FormData fd_lbl_image = new FormData();
		fd_lbl_image.top = new FormAttachment(50, -1 * (data.width / 2));
		fd_lbl_image.bottom = new FormAttachment(50, data.width / 2);
		fd_lbl_image.left = new FormAttachment(0, 10);
		fd_lbl_image.width = data.width;
		lbl_image.setLayoutData(fd_lbl_image);
		lbl_image.setImage(mobile);
		
		
		this.txt_password = new Text(containerComposite, SWT.SINGLE | SWT.PASSWORD
				| SWT.BORDER | SWT.NATIVE);
		FormData fd_password = new FormData();
		fd_password.top = new FormAttachment(50, 10);
		//fd_password.bottom = new FormAttachment(50, 15);
		fd_password.left = new FormAttachment(50, 10);
		fd_password.right = new FormAttachment(100, -20);
		this.txt_password.setLayoutData(fd_password);
		this.txt_password.setEditable(true);

		this.lbl_password = new Label(containerComposite, SWT.NATIVE);
		this.lbl_password.setAlignment(SWT.RIGHT);
		this.lbl_password.setText(Messages.getString("mobileBKU.password")); //$NON-NLS-1$
		FormData fd_lbl_password = new FormData();
		fd_lbl_password.top = new FormAttachment(50, 10);
		//fd_lbl_password.bottom = new FormAttachment(50, 15);
		//fd_lbl_password.left = new FormAttachment(0, 20);
		fd_lbl_password.right = new FormAttachment(50, -10);
		this.lbl_password.setLayoutData(fd_lbl_password);

		this.btn_ok = new Button(containerComposite, SWT.NATIVE);
		this.btn_ok.setText(Messages.getString("common.Ok")); //$NON-NLS-1$
		FormData fd_btn_ok = new FormData();
		//fd_btn_ok.top = new FormAttachment(87, 0);
		fd_btn_ok.bottom = new FormAttachment(100, -20);
		fd_btn_ok.right = new FormAttachment(100, -20);
		fd_btn_ok.left = new FormAttachment(100, -70);
		this.btn_ok.setLayoutData(fd_btn_ok);
		this.btn_ok.addSelectionListener(this.okListener);
		
		this.lbl_error = new Label(containerComposite, SWT.WRAP | SWT.NATIVE);
		FormData fd_lbl_error = new FormData();
		//fd_lbl_error.top = new FormAttachment(70, -15);
		fd_lbl_error.bottom = new FormAttachment(100, -20);
		fd_lbl_error.left = new FormAttachment(15, 0);
		fd_lbl_error.right = new FormAttachment(this.btn_ok, -10);
		this.lbl_error.setLayoutData(fd_lbl_error);

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
	
	/*
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

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		this.lbl_number.setText(Messages.getString("mobileBKU.number")); //$NON-NLS-1$
		this.lbl_password.setText(Messages.getString("mobileBKU.password")); //$NON-NLS-1$
		this.btn_ok.setText(Messages.getString("common.Ok")); //$NON-NLS-1$
	}

}
