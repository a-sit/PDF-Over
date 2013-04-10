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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.exceptions.InitializationException;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHelper;

/**
 * CLI Argument to provide Mobile BKU phone number to use
 */
public class PhoneNumberArgument extends CLIArgument {
	/**
	 * Constructor
	 */
	public PhoneNumberArgument() {
		super(new String[] {"-n" }, Messages.getString("argument.help.number")); //$NON-NLS-1$ //$NON-NLS-2$
	}


	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(PhoneNumberArgument.class);

	
	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.cliarguments.CLIArgument#handleArgument(java.lang.String[], int, at.asit.pdfover.gui.workflow.StateMachine, at.asit.pdfover.gui.cliarguments.ArgumentHandler)
	 */
	@Override
	public int handleArgument(String[] args, int argOffset,
			ArgumentHandler handler)
			throws InitializationException {

		try {
			if (args.length > argOffset + 1) {

				String number = args[argOffset + 1];

				number = MobileBKUHelper.normalizeMobileNumber(number);
				
				getConfiguration().setDefaultMobileNumberOverlay(number);
				
				return argOffset + 1;
			}
		} catch (Exception ex) {
			log.error("Phone Number Argument invalid!", ex); //$NON-NLS-1$
			throw new InitializationException(
					Messages.getString("argument.invalid.number") + this.getHelpText(), ex); //$NON-NLS-1$
		}

		throw new InitializationException(
				Messages.getString("argument.invalid.number") + this.getHelpText(), null); //$NON-NLS-1$
	}

}
