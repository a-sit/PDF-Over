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
import at.asit.pdfover.gui.exceptions.InvalidPortException;
import at.asit.pdfover.gui.workflow.ConfigManipulator;
import at.asit.pdfover.gui.workflow.StateMachine;

/**
 * 
 */
public class ProxyPortArgument extends CLIArgument {
	/**
	 * Constructor
	 */
	public ProxyPortArgument() {
		super(new String[] {"-proxyport"}, "Sets the proxy port to use. Example: -proxyport <port>"); //$NON-NLS-1$
	}

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(ProxyPortArgument.class);

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.cliarguments.CLIArgument#handleArgument(java.lang.String[], int, at.asit.pdfover.gui.workflow.StateMachine, at.asit.pdfover.gui.cliarguments.ArgumentHandler)
	 */
	@Override
	public int handleArgument(String[] args, int argOffset,
			StateMachine stateMachine, ArgumentHandler handler)
			throws InitializationException {
		try {
			if (args.length > argOffset + 1) {

				String proxyPortString = args[argOffset + 1];
				
				int port = Integer.parseInt(proxyPortString);
				
				if(port <= 0 || port > 0xFFFF) {
					throw new InvalidPortException(port);
				}
				
				ConfigManipulator configManipulator = stateMachine.getConfigManipulator();
				
				configManipulator.setProxyPort(port);
				
				return argOffset + 1;
			}
		} catch (Exception ex) {
			log.error("Proxy port argument invalid!", ex); //$NON-NLS-1$
			throw new InitializationException(
					"Proxy port argument invalid! Use: " + this.getHelpText(), ex);
		}

		throw new InitializationException(
				"Proxy port argument invalid! Use: " + this.getHelpText(), null);
	}

}
