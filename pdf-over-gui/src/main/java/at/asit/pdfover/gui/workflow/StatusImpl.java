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
import at.asit.pdfover.gui.workflow.states.BKUSelectionState.BKUs;
import at.asit.pdfover.signator.SignaturePosition;

/**
 * 
 */
public class StatusImpl implements Status {
	/**
	 * SLF4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(StatusImpl.class);

	private File document = null;
	
	private SignaturePosition signaturePosition = null;
	
	private BKUs bku = BKUs.NONE;
	
	private State currentState = null;

	private MainWindowBehavior behavior;

	/**
	 * Constructor
	 */
	public StatusImpl() {
		this.behavior = new MainWindowBehavior();
	}

	@Override
	public State getCurrentState() {
		return this.currentState;
	}

	/**
	 * Sets the current state
	 * @param currentState
	 */
	public void setCurrentState(State currentState) {
		this.currentState = currentState;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.Status#setDocument(java.io.File)
	 */
	@Override
	public void setDocument(File document) {
		this.document = document;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.Status#getDocument()
	 */
	@Override
	public File getDocument() {
		return this.document;
	}
	
	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.Status#setSignaturePosition(at.asit.pdfover.signator.SignaturePosition)
	 */
	@Override
	public void setSignaturePosition(SignaturePosition position) {
		this.signaturePosition = position;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.Status#getSignaturePosition()
	 */
	@Override
	public SignaturePosition getSignaturePosition() {
		return this.signaturePosition;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.Status#setBKU(at.asit.pdfover.gui.workflow.states.BKUSelectionState.BKUs)
	 */
	@Override
	public void setBKU(BKUs bku) {
		this.bku = bku;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.Status#getBKU()
	 */
	@Override
	public BKUs getBKU() {
		return this.bku;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.Status#getBehavior()
	 */
	@Override
	public MainWindowBehavior getBehavior() {
		return behavior;
	}
}
