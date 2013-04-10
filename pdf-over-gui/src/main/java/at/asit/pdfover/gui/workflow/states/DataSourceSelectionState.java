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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.components.DataSourceSelectComposite;
import at.asit.pdfover.gui.workflow.Workflow;
import at.asit.pdfover.gui.workflow.WorkflowState;

/**
 * Selects the data source for the signature process.
 */
public class DataSourceSelectionState extends WorkflowState {

	/**
	 * SFL4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DataSourceSelectionState.class);
	
	private DataSourceSelectComposite selectionComposite = null;
	
	private DataSourceSelectComposite getSelectionComposite(Workflow workflow) {
		if(this.selectionComposite == null) {
			this.selectionComposite = new DataSourceSelectComposite(workflow.getComposite(), SWT.RESIZE, workflow);
		}
		
		return this.selectionComposite;
	}
	
	@Override
	public void update(Workflow workflow) {
		DataSourceSelectComposite selection = this.getSelectionComposite(workflow);
		
		workflow.setTopControl(selection);
		selection.layout();
		
		if(selection.isPress()) {
			this.setNextState(new PositioningState());
		} else {
			this.setNextState(this);
		}
	}

	private void recursiveHide(Control ctrl) {
		if(ctrl instanceof Composite) {
			Composite comp = (Composite)ctrl;
			Control[] childs = comp.getChildren();
			for(int i = 0; i < childs.length; i++) {
				this.recursiveHide(childs[i]);
			}
		}
		ctrl.setVisible(false);
	}
	
	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.WorkflowState#hideGUI()
	 */
	@Override
	public void hideGUI() {
		/*if(this.selectionComposite != null) {
			Shell shell = this.selectionComposite.getShell();
			Composite comp = this.selectionComposite.getParent();
			recursiveHide(this.selectionComposite);
			this.selectionComposite.layout();
			//this.selectionComposite.setVisible(false);
			comp.layout();
			//shell.pack(true);
			shell.layout(true);
		}*/
	}

}
