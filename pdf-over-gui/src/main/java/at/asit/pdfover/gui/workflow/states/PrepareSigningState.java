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

import at.asit.pdfover.gui.components.WaitingComposite;
import at.asit.pdfover.gui.workflow.Workflow;
import at.asit.pdfover.gui.workflow.WorkflowState;
import at.asit.pdfover.gui.workflow.states.BKUSelectionState.BKUS;

/**
 * User waiting state, wait for PDF Signator library to prepare document for signing.
 */
public class PrepareSigningState extends WorkflowState {

	/**
	 * Debug background thread
	 */
	private final class DebugSleeperThread implements Runnable {
		
		private Workflow workflow;
		
		/**
		 * Default constructor
		 * @param work
		 */
		public DebugSleeperThread(Workflow work) {
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

	private WaitingComposite getSelectionComposite(Workflow workflow) {
		if (this.selectionComposite == null) {
			this.selectionComposite = new WaitingComposite(
					workflow.getComposite(), SWT.RESIZE, workflow);
		}

		return this.selectionComposite;
	}
	
	private boolean run = false;
	
	@Override
	public void update(Workflow workflow) {
		// TODO SHOW BACKGROUND ACTIVITY ....
		WaitingComposite waiting = this.getSelectionComposite(workflow);
		
		workflow.setTopControl(waiting);
		
		if(!this.run) {
			Thread t = new Thread(new DebugSleeperThread(workflow));
			this.run = true;
			t.start();
			return;
		}
		
		// WAIT FOR SLREQUEST and dispatch according to BKU selection
		
		if(workflow.getSelectedBKU() == BKUS.LOCAL) {
			this.setNextState(new LocalBKUState());
		} else if(workflow.getSelectedBKU() == BKUS.MOBILE) {
			this.setNextState(new MobileBKUState());
		} else {
			log.error("Invalid selected BKU Value \"NONE\" in PrepareSigningState!");
			this.setNextState(new BKUSelectionState());
		}
	}
	
	@Override
	public String toString()  {
		return "PrepareSigningState";
	}
	
	
}
