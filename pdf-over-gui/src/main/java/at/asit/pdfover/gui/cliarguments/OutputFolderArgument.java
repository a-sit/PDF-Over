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
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Messages;
import at.asit.pdfover.gui.exceptions.InitializationException;
import at.asit.pdfover.gui.workflow.ConfigManipulator;
import at.asit.pdfover.gui.workflow.StateMachine;

/**
 * 
 */
public class OutputFolderArgument extends CLIArgument {
	/**
	 * Constructor
	 */
	public OutputFolderArgument() {
		super(new String[] {"-o"}, Messages.getString("argument.help.output")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(OutputFolderArgument.class);

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.cliarguments.CLIArgument#handleArgument(java.lang.String[], int, at.asit.pdfover.gui.workflow.StateMachine, at.asit.pdfover.gui.cliarguments.ArgumentHandler)
	 */
	@Override
	public int handleArgument(String[] args, int argOffset,
			StateMachine stateMachine, ArgumentHandler handler)
			throws InitializationException {
		try {
			if (args.length > argOffset + 1) {

				String outputFolder = args[argOffset + 1];
				
				File outputFolderDir = new File(outputFolder);
				
				if(!outputFolderDir.exists()) {
					throw new FileNotFoundException(outputFolder);
				}
				
				if(!outputFolderDir.isDirectory()) {
					throw new IOException(outputFolderDir + Messages.getString("argument.error.output")); //$NON-NLS-1$
				}
				
				ConfigManipulator configManipulator = stateMachine.getConfigManipulator();
				
				configManipulator.setDefaultOutputFolder(outputFolder);
				
				return argOffset + 1;
			}
		} catch (Exception ex) {
			log.error("Output folder argument invalid!", ex); //$NON-NLS-1$
			throw new InitializationException(
					Messages.getString("argument.invalid.output") + this.getHelpText(), ex); //$NON-NLS-1$
		}

		throw new InitializationException(
				Messages.getString("argument.invalid.output") + this.getHelpText(), null); //$NON-NLS-1$
	}

}
