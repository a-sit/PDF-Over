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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.utils.SWTUtils;
import at.asit.pdfover.gui.workflow.states.State;

/**
 * Composite for entering the TAN for the mobile BKU
 */
public class MobileBKUEnterTANComposite extends StateComposite {

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
			if(!MobileBKUEnterTANComposite.this.btn_ok.getEnabled()) {
				return;
			}

			String tan = MobileBKUEnterTANComposite.this.txt_tan.getText();

			tan = tan.trim();

			if (tan.isEmpty()) {
				MobileBKUEnterTANComposite.this.setMessage(Messages
						.getString("error.NoTan"));
				return;
			}

			if (MobileBKUEnterTANComposite.this.refVal.startsWith(tan)) {
				MobileBKUEnterTANComposite.this.setMessage(Messages
						.getString("error.EnteredReferenceValue"));
				return;
			}

			if (tan.length() > 6) {
				MobileBKUEnterTANComposite.this.setMessage(Messages
						.getString("error.TanTooLong"));
				return;
			}

			MobileBKUEnterTANComposite.this.tan = tan;
			MobileBKUEnterTANComposite.this.setUserAck(true);
			MobileBKUEnterTANComposite.this.btn_ok.setEnabled(false);
			//MobileBKUEnterTANComposite.this.state.updateStateMachine();
			//MobileBKUEnterTANComposite.this.btn_ok.setEnabled(true);
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
			MobileBKUEnterTANComposite.this.setUserCancel(true);
		}
	}

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(MobileBKUEnterTANComposite.class);

	Text txt_tan;

	boolean userAck = false;
	boolean userCancel = false;

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

	String tan;

	private Label lblTries;
	private Label lblRefValLabel;
	private Label lblTan;

	Button btn_ok;
	Button btn_cancel;

	/**
	 * @return the userAck
	 */
	public boolean isUserAck() {
		return this.userAck;
	}

	/**
	 * @return the userCancel
	 */
	public boolean isUserCancel() {
		return this.userCancel;
	}

	/**
	 * Set how many tries are left
	 *
	 * @param tries
	 */
	public void setTries(int tries) {
		this.lblTries.setText(tries == 1 ? Messages.getString("tanEnter.try") :
				String.format(Messages.getString("tanEnter.tries"), tries));
	}

	/**
	 * Set an error message
	 * @param errorMessage the error message
	 */
	public void setErrorMessage(String errorMessage) {
		if (errorMessage == null)
			this.lblTries.setText("");
		else
			this.lblTries.setText(
					Messages.getString("error.Title") + ": " + errorMessage);
	}


	/**
	 * Sets the message
	 *
	 * @param msg
	 */
	public void setMessage(String msg) {
		this.lblTries.setText(msg);
		this.lblTries.redraw();
		this.lblTries.getParent().layout(true, true);
	}

	/**
	 * @param userAck
	 *            the userAck to set
	 */
	public void setUserAck(boolean userAck) {
		this.userAck = userAck;
	}

	/**
	 * @param userCancel
	 *            the userCancel to set
	 */
	public void setUserCancel(boolean userCancel) {
		this.userCancel = userCancel;
	}

	/**
	 * @return the reference value
	 */
	public String getRefVal() {
		return this.refVal;
	}

	/**
	 * Enables the submit button
	 */
	public void enableButton() {
		this.btn_ok.setEnabled(true);
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
			this.lblRefVal.setText("");
		}

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
				String signatureData = MobileBKUEnterTANComposite.this
						.getSignatureData();
				if (signatureData != null && !signatureData.equals("")) {
					log.debug("Trying to open " + signatureData);
					if (Desktop.isDesktopSupported()) {
						Desktop.getDesktop().browse(new URI(signatureData));
					} else {
						log.info("SWT Desktop is not supported on this platform");
						Program.launch(signatureData);
					}
				}
			} catch (Exception ex) {
				log.error("OpenSelectionListener: ", ex);
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
		SWTUtils.anchor(containerComposite).top(50, -120).bottom(50, 120).left(50, -200).right(50, 200).set();

		this.lblRefValLabel = new Label(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(lblRefValLabel).right(50, -10).bottom(50,-10).set();
		SWTUtils.setLocalizedText(lblRefValLabel, "tanEnter.ReferenceValue");
		this.lblRefValLabel.setAlignment(SWT.RIGHT);

		ImageData mobileIcon = new ImageData(this.getClass().getResourceAsStream(Constants.RES_IMG_MOBILE));
		Label lbl_image = new Label(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(lbl_image).top(50, -1 * (mobileIcon.width / 2)).bottom(50, mobileIcon.width / 2).left(0, 10).width(mobileIcon.width).set();
		lbl_image.setImage(new Image(getDisplay(), mobileIcon));

		this.lblRefVal = new Label(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(lblRefVal).left(50,10).right(100,-20).bottom(50,-10).set();
		this.lblRefVal.setText("");

		this.lblTan = new Label(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(lblTan).right(50,-10).top(50,10).set();
		SWTUtils.setLocalizedText(lblTan, "tanEnter.TAN");
		this.lblTan.setAlignment(SWT.RIGHT);

		this.txt_tan = new Text(containerComposite, SWT.BORDER | SWT.NATIVE);
		SWTUtils.anchor(txt_tan).left(50,10).right(100,-20).top(50,10).set();
		this.txt_tan.setEditable(true);

		this.txt_tan.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					if(MobileBKUEnterTANComposite.this.btn_ok.isEnabled()) {
						(new OkSelectionListener()).widgetSelected(null);
					}
				}
			}
		});

		this.txt_tan.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {

				String text = MobileBKUEnterTANComposite.this.txt_tan.getText();
				//log.debug("Current TAN: " + text);
				if (text.length() > 3
						&& MobileBKUEnterTANComposite.this.getRefVal()
								.startsWith(text.trim())) {
					MobileBKUEnterTANComposite.this.setMessage(Messages
							.getString("error.EnteredReferenceValue"));
				}
			}
		});

		Link lnk_sig_data = new Link(containerComposite, SWT.NATIVE | SWT.RESIZE);
		SWTUtils.anchor(lnk_sig_data).right(100,-20).top(0,20).set();
		lnk_sig_data.setEnabled(true);
		lnk_sig_data.addSelectionListener(new ShowSignatureDataListener());
		SWTUtils.setLocalizedText(lnk_sig_data, "mobileBKU.show");
		SWTUtils.setLocalizedToolTipText(lnk_sig_data, "mobileBKU.show_tooltip");

		this.btn_ok = new Button(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(btn_ok).right(100,-20).bottom(100,-20).set();
		SWTUtils.setLocalizedText(btn_ok, "common.Ok");
		this.btn_ok.addSelectionListener(new OkSelectionListener());
		
		this.btn_cancel = new Button(containerComposite, SWT.NATIVE);
		SWTUtils.anchor(btn_cancel).right(btn_ok, -20).bottom(100, -20).set();
		SWTUtils.setLocalizedText(btn_cancel, "common.Cancel");
		this.btn_cancel.addSelectionListener(new CancelSelectionListener());

		this.lblTries = new Label(containerComposite, SWT.WRAP | SWT.NATIVE);
		SWTUtils.anchor(lblTries).right(btn_cancel, -10).bottom(100, -20).set();
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
		SWTUtils.setLocalizedText(lblTan, "tanEnter.TAN");
	}
}
