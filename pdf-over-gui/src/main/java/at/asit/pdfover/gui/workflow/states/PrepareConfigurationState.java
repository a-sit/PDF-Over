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
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.cliarguments.ArgumentHandler;
import at.asit.pdfover.gui.cliarguments.AutomaticPositioningArgument;
import at.asit.pdfover.gui.cliarguments.BKUArgument;
import at.asit.pdfover.gui.cliarguments.ConfigFileArgument;
import at.asit.pdfover.gui.cliarguments.EmblemArgument;
import at.asit.pdfover.gui.cliarguments.HelpArgument;
import at.asit.pdfover.gui.cliarguments.InputDocumentArgument;
import at.asit.pdfover.gui.cliarguments.OutputFolderArgument;
import at.asit.pdfover.gui.cliarguments.PasswordArgument;
import at.asit.pdfover.gui.cliarguments.PhoneNumberArgument;
import at.asit.pdfover.gui.cliarguments.ProxyHostArgument;
import at.asit.pdfover.gui.cliarguments.ProxyPassArgument;
import at.asit.pdfover.gui.cliarguments.ProxyPortArgument;
import at.asit.pdfover.gui.cliarguments.ProxyUserArgument;
import at.asit.pdfover.gui.cliarguments.SkipFinishArgument;
import at.asit.pdfover.gui.controls.Dialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.Dialog.ICON;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.exceptions.InitializationException;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.utils.Unzipper;
import at.asit.pdfover.gui.utils.VersionComparator;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUHelper;
import at.asit.pdfover.signator.Signator;

/**
 * Starting state of workflow proccess
 * 
 * Reads configuration, command arguments and initializes configured variables
 */
public class PrepareConfigurationState extends State {

	/** SLF4J Logger instance **/
	private static final Logger log = LoggerFactory
			.getLogger(PrepareConfigurationState.class);

	private static String FILE_SEPARATOR = File.separator;

	private ArgumentHandler handler;

	private ArgumentHandler configFileHandler;

	/**
	 * @param stateMachine
	 */
	public PrepareConfigurationState(StateMachine stateMachine) {
		super(stateMachine);
		this.handler = new ArgumentHandler(getStateMachine());
		this.handler.addCLIArgument(HelpArgument.class);
		this.handler.addCLIArgument(InputDocumentArgument.class);
		this.handler.addCLIArgument(OutputFolderArgument.class);
		this.handler.addCLIArgument(BKUArgument.class);
		this.handler.addCLIArgument(PhoneNumberArgument.class);
		this.handler.addCLIArgument(PasswordArgument.class);
		this.handler.addCLIArgument(ProxyHostArgument.class);
		this.handler.addCLIArgument(ProxyPortArgument.class);
		this.handler.addCLIArgument(ProxyUserArgument.class);
		this.handler.addCLIArgument(ProxyPassArgument.class);
		this.handler.addCLIArgument(EmblemArgument.class);
		this.handler.addCLIArgument(AutomaticPositioningArgument.class);
		this.handler.addCLIArgument(SkipFinishArgument.class);
		// adding config file argument to this handler so it appears in help
		this.handler.addCLIArgument(ConfigFileArgument.class);

		this.configFileHandler = new ArgumentHandler(getStateMachine());
		this.configFileHandler.addCLIArgument(ConfigFileArgument.class);
	}

	private void initializeFromConfigurationFile(String filename)
			throws InitializationException {
		try {

			try {
				getStateMachine().getConfigProvider().loadConfiguration(
						new FileInputStream(
								getStateMachine().getConfigProvider().getConfigurationDirectory() + FILE_SEPARATOR + filename));

				log.info("Loaded config from file : " + filename); //$NON-NLS-1$
			} catch (FileNotFoundException ex) {
				if (filename.equals(Constants.DEFAULT_CONFIG_FILENAME)) {
					// we only check for resource config file if it is the
					// default value!
					try {
						InputStream is = this.getClass().getResourceAsStream(
								Constants.RES_PKG_PATH + filename);
						getStateMachine().getConfigProvider()
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

		if (handler.doesRequireExit()) {
			getStateMachine().exit();
		}
	}

	private void createConfiguration(File configDir) throws InitializationException {
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
						Constants.RES_PKG_PATH + Constants.DEFAULT_CONFIG_FILENAME);
				pdfOverConfig = new FileOutputStream(
						getStateMachine().getConfigProvider().getConfigurationDirectory() +
						FILE_SEPARATOR + Constants.DEFAULT_CONFIG_FILENAME);

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

			inputStream = null;
			pdfOverConfig = null;
			try {
				inputStream = this.getClass().getResourceAsStream(
						Constants.RES_PKG_PATH + Constants.DEFAULT_LOG4J_FILENAME);
				String filename = getStateMachine().getConfigProvider().getConfigurationDirectory()
						+ FILE_SEPARATOR + Constants.DEFAULT_LOG4J_FILENAME;
				pdfOverConfig = new FileOutputStream(filename);

				while ((byteCount = inputStream.read(buffer)) >= 0) {
					pdfOverConfig.write(buffer, 0, byteCount);
				}

				PropertyConfigurator.configureAndWatch(filename);
			} catch (Exception e) {
				log.error(
						"Failed to write log4j config file to config directory", e); //$NON-NLS-1$
				throw new InitializationException(
						"Failed to write log4j config file to config directory", //$NON-NLS-1$
						e);
			} finally {
				if (pdfOverConfig != null) {
					try {
						pdfOverConfig.close();
					} catch (IOException e) {
						log.warn(
								"Failed to close File stream for log4j config", e); //$NON-NLS-1$
					}
				}

				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						log.warn(
								"Failed to close Resource stream for log4j config", e); //$NON-NLS-1$
					}
				}
			}
			
			InputStream is = this.getClass().getResourceAsStream(
					Constants.RES_CFG_ZIP);

			try {
				Unzipper.unzip(is, configDir.getAbsolutePath());
			} catch (IOException e) {
				log.error(
						"Failed to create local configuration directory!", e); //$NON-NLS-1$
				throw new InitializationException(
						"Failed to create local configuration directory!", //$NON-NLS-1$
						e);
			}
			
			// initialize from config file
			this.initializeFromConfigurationFile(getStateMachine()
					.getConfigProvider().getConfigurationFile());
			
			getStateMachine().getConfigManipulator().setSignatureNote(Messages.getString("simple_config.Note_Default")); //$NON-NLS-1$
			
			try {
				getStateMachine().getConfigManipulator().saveCurrentConfiguration();
			} catch (IOException e) {
				log.error(
						"Failed to set local configuration signature note!", e); //$NON-NLS-1$
				throw new InitializationException(
						"Failed to set local configuration signature note!", //$NON-NLS-1$
						e);
			}
			
			allOK = true;
		} finally {
			if (!allOK) {
				configDir.delete();
			}
		}
	}

