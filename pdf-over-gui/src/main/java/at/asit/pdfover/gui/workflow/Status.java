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
package at.asit.pdfover.gui.workflow;

// Imports
import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;

import at.asit.pdfover.commons.BKUs;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signer.SignResult;
import at.asit.pdfover.signer.SignaturePosition;
import at.asit.pdfover.signer.pdfas.PdfAs4SigningState;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Status {

	public File document = null;
	public Queue<File> pendingDocuments = new ArrayDeque<>();

	public SignaturePosition signaturePosition = null;

	public BKUs bku = BKUs.NONE;

	private State currentState = null;

	private State previousState = null;

	public PdfAs4SigningState signingState = null;

	public SignResult signResult = null;

	public final MainWindowBehavior behavior;

	public boolean searchForPlaceholderSignature = false;
	public String placeholderId = null;

	public Status() {
		this.behavior = new MainWindowBehavior();
	}

	public State getCurrentState() {
		return this.currentState;
	}

	public void setCurrentState(State newState) {
		log.debug("Changing from " + currentState + " to " + newState);

		if ((previousState != null) && (previousState != currentState) && (previousState != newState))
		{
			// Reference to previous state will be lost - perform cleanup
			log.debug("Cleaning up " + previousState);
			previousState.cleanUp();
		}

		this.previousState = currentState;
		this.currentState = newState;
	}

	public State getPreviousState() {
		return this.previousState;
	}
}
