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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.BKUs;
import at.asit.pdfover.signator.SignResult;
import at.asit.pdfover.signator.SignaturePosition;
import at.asit.pdfover.signator.SigningState;

public class Status {
	private static final Logger log = LoggerFactory.getLogger(Status.class);

	private File document = null;

	private SignaturePosition signaturePosition = null;

	private BKUs bku = BKUs.NONE;

	private State currentState = null;

	private State previousState = null;

	private SigningState signingState = null;

	private SignResult signResult = null;

	private MainWindowBehavior behavior;

	private boolean searchForPlacehoderSignature = false;

	public Status() {
		this.behavior = new MainWindowBehavior();
	}

	public State getCurrentState() {
		return this.currentState;
	}

	public void setCurrentState(State currentState) {
		//if (this.previousState == this.currentState)
		//	log.error("Changing to same state? " + this.currentState);

		if (this.previousState != this.currentState)
		{
			//Reference to previous state will be lost - perform cleanup
			log.debug("Changing from " + this.currentState + " to " + currentState); //
			log.debug("Cleaning up " + this.previousState);
			this.previousState.cleanUp();
		}

		this.previousState = this.currentState;
		this.currentState = currentState;
	}

	public State getPreviousState() {
		return this.previousState;
	}

	public void setDocument(File document) {
		this.document = document;
	}

	public File getDocument() {
		return this.document;
	}

	public void setSignaturePosition(SignaturePosition position) {
		this.signaturePosition = position;
	}

	public SignaturePosition getSignaturePosition() {
		return this.signaturePosition;
	}

	public void setBKU(BKUs bku) {
		this.bku = bku;
	}

	public BKUs getBKU() {
		return this.bku;
	}

	public MainWindowBehavior getBehavior() {
		return this.behavior;
	}

	public SigningState getSigningState() {
		return this.signingState;
	}

	public void setSigningState(SigningState state) {
		this.signingState = state;
	}

	public void setSignResult(SignResult signResult) {
		this.signResult = signResult;
	}

	public SignResult getSignResult() {
		return this.signResult;
	}

	public boolean isSearchForPlaceholderSignature() {
		return this.searchForPlacehoderSignature;
	}

	public void setSearchForPlaceholderSignature(boolean value) {
		this.searchForPlacehoderSignature = value;
	}
}