	@Override
	public void run() {
		// Read config file
		try {

			File configDir = new File(getStateMachine().getConfigProvider().getConfigurationDirectory());
			File configFile = new File(getStateMachine().getConfigProvider().getConfigurationDirectory()
					+ FILE_SEPARATOR + Constants.DEFAULT_CONFIG_FILENAME);
			if (!configDir.exists() || !configFile.exists()) {
				log.debug("Creating configuration file"); //$NON-NLS-1$
				createConfiguration(configDir);
			} else {
				log.debug("Configuration directory exists!"); //$NON-NLS-1$
			}

			// Read cli arguments for config file first
			try {
				this.initializeFromArguments(getStateMachine().getCmdArgs(),
						this.configFileHandler);
			} catch (InitializationException e) {
				log.error("Error in cmd line arguments: ", e); //$NON-NLS-1$
				ErrorDialog error = new ErrorDialog(getStateMachine()
						.getGUIProvider().getMainShell(),
						Messages.getString("error.CmdLineArgs") + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
						e.getMessage(),
						BUTTONS.OK);
				error.open();
				getStateMachine().exit();
			}

			// initialize from config file
			this.initializeFromConfigurationFile(getStateMachine()
					.getConfigProvider().getConfigurationFile());

			// Read cli arguments
			try {
				this.initializeFromArguments(getStateMachine().getCmdArgs(),
						this.handler);
			} catch (InitializationException e) {
				log.error("Error in cmd line arguments: ", e); //$NON-NLS-1$
				ErrorDialog error;
				
				if (e.getCause() instanceof FileNotFoundException) {
					error = new ErrorDialog(getStateMachine()
						.getGUIProvider().getMainShell(),
						String.format(
								Messages.getString("error.FileNotExist"), //$NON-NLS-1$
								e.getCause().getMessage()),
						BUTTONS.OK);
				} else {
					error = new ErrorDialog(getStateMachine()
							.getGUIProvider().getMainShell(),
							Messages.getString("error.CmdLineArgs") + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
							e.getMessage(),
							BUTTONS.OK);
				}
				error.open();
				getStateMachine().exit();
			}

			// Check for updates
			if (getStateMachine().getConfigProvider().getUpdateCheck() && Constants.APP_VERSION != null) {
				HttpClient client = MobileBKUHelper.getHttpClient();
				GetMethod method = new GetMethod(Constants.CURRENT_RELEASE_URL);
				try {
					client.executeMethod(method);
					String version = method.getResponseBodyAsString().trim();

					if (VersionComparator.before(Constants.APP_VERSION, version)) {
						Dialog info = new Dialog(getStateMachine()
							.getGUIProvider().getMainShell(),
							Messages.getString("version_check.UpdateTitle"), //$NON-NLS-1$
							String.format(Messages.getString("version_check.UpdateText"), //$NON-NLS-1$
									version),
							BUTTONS.OK_CANCEL, ICON.INFORMATION);
						if (info.open() == SWT.OK)
						{
							if (Desktop.isDesktopSupported()) {
								Desktop.getDesktop().browse(new URI(Constants.UPDATE_URL));
							} else {
								log.info("SWT Desktop is not supported on this platform"); //$NON-NLS-1$
								Program.launch(Constants.UPDATE_URL);
							}
						}
					}
				} catch (Exception e) {
					log.error("Error downloading update information: ", e); //$NON-NLS-1$
				}
			}

			// Set usedSignerLib ...
			getStateMachine().getPDFSigner().setUsedPDFSignerLibrary(
					Signator.Signers.PDFAS);

			// Create PDF Signer
			getStateMachine().getStatus().setBKU(
					getStateMachine().getConfigProvider().getDefaultBKU());

			getStateMachine().getStatus().setSignaturePosition(
					getStateMachine().getConfigProvider()
							.getDefaultSignaturePosition());

			this.setNextState(new OpenState(getStateMachine()));

		} catch (InitializationException e) {
			log.error("Failed to initialize: ", e); //$NON-NLS-1$
			ErrorDialog error = new ErrorDialog(getStateMachine()
					.getGUIProvider().getMainShell(), 
					Messages.getString("error.Initialization"), //$NON-NLS-1$
					BUTTONS.OK);
			// error.setException(e);
			// this.setNextState(error);
			error.open();
			getStateMachine().exit();
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
