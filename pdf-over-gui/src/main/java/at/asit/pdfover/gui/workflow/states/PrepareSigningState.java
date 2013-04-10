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
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.composites.WaitingComposite;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.states.BKUSelectionState.BKUs;

/**
 * User waiting state, wait for PDF Signator library to prepare document for signing.
 */
public class PrepareSigningState extends State {

	/**
	 * @param stateMachine
	 */
	public PrepareSigningState(StateMachine stateMachine) {
		super(stateMachine);
	}

	/**
	 * Debug background thread
	 */
	private final class DebugSleeperThread implements Runnable {
		
		private StateMachine workflow;
		
		/**
		 * Default constructor
		 * @param work
		 */
		public DebugSleeperThread(final StateMachine work) {
			this.workflow = work;
		}
		
		@Override
		public void run() {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.workflow.InvokeUpdate();
		}
	}

	/**
	 * SFL4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(PrepareSigningState.class);
	
	private WaitingComposite selectionComposite = null;

	private WaitingComposite getSelectionComposite() {
		if (this.selectionComposite == null) {
			this.selectionComposite = new WaitingComposite(
					this.stateMachine.getGUIProvider().getComposite(), SWT.RESIZE, this);
		}

		return this.selectionComposite;
	}
	
	private boolean run = false;
	
	@Override
	public void run() {
		// TODO SHOW BACKGROUND ACTIVITY ....
		WaitingComposite waiting = this.getSelectionComposite();
		
		this.stateMachine.getGUIProvider().display(waiting);
		
		if(!this.run) {
			Thread t = new Thread(new DebugSleeperThread(this.stateMachine));
			this.run = true;
			t.start();
			return;
		}
		
		// WAIT FOR SLREQUEST and dispatch according to BKU selection
		
		if(this.stateMachine.getStatus().getBKU() == BKUs.LOCAL) {
			this.setNextState(new LocalBKUState(this.stateMachine));
		} else if(this.stateMachine.getStatus().getBKU() == BKUs.MOBILE) {
			this.setNextState(new MobileBKUState(this.stateMachine));
		} else {
			log.error("Invalid selected BKU Value \"NONE\" in PrepareSigningState!"); //$NON-NLS-1$
			this.setNextState(new BKUSelectionState(this.stateMachine));
		}
	}
	
	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#setMainWindowBehavior()
	 */
	@Override
	public void updateMainWindowBehavior() {
		MainWindowBehavior behavior = this.stateMachine.getStatus().getBehavior();
		behavior.reset();
		behavior.setActive(Buttons.OPEN, true);
		behavior.setActive(Buttons.POSITION, true);
		behavior.setActive(Buttons.SIGN, true);
	}

	@Override
	public String toString()  {
		return this.getClass().getName();
	}
}
