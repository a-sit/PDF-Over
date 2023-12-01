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
 * CLI Argument to show the usage message
 */
@Slf4j
public class OutputFolderArgument extends Argument {
	/**
	 * Constructor
	 */
	public OutputFolderArgument() {
		super(new String[] {"-o"}, "argument.help.output");
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
				String outputFolder = args[argOffset + 1];

//				File outputFolderDir = new File(outputFolder);
//				if(!outputFolderDir.exists()) {
//					throw new FileNotFoundException(outputFolder);
//				}
//				if(!outputFolderDir.isDirectory()) {
//					throw new IOException(outputFolderDir + Messages.getString("argument.error.output"));
//				}

				getConfiguration().setDefaultOutputFolderOverlay(outputFolder);

				return argOffset + 1;
			}
		} catch (Exception ex) {
			log.error("Output folder argument invalid!", ex);
			throw new InitializationException(
					Messages.getString("argument.invalid.output") + this.getHelpText(), ex);
		}

		throw new InitializationException(
				Messages.getString("argument.invalid.output") + this.getHelpText(), null);
	}

}
