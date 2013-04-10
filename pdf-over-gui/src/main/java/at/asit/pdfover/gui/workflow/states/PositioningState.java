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
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.composites.PositioningComposite;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.signator.Emblem;
import at.asit.pdfover.signator.FileNameEmblem;
import at.asit.pdfover.signator.SignatureParameter;

/**
 * Decides where to position the signature block
 */
public class PositioningState extends State {

	/**
	 * @param stateMachine
	 */
	public PositioningState(StateMachine stateMachine) {
		super(stateMachine);
	}

	/**
	 * SFL4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(PositioningState.class);

	private PositioningComposite positionComposite = null;

	private PositioningComposite getPositioningComposite() throws IOException {
		if (this.positionComposite == null) {
			this.positionComposite =
					this.stateMachine.getGUIProvider().createComposite(PositioningComposite.class, SWT.RESIZE, this);
			log.debug("Displaying " +  this.stateMachine.getStatus().getDocument()); //$NON-NLS-1$
			this.positionComposite.displayDocument(this.stateMachine.getStatus().getDocument());
		}
		// Update possibly changed values
		SignatureParameter param = this.stateMachine.getPDFSigner().getPDFSigner().newParameter();
		Emblem emblem = new FileNameEmblem(this.stateMachine.getConfigProvider().getDefaultEmblem());
		param.setEmblem(emblem);
		this.positionComposite.setPlaceholder(
				param.getPlaceholder(),
				param.getPlaceholderDimension().getWidth(),
				param.getPlaceholderDimension().getHeight(),
				this.stateMachine.getConfigProvider().getPlaceholderTransparency());
		this.positionComposite.requestFocus();

		return this.positionComposite;
	}

	@Override
	public void run() {
		Status status = this.stateMachine.getStatus();
		if (!(status.getPreviousState() instanceof PositioningState) &&
			!(status.getPreviousState() instanceof OpenState))
		{
			status.setSignaturePosition(null);
		}

		if(status.getSignaturePosition() == null) {
			PositioningComposite position = null;
			try {
				position = this.getPositioningComposite();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// FIXME
				e.printStackTrace();
			}
			
			this.stateMachine.getGUIProvider().display(position);
			
			status.setSignaturePosition(position.getPosition());
			
			if(status.getSignaturePosition() == null) {
				return;
			}
		}
		this.setNextState(new BKUSelectionState(this.stateMachine));
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#cleanUp()
	 */
	@Override
	public void cleanUp() {
		if (this.positionComposite != null)
			this.positionComposite.dispose();
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#setMainWindowBehavior()
	 */
	@Override
	public void updateMainWindowBehavior() {
		MainWindowBehavior behavior = this.stateMachine.getStatus().getBehavior();
		behavior.reset();
		behavior.setEnabled(Buttons.CONFIG, true);
		behavior.setEnabled(Buttons.OPEN, true);
		behavior.setActive(Buttons.OPEN, true);
		behavior.setActive(Buttons.POSITION, true);
	}

	@Override
	public String toString()  {
		return this.getClass().getName();
	}
}
