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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Locale;

import at.asit.pdfover.commons.Profile;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.bku.BKUHelper;
import at.asit.pdfover.gui.cliarguments.*;
import at.asit.pdfover.gui.controls.Dialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.Dialog.ICON;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.exceptions.InitializationException;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.utils.VersionComparator;
import at.asit.pdfover.gui.utils.Zipper;
import at.asit.pdfover.gui.workflow.GUIProvider;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.gui.workflow.config.ConfigProviderImpl;
import at.asit.pdfover.signator.Signator;

/**
 * Starting state of workflow proccess
 *
 * Reads configuration, command arguments and initializes configured variables
 */
public class PrepareConfigurationState extends State {

	/** SLF4J Logger instance **/
	static final Logger log = LoggerFactory
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

	private void initializeFromConfigurationFile(String filename)
			throws InitializationException {
		try {

			// TODO: move this to ConfigProviderImpl to mirror save logic
			try {
				getStateMachine().configProvider.loadConfiguration(new FileInputStream(getStateMachine().configProvider.getConfigurationDirectory() + FILE_SEPARATOR + filename));

				log.info("Loaded config from file : " + filename);
			} catch (FileNotFoundException ex) {
				if (filename.equals(Constants.DEFAULT_CONFIG_FILENAME)) {
					// we only check for resource config file if it is the
					// default value!
					try {
						InputStream is = getClass().getResourceAsStream(
								Constants.RES_PKG_PATH + filename);
						getStateMachine().configProvider
								.loadConfiguration(is);

						log.info("Loaded config from resource : " + filename);
					} catch (Exception eex) {
						throw ex;
					}
				} else {
					throw ex;
				}
			}

		} catch (IOException ex) {
			throw new InitializationException(
					"Failed to read configuration from config file", ex);
		}
	}

	/**
	 * Update configuration values as necessary
	 */
	private void updateConfiguration() {
		ConfigProviderImpl config = getStateMachine().configProvider;

		//Update signature note if old default is used
		String note = config.getSignatureNote();
		Locale loc = config.getSignatureLocale();

		String note_old = Messages.getString("simple_config.Note_Default_Old", loc);
		if (note.equals(note_old))
			resetSignatureNoteField(config);
	}

	private void resetSignatureNoteField(ConfigProviderImpl config){
		getStateMachine().configProvider.setSignatureNote(
			Profile.getProfile(config.getSignatureProfile()).getDefaultSignatureBlockNote(config.getLocale())
		);
	}

	private void initializeFromArguments(String[] args, ArgumentHandler handler)
			throws InitializationException {
		handler.handleArguments(args);

		if (handler.doesRequireExit()) {
			getStateMachine().exit();
		}
	}

