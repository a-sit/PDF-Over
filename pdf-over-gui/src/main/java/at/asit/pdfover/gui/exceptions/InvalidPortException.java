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
package at.asit.pdfover.gui.exceptions;

import at.asit.pdfover.gui.Messages;

/**
 * 
 */
public class InvalidPortException extends PDFOverGUIException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4809078091773253962L;
	
		
	/**
	 * Constructor
	 * @param port
	 */
	public InvalidPortException(int port) {
		super(port + Messages.getString("exception.InvalidPort") + 0xffff); //$NON-NLS-1$
	}
}
