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
package at.asit.pdfover.gui;

//Imports
import java.io.File;

import javax.swing.JOptionPane;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.exceptions.InitializationException;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.utils.SWTLoader;
import at.asit.pdfover.gui.workflow.StateMachineImpl;

/**
 * Main entry point for production
 */
public class Main {

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(Main.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log.debug("Loading SWT libraries"); //$NON-NLS-1$
		try {
			SWTLoader.loadSWT();
		} catch (InitializationException e) {
			log.error("Could not load SWT libraries", e); //$NON-NLS-1$
			JOptionPane.showMessageDialog(null,
					Messages.getString("error.SWTLib"), //$NON-NLS-1$
					Messages.getString("error.TitleFatal"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
		}
		log.info("===== Starting " + Constants.APP_NAME_VERSION + " ====="); //$NON-NLS-1$ //$NON-NLS-2$

		File configDir = new File(Constants.CONFIG_DIRECTORY);
		if(!configDir.exists()) {
			configDir.mkdir();
		}

		File log4j = new File(configDir.getAbsolutePath() + File.separator + Constants.DEFAULT_LOG4J_FILENAME);
		if(log4j.exists()) {
			PropertyConfigurator.configureAndWatch(log4j.getAbsolutePath());
		}

		log.debug("SWT version: " + SWT.getVersion()); //$NON-NLS-1$

		StateMachineImpl stateMachine = new StateMachineImpl(args);

		log.debug("Starting stateMachine ..."); //$NON-NLS-1$
		stateMachine.start();
		log.debug("Ended stateMachine ..."); //$NON-NLS-1$

		// Workaround for remaining AWT-Shutdown thread on OSX
		System.exit(0);
	}
}
