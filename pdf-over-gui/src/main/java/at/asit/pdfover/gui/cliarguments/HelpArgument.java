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

import at.asit.pdfover.gui.exceptions.InitializationException;
import at.asit.pdfover.gui.workflow.StateMachine;

/**
 * 
 */
public class HelpArgument extends CLIArgument {
	
	/**
	 * Constructor
	 */
	public HelpArgument() {
		super(new String[] {"-h", "-?" }, "shows this help message"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.cliarguments.CLIArgument#handleArgument(java.lang.String[], int, at.asit.pdfover.gui.workflow.StateMachine, at.asit.pdfover.gui.cliarguments.ArgumentHandler)
	 */
	@Override
	public int handleArgument(String[] args, int argOffset,
			StateMachine stateMachine, ArgumentHandler handler)
			throws InitializationException {
		Set<CLIArgument> arguments = handler.getArguments();
		
		Iterator<CLIArgument> argumentIterator = arguments.iterator();
		
		System.out.println("The following options are available"); //$NON-NLS-1$
		
		while(argumentIterator.hasNext()) {
			CLIArgument agrument = argumentIterator.next();
			StringBuilder sb = new StringBuilder();
			
			for(int i = 0; i < agrument.getCommandOptions().length; i++) {
				sb.append(agrument.getCommandOptions()[i]);
				
				if(i < agrument.getCommandOptions().length -1) {
					sb.append(", "); //$NON-NLS-1$
				}
			}
			
			System.out.println(sb.toString() + ":"); //$NON-NLS-1$
			System.out.println("\t" + agrument.getHelpText()); //$NON-NLS-1$
		}
		
		handler.setRequireExit(true);
		
		return argOffset;
	}

}
