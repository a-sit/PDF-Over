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
package at.asit.pdfover.gui.workflow.states;

//Imports
import java.io.File;

import org.eclipse.swt.SWT;

import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.bku.LocalBKUConnector;
import at.asit.pdfover.commons.BKUs;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.composites.BKUSelectionComposite;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;

/**
 * Decides which BKU to use (preconfigured or let user choose)
 */
public class BKUSelectionState extends State {

	/**
	 * @param stateMachine
	 */
	public BKUSelectionState(StateMachine stateMachine) {
		super(stateMachine);
	}

	private BKUSelectionComposite selectionComposite = null;
	private BKUSelectionComposite getSelectionComposite() {
		if (this.selectionComposite == null) {
			this.selectionComposite =
					getStateMachine().createComposite(BKUSelectionComposite.class, SWT.RESIZE, this);
		}

		return this.selectionComposite;
	}

	private boolean isKeystoreEnabled() {
		if (getStateMachine().configProvider.getKeyStoreEnabled()) {
			File ks = new File(getStateMachine().configProvider.getKeyStoreFile());
			return ks.exists();
		} else
			return false;
	}

	@Override
	public void run() {
		Status status = getStateMachine().status;
		State previousState = status.getPreviousState();

		final boolean hasLocalBKU = LocalBKUConnector.IsAvailable();
		final boolean hasKeystore = isKeystoreEnabled();
		if (
		  (previousState instanceof OpenState) ||
		  (previousState instanceof PositioningState)
		) {
			if (!hasLocalBKU && !hasKeystore)
				status.bku = BKUs.MOBILE;
		} else if (!(previousState instanceof BKUSelectionState)) {
			status.bku = BKUs.NONE;
		}

		if(status.bku == BKUs.NONE) {
			BKUSelectionComposite selection = this.getSelectionComposite();
			selection.setLocalBKUEnabled(hasLocalBKU);
			selection.setKeystoreEnabled(hasKeystore);

			getStateMachine().display(selection);
			selection.layout();

			status.bku = selection.getSelected();

			if(status.bku == BKUs.NONE) {
				return;
			}
		}
		this.setNextState(new PrepareSigningState(getStateMachine()));
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#cleanUp()
	 */
	@Override
	public void cleanUp() {
		if (this.selectionComposite != null)
			this.selectionComposite.dispose();
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#setMainWindowBehavior()
	 */
	@Override
	public void updateMainWindowBehavior() {
		MainWindowBehavior behavior = getStateMachine().status.behavior;
		behavior.reset();
		behavior.setEnabled(Buttons.CONFIG, true);
		behavior.setEnabled(Buttons.OPEN, true);
		behavior.setEnabled(Buttons.POSITION, true);
		behavior.setActive(Buttons.OPEN, true);
		behavior.setActive(Buttons.POSITION, true);
		behavior.setActive(Buttons.SIGN, true);
	}

	@Override
	public String toString()  {
		return this.getClass().getName();
	}
}
