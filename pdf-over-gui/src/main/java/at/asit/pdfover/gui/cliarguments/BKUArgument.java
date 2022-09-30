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

import at.asit.pdfover.gui.exceptions.InitializationException;
import at.asit.pdfover.commons.BKUs;
import at.asit.pdfover.commons.Messages;

/**
 * CLI Argument to set the BKU to use
 */
public class BKUArgument extends Argument {
	/**
	 * Constructor
	 */
	public BKUArgument() {
		super(
				new String[] { "-b" }, "argument.help.bku");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * at.asit.pdfover.gui.cliarguments.CLIArgument#handleArgument(java.lang
	 * .String[], int, at.asit.pdfover.gui.workflow.StateMachine,
	 * at.asit.pdfover.gui.cliarguments.ArgumentHandler)
	 */
	@Override
	public int handleArgument(String[] args, int argOffset,
			ArgumentHandler handler)
			throws InitializationException {
		try {
			if (args.length > argOffset + 1) {

				BKUs argumentValue = BKUs.valueOf(args[argOffset + 1]);

				getConfiguration().setDefaultBKUOverlay(argumentValue);

				return argOffset + 1;
			}
		} catch (Exception ex) {
			throw new InitializationException(
					Messages.getString("argument.invalid.bku") + this.getHelpText(), ex);
		}

		throw new InitializationException(
				Messages.getString("argument.invalid.bku") + this.getHelpText(), null);
	}

}
