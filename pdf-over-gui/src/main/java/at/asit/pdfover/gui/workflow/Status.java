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

import java.io.File;

import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.workflow.states.BKUSelectionState;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.SignaturePosition;

/**
 * Interface for persistent status of state machine
 */
public interface Status {
	/**
	 * Sets the document
	 * @param document the document
	 */
	public void setDocument(File document);
	
	/**
	 * Gets the document
	 * @return the document
	 */
	public File getDocument();

	/**
	 * Sets the signature position
	 * @param position the position
	 */
	public void setSignaturePosition(SignaturePosition position);
	
	/**
	 * Gets the signature position
	 * @return the signature position
	 */
	public SignaturePosition getSignaturePosition();

	/**
	 * Sets the selected BKU
	 * @param bku the selected BKU
	 */
	public void setBKU(BKUSelectionState.BKUs bku);
	
	/**
	 * Gets the selected BKU
	 * @return the selected BKU
	 */
	public BKUSelectionState.BKUs getBKU();
	
	/**
	 * Gets the current state
	 * @return the current state
	 */
	public State getCurrentState();

	/**
	 * Gets the main window behavior
	 * @return the main window behavior
	 */
	public MainWindowBehavior getBehavior();

	/**
	 * Gets the previous State
	 * @return the previous State
	 */
	public State getPreviousState();
}
