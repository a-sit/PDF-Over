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
import java.net.URI;
import java.util.Objects;

import javax.annotation.CheckForNull;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.internal.Nullable;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.composites.StateComposite;
import at.asit.pdfover.gui.utils.SWTUtils;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.webauthnclient.WebAuthN;

/**
 * Composite for entering the TAN for the mobile BKU
 */
public class MobileBKUEnterTANComposite extends StateComposite {

	private void validateAndConfirmTAN() {
		String tan = this.txt_tan.getText();

		tan = tan.trim();

		if (tan.isEmpty()) {
			this.setMessage(Messages.getString("error.NoTan"));
			return;
		}

		if (MobileBKUEnterTANComposite.this.refVal.startsWith(tan)) {
			this.setMessage(Messages.getString("error.EnteredReferenceValue"));
			return;
		}

		if (tan.length() > 6) {
			this.setMessage(Messages.getString("error.TanTooLong"));
			return;
		}

		this.tan = tan;
		this.userAck = true;
	}

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(MobileBKUEnterTANComposite.class);

	private Text txt_tan;

	private boolean userAck = false;
	private boolean userCancel = false;
	private boolean userFido2 = false;

	private Label lblRefVal;

	private String refVal;

	private URI signatureDataURI;

	/**
	 * @param signatureData
	 *            the signatureData to set
	 */
	public void setSignatureDataURI(@Nullable URI uri) {
		this.signatureDataURI = uri;
		this.lnk_sig_data.setVisible(uri != null);
	}

	private String tan;

	private Link lnk_sig_data;

	private Label lblMessage;
	private Label lblRefValLabel;
	private Label lblTan;

	private Button btn_ok;
	private Button btn_cancel;
	private Button btn_fido2;

	public boolean isDone() { return (this.userAck || this.userCancel || this.userFido2); }
	public boolean isUserAck() { return this.userAck; }
	public boolean isUserCancel() { return this.userCancel; }
	public boolean isUserFido2() { return this.userFido2; }

	public void reset() { this.userAck = this.userCancel = this.userFido2 = false; }

	/**
	 * Set an error message
	 * @param errorMessage the error message
	 */
	public void setErrorMessage(String errorMessage) {
		if (errorMessage == null)
			this.lblMessage.setText("");
		else
			this.lblMessage.setText(
					Messages.getString("error.Title") + ": " + errorMessage);
	}

	public void setFIDO2Enabled(boolean state) {
		this.btn_fido2.setEnabled(state);
	}

	/**
	 * Sets the message
	 *
	 * @param msg
	 */
	public void setMessage(String msg) {
		this.lblMessage.setText(msg);
		this.lblMessage.redraw();
		this.lblMessage.getParent().layout(true, true);
	}

	public String getRefVal() { return this.refVal; }
	public void setRefVal(@CheckForNull String refVal) {
		this.refVal = (refVal != null) ? refVal.trim() : null;
		this.lblRefVal.setText(Objects.requireNonNullElse(this.refVal, ""));
	}

	/**
	 * @return the tan
	 */
	public String getTan() {
		return this.tan;
	}

	/**
	 * @param tan
	 *            the tan to set
	 */
	public void setTan(String tan) {
		this.tan = tan;

		if (this.tan == null) {
			this.txt_tan.setText("");
		} else {
			this.txt_tan.setText(this.tan);
		}
	}

	/**
	 * Create the composite.
	 *
	 * @param parent
	 * @param style
	 * @param state
	 */
	public MobileBKUEnterTANComposite(Composite parent, int style, State state) {
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
		SWTUtils.anchor(containerComposite).top(50, -120).bottom(50, 120).left(50, -200).right(50, 200);

		this.lblRefValLabel = new Label(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(lblRefValLabel).right(50, -10).bottom(50,-10);
		SWTUtils.setLocalizedText(lblRefValLabel, "tanEnter.ReferenceValue");
		this.lblRefValLabel.setAlignment(SWT.RIGHT);

		ImageData mobileIcon = new ImageData(this.getClass().getResourceAsStream(Constants.RES_IMG_MOBILE));
		Label lbl_image = new Label(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(lbl_image).top(50, -1 * (mobileIcon.width / 2)).bottom(50, mobileIcon.width / 2).left(0, 10).width(mobileIcon.width);
		lbl_image.setImage(new Image(getDisplay(), mobileIcon));

		this.lblRefVal = new Label(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(lblRefVal).left(50,10).right(100,-20).bottom(50,-10);
		this.lblRefVal.setText("");

		this.lblTan = new Label(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(lblTan).right(50,-10).top(50,10);
		SWTUtils.setLocalizedText(lblTan, "tanEnter.TAN");
		this.lblTan.setAlignment(SWT.RIGHT);

		this.txt_tan = new Text(containerComposite, SWT.BORDER | SWT.NATIVE);
		SWTUtils.anchor(txt_tan).left(50,10).right(100,-20).top(50,10);
		this.txt_tan.setEditable(true);

		this.txt_tan.addTraverseListener((e) -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				validateAndConfirmTAN();
			}
		});

		this.txt_tan.addModifyListener((e) -> {
			String text = this.txt_tan.getText();
			if (text.length() > 3 && this.getRefVal().startsWith(text.trim()))
				this.setMessage(Messages.getString("error.EnteredReferenceValue"));
		});

		this.lnk_sig_data = new Link(containerComposite, SWT.NATIVE | SWT.RESIZE);
		SWTUtils.anchor(lnk_sig_data).right(100,-20).top(0,20);
		lnk_sig_data.setEnabled(true);
		SWTUtils.addSelectionListener(lnk_sig_data, (e) -> { SWTUtils.openURL(this.signatureDataURI); });

		this.btn_ok = new Button(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(btn_ok).right(100,-20).bottom(100,-20);
		SWTUtils.addSelectionListener(btn_ok, (e) -> { validateAndConfirmTAN(); });
		
		this.btn_cancel = new Button(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(btn_cancel).right(btn_ok, -20).bottom(100, -20);
		SWTUtils.addSelectionListener(btn_cancel, (e) -> { this.userCancel = true; });

		this.btn_fido2 = new Button(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(btn_fido2).right(btn_cancel, -20).bottom(100, -20);
		SWTUtils.addSelectionListener(btn_fido2, (e) -> { this.userFido2 = true; });
		this.btn_fido2.setVisible(WebAuthN.isAvailable());

		this.lblMessage = new Label(containerComposite, SWT.WRAP | SWT.NATIVE);
		SWTUtils.anchor(lblMessage).right(btn_fido2, -10).bottom(100, -20);
	}
	
	@Override public void onDisplay() { getShell().setDefaultButton(btn_ok); txt_tan.setFocus(); }

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		SWTUtils.setLocalizedText(lnk_sig_data, "mobileBKU.show");
		SWTUtils.setLocalizedToolTipText(lnk_sig_data, "mobileBKU.show_tooltip");
		SWTUtils.setLocalizedText(lblRefValLabel, "tanEnter.ReferenceValue");
		SWTUtils.setLocalizedText(lblTan, "tanEnter.TAN");
		SWTUtils.setLocalizedText(btn_cancel, "common.Cancel");
		SWTUtils.setLocalizedText(btn_ok, "common.Ok");
		SWTUtils.setLocalizedText(btn_fido2, "tanEnter.FIDO2");
	}
}
