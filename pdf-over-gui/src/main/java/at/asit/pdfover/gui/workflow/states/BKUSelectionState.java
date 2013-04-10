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

import at.asit.pdfover.gui.components.BKUSelectionComposite;
import at.asit.pdfover.gui.workflow.Workflow;
import at.asit.pdfover.gui.workflow.WorkflowState;

/**
 * Decides which BKU to use (preconfigured or let user choose)
 */
public class BKUSelectionState extends WorkflowState {

	/**
	 * Enumeration of available BKU type
	 */
	public enum BKUS {
		/**
		 * Local bku
		 */
		LOCAL,
		
		/**
		 * Mobile bku
		 */
		MOBILE,
		
		/**
		 * None bku
		 */
		NONE
	}
	
	/**
	 * SFL4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BKUSelectionState.class);
	
	private BKUSelectionComposite selectionComposite = null;

	private BKUSelectionComposite getSelectionComposite(Workflow workflow) {
		if (this.selectionComposite == null) {
			this.selectionComposite = new BKUSelectionComposite(
					workflow.getComposite(), SWT.RESIZE, workflow);
		}

		return this.selectionComposite;
	}
	
	@Override
	public void update(Workflow workflow) {
		
		if(workflow.getSelectedBKU() == BKUS.NONE) {
			BKUSelectionComposite selection = this
					.getSelectionComposite(workflow);

			workflow.setTopControl(selection);
			selection.layout();
			
			workflow.setSelectedBKU(selection.getSelected());
		
			if(workflow.getSelectedBKU() == BKUS.NONE) {
				this.setNextState(this);
				return;
			}
		} 
		this.setNextState(new PrepareSigningState());
	}
	
	@Override
	public String toString()  {
		return "BKUSelectionState";
	}
}
