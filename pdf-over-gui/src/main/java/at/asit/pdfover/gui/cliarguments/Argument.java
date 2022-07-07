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
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;

/**
 * CLI Argument base class
 *
 * Implementing arguments have to be registered in PrepareConfigurationState
 */
public abstract class Argument {

	private String helpTextKey = null;

	private String[] commandOptions = null;

	private StateMachine stateMachine;

	/**
	 * @param commandOptions
	 * @param helpTextKey
	 */
	protected Argument(String[] commandOptions, String helpTextKey) {
		this.helpTextKey = helpTextKey;
		this.commandOptions = commandOptions;
	}

	/**
	 * Set the state machine
	 * Used for configuration overlay manipulator and status
	 * @param stateMachine the state machine
	 */
	protected void setStateMachine(StateMachine stateMachine) {
		this.stateMachine = stateMachine;
	}

	protected ConfigurationManager getConfiguration() {
		return this.stateMachine.configProvider;
	}

	/**
	 * Get the status
	 * @return the status
	 */
	protected Status getStatus() {
		return this.stateMachine.status;
	}

	/**
	 * Set help text key
	 * @param key
	 */
	protected void setHelpTextKey(String key) {
		this.helpTextKey = key;
	}

	/**
	 * Gets help text
	 * @return help text
	 */
	public String getHelpText() {
		return Messages.getString(this.helpTextKey);
	}

	/**
	 * Set the command option in format: -...
	 *
	 * Examples: -h
	 *
	 * @param value
	 */
	protected void setCommandOptions(String[] value) {
		this.commandOptions = value;
	}

	/**
	 * Get the command option
	 *
	 * Examples: -h
	 * @return the command option
	 */
	public String[] getCommandOptions() {
		return this.commandOptions;
	}

	/**
	 * Invokes the argument to set stuff within the stateMachine
	 *
	 * It should return the offset within the args array where the last used argument of this Argument was used.
	 *
	 * Example:
	 * args[] = { "-h", "-b", "LOCAL" }
	 *
	 * Help Argument call:
	 *     offset = 0
	 *     returns 0
	 *
	 * BKU Argument call:
	 *     offset = 1
	 *     returns 2
	 *
	 * @param args
	 * @param argOffset
	 * @param handler
	 * @return returns the argumentOffset ending the section of this Argument
	 * @throws InitializationException
	 */
	public abstract int handleArgument(String[] args, int argOffset, ArgumentHandler handler) throws InitializationException;
}
