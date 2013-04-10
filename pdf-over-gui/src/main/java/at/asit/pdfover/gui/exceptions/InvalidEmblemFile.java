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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class InvalidEmblemFile extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5826910929131650685L;
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(InvalidEmblemFile.class);

	/**
	 * Constructor
	 * @param file
	 */
	public InvalidEmblemFile(final File file) {
		super("File: " + file.getAbsolutePath() + " is an invalid emblem file!");
	}
	
	/**
	 * Constructor
	 * @param file
	 */
	public InvalidEmblemFile(final File file, Throwable reason) {
		super("File: " + file.getAbsolutePath() + " is an invalid emblem file!", reason);
	}
}
