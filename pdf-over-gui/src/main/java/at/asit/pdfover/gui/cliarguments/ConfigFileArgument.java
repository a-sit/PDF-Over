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
import lombok.extern.slf4j.Slf4j;
import at.asit.pdfover.commons.Messages;

/**
 * CLI Argument to set the configuration file
 */
@Slf4j
public class ConfigFileArgument extends Argument {
	/**
	 * Constructor
	 */
	public ConfigFileArgument() {
		super(new String[] {"-c"}, "argument.help.config");
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.cliarguments.CLIArgument#handleArgument(java.lang.String[], int, at.asit.pdfover.gui.workflow.StateMachine, at.asit.pdfover.gui.cliarguments.ArgumentHandler)
	 */
	@Override
	public int handleArgument(String[] args, int argOffset,
			ArgumentHandler handler)
			throws InitializationException {
		try {
			if (args.length > argOffset + 1) {

				String configFile = args[argOffset + 1];

				getConfiguration().setConfigurationFileName(configFile);

				return argOffset + 1;
			}
		} catch (Exception ex) {
			log.error("Configuration File Argument invalid!", ex);
			throw new InitializationException(
					Messages.getString("argument.invalid.config") + this.getHelpText(), ex);
		}

		throw new InitializationException(
				Messages.getString("argument.invalid.config") + this.getHelpText(), null);
	}

}
