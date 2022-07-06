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
import java.io.IOException;

import javax.swing.JOptionPane;

import at.asit.pdfover.commons.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.workflow.StateMachineImpl;
import iaik.security.provider.IAIK;

/**
 * Main entry point for production
 */
public class Main {

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(Main.class);
//	private static URL url=null;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		try {
			log.info("This is " + Constants.APP_NAME_VERSION + ", " +
			  "running on " + System.getProperty("os.arch") + " " + System.getProperty("os.name") + ", " +
			  "powered by "+ System.getProperty("java.vendor") + " Java " + System.getProperty("java.version") + ".");
			File configDir = new File(Constants.CONFIG_DIRECTORY);

			if (!configDir.exists()) {
				configDir.mkdir();
			}

			// force loading the IAIK JCE
			IAIK.addAsProvider();

			// force keystore type (Adoptium JRE 17 still ships with JKS)
			System.setProperty("javax.net.ssl.trustStoreType", "jks");

			StateMachineImpl stateMachine = new StateMachineImpl(args);

			log.debug("Starting stateMachine ...");
			stateMachine.start();


			log.debug("Ended stateMachine ...");
		}
		catch (Throwable e) {

			log.error("Unhandled error", e);

			JOptionPane.showMessageDialog(null,
					"Error occured " + e.getMessage(),
					null, JOptionPane.ERROR_MESSAGE);
		}

		// Workaround for remaining AWT-Shutdown thread on OSX
		System.exit(0);
	}


}
