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
import java.awt.Desktop;
import java.net.URI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.states.State;

/**
 * Composite for displaying the QR code for the mobile BKU
 */
public class MobileBKUFingerprintComposite extends StateComposite {

	/**
	 *
	 */
	private final class SMSSelectionListener extends SelectionAdapter {
		/**
		 * Empty constructor
		 */
		public SMSSelectionListener() {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if(!MobileBKUFingerprintComposite.this.btn_sms.getEnabled()) {
				return;
			}

			MobileBKUFingerprintComposite.this.setUserSMS(true);
			MobileBKUFingerprintComposite.this.btn_sms.setEnabled(false);
		}
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
			MobileBKUFingerprintComposite.this.setUserCancel(true);
		}
	}

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(MobileBKUFingerprintComposite.class);

	boolean userCancel = false;
	boolean userSMS = false;
	boolean done = false;

	private Label lblRefVal;

	String refVal;

	String signatureData;

	/**
	 * @return the signatureData
	 */
	public String getSignatureData() {
		return this.signatureData;
	}

	/**
	 * @param signatureData
	 *            the signatureData to set
	 */
	public void setSignatureData(String signatureData) {
		this.signatureData = signatureData;
	}

	private Label lblError;
	private Label lblRefValLabel;
	private Label lblFPLabel;

	Button btn_sms;
	Button btn_cancel;

	Link lnk_sig_data;

	/**
	 * @return the userCancel
	 */
	public boolean isUserCancel() {
		return this.userCancel;
	}

	/**
	 * @return the userSMS
	 */
	public boolean isUserSMS() {
		return this.userSMS;
	}

	/**
	 * @return the done
	 */
	public boolean isDone() {
		return this.done;
	}

	/**
	 * Set an error message
	 * @param errorMessage the error message
	 */
	public void setErrorMessage(String errorMessage) {
		if (errorMessage == null)
			this.lblError.setText(""); //
		else
			this.lblError.setText(
					Messages.getString("error.Title") + ": " + errorMessage); // //
	}

	/**
	 * @param userCancel
	 *            the userCancel to set
	 */
	public void setUserCancel(boolean userCancel) {
		this.userCancel = userCancel;
	}

	/**
	 * @param userSMS
	 *            the userSMS to set
	 */
	public void setUserSMS(boolean userSMS) {
		this.userSMS = userSMS;
	}

	/**
	 * @param done
	 *            the done to set
	 */
	public void setDone(boolean done) {
		this.done = done;
	}

	/**
	 * @return the reference value
	 */
	public String getRefVal() {
		return this.refVal;
	}

	/**
	 * @param refVal
	 *            the reference value to set
	 */
	public void setRefVal(String refVal) {
		this.refVal = refVal.trim();

		if (this.refVal != null) {
			this.lblRefVal.setText(this.refVal);
		} else {
			this.lblRefVal.setText(""); //
		}

	}


	/**
	 * Selection Listener for open button
	 */
	private final class ShowSignatureDataListener extends SelectionAdapter {
		/**
		 * Empty constructor
		 */
		public ShowSignatureDataListener() {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				String signatureData = MobileBKUFingerprintComposite.this
						.getSignatureData();
				if (signatureData != null && !signatureData.equals("")) { //
					log.debug("Trying to open " + signatureData); //
					if (Desktop.isDesktopSupported()) {
						Desktop.getDesktop().browse(new URI(signatureData));
					} else {
						log.info("SWT Desktop is not supported on this platform"); //
						Program.launch(signatureData);
					}
				}
			} catch (Exception ex) {
				log.error("OpenSelectionListener: ", ex); //
			}
		}
	}

	/**
	 * Create the composite.
	 *
	 * @param parent
	 * @param style
	 * @param state
	 */
	public MobileBKUFingerprintComposite(Composite parent, int style, State state) {
		super(parent, style, state);
		setLayout(new FormLayout());

		final Composite containerComposite = new Composite(this, SWT.NATIVE);
		containerComposite.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Rectangle clientArea = containerComposite.getClientArea();

				// e.gc.setForeground();
				e.gc.setForeground(Constants.MAINBAR_ACTIVE_BACK_DARK);
				e.gc.setLineWidth(3);
				e.gc.setLineStyle(SWT.LINE_SOLID);
				e.gc.drawRoundRectangle(clientArea.x, clientArea.y,
						clientArea.width - 2, clientArea.height - 2, 10, 10);
			}
		});
		containerComposite.setLayout(new FormLayout());
		FormData fd_containerComposite = new FormData();
		fd_containerComposite.top = new FormAttachment(50, -140);
		fd_containerComposite.bottom = new FormAttachment(50, 140);
		fd_containerComposite.left = new FormAttachment(50, -200);
		fd_containerComposite.right = new FormAttachment(50, 200);
		containerComposite.setLayoutData(fd_containerComposite);

		this.lblRefValLabel = new Label(containerComposite, SWT.NATIVE);
		this.lblRefValLabel.setAlignment(SWT.RIGHT);
		FormData fd_lblRefValLabel = new FormData();
		// fd_lblRefValLabel.left = new FormAttachment(0, 20);
		fd_lblRefValLabel.right = new FormAttachment(50, -10);
		fd_lblRefValLabel.top = new FormAttachment(30, -10);
		//fd_lblRefValLabel.bottom = new FormAttachment(50, -10);
		this.lblRefValLabel.setLayoutData(fd_lblRefValLabel);

		Label lbl_image = new Label(containerComposite, SWT.NATIVE);

		ImageData data = new ImageData(this.getClass().getResourceAsStream(
				Constants.RES_IMG_MOBILE));
		Image mobile = new Image(getDisplay(), data);

		FormData fd_lbl_image = new FormData();
		fd_lbl_image.top = new FormAttachment(50, -1 * (data.width / 2));
		fd_lbl_image.bottom = new FormAttachment(50, data.width / 2);
		fd_lbl_image.left = new FormAttachment(0, 10);
		fd_lbl_image.width = data.width;
		lbl_image.setLayoutData(fd_lbl_image);
		lbl_image.setImage(mobile);

		this.lblRefVal = new Label(containerComposite, SWT.NATIVE);
		FormData fd_lblRefVal = new FormData();
		fd_lblRefVal.left = new FormAttachment(50, 10);
		fd_lblRefVal.right = new FormAttachment(100, -20);
		fd_lblRefVal.top = new FormAttachment(30, -10);
		//fd_lblRefVal.bottom = new FormAttachment(50, -10);
		this.lblRefVal.setLayoutData(fd_lblRefVal);
		this.lblRefVal.setText("test"); //

		this.lblFPLabel = new Label(containerComposite, SWT.NATIVE);
		this.lblFPLabel.setAlignment(SWT.LEFT);
		FormData fd_lblFPLabel = new FormData();
		fd_lblFPLabel.left = new FormAttachment(25, 10);
		fd_lblFPLabel.top = new FormAttachment(this.lblRefValLabel, 10);
		this.lblFPLabel.setLayoutData(fd_lblFPLabel);


		this.lnk_sig_data = new Link(containerComposite, SWT.NATIVE | SWT.RESIZE);

		FormData fd_lnk_data = new FormData();
		fd_lnk_data.right = new FormAttachment(100, -20);
		fd_lnk_data.top = new FormAttachment(0, 20);
		this.lnk_sig_data.setEnabled(true);
		this.lnk_sig_data.setLayoutData(fd_lnk_data);
		this.lnk_sig_data.addSelectionListener(new ShowSignatureDataListener());

		this.btn_cancel = new Button(containerComposite, SWT.NATIVE);
		this.btn_sms = new Button(containerComposite, SWT.NATIVE);

		this.lblError = new Label(containerComposite, SWT.WRAP | SWT.NATIVE);
		FormData fd_lbl_error = new FormData();
		// fd_lbl_error.left = new FormAttachment(15, 5);
		fd_lbl_error.right = new FormAttachment(this.btn_sms, -10);
		// fd_lbl_error.top = new FormAttachment(70, -15);
		fd_lbl_error.bottom = new FormAttachment(100, -20);
		this.lblError.setLayoutData(fd_lbl_error);

		FormData fd_btn_cancel = new FormData();
		// fd_btn_cancel.left = new FormAttachment(95, 0);
		fd_btn_cancel.right = new FormAttachment(100, -20);
		//fd_btn_cancel.left = new FormAttachment(100, -70);
		fd_btn_cancel.bottom = new FormAttachment(100, -20);

		this.btn_cancel.setLayoutData(fd_btn_cancel);
		this.btn_cancel.addSelectionListener(new CancelSelectionListener());

		FormData fd_btn_sms = new FormData();
		// fd_btn_sms.left = new FormAttachment(95, 0);
		fd_btn_sms.right = new FormAttachment(this.btn_cancel, -20);
		//fd_btn_sms.left = new FormAttachment(100, -70);
		fd_btn_sms.bottom = new FormAttachment(100, -20);

		this.btn_sms.setLayoutData(fd_btn_sms);
		this.btn_sms.addSelectionListener(new SMSSelectionListener());

		reloadResources();
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
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		this.lblRefValLabel.setText(Messages
				.getString("tanEnter.ReferenceValue")); //
		this.lblFPLabel.setText(Messages.getString("tanEnter.FP")); //
		this.lnk_sig_data.setText(Messages.getString("mobileBKU.show")); //
		this.lnk_sig_data.setToolTipText(Messages.getString("mobileBKU.show_tooltip")); //
		this.btn_cancel.setText(Messages.getString("common.Cancel")); //
		this.btn_sms.setText(Messages.getString("tanEnter.SMS")); //
	}
}
