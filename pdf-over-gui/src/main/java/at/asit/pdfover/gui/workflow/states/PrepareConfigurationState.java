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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.cliarguments.*;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.exceptions.InitializationException;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.utils.UpdateCheckManager;
import at.asit.pdfover.gui.utils.VersionComparator;
import at.asit.pdfover.gui.utils.Zipper;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;
import at.asit.pdfover.signer.SignaturePosition;

/**
 * Starting state of workflow proccess
 *
 * Reads configuration, command arguments and initializes configured variables
 */
public class PrepareConfigurationState extends State {

	/** SLF4J Logger instance **/
	static final Logger log = LoggerFactory.getLogger(PrepareConfigurationState.class);

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
		this.handler.addCLIArgument(KeystoreFileArgument.class);
		this.handler.addCLIArgument(KeystoreTypeArgument.class);
		this.handler.addCLIArgument(KeystoreStorePassArgument.class);
		this.handler.addCLIArgument(KeystoreAliasArgument.class);
		this.handler.addCLIArgument(KeystoreKeyPassArgument.class);
		this.handler.addCLIArgument(ProxyHostArgument.class);
		this.handler.addCLIArgument(ProxyPortArgument.class);
		this.handler.addCLIArgument(ProxyUserArgument.class);
		this.handler.addCLIArgument(ProxyPassArgument.class);
		this.handler.addCLIArgument(EmblemArgument.class);
		this.handler.addCLIArgument(AutomaticPositioningArgument.class);
		this.handler.addCLIArgument(SkipFinishArgument.class);
		// adding config file argument to this handler so it appears in help
		this.handler.addCLIArgument(ConfigFileArgument.class);
		this.handler.addCLIArgument(InvisibleProfile.class);

