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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Messages;
import at.asit.pdfover.gui.Unzipper;
import at.asit.pdfover.gui.cliarguments.ArgumentHandler;
import at.asit.pdfover.gui.cliarguments.BKUArgument;
import at.asit.pdfover.gui.cliarguments.ConfigFileArgument;
import at.asit.pdfover.gui.cliarguments.EmblemArgument;
import at.asit.pdfover.gui.cliarguments.HelpArgument;
import at.asit.pdfover.gui.cliarguments.InputDocumentArgument;
import at.asit.pdfover.gui.cliarguments.OutputFolderArgument;
import at.asit.pdfover.gui.cliarguments.PasswordArgument;
import at.asit.pdfover.gui.cliarguments.PhoneNumberArgument;
import at.asit.pdfover.gui.cliarguments.ProxyHostArgument;
import at.asit.pdfover.gui.cliarguments.ProxyPortArgument;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.exceptions.InitializationException;
import at.asit.pdfover.gui.workflow.ConfigManipulator;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.signator.Signator;

/**
 * Starting state of workflow proccess
 * 
 * Reads configuration, command arguments and initializes configured variables
 */
public class PrepareConfigurationState extends State {

	
	/**
	 * @param stateMachine
	 */
	public PrepareConfigurationState(StateMachine stateMachine) {
		super(stateMachine);
		this.handler = new ArgumentHandler(this.stateMachine);
		this.handler.addCLIArgument(new HelpArgument());
		this.handler.addCLIArgument(new BKUArgument());
		this.handler.addCLIArgument(new PhoneNumberArgument());
		this.handler.addCLIArgument(new EmblemArgument());
		this.handler.addCLIArgument(new PasswordArgument());
		this.handler.addCLIArgument(new ProxyHostArgument());
		this.handler.addCLIArgument(new ProxyPortArgument());
		this.handler.addCLIArgument(new OutputFolderArgument());
		this.handler.addCLIArgument(new InputDocumentArgument());
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

			try {
				this.stateMachine.getConfigProvider().loadConfiguration(
						new FileInputStream(
								this.stateMachine.getConfigProvider().getConfigurationDirectory() + "/" + filename)); //$NON-NLS-1$

				log.info("Loaded config from file : " + filename); //$NON-NLS-1$

			} catch (FileNotFoundException ex) {
				if (filename.equals(ConfigManipulator.DEFAULT_CONFIG_FILE)) {
					// we only check for resource config file if it is the
					// default value!
					try {
						InputStream is = this.getClass().getResourceAsStream(
								"/" + filename); //$NON-NLS-1$
						this.stateMachine.getConfigProvider()
								.loadConfiguration(is);

						log.info("Loaded config from resource : " + filename); //$NON-NLS-1$
					} catch (Exception eex) {
						throw ex;
					}
				} else {
					throw ex;
				}
			}

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

			File configDir = new File(this.stateMachine.getConfigProvider().getConfigurationDirectory());
			File configFile = new File(this.stateMachine.getConfigProvider().getConfigurationDirectory() + "/" //$NON-NLS-1$
					+ ConfigManipulator.DEFAULT_CONFIG_FILE);
			if (!configDir.exists() || !configFile.exists()) {
				boolean allOK = false;

				log.info("Creating configuration directory"); //$NON-NLS-1$

				try {
					if (!configDir.exists()) {
						configDir.mkdir();
					}
					// Copy PDFOver config to config Dir

					// 1Kb buffer
					byte[] buffer = new byte[1024];
					int byteCount = 0;

					InputStream inputStream = null;
					FileOutputStream pdfOverConfig = null;
					try {
						inputStream = this.getClass().getResourceAsStream(
								"/" + ConfigManipulator.DEFAULT_CONFIG_FILE); //$NON-NLS-1$
						pdfOverConfig = new FileOutputStream(this.stateMachine.getConfigProvider().getConfigurationDirectory()
								+ "/" //$NON-NLS-1$
								+ ConfigManipulator.DEFAULT_CONFIG_FILE);

						while ((byteCount = inputStream.read(buffer)) >= 0) {
							pdfOverConfig.write(buffer, 0, byteCount);
						}
					} catch (Exception e) {
						log.error(
								"Failed to write PDF Over config file to config directory", e); //$NON-NLS-1$
						throw new InitializationException(
								"Failed to write PDF Over config file to config directory", //$NON-NLS-1$
								e);
					} finally {
						if (pdfOverConfig != null) {
							try {
								pdfOverConfig.close();
							} catch (IOException e) {
								log.warn(
										"Failed to close File stream for PDFOver config", e); //$NON-NLS-1$
							}
						}

						if (inputStream != null) {
							try {
								inputStream.close();
							} catch (IOException e) {
								log.warn(
										"Failed to close Resource stream for PDFOver config", e); //$NON-NLS-1$
							}
						}
					}

					InputStream is = this.getClass().getResourceAsStream(
							"/cfg/PDFASConfig.zip"); //$NON-NLS-1$

					try {
						Unzipper.unzip(is, configDir.getAbsolutePath());
					} catch (IOException e) {
						log.error(
								"Failed to create local configuration directory!", e); //$NON-NLS-1$
						throw new InitializationException(
								"Failed to create local configuration directory!", //$NON-NLS-1$
								e);
					}
					allOK = true;
				} finally {
					if (!allOK) {
						configDir.delete();
					}
				}
			} else {
				log.debug("Configuration directory exists!"); //$NON-NLS-1$
			}

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
			ErrorDialog error = new ErrorDialog(this.stateMachine
					.getGUIProvider().getMainShell(), SWT.NONE,
					Messages.getString("error.Initialization"), //$NON-NLS-1$
					e, false);
			// error.setException(e);
			// this.setNextState(error);
			error.open();
			this.stateMachine.exit();
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
