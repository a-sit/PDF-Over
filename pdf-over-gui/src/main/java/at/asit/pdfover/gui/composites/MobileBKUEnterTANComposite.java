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
import org.eclipse.swt.graphics.Color;
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

import at.asit.pdfover.gui.utils.Messages;
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
			String tan = MobileBKUEnterTANComposite.this.txt_tan.getText();

			tan = tan.trim();

			if (MobileBKUEnterTANComposite.this.refVal.startsWith(tan)) {
				MobileBKUEnterTANComposite.this.setMessage(Messages
						.getString("error.EnteredReferenceValue")); //$NON-NLS-1$
				return;
			}

			if (tan.length() > 6) {
				MobileBKUEnterTANComposite.this.setMessage(Messages
						.getString("error.TanTooLong")); //$NON-NLS-1$
				return;
			}

			MobileBKUEnterTANComposite.this.tan = tan;
			MobileBKUEnterTANComposite.this.setUserAck(true);
			MobileBKUEnterTANComposite.this.state.updateStateMachine();
		}
	}

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(MobileBKUEnterTANComposite.class);

	Text txt_tan;

	boolean userAck = false;

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

	/**
	 * @return the userAck
	 */
	public boolean isUserAck() {
		return this.userAck;
	}

	/**
	 * Set how many tries are left
	 * 
	 * @param tries
	 */
	public void setTries(int tries) {
		this.lblTries.setText(tries == 1 ? Messages.getString("tanEnter.try") : //$NON-NLS-1$
				String.format(Messages.getString("tanEnter.tries"), tries)); //$NON-NLS-1$
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
			this.lblRefVal.setText(""); //$NON-NLS-1$
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
			this.txt_tan.setText(""); //$NON-NLS-1$
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
				if (signatureData != null && !signatureData.equals("")) { //$NON-NLS-1$
					log.debug("Trying to open " + signatureData); //$NON-NLS-1$
					if (Desktop.isDesktopSupported()) {
						Desktop.getDesktop().browse(new URI(signatureData));
					} else {
						log.error("SWT Desktop is not supported on this platform!"); //$NON-NLS-1$
					}
				}
			} catch (Exception ex) {
				log.error("OpenSelectionListener: ", ex); //$NON-NLS-1$
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
				e.gc.setForeground(new Color(getDisplay(), 0x6B, 0xA5, 0xD9));
				e.gc.setLineWidth(3);
				e.gc.setLineStyle(SWT.LINE_SOLID);
				e.gc.drawRoundRectangle(clientArea.x, clientArea.y,
						clientArea.width - 2, clientArea.height - 2, 10, 10);
			}
		});
		containerComposite.setLayout(new FormLayout());
		FormData fd_containerComposite = new FormData();
		fd_containerComposite.top = new FormAttachment(50, -100);
		fd_containerComposite.bottom = new FormAttachment(50, 100);
		fd_containerComposite.left = new FormAttachment(50, -200);
		fd_containerComposite.right = new FormAttachment(50, 200);
		containerComposite.setLayoutData(fd_containerComposite);

		this.lblRefValLabel = new Label(containerComposite, SWT.NATIVE);
		this.lblRefValLabel.setAlignment(SWT.RIGHT);
		FormData fd_lblRefValLabel = new FormData();
		// fd_lblRefValLabel.left = new FormAttachment(0, 20);
		fd_lblRefValLabel.right = new FormAttachment(50, -10);
		// fd_lblRefValLabel.top = new FormAttachment(30, -15);
		fd_lblRefValLabel.bottom = new FormAttachment(50, -10);
		this.lblRefValLabel.setLayoutData(fd_lblRefValLabel);
		this.lblRefValLabel.setText(Messages
				.getString("tanEnter.ReferenceValue")); //$NON-NLS-1$

		Label lbl_image = new Label(containerComposite, SWT.NATIVE);

		ImageData data = new ImageData(this.getClass().getResourceAsStream(
				"/img/handy.gif"));//$NON-NLS-1$
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
		// fd_lblRefVal.top = new FormAttachment(30, -15);
		fd_lblRefVal.bottom = new FormAttachment(50, -10);
		this.lblRefVal.setLayoutData(fd_lblRefVal);
		this.lblRefVal.setText(""); //$NON-NLS-1$

		this.lblTan = new Label(containerComposite, SWT.NATIVE);
		this.lblTan.setAlignment(SWT.RIGHT);
		FormData fd_lblTan = new FormData();
		// fd_lblTan.left = new FormAttachment(0, 20);
		fd_lblTan.right = new FormAttachment(50, -10);
		fd_lblTan.top = new FormAttachment(50, 10);
		// fd_lblTan.bottom = new FormAttachment(50, 15);
		this.lblTan.setLayoutData(fd_lblTan);
		this.lblTan.setText(Messages.getString("tanEnter.TAN")); //$NON-NLS-1$

		this.txt_tan = new Text(containerComposite, SWT.BORDER | SWT.NATIVE);
		FormData fd_text = new FormData();
		fd_text.left = new FormAttachment(50, 10);
		fd_text.right = new FormAttachment(100, -20);
		fd_text.top = new FormAttachment(50, 10);
		this.txt_tan.setEditable(true);
		this.txt_tan.setLayoutData(fd_text);

		this.txt_tan.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					(new OkSelectionListener()).widgetSelected(null);
				}
			}
		});

		this.txt_tan.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {

				String text = MobileBKUEnterTANComposite.this.txt_tan.getText();
				//log.debug("Current TAN: " + text); //$NON-NLS-1$
				if (text.length() > 3
						&& MobileBKUEnterTANComposite.this.getRefVal()
								.startsWith(text.trim())) {
					MobileBKUEnterTANComposite.this.setMessage(Messages
							.getString("error.EnteredReferenceValue")); //$NON-NLS-1$
				}
			}
		});

		Button btn_ok = new Button(containerComposite, SWT.NATIVE);

		Button lnk_sig_data = new Button(containerComposite, SWT.NATIVE | SWT.RESIZE);

		FormData fd_lnk_data = new FormData();
		fd_lnk_data.right = new FormAttachment(100, -20);
		fd_lnk_data.bottom = new FormAttachment(100, -20);
		lnk_sig_data.setEnabled(true);
		lnk_sig_data.setLayoutData(fd_lnk_data);
		lnk_sig_data.addSelectionListener(new ShowSignatureDataListener());
		lnk_sig_data.setText(Messages.getString("mobileBKU.show")); //$NON-NLS-1$
		lnk_sig_data.setToolTipText(Messages.getString("mobileBKU.show_tooltip")); //$NON-NLS-1$
		
		this.lblTries = new Label(containerComposite, SWT.WRAP | SWT.NATIVE);
		FormData fd_lbl_tries = new FormData();
		// fd_lbl_tries.left = new FormAttachment(15, 5);
		fd_lbl_tries.right = new FormAttachment(btn_ok, -10);
		// fd_lbl_tries.top = new FormAttachment(70, -15);
		fd_lbl_tries.bottom = new FormAttachment(100, -20);
		this.lblTries.setLayoutData(fd_lbl_tries);

		FormData fd_btn_ok = new FormData();
		// fd_btn_ok.left = new FormAttachment(95, 0);
		fd_btn_ok.right = new FormAttachment(lnk_sig_data, -10);
		//fd_btn_ok.left = new FormAttachment(100, -70);
		fd_btn_ok.bottom = new FormAttachment(100, -20);

		btn_ok.setLayoutData(fd_btn_ok);
		btn_ok.setText(Messages.getString("common.Ok")); //$NON-NLS-1$
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
				.getString("tanEnter.ReferenceValue")); //$NON-NLS-1$
		this.lblTan.setText(Messages.getString("tanEnter.TAN")); //$NON-NLS-1$
	}

}
