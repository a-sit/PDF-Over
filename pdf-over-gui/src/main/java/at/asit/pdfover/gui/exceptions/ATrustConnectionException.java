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

// Imports

import at.asit.pdfover.commons.Messages;

/**
 *
 */
public class ATrustConnectionException extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = -5826910929587650685L;

	/**
	 * Constructor
	 * @param file
	 */
	public ATrustConnectionException() {
		super(Messages.getString("error.ATrustConnection")); //
	}


}
