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

import at.asit.pdfover.gui.Messages;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.BKUs;

/**
 * Composite for BKU selection
 */
public class BKUSelectionComposite extends StateComposite {
	
	
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
		
		
		Button btn_mobile = new Button(this, SWT.NATIVE | SWT.RESIZE);
		btn_mobile.setText(Messages.getString("bku_selection.mobile")); //$NON-NLS-1$
		//Point mobile_size = btn_mobile.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		FormData fd_btn_mobile = new FormData();
		//fd_btn_mobile.left = new FormAttachment(40, 0);
		fd_btn_mobile.right = new FormAttachment(50, -5);
		fd_btn_mobile.top = new FormAttachment(40, 0);
		//fd_btn_mobile.bottom = new FormAttachment(55, 0);
		btn_mobile.setLayoutData(fd_btn_mobile);
		btn_mobile.addSelectionListener(new MobileSelectionListener());

		Image mobile = new Image(getDisplay(), new ImageData(this.getClass().getResourceAsStream("/img/handy.gif"))); //$NON-NLS-1$
		
		btn_mobile.setImage(mobile);
		
		FontData[] fD_btn_mobile = btn_mobile.getFont().getFontData();
		fD_btn_mobile[0].setHeight(TEXT_SIZE_BUTTON);
		btn_mobile.setFont(new Font(Display.getCurrent(), fD_btn_mobile[0]));
		
		Button btn_card = new Button(this, SWT.NATIVE | SWT.RESIZE);
		btn_card.setText(Messages.getString("bku_selection.card")); //$NON-NLS-1$
		//Point card_size = btn_card.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		FormData fd_btn_card = new FormData();
		fd_btn_card.left = new FormAttachment(50, 5);
		//fd_btn_card.right = new FormAttachment(60, 0);
		fd_btn_card.top = new FormAttachment(40, 0);
		//fd_btn_card.bottom = new FormAttachment(55, 0);
		btn_card.setLayoutData(fd_btn_card);
		btn_card.addSelectionListener(new LocalSelectionListener());
		
		FontData[] fD_btn_card = btn_card.getFont().getFontData();
		fD_btn_card[0].setHeight(TEXT_SIZE_BUTTON);
		btn_card.setFont(new Font(Display.getCurrent(), fD_btn_card[0]));
		
		Image karte = new Image(getDisplay(), new ImageData(this.getClass().getResourceAsStream("/img/karte.gif"))); //$NON-NLS-1$
		
		btn_card.setImage(karte);
		
		this.pack();
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

}
