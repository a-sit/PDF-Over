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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import at.asit.pdfover.commons.BKUs;
import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.controls.ClickableCanvas;
import at.asit.pdfover.gui.utils.SWTUtils;
import at.asit.pdfover.gui.workflow.states.State;

/**
 * Composite for BKU selection
 */
public class BKUSelectionComposite extends StateComposite {

	private BKUs selected = BKUs.NONE;

	private ClickableCanvas cc_mobile;
	private ClickableCanvas cc_karte;
	private Button btnMobile;
	private Button btnCard;
	private Button btnKS = null;

	public BKUs getSelected() { return this.selected; }

	public void setSelected(final BKUs selected) {
		this.selected = selected;
		this.state.updateStateMachine();
	}

	public void setLocalBKUEnabled(boolean state) {
		this.btnCard.setEnabled(state);
		this.cc_karte.setEnabled(false);
	}

	/**
	 * Sets whether keystore option is enabled
	 * @param enabled
	 */
	public void setKeystoreEnabled(boolean enabled) {
		if (enabled) {
			this.btnKS = new Button(this, SWT.NONE);
			SWTUtils.anchor(this.btnKS).top(this.btnCard, 10).left(this.btnMobile, 0, SWT.LEFT).right(this.btnCard, 0, SWT.RIGHT);
			SWTUtils.addSelectionListener(btnKS, () -> { setSelected(BKUs.KS); });

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

		this.cc_mobile = new ClickableCanvas(this, SWT.NATIVE | SWT.RESIZE);
		SWTUtils.anchor(cc_mobile).right(50, -5).top(40, -20);
		Image mobile = new Image(getDisplay(), new ImageData(this.getClass().getResourceAsStream(Constants.RES_IMG_MOBILE)));
		cc_mobile.setImage(mobile);
		SWTUtils.setFontHeight(cc_mobile, Constants.TEXT_SIZE_BUTTON);
		SWTUtils.addMouseDownListener(cc_mobile, () -> { setSelected(BKUs.MOBILE); });

		this.cc_karte = new ClickableCanvas(this, SWT.NATIVE | SWT.RESIZE);
		SWTUtils.anchor(cc_karte).left(50, 5).top(40, -20);
		Image karte = new Image(getDisplay(), this.getClass().getResourceAsStream(Constants.RES_IMG_CARD));
		cc_karte.setImage(karte);
		SWTUtils.setFontHeight(cc_karte, Constants.TEXT_SIZE_BUTTON);
		SWTUtils.addMouseDownListener(cc_karte, () -> { setSelected(BKUs.LOCAL); });


		this.btnMobile = new Button(this, SWT.NONE);
		SWTUtils.anchor(btnMobile).top(cc_mobile, 10).right(50,-5);
		int mobilesize = cc_mobile.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		int btnmsize = this.btnMobile.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		SWTUtils.reanchor(btnMobile).width(Math.max(btnmsize, mobilesize));
		SWTUtils.addSelectionListener(btnMobile, () -> { setSelected(BKUs.MOBILE); });

		this.btnCard = new Button(this, SWT.NONE);
		SWTUtils.anchor(btnCard).top(cc_karte, 10).left(50,5);
		int cardsize = cc_karte.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		int btncsize = this.btnCard.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		SWTUtils.reanchor(btnCard).width(Math.max(btncsize, cardsize));
		SWTUtils.addSelectionListener(btnCard, () -> { setSelected(BKUs.LOCAL); });

		reloadResources();
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
