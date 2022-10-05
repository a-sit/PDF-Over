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
import at.asit.pdfover.gui.utils.SWTUtils;
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
	static final Logger log = LoggerFactory.getLogger(MobileBKUFingerprintComposite.class);

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
			this.lblError.setText("");
		else
			this.lblError.setText(
					Messages.getString("error.Title") + ": " + errorMessage);
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

		if (this.refVal != null) {
			this.refVal = refVal.trim();
			this.lblRefVal.setText(this.refVal);
		} else {
			this.lblRefVal.setText("");
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
		SWTUtils.anchor(containerComposite).top(50, -140).bottom(50, 140).left(50, -200).right(50, 200);

		this.lblRefValLabel = new Label(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(lblRefValLabel).right(50, -10).top(30, -10);
		this.lblRefValLabel.setAlignment(SWT.RIGHT);

		ImageData mobileIcon = new ImageData(this.getClass().getResourceAsStream(Constants.RES_IMG_MOBILE));
		Label lbl_image = new Label(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(lbl_image).top(50, -1 * (mobileIcon.width / 2)).bottom(50, mobileIcon.width / 2).left(0, 10).width(mobileIcon.width);
		lbl_image.setImage(new Image(getDisplay(), mobileIcon));

		this.lblRefVal = new Label(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(lblRefVal).left(50, 10).right(100, -20).top(30, -10);

		this.lblFPLabel = new Label(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(lblFPLabel).left(25, 10).top(lblRefValLabel, 10);
		this.lblFPLabel.setAlignment(SWT.LEFT);

		this.lnk_sig_data = new Link(containerComposite, SWT.NATIVE | SWT.RESIZE);
		SWTUtils.anchor(lnk_sig_data).right(100, -20).top(0, 20);
		this.lnk_sig_data.setEnabled(true);
		SWTUtils.addSelectionListener(lnk_sig_data, (e) -> { SWTUtils.openURL(getSignatureData()); });

		this.btn_cancel = new Button(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(btn_cancel).right(100, -20).bottom(100, -20);
		this.btn_cancel.addSelectionListener(new CancelSelectionListener());

		this.btn_sms = new Button(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(btn_sms).right(btn_cancel, -20).bottom(100, -20);
		this.btn_sms.addSelectionListener(new SMSSelectionListener());

		this.lblError = new Label(containerComposite, SWT.WRAP | SWT.NATIVE);
		SWTUtils.anchor(lblError).right(btn_sms, -10).bottom(100, -20);

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
		SWTUtils.setLocalizedText(lblRefValLabel, "tanEnter.ReferenceValue");
		SWTUtils.setLocalizedText(lblFPLabel, "tanEnter.FP");
		SWTUtils.setLocalizedText(lnk_sig_data, "mobileBKU.show");
		SWTUtils.setLocalizedToolTipText(lnk_sig_data, "mobileBKU.show_tooltip");
		SWTUtils.setLocalizedText(btn_cancel, "common.Cancel");
		SWTUtils.setLocalizedText(btn_sms, "tanEnter.SMS");
	}
}