		this.configFileHandler = new ArgumentHandler(getStateMachine());
		this.configFileHandler.addCLIArgument(ConfigFileArgument.class);
	}

	private void initializeFromConfigurationFile() throws InitializationException {
		try {
			getStateMachine().configProvider.loadFromDisk();
		} catch (IOException ex) {
			throw new InitializationException("Failed to read configuration from config file", ex);
		}
	}

	private void initializeFromArguments(String[] args, ArgumentHandler handler) throws InitializationException {
		handler.handleArguments(args);

		if (handler.doesRequireExit()) {
			getStateMachine().exit();
		}
	}

	private void ensurePdfOverConfigExists() throws InitializationException {
		try {
			File pdfOverConfig = new File(Constants.CONFIG_DIRECTORY + File.separator + Constants.DEFAULT_CONFIG_FILENAME);
			if (!pdfOverConfig.exists())
				pdfOverConfig.createNewFile();
		} catch (Exception e) {
			log.error("Failed to create PDF-Over config file", e);
			throw new InitializationException("Failed to create PDF-Over config file", e);
		}
	}

	private void unzipPdfAsConfig(File configDir) throws InitializationException {
		InputStream is = getClass().getResourceAsStream(Constants.RES_CFG_ZIP);

		try {
			Zipper.unzip(is, configDir.getAbsolutePath());
		} catch (IOException e) {
			log.error(
					"Failed to create local configuration directory!", e);
			throw new InitializationException(
					"Failed to create local configuration directory!",
					e);
		}
	}

	private static void updateVersionFile(File configDir) throws InitializationException {
		File versionFile = new File(configDir, Constants.CONFIG_VERSION_FILENAME);
		try {
			BufferedWriter versionWriter = new BufferedWriter(new FileWriter(versionFile));
			String version = Constants.APP_VERSION == null ? "Unknown" : Constants.APP_VERSION;
			versionWriter.write(version);
			versionWriter.close();
		} catch (IOException e) {
			log.error(
					"Failed to create configuration version file!", e);
			throw new InitializationException(
					"Failed to create configuration version file!",
					e);
		}
	}

	private void createConfiguration(File configDir) throws InitializationException {
		boolean allOK = false;

		log.info("Creating configuration directory");
		if (!configDir.exists()) {
			configDir.mkdir();
		}

		try {
			ensurePdfOverConfigExists();
			unzipPdfAsConfig(configDir);
			updateVersionFile(configDir);

			allOK = true;
		} finally {
			if (!allOK) {
				configDir.delete();
			}
		}
	}

	/**
	 * @return The first valid (not empty, non comment) line of the version file
	 *         or null if version file cannot be read or does not contain
	 *         such a line.
	 */
	private static String getVersion(File versionFile) {
		if (versionFile.exists() && versionFile.canRead()) {
			BufferedReader versionReader = null;
			try {
				versionReader = new BufferedReader(new FileReader(versionFile));
				String version;
				while ((version = versionReader.readLine()) != null) {
					version = version.trim();
					if (version.length() > 0 && !version.startsWith("#")) {
						log.trace("configuration version from " + versionFile
								+ ": " + version);
						return version;
					}
				}
			} catch (IOException ex) {
				log.error("failed to read configuration version from "
						+ versionFile, ex);
			} finally {
				try {
					if (versionReader != null)
						versionReader.close();
				} catch (IOException ex) {
					// ignore
				}
			}
		}
		log.debug("unknown configuration version");
		return null;
	}

	/**
	 * Backup old configuration, create new
	 * @param configDir
	 * @throws InitializationException
	 */
	private void backupAndCreatePdfAsConfiguration(File configDir) throws InitializationException {
		try {
			File backup = File.createTempFile(Constants.PDF_AS_CONFIG_BACKUP_FILENAME, ".zip");
			OutputStream os = new FileOutputStream(backup);
			Zipper.zip(configDir + File.separator + "cfg", os, true);
			os.close();
			unzipPdfAsConfig(configDir);
			File b = new File(configDir, Constants.PDF_AS_CONFIG_BACKUP_FILENAME + ".zip");
			int i = 1;
			while (b.exists()) {
				b = new File(configDir, Constants.PDF_AS_CONFIG_BACKUP_FILENAME + i++ + ".zip");
			}
			backup.renameTo(b);
			updateVersionFile(configDir);
		} catch (FileNotFoundException e) {
			log.error("Backup file not found", e);
			throw new InitializationException("Backup file not found", e);
		} catch (IOException e) {
			log.error("Error creating configuration backup", e);
			throw new InitializationException("Error creating configuration backup", e);
		}
	}

	@Override
	public void run() {
		// Read config file
		try {
			StateMachine stateMachine = getStateMachine();
			ConfigurationManager config = stateMachine.configProvider;
			File configDir = new File(Constants.CONFIG_DIRECTORY);
			File configFile = new File(configDir, Constants.DEFAULT_CONFIG_FILENAME);
			if (!configDir.exists() || !configFile.exists()) {
				log.debug("Creating configuration file");
				createConfiguration(configDir);
			} else {
				log.debug("Configuration directory exists!");
				// Check PDF-AS config version
				File versionFile = new File(configDir, Constants.CONFIG_VERSION_FILENAME);
				String configVersion = getVersion(versionFile);
				if (configVersion == null || VersionComparator.lessThan(configVersion, Constants.MIN_PDF_AS_CONFIG_VERSION))
					backupAndCreatePdfAsConfiguration(configDir);
			}


			// Read cli arguments for config file location first
			try {
				initializeFromArguments(stateMachine.cmdLineArgs, this.configFileHandler);
			} catch (InitializationException e) {
				log.error("Error in cmd line arguments: ", e);
				ErrorDialog error = new ErrorDialog(stateMachine.getMainShell(),
						Messages.getString("error.CmdLineArgs") + "\n" +
						e.getMessage(),
						BUTTONS.OK);
				error.open();
				stateMachine.exit();
			}

			// initialize from config file
			initializeFromConfigurationFile();

			// Read cli arguments
			try {
				initializeFromArguments(stateMachine.cmdLineArgs, this.handler);
			} catch (InitializationException e) {
				log.error("Error in cmd line arguments: ", e);
				ErrorDialog error;

				if (e.getCause() instanceof FileNotFoundException) {
					error = new ErrorDialog(stateMachine.getMainShell(),
						String.format(
								Messages.getString("error.FileNotExist"),
								e.getCause().getMessage()),
						BUTTONS.OK);
				} else {
					error = new ErrorDialog(stateMachine.getMainShell(),
							Messages.getString("error.CmdLineArgs") + "\n" +
							e.getMessage(),
							BUTTONS.OK);
				}
				error.open();
				stateMachine.exit();
			}

			// Check for updates
			if (config.getUpdateCheck())
				UpdateCheckManager.checkNow(stateMachine.getMainShell());

			// Create PDF Signer
			Status status = stateMachine.status;
			status.bku = getStateMachine().configProvider.getDefaultBKU();
			status.signaturePosition = getStateMachine().configProvider.getAutoPositionSignature() ? (new SignaturePosition()) : null;

			setNextState(new OpenState(stateMachine));

		} catch (Exception e) {
			log.error("Failed to initialize: ", e);
			ErrorDialog error = new ErrorDialog(
				getStateMachine().getMainShell(),
				Messages.getString("error.Initialization"),
				BUTTONS.YES_NO
			);
			// error.setException(e);
			// setNextState(error);
			int selection = error.open();
			if (selection == SWT.YES)
				ConfigurationManager.factoryResetPersistentConfig();

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
		return getClass().getName();
	}
}
