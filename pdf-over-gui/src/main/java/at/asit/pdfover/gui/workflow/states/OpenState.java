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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import at.gv.egiz.pdfas.lib.impl.pdfbox2.placeholder.SignatureFieldsAndPlaceHolderExtractor;

import org.apache.pdfbox.pdmodel.PDDocument;

//Imports
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.PlaceholderSelectionGui;
import at.asit.pdfover.gui.composites.DataSourceSelectComposite;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;
import at.asit.pdfover.signator.SignaturePosition;
import at.gv.egiz.pdfas.lib.impl.placeholder.SignaturePlaceholderData;



import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;


/**
 * Selects the data source for the signature process.
 */
public class OpenState extends State {

	/**
	 * @param stateMachine
	 */
	public OpenState(StateMachine stateMachine) {
		super(stateMachine);
	}

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(OpenState.class);
	private static final String advancedConfig = Constants.CONFIG_DIRECTORY + File.separator + "/cfg/advancedconfig.properties";

	private DataSourceSelectComposite selectionComposite = null;

	private DataSourceSelectComposite getSelectionComposite() {
		if (this.selectionComposite == null) {
			this.selectionComposite =
					getStateMachine().createComposite(DataSourceSelectComposite.class, SWT.RESIZE, this);
		}
		return this.selectionComposite;
	}

	@Override
	public void run() {
		ConfigurationManager config = getStateMachine().configProvider;
		Status status = getStateMachine().status;
		if (!(status.getPreviousState() instanceof PrepareConfigurationState)
				&& !(status.getPreviousState() instanceof OpenState)) {
			status.bku = config.getDefaultBKU();
			status.document = null;
			status.signaturePosition = config.getAutoPositionSignature() ? (new SignaturePosition()) : null;

			/* ensure that files get closed */
			status.getPreviousState().cleanUp();
		}

		if (status.document == null) {
			DataSourceSelectComposite selection = this.getSelectionComposite();

			getStateMachine().display(selection);
			selection.layout();

			status.document = selection.getSelected();

			if (status.document == null) {
				// Not selected yet
				return;
			}
		}
		log.debug("Got Datasource: " + getStateMachine().status.document.getAbsolutePath());

		// scan for signature placeholders
		// - see if we want to scan for placeholders in the settings
		if (config.getEnablePlaceholderUsage()) {
			try (PDDocument pddocument = PDDocument.load(getStateMachine().status.document)) {
				// - scan for placeholders
				boolean useSignatureFields = config.getUseSignatureFields();
				boolean useMarker = config.getUseMarker();
				log.debug("Placeholder usage enabled. Signature fields: {}, QR Markers: {}", useSignatureFields, useMarker);
				//first check the signature fields placeholder
				if (useSignatureFields) {

					List<String> fields = SignatureFieldsAndPlaceHolderExtractor.findEmptySignatureFields(pddocument);

					if (fields.size() > 0) {
						while (true)
						{
							// create a dialog with ok and cancel buttons and a question
							// icon
							MessageBox dialog = new MessageBox(getStateMachine().getMainShell(),
									SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
							dialog.setText(Messages.getString("dataSourceSelection.usePlaceholderTitle"));
							dialog.setMessage(Messages.getString("dataSourceSelection.usePlaceholderText"));

							// open dialog and await user selection
							int result = dialog.open();
							if (result == SWT.YES) {

								if (fields.size() == 1) {
									addPlaceholderSelectionToConfig(fields.get(0));
									this.setNextState(new BKUSelectionState(getStateMachine()));
									return;

								} else if (fields.size() > 1) {

									PlaceholderSelectionGui gui = new PlaceholderSelectionGui(
											getStateMachine().getMainShell(), 65570, "text",
											"select the fields", fields);
									int res = gui.open();
									if (res != -1) {
										getStateMachine().status.searchForPlacehoderSignature = true;
										addPlaceholderSelectionToConfig(fields.get(res));
										this.setNextState(new BKUSelectionState(getStateMachine()));
									}
									else
										continue;
								}

							} else if (result == SWT.NO) {
								getStateMachine().status.searchForPlacehoderSignature = false;
							} else {
								status.document = null;
								return;
							}
							break;
						}
					}
					// second check if qr code placeholder search is enabled
				} else if (useMarker) {

					SignaturePlaceholderData signaturePlaceholderData = SignatureFieldsAndPlaceHolderExtractor.getNextUnusedSignaturePlaceHolder(pddocument);

					if (null != signaturePlaceholderData) {

						// create a dialog with ok and cancel buttons and a question icon
						MessageBox dialog = new MessageBox(getStateMachine().getMainShell(),
								SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
						dialog.setText(Messages.getString("dataSourceSelection.usePlaceholderTitle"));
						dialog.setMessage(Messages.getString("dataSourceSelection.usePlaceholderText"));

						// open dialog and await user selection
						int result = dialog.open();
						if (result == SWT.YES) {

							// if the user chooses to use the signature placeholder
							// - fill the position information so that we skip to
							// the
							// next stages without breaking stuff
							status.signaturePosition = new SignaturePosition(
									signaturePlaceholderData.getTablePos().getPosX(),
									signaturePlaceholderData.getTablePos().getPosY(),
									signaturePlaceholderData.getTablePos().getPage());

							getStateMachine().status.searchForPlacehoderSignature = true;

						} else if (result == SWT.NO) {
							getStateMachine().status.searchForPlacehoderSignature = false;
						} else {
							status.document = null;
							return;
						}
						// TODO: why does this use a different logic (via PositioningState) than the signature placeholders?
					}

				} else {
					// Do nothing
				}

			} catch (IOException e) {
				// fail silently. In case we got here no dialog has been shown.
				// Just
				// proceed with the usual process.
			}
		}

		this.setNextState(new PositioningState(getStateMachine()));
	}

	/**
	 * The selected placeholder is added to the configuration file
	 * @param selection
	 */
	private void addPlaceholderSelectionToConfig(String selection) {
		try {
	        PropertiesConfiguration config = new PropertiesConfiguration();
	        PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout(config);
	        layout.load(new InputStreamReader(new FileInputStream(advancedConfig)));

	        config.setProperty(Constants.SIGNATURE_FIELD_NAME_CONF, selection);
	        layout.save(new FileWriter(advancedConfig, false));

		} catch (Exception e) {
			log.error("Failed to add placeholder selection to config", e);
		}

	}

	/**
	 * Open the input document selection dialog
	 */
	public void openFileDialog() {
		if (this.selectionComposite != null)
			this.selectionComposite.openFileDialog();
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#cleanUp()
	 */
	@Override
	public void cleanUp() {
		if (this.selectionComposite != null)
			this.selectionComposite.dispose();
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#setMainWindowBehavior()
	 */
	@Override
	public void updateMainWindowBehavior() {
		MainWindowBehavior behavior = getStateMachine().status.behavior;
		behavior.reset();
		behavior.setEnabled(Buttons.CONFIG, true);
		behavior.setEnabled(Buttons.OPEN, true);
		behavior.setActive(Buttons.OPEN, true);
	}

	@Override
	public String toString()  {
		return this.getClass().getName();
	}
}
