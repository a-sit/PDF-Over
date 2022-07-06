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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.exceptions.InitializationException;
import at.asit.pdfover.gui.workflow.StateMachine;

/**
 * Handler to process CLI Arguments
 */
public class ArgumentHandler {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(ArgumentHandler.class);

	private Map<String, Argument> cliArguments = new LinkedHashMap<String, Argument>();

	private StateMachine stateMachine = null;

	private boolean requiredExit = false;

	/**
	 * Constructor
	 *
	 * @param stateMachine
	 */
	public ArgumentHandler(StateMachine stateMachine) {
		this.stateMachine = stateMachine;
	}

	/**
	 * Gets available Arguments
	 *
	 * @return the list of available arguments
	 */
	public Set<Argument> getArguments() {
		return new LinkedHashSet<Argument>(this.cliArguments.values());
	}

	/**
	 * Adds a CLIArgument to the handler
	 *
	 * @param cliArgument the CLIArgument subclass to add
	 */
	public void addCLIArgument(Class<? extends Argument> cliArgument) {

		Argument arg;
		try {
			arg = cliArgument.newInstance();

			arg.setStateMachine(this.stateMachine);

			String[] commandOptions = arg.getCommandOptions();

			if (commandOptions == null) {
				return;
			}

			for (int i = 0; i < commandOptions.length; i++) {
				this.cliArguments.put(commandOptions[i], arg);
			}
		} catch (Exception e) {
			log.error("Error instantiating CLI argument" , e);
		}
	}

	/**
	 * Handle CLI Arguments
	 *
	 * @param args
	 * @throws InitializationException
	 */
	public void handleArguments(String[] args) throws InitializationException {

		for (int i = 0; i < args.length; i++) {
			if (this.cliArguments.containsKey(args[i])) {
				i = this.cliArguments.get(args[i]).handleArgument(args, i, this);
			} else {
				// Assume we got the document we want to sign
				if (this.cliArguments.containsKey("-i")) {
					i = this.cliArguments.get("-i").handleArgument(args, i-1, this);
				}
			}
		}
	}

	/**
	 * Set by a cli argument if it wants the program to exit
	 *
	 * @param requireExit
	 */
	public void setRequireExit(boolean requireExit) {
		this.requiredExit = requireExit;
	}

	/**
	 * Checks if one argument required the program to exit immediately
	 *
	 * @return true or false
	 */
	public boolean doesRequireExit() {
		return this.requiredExit;
	}
}