	private void copyPdfOverConfig() throws InitializationException {
		// 1Kb buffer
		byte[] buffer = new byte[1024];
		int byteCount = 0;

		InputStream inputStream = null;
		FileOutputStream pdfOverConfig = null;
		try {
			inputStream = getClass().getResourceAsStream(
					Constants.RES_PKG_PATH + Constants.DEFAULT_CONFIG_FILENAME);
			pdfOverConfig = new FileOutputStream(
					getStateMachine().configProvider.getConfigurationDirectory() +
					FILE_SEPARATOR + Constants.DEFAULT_CONFIG_FILENAME);

			while ((byteCount = inputStream.read(buffer)) >= 0) {
				pdfOverConfig.write(buffer, 0, byteCount);
			}
		} catch (Exception e) {
			log.error(
					"Failed to write PDF Over config file to config directory", e);
			throw new InitializationException(
					"Failed to write PDF Over config file to config directory",
					e);
		} finally {
			if (pdfOverConfig != null) {
				try {
					pdfOverConfig.close();
				} catch (IOException e) {
					log.warn(
							"Failed to close File stream for PDFOver config", e);
				}
			}

			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.warn(
							"Failed to close Resource stream for PDFOver config", e);
				}
			}
		}
	}

	private void copyLog4jConfig() throws InitializationException {
		// TODO: figure out if we still need this
		/*
		// 1Kb buffer
		byte[] buffer = new byte[1024];
		int byteCount = 0;

		InputStream inputStream = null;
		FileOutputStream pdfOverConfig = null;
		try {
			inputStream = getClass().getResourceAsStream(
					Constants.RES_PKG_PATH + Constants.DEFAULT_LOG4J_FILENAME);
			String filename = getStateMachine().configProvider.getConfigurationDirectory()
					+ FILE_SEPARATOR + Constants.DEFAULT_LOG4J_FILENAME;
			pdfOverConfig = new FileOutputStream(filename);

			while ((byteCount = inputStream.read(buffer)) >= 0) {
				pdfOverConfig.write(buffer, 0, byteCount);
			}

			PropertyConfigurator.configureAndWatch(filename);
		} catch (Exception e) {
			log.error(
					"Failed to write log4j config file to config directory", e);
			throw new InitializationException(
					"Failed to write log4j config file to config directory",
					e);
		} finally {
			if (pdfOverConfig != null) {
				try {
					pdfOverConfig.close();
				} catch (IOException e) {
					log.warn(
							"Failed to close File stream for log4j config", e);
				}
			}

			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.warn(
							"Failed to close Resource stream for log4j config", e);
				}
			}
		}*/
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

	private void initializeConfig() throws InitializationException {
		initializeFromConfigurationFile(getStateMachine()
				.configProvider.getConfigurationFile());

		resetSignatureNoteField(getStateMachine().configProvider);

		try {
			getStateMachine().configProvider.saveCurrentConfiguration();
		} catch (IOException e) {
			log.error(
					"Failed to set local configuration signature note!", e);
			throw new InitializationException(
					"Failed to set local configuration signature note!",
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
			copyPdfOverConfig();
			copyLog4jConfig();
			unzipPdfAsConfig(configDir);
			updateVersionFile(configDir);
			initializeConfig();

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
			Zipper.zip(configDir + FILE_SEPARATOR + "cfg", os, true);
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
			ConfigProviderImpl config = stateMachine.configProvider;
			final GUIProvider gui = stateMachine;
			String cDir = config.getConfigurationDirectory();
			File configDir = new File(cDir);
			File configFile = new File(configDir, Constants.DEFAULT_CONFIG_FILENAME);
			if (!configDir.exists() || !configFile.exists()) {
				log.debug("Creating configuration file");
				createConfiguration(configDir);
			} else {
				log.debug("Configuration directory exists!");
				// Check PDF-AS config version
				File versionFile = new File(configDir, Constants.CONFIG_VERSION_FILENAME);
				String configVersion = getVersion(versionFile);
				if (configVersion == null || VersionComparator.before(configVersion, Constants.MIN_PDF_AS_CONFIG_VERSION))
					backupAndCreatePdfAsConfiguration(configDir);
			}


			// Read cli arguments for config file first
			try {
				initializeFromArguments(stateMachine.cmdLineArgs, this.configFileHandler);
			} catch (InitializationException e) {
				log.error("Error in cmd line arguments: ", e);
				ErrorDialog error = new ErrorDialog(gui.getMainShell(),
						Messages.getString("error.CmdLineArgs") + "\n" +
						e.getMessage(),
						BUTTONS.OK);
				error.open();
				stateMachine.exit();
			}

			// initialize from config file
			initializeFromConfigurationFile(config.getConfigurationFile());
			updateConfiguration();

			// Read cli arguments
			try {
				initializeFromArguments(stateMachine.cmdLineArgs, this.handler);
			} catch (InitializationException e) {
				log.error("Error in cmd line arguments: ", e);
				ErrorDialog error;

				if (e.getCause() instanceof FileNotFoundException) {
					error = new ErrorDialog(gui.getMainShell(),
						String.format(
								Messages.getString("error.FileNotExist"),
								e.getCause().getMessage()),
						BUTTONS.OK);
				} else {
					error = new ErrorDialog(gui.getMainShell(),
							Messages.getString("error.CmdLineArgs") + "\n" +
							e.getMessage(),
							BUTTONS.OK);
				}
				error.open();
				stateMachine.exit();
			}

			// Check for updates
			if (config.getUpdateCheck() && Constants.APP_VERSION != null) {
				new Thread(() -> {
					HttpClient client = (HttpClient) BKUHelper.getHttpClient();
					GetMethod method = new GetMethod(Constants.CURRENT_RELEASE_URL);
					try {
						client.executeMethod(method);
						final String version = method.getResponseBodyAsString().trim();
						if (!VersionComparator.before(Constants.APP_VERSION, version))
							return;

						// wait 500ms before invoke the GUI message, because GUI had to be started from
						// main thread
						try {Thread.sleep(500); } catch (InterruptedException e1) { }
						// invoke GUI message in main thread
						gui.getMainShell().getDisplay().asyncExec(() -> {
							Dialog info = new Dialog(gui.getMainShell(),
									Messages.getString("version_check.UpdateTitle"),
									String.format(Messages.getString("version_check.UpdateText"), version),
									BUTTONS.OK_CANCEL, ICON.INFORMATION);

							if (info.open() == SWT.OK)
							{
								if (Desktop.isDesktopSupported()) {
									try {
										Desktop.getDesktop().browse(new URI(Constants.UPDATE_URL));
									} catch (Exception e) {
										log.error("Error opening update location ", e);
									}
								} else {
									log.info("SWT Desktop is not supported on this platform");
									Program.launch(Constants.UPDATE_URL);
								}
							}
						});
					} catch (Exception e) {
						log.error("Error downloading update information: ", e);
					}
				}).start();
			}

			// Set usedSignerLib ...
			stateMachine.pdfSigner.setUsedPDFSignerLibrary(
					Signator.Signers.PDFAS4);

			// Create PDF Signer
			Status status = stateMachine.status;
			status.bku = getStateMachine().configProvider.getDefaultBKU();
			status.signaturePosition = getStateMachine().configProvider.getDefaultSignaturePosition();

			setNextState(new OpenState(stateMachine));

		} catch (InitializationException e) {
			log.error("Failed to initialize: ", e);
			ErrorDialog error = new ErrorDialog(getStateMachine()
					.getMainShell(),
					Messages.getString("error.Initialization"),
					BUTTONS.OK);
			// error.setException(e);
			// setNextState(error);
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
		return getClass().getName();
	}
}
