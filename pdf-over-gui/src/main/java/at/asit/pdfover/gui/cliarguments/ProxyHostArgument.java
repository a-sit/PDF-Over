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
import at.asit.pdfover.commons.Messages;

/**
 * CLI Argument to provide the proxy host
 */
public class ProxyHostArgument extends Argument {
	/**
	 * Constructor
	 */
	public ProxyHostArgument() {
		super(new String[] {"-proxy"}, "argument.help.proxyhost"); // //
	}

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(ProxyHostArgument.class);

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.cliarguments.CLIArgument#handleArgument(java.lang.String[], int, at.asit.pdfover.gui.workflow.StateMachine, at.asit.pdfover.gui.cliarguments.ArgumentHandler)
	 */
	@Override
	public int handleArgument(String[] args, int argOffset,
			ArgumentHandler handler)
			throws InitializationException {
		try {
			if (args.length > argOffset + 1) {

				String proxyHost = args[argOffset + 1];

				getConfiguration().setProxyHostOverlay(proxyHost);

				return argOffset + 1;
			}
		} catch (Exception ex) {
			log.error("Proxy host argument invalid!", ex); //
			throw new InitializationException(
					Messages.getString("argument.invalid.proxyhost") + this.getHelpText(), ex); //
		}

		throw new InitializationException(
				Messages.getString("argument.invalid.proxyhost") + this.getHelpText(), null); //
	}

}
