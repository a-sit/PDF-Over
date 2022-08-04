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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.controls.ClickableCanvas;
import at.asit.pdfover.gui.utils.SWTUtils;
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
			log.debug("Setting BKU to LOCAL");
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
			log.debug("Setting BKU to MOBILE");
			setSelected(BKUs.MOBILE);
		}
	}

	/**
	 * Listener for keystore selection
	 */
	private final class KSSelectionListener extends SelectionAdapter {
		/**
		 * Empty constructor
		 */
		public KSSelectionListener() {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			log.debug("Setting BKU to KS");
			setSelected(BKUs.KS);
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

	private Button btnKS = null;

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
	 * Sets whether keystore option is enabled
	 * @param enabled
	 */
	public void setKeystoreEnabled(boolean enabled) {
		if (enabled) {
			this.btnKS = new Button(this, SWT.NONE);
			SWTUtils.anchor(this.btnKS).top(this.btnCard, 10).left(this.btnMobile, 0, SWT.LEFT).right(this.btnCard, 0, SWT.RIGHT).set();
			this.btnKS.addSelectionListener(new KSSelectionListener());

			reloadResources();
		} else if (this.btnKS != null) {
			this.btnKS.dispose();
			this.btnKS = null;
		}
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
		SWTUtils.anchor(cc_mobile).right(50, -5).top(40, -20).set();
		Image mobile = new Image(getDisplay(), new ImageData(this.getClass().getResourceAsStream(Constants.RES_IMG_MOBILE)));
		cc_mobile.setImage(mobile);
		SWTUtils.setFontHeight(cc_mobile, Constants.TEXT_SIZE_BUTTON);

		cc_mobile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
				setSelected(BKUs.MOBILE);
			}
		});

		ClickableCanvas cc_karte = new ClickableCanvas(this, SWT.NATIVE | SWT.RESIZE);
		SWTUtils.anchor(cc_karte).left(50, 5).top(40, -20).set();
		Image karte = new Image(getDisplay(), new ImageData(this.getClass().getResourceAsStream(Constants.RES_IMG_CARD)));
		cc_karte.setImage(karte);
		SWTUtils.setFontHeight(cc_karte, Constants.TEXT_SIZE_BUTTON);

		cc_karte.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
				setSelected(BKUs.LOCAL);
			}
		});

		int mobilesize = cc_mobile.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

		this.btnMobile = new Button(this, SWT.NONE);
		FormData fd_btnMobile = new FormData();
		fd_btnMobile.top = new FormAttachment(cc_mobile, 10);
		fd_btnMobile.right = new FormAttachment(50, -5);
		this.btnMobile.setLayoutData(fd_btnMobile);
		this.btnMobile.addSelectionListener(new MobileSelectionListener());

		int btnmsize = this.btnMobile.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

		fd_btnMobile.width = Math.max(btnmsize, mobilesize);

		this.btnCard = new Button(this, SWT.NONE);
		FormData fd_btnCard = new FormData();
		fd_btnCard.top = new FormAttachment(cc_karte, 10);
		fd_btnCard.left = new FormAttachment(50, 5);
		int cardsize = cc_karte.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

		this.btnCard.setLayoutData(fd_btnCard);
		this.btnCard.addSelectionListener(new LocalSelectionListener());

		int btncsize = this.btnCard.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

		fd_btnCard.width = Math.max(btncsize, cardsize);

		reloadResources();
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
		SWTUtils.setLocalizedText(btnMobile, "bku_selection.mobile");
		SWTUtils.setLocalizedText(btnCard, "bku_selection.card");
		if (this.btnKS != null)
			SWTUtils.setLocalizedText(btnKS, "bku_selection.ks");
	}
}
