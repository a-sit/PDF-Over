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

import at.asit.pdfover.commons.Messages;

/**
 *
 */
public class CantLoadKeystoreException extends ResumableException {
	/**
	 *
	 */
	private static final long serialVersionUID = 7554121273052104624L;

	/**
	 * @param resumeIndex The resume Index
	 * @param cause The original exception
	 */
	public CantLoadKeystoreException(Throwable cause, int resumeIndex) {
		super(Messages.getString("error.KeyStore"), cause, resumeIndex); //
	}
}
