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
package at.asit.pdfover.gui.workflow.states;

//Imports
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.cliarguments.ArgumentHandler;
import at.asit.pdfover.gui.cliarguments.BKUArgument;
import at.asit.pdfover.gui.cliarguments.ConfigFileArgument;
import at.asit.pdfover.gui.cliarguments.HelpArgument;
import at.asit.pdfover.gui.cliarguments.PhoneNumberArgument;
import at.asit.pdfover.gui.exceptions.InitializationException;
import at.asit.pdfover.gui.workflow.ConfigManipulator;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.signator.BKUs;
import at.asit.pdfover.signator.Signator;
import at.asit.pdfover.signator.SignaturePosition;

/**
 * Starting state of workflow proccess
 * 
 * Reads configuration, command arguments and initializes configured variables
 */
public class PrepareConfigurationState extends State {

	/**
	 * Regex for parsing signature position
	 */
	public static final String SIGN_POS_REGEX = "(x=(\\d\\.?\\d?);y=(\\d\\.?\\d?);p=(\\d))|(auto)|(x=(\\d\\.?\\d?);y=(\\d\\.?\\d?))"; //$NON-NLS-1$

	/**
	 * @param stateMachine
	 */
	public PrepareConfigurationState(StateMachine stateMachine) {
		super(stateMachine);
		this.handler = new ArgumentHandler(this.stateMachine);
		this.handler.addCLIArgument(new HelpArgument());
		this.handler.addCLIArgument(new BKUArgument());
		this.handler.addCLIArgument(new PhoneNumberArgument());

		// adding config file argument to this handler so it appears in help
		this.handler.addCLIArgument(new ConfigFileArgument());

		this.configFilehandler = new ArgumentHandler(this.stateMachine);
		this.configFilehandler.addCLIArgument(new ConfigFileArgument());
	}

	private ArgumentHandler handler;

	private ArgumentHandler configFilehandler;

