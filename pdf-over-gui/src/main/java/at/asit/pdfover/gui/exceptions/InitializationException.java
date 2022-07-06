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

/**
 *
 */
public class InitializationException extends PDFOverGUIException {
	/**
	 *
	 */
	private static final long serialVersionUID = 433744698649684034L;

	/**
	 * @param msg
	 * @param cause
	 */
	public InitializationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
