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
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.controls.ClickableCanvas;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.BKUs;

/**
 * Composite for BKU selection
 */
public class BKUSelectionComposite extends StateComposite {
	
	/**
	 * Margin for button
	 */
	public static final int btnMargin = 2;
	
	/**
	 * Listener for local bku selection 
	 */
	private final class LocalSelectionListener extends SelectionAdapter {
		/**
		 * Empty constructor
		 */
		public LocalSelectionListener() {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			log.debug("Setting BKU to LOCAL"); //$NON-NLS-1$
			setSelected(BKUs.LOCAL);
		}
	}
	
	/**
	 * Listener for mobile bku selection 
	 */
	private final class MobileSelectionListener extends SelectionAdapter {
		/**
		 * Empty constructor
		 */
		public MobileSelectionListener() {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			log.debug("Setting BKU to MOBILE"); //$NON-NLS-1$
			setSelected(BKUs.MOBILE);
		}
	}

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(BKUSelectionComposite.class);

	private BKUs selected = BKUs.NONE;

	private Button btnMobile;

	private Button btnCard;
	
	/**
	 * Gets selected BKU type
	 * @return BKUS enum
	 */
	public BKUs getSelected() {
		return this.selected;
	}

	/**
	 * Sets selected BKU and updates workflow
	 * @param selected
	 */
	public void setSelected(final BKUs selected) {
		this.selected = selected;
		this.state.updateStateMachine();
	}

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 * @param state 
	 */
	public BKUSelectionComposite(Composite parent, int style, State state) {
		super(parent, style, state);

		this.setLayout(new FormLayout());
		
		ClickableCanvas cc_mobile = new ClickableCanvas(this, SWT.NATIVE | SWT.RESIZE);
		FormData fd_cc_mobile = new FormData();
		fd_cc_mobile.right = new FormAttachment(50, -5);
		fd_cc_mobile.top = new FormAttachment(40, -20);
		cc_mobile.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
				// Nothing to do here
			}
			
			@Override
			public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
				setSelected(BKUs.MOBILE);
			}
			
			@Override
			public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {
				// Nothing to do here
			}
		});
		cc_mobile.setLayoutData(fd_cc_mobile);
		
		Image mobile = new Image(getDisplay(), new ImageData(this.getClass().getResourceAsStream("/img/handy.gif"))); //$NON-NLS-1$
		cc_mobile.setImage(mobile);
		FontData[] fD_cc_mobile = cc_mobile.getFont().getFontData();
		fD_cc_mobile[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		cc_mobile.setFont(new Font(Display.getCurrent(), fD_cc_mobile[0]));
		
		ClickableCanvas cc_karte = new ClickableCanvas(this, SWT.NATIVE | SWT.RESIZE);
		FormData fd_cc_karte = new FormData();
		fd_cc_karte.left = new FormAttachment(50, 5);
		fd_cc_karte.top = new FormAttachment(40, -20);
		cc_karte.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
				// Nothing to do here
			}
			
			@Override
			public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
				setSelected(BKUs.LOCAL);
			}
			
			@Override
			public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {
				// Nothing to do here
			}
		});
		cc_karte.setLayoutData(fd_cc_karte);
		
		Image karte = new Image(getDisplay(), new ImageData(this.getClass().getResourceAsStream("/img/karte.gif"))); //$NON-NLS-1$
		
		cc_karte.setImage(karte);
		FontData[] fD_cc_karte = cc_mobile.getFont().getFontData();
		fD_cc_karte[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		cc_mobile.setFont(new Font(Display.getCurrent(), fD_cc_karte[0]));
		
		int mobilesize = cc_mobile.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		
		this.btnMobile = new Button(this, SWT.NONE);
		FormData fd_btnMobile = new FormData();
		fd_btnMobile.top = new FormAttachment(cc_mobile, 10);
		//fd_btnMobile.left = new FormAttachment(btn_mobile, 0);
		fd_btnMobile.right = new FormAttachment(50, -5);
		//fd_btnMobile.width = cc_mobile.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		this.btnMobile.setLayoutData(fd_btnMobile);
		this.btnMobile.setText(Messages.getString("bku_selection.mobile")); //$NON-NLS-1$
		this.btnMobile.addSelectionListener(new MobileSelectionListener());
		
		int btnmsize = this.btnMobile.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		
		fd_btnMobile.width = (btnmsize > mobilesize) ? btnmsize : mobilesize;
		
		this.btnCard = new Button(this, SWT.NONE);
		FormData fd_btnCard = new FormData();
		fd_btnCard.top = new FormAttachment(cc_karte, 10);
		//fd_btnMobile.left = new FormAttachment(btn_mobile, 0);
		fd_btnCard.left = new FormAttachment(50, 5);
		int cardsize = cc_karte.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		
		this.btnCard.setLayoutData(fd_btnCard);
		this.btnCard.setText(Messages.getString("bku_selection.card")); //$NON-NLS-1$
		this.btnCard.addSelectionListener(new LocalSelectionListener());
		
		int btncsize = this.btnCard.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		
		fd_btnCard.width = (btncsize > cardsize) ? btncsize : cardsize;
		//this.pack();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.components.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		this.layout(true, true);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		this.btnMobile.setText(Messages.getString("bku_selection.mobile")); //$NON-NLS-1$
		this.btnCard.setText(Messages.getString("bku_selection.card")); //$NON-NLS-1$
	}
}
