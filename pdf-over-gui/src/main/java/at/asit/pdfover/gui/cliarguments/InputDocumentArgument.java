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
package at.asit.pdfover.gui.cliarguments;

// Imports
import java.io.File;
import java.io.FileNotFoundException;

import at.asit.pdfover.gui.exceptions.InitializationException;
import lombok.extern.slf4j.Slf4j;
import at.asit.pdfover.commons.Messages;

/**
 * CLI Argument to set the input document to sign
 */
@Slf4j
public class InputDocumentArgument extends Argument {
	/**
	 * Constructor
	 */
	public InputDocumentArgument() {
		super(new String[] {"-i"}, "argument.help.input");
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfovewr.gui.cliarguments.CLIArgument#handleArgument(java.lang.String[], int, at.asit.pdfover.gui.workflow.StateMachine, at.asit.pdfover.gui.cliarguments.ArgumentHandler)
	 */
	@Override
	public int handleArgument(String[] args, int argOffset,
			ArgumentHandler handler)
			throws InitializationException {
		try {
			if (args.length > argOffset + 1) {

				String signatureDocument = args[argOffset + 1];

				File signatureDocumentFile = new File(signatureDocument);

				if(!signatureDocumentFile.exists()) {
					throw new FileNotFoundException(signatureDocument);
				}

				var status = getStatus();
				if (status.document == null)
					status.document = signatureDocumentFile;
				else
					status.pendingDocuments.add(signatureDocumentFile);

				return argOffset + 1;
			}
		} catch (Exception ex) {
			log.error("Document to sign argument invalid!", ex);
			throw new InitializationException(
					Messages.getString("argument.invalid.input") + this.getHelpText(), ex);
		}

		throw new InitializationException(
				Messages.getString("argument.invalid.input") + this.getHelpText(), null);
	}

}
