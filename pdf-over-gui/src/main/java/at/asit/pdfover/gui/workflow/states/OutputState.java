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
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.composites.OutputComposite;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;

/**
 * Procduces the output of the signature process. (save file, open file)
 */
public class OutputState extends State {

	private OutputComposite outputComposite = null;

	/**
	 * @param stateMachine
	 */
	public OutputState(StateMachine stateMachine) {
		super(stateMachine);
	}

	private OutputComposite getOutputComposite() {
		if (this.outputComposite == null) {
			this.outputComposite = this.stateMachine.getGUIProvider()
					.createComposite(OutputComposite.class, SWT.RESIZE, this);
			
			File tmpDir = new File(this.stateMachine.getConfigProvider().getConfigurationDirectory() + "/tmp"); //$NON-NLS-1$
			
			if(!tmpDir.exists()) {
				tmpDir.mkdir();
			}
			
			this.outputComposite.setOutputDir(this.stateMachine.getConfigProvider().getDefaultOutputFolder());
			this.outputComposite.setTempDir(tmpDir.getAbsolutePath());
			this.outputComposite.setInputFile(this.stateMachine.getStatus().getDocument());
		}

		return this.outputComposite;
	}

	@Override
	public void run() {
		Status status = this.stateMachine.getStatus();

		if (status.getSignResult() == null) {
			// TODO
			return;
		}

		OutputComposite outputComposite = this.getOutputComposite();
		outputComposite.setSignedDocument(status.getSignResult()
				.getSignedDocument());

		// Save signed document
		outputComposite.saveDocument();

		// Display dialog
		this.stateMachine.getGUIProvider().display(outputComposite);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.states.State#cleanUp()
	 */
	@Override
	public void cleanUp() {
		
		this.stateMachine.getStatus().setSignResult(null);
		
		if (this.outputComposite != null)
			this.outputComposite.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.states.State#setMainWindowBehavior()
	 */
	@Override
	public void updateMainWindowBehavior() {
		MainWindowBehavior behavior = this.stateMachine.getStatus()
				.getBehavior();
		behavior.reset();
		behavior.setEnabled(Buttons.CONFIG, true);
		behavior.setEnabled(Buttons.OPEN, true);
		behavior.setEnabled(Buttons.POSITION, true);
		behavior.setEnabled(Buttons.SIGN, true);
		behavior.setActive(Buttons.OPEN, true);
		behavior.setActive(Buttons.POSITION, true);
		behavior.setActive(Buttons.SIGN, true);
		behavior.setActive(Buttons.FINAL, true);
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
