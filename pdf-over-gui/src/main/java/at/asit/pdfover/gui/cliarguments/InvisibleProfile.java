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

import at.asit.pdfover.gui.exceptions.InitializationException;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.signer.pdfas.PdfAs4SignatureParameter;
import lombok.extern.slf4j.Slf4j;

/**
 * CLI Argument to set the visibility of signature
 */
@Slf4j
public class InvisibleProfile extends Argument {
	/**
	 * Constructor
	 */
	public InvisibleProfile() {
		super(new String[] {"-v"}, "argument.help.vis");
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfovewr.gui.cliarguments.CLIArgument#handleArgument(java.lang.String[], int, at.asit.pdfover.gui.workflow.StateMachine, at.asit.pdfover.gui.cliarguments.ArgumentHandler)
	 */
	@Override
	public int handleArgument(String[] args, int argOffset,
			ArgumentHandler handler)
			throws InitializationException {
		try {
			log.info("Set Profile Invisible");

			if (args.length > argOffset + 1) {
				// TODO: this is a colossal monumental terrible hack
				PdfAs4SignatureParameter.PROFILE_VISIBILITY=false;
				log.debug("We are setting the profile visibility to false");
				return argOffset + 1;
			}

		} catch (Exception ex) {
			log.error("Signature Profile Visibilty Error", ex);
			throw new InitializationException(
					Messages.getString("argument.invalid.vis") + this.getHelpText(), ex);
		}

		throw new InitializationException(
				Messages.getString("argument.invalid.vis") + this.getHelpText(), null);
	}

}
