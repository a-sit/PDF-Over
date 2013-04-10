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

import at.asit.pdfover.gui.workflow.states.BKUSelectionState;
import at.asit.pdfover.signator.SignaturePosition;

/**
 * 
 */
public interface Status {
	public void setDocument(File document);
	public File getDocument();

	public void setSignaturePosition(SignaturePosition position);
	public SignaturePosition getSignaturePosition();

	public void setBKU(BKUSelectionState.BKUs bku);
	public BKUSelectionState.BKUs getBKU();
}
