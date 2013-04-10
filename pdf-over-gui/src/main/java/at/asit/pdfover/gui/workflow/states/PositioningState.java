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

import at.asit.pdfover.gui.components.PositioningComposite;
import at.asit.pdfover.gui.workflow.Workflow;
import at.asit.pdfover.gui.workflow.WorkflowState;

/**
 * Decides where to position the signature block
 */
public class PositioningState extends WorkflowState {

	/**
	 * SFL4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(PositioningState.class);

	private PositioningComposite positionComposite = null;

	private PositioningComposite getPositioningComosite(Workflow workflow) {
		if (this.positionComposite == null) {
			this.positionComposite = new PositioningComposite(
					workflow.getComposite(), SWT.NONE, workflow);
		}

		return this.positionComposite;
	}

	@Override
	public void update(Workflow workflow) {
		
		if(workflow.getParameter().getSignaturePosition() == null) {
			PositioningComposite position = this.getPositioningComosite(workflow);
			
			workflow.setTopControl(position);
			
			workflow.getParameter().setSignaturePosition(position.getPosition());
			
			if(workflow.getParameter().getSignaturePosition() == null) {
				this.setNextState(this);
				return;
			}
		}
		this.setNextState(new BKUSelectionState());
	}

	@Override
	public String toString()  {
		return "PositioningState";
	}
}
