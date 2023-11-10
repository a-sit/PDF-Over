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

import javax.annotation.Nonnull;

import org.eclipse.swt.SWT;

import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.composites.OutputComposite;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;

/**
 * Produces the output of the signature process. (save file, open file)
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
			this.outputComposite = getStateMachine()
					.createComposite(OutputComposite.class, SWT.RESIZE, this);

			ConfigurationManager config = getStateMachine().configProvider;
			Status status = getStateMachine().status;

			File tmpDir = new File(Constants.CONFIG_DIRECTORY + File.separator + "tmp");

			if(!tmpDir.exists()) {
				tmpDir.mkdir();
			}

			this.outputComposite.setOutputDir(config.getDefaultOutputFolder());
			this.outputComposite.setSaveFilePostFix(config.getSaveFilePostFix());
			this.outputComposite.setTempDir(tmpDir.getAbsolutePath());
			this.outputComposite.setInputFile(status.document);

			this.outputComposite.setSignedDocument(status.signResult.getSignedDocument());

			// Save signed document
			this.outputComposite.saveDocument();
		}

		return this.outputComposite;
	}

	@Override
	public void run() {
		Status status = getStateMachine().status;

		if (status.signResult == null) {
			ErrorDialog error = new ErrorDialog(getStateMachine().getMainShell(),
					Messages.getString("error.Signatur"), BUTTONS.RETRY_CANCEL);
			if(error.open() == SWT.RETRY) {
				this.setNextState(new PrepareSigningState(getStateMachine()));
			} else {
				this.setNextState(new BKUSelectionState(getStateMachine()));
			}
			return;
		}

		OutputComposite outputComposite = this.getOutputComposite();

		if (outputComposite.getSaveSuccessful()) {
			if (!getStateMachine().status.pendingDocuments.isEmpty()) {
				this.setNextState(new OpenState(getStateMachine()));
				return;
			}
			if (getConfig().getSkipFinish())
				getStateMachine().exit();
		}
		// Display dialog
		getStateMachine().display(outputComposite);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.workflow.states.State#cleanUp()
	 */
	@Override
	public void cleanUp() {

		getStateMachine().status.signResult = null;

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
		MainWindowBehavior behavior = getStateMachine().status.behavior;
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

	public void enqueueNewDocuments(@Nonnull String[] pathStrs) {
		for (String pathStr : pathStrs) {
			if (pathStr == null) continue;
			var path = new File(pathStr);
			var queue = getStateMachine().status.pendingDocuments;
			if (!queue.contains(path))
				queue.add(path);
		}
		if (outputComposite.getSaveSuccessful()) {
			this.setNextState(new OpenState(getStateMachine()));
			this.updateStateMachine();
		}
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
