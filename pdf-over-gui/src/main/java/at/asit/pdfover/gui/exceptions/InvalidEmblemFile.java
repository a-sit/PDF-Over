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
import java.io.File;

import at.asit.pdfover.commons.Messages;

/**
 *
 */
public class InvalidEmblemFile extends PDFOverGUIException {
	/**
	 *
	 */
	private static final long serialVersionUID = -5826910929131650685L;

	/**
	 * Constructor
	 * @param file
	 */
	public InvalidEmblemFile(final File file) {
		super(String.format(Messages.getString("exception.InvalidEmblemFile"), file.getAbsolutePath()));
	}

	/**
	 * Constructor
	 * @param file
	 * @param reason
	 */
	public InvalidEmblemFile(final File file, Throwable reason) {
		super(String.format(Messages.getString("exception.InvalidEmblemFile"), file.getAbsolutePath()), reason);
	}
}
