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
import at.asit.pdfover.gui.workflow.ConfigManipulator;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.signator.BKUs;

/**
 * 
 */
public class BKUArgument extends CLIArgument {
	/**
	 * Constructor
	 */
	public BKUArgument() {
		super(
				new String[] { "-b" }, "Select the BKU to use values are: LOCAL, MOBILE (example: -b <option>"); //$NON-NLS-1$
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
			StateMachine stateMachine, ArgumentHandler handler)
			throws InitializationException {
		try {
			if (args.length > argOffset + 1) {

				BKUs argumentValue = BKUs.valueOf(args[argOffset + 1]);

				ConfigManipulator configManipulator = stateMachine.getConfigManipulator();
				
				configManipulator.setDefaultBKU(argumentValue);
				
				return argOffset + 1;
			}
		} catch (Exception ex) {
			throw new InitializationException(
					"BKU Argument invalid! Use: " + this.getHelpText(), ex); //$NON-NLS-1$
		}

		throw new InitializationException(
				"BKU Argument invalid! Use: " + this.getHelpText(), null); //$NON-NLS-1$
	}

}