	/**
	 * SFL4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(PrepareConfigurationState.class);

	private void initializeFromConfigurationFile(String filename)
			throws InitializationException {
		try {
			Properties config = new Properties();

			try {
				config.load(new FileInputStream(filename));
			} catch (FileNotFoundException ex) {
				if (filename.equals(ConfigManipulator.DEFAULT_CONFIG_FILE)) {
					// we only check for resource config file if it is the
					// default value!
					try {
						InputStream is = this.getClass().getResourceAsStream(
								"/" + filename); //$NON-NLS-1$
						config.load(is);
					} catch (Exception eex) {
						throw ex;
					}
				} else {
					throw ex;
				}
			}

			// Load ok ...
			ConfigManipulator configManipulator = this.stateMachine
					.getConfigManipulator();

			// Set Emblem
			configManipulator.setDefaultEmblem(config
					.getProperty(ConfigManipulator.EMBLEM_CONFIG));

			// Set Mobile Phone Number
			configManipulator.setDefaultMobileNumber(config
					.getProperty(ConfigManipulator.MOBILE_NUMBER_CONFIG));

			// Set Proxy Host
			configManipulator.setProxyHost(config
					.getProperty(ConfigManipulator.PROXY_HOST_CONFIG));

			// Set Proxy Port
			String proxyPortString = config
					.getProperty(ConfigManipulator.PROXY_PORT_CONFIG);

			if (proxyPortString != null && !proxyPortString.trim().equals("")) { //$NON-NLS-1$
				int port = Integer.parseInt(proxyPortString);

				if (port > 0 && port <= 0xFFFF) {
					configManipulator.setProxyPort(port);
				} else {
					log.warn("Proxy port is out of range!: " + port); //$NON-NLS-1$
				}
			}

			// Set Default BKU
			String bkuString = config.getProperty(ConfigManipulator.BKU_CONFIG);

			BKUs defaultBKU = BKUs.NONE;

			try {
				defaultBKU = BKUs.valueOf(bkuString);
			} catch (IllegalArgumentException ex) {
				log.error("Invalid BKU config value " + bkuString + " using none!"); //$NON-NLS-1$ //$NON-NLS-2$
				defaultBKU = BKUs.NONE;
			} catch (NullPointerException ex) {
				log.error("Invalid BKU config value " + bkuString + " using none!"); //$NON-NLS-1$ //$NON-NLS-2$
				defaultBKU = BKUs.NONE;
			}

			configManipulator.setDefaultBKU(defaultBKU);

			// Set Signature Position
			String signaturePosition = config
					.getProperty(ConfigManipulator.SIGNATURE_POSITION_CONFIG);

			SignaturePosition position = null;

			if (signaturePosition != null
					&& !signaturePosition.trim().equals("")) { //$NON-NLS-1$

				signaturePosition = signaturePosition.trim().toLowerCase();

				Pattern pattern = Pattern.compile(SIGN_POS_REGEX);

				Matcher matcher = pattern.matcher(signaturePosition);

				if (matcher.matches()) {
					if (matcher.groupCount() == 8) {
						if (matcher.group(1) != null) {
							// we have format: x=..;y=..;p=...
							try {
								// group 2 = x value
								float x = Float.parseFloat(matcher.group(2));

								// group 3 = y value
								float y = Float.parseFloat(matcher.group(3));

								// group 4 = p value
								int p = Integer.parseInt(matcher.group(3));

								position = new SignaturePosition(x, y, p);
							} catch (NumberFormatException ex) {
								log.error(
										"Signature Position read from config failed: Not a valid number", ex); //$NON-NLS-1$
							}
						} else if (matcher.group(5) != null) {
							// we have format auto
							position = new SignaturePosition();
						} else if (matcher.group(6) != null) {
							// we have format x=...;y=...;
							// group 7 = x value
							float x = Float.parseFloat(matcher.group(7));

							// group 8 = y value
							float y = Float.parseFloat(matcher.group(8));

							position = new SignaturePosition(x, y);
						}
					} else {
						log.error("Signature Position read from config failed: wrong group Count!"); //$NON-NLS-1$
					}
				} else {
					log.error("Signature Position read from config failed: not matching string"); //$NON-NLS-1$
				}

			}

			configManipulator.setDefaultSignaturePosition(position);

		} catch (IOException ex) {
			throw new InitializationException(
					"Failed to read configuration from config file", ex); //$NON-NLS-1$
		}
	}

	private void initializeFromArguments(String[] args, ArgumentHandler handler)
			throws InitializationException {
		handler.handleArguments(args);

		if (handler.IsRequireExit()) {
			this.stateMachine.exit();
		}
	}

	@Override
	public void run() {
		// Read config file
		try {
			// Read cli arguments with for config file!
			this.initializeFromArguments(this.stateMachine.getCmdArgs(),
					this.configFilehandler);

			// initialize from config file
			this.initializeFromConfigurationFile(this.stateMachine
					.getConfigProvider().getConfigurationFile());

			// Read cli arguments
			this.initializeFromArguments(this.stateMachine.getCmdArgs(),
					this.handler);

			// Set usedSignerLib ...
			this.stateMachine.getPDFSigner().setUsedPDFSignerLibrary(
					Signator.Signers.PDFAS);

			// Create PDF Signer
			this.stateMachine.getStatus().setBKU(
					this.stateMachine.getConfigProvider().getDefaultBKU());

			this.stateMachine.getStatus().setSignaturePosition(
					this.stateMachine.getConfigProvider()
							.getDefaultSignaturePosition());

			this.setNextState(new OpenState(this.stateMachine));

		} catch (InitializationException e) {
			log.error("Failed to initialize: ", e); //$NON-NLS-1$
			ErrorState error = new ErrorState(this.stateMachine);
			error.setException(e);
			this.setNextState(error);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.states.State#cleanUp()
	 */
	@Override
	public void cleanUp() {
		// No composite - no cleanup necessary
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.workflow.states.State#setMainWindowBehavior()
	 */
	@Override
	public void updateMainWindowBehavior() {
		// no behavior necessary yet
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
