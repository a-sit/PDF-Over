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

import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.composites.DataSourceSelectComposite;
import at.asit.pdfover.gui.workflow.ConfigProvider;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;

/**
 * Selects the data source for the signature process.
 */
public class OpenState extends State {

	/**
	 * @param stateMachine
	 */
	public OpenState(StateMachine stateMachine) {
		super(stateMachine);
	}

	/**
	 * SFL4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(OpenState.class);

	private DataSourceSelectComposite selectionComposite = null;

	private DataSourceSelectComposite getSelectionComposite() {
		if (this.selectionComposite == null) {
			this.selectionComposite =
					this.stateMachine.getGUIProvider().createComposite(DataSourceSelectComposite.class, SWT.RESIZE, this);
		}

		return this.selectionComposite;
	}

	@Override
	public void run() {
		Status status = this.stateMachine.getStatus();
		if (!(status.getPreviousState() instanceof PrepareConfigurationState) &&
			!(status.getPreviousState() instanceof OpenState))
		{
			ConfigProvider config = this.stateMachine.getConfigProvider();
			status.setBKU(config.getDefaultBKU());
			status.setDocument(null);
			status.setSignaturePosition(config.getDefaultSignaturePosition());
		}

		if (status.getDocument() == null) {
			DataSourceSelectComposite selection = this
					.getSelectionComposite();

			this.stateMachine.getGUIProvider().display(selection);
			selection.layout();

			status.setDocument(selection.getSelected());

			if (status.getDocument() == null) {
				// Not selected yet
				return;
			} 
		}
		log.debug("Got Datasource: " + this.stateMachine.getStatus().getDocument().getAbsolutePath()); //$NON-NLS-1$
		this.setNextState(new PositioningState(this.stateMachine));
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
		MainWindowBehavior behavior = this.stateMachine.getStatus().getBehavior();
		behavior.reset();
		behavior.setEnabled(Buttons.CONFIG, true);
		behavior.setActive(Buttons.OPEN, true);
	}

	@Override
	public String toString()  {
		return this.getClass().getName();
	}
}
