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
import java.util.Iterator;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import at.asit.pdfover.gui.exceptions.InitializationException;
import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;

/**
 * CLI Argument to show the useage message
 */
public class HelpArgument extends Argument {

	/**
	 * Constructor
	 */
	public HelpArgument() {
		super(new String[] {"-h", "-?", "--help" },
				"argument.help.help");
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.cliarguments.CLIArgument#handleArgument(java.lang.String[], int, at.asit.pdfover.gui.workflow.StateMachine, at.asit.pdfover.gui.cliarguments.ArgumentHandler)
	 */
	@Override
	public int handleArgument(String[] args, int argOffset,
			ArgumentHandler handler)
			throws InitializationException {
		Set<Argument> arguments = handler.getArguments();

		Iterator<Argument> argumentIterator = arguments.iterator();

		StringBuilder message = new StringBuilder(Messages.getString("argument.info.help"));
		message.append("\n");

		while(argumentIterator.hasNext()) {
			Argument argument = argumentIterator.next();

			for (int i = 0; i < argument.getCommandOptions().length; i++) {
				message.append(argument.getCommandOptions()[i]);

				if(i < argument.getCommandOptions().length -1) {
					message.append(", ");
				}
			}

			message.append(":\n");
			boolean first = true;
			for (String line : argument.getHelpText().split("\\.\\s+")) {
				message.append("  ");
				if (first) first = false; else message.append("  ");
				message.append(line);
				message.append("\n");
			}
		}

		String msg = message.toString();
		System.out.println(msg);
		
		Display display = Display.getCurrent();
		if (display != null) {
			MessageBox box = new MessageBox(new Shell(display), SWT.ICON_INFORMATION | SWT.OK);
			box.setText(Constants.APP_NAME_VERSION);
			box.setMessage(msg);
			box.open();
		}

		handler.setRequireExit(true);

		return argOffset;
	}

}
