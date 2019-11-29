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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.pdfbox.pdmodel.PDDocument;

//Imports
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.PlaceholderSelectionGui;
import at.asit.pdfover.gui.composites.DataSourceSelectComposite;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.gui.workflow.config.ConfigProvider;
import at.asit.pdfover.signator.SignaturePosition;
import at.gv.egiz.pdfas.common.exceptions.PdfAsException;
import at.gv.egiz.pdfas.lib.impl.placeholder.SignaturePlaceholderData;
import at.gv.egiz.pdfas.lib.impl.pdfbox2.placeholder.SignatureFieldsExtractor;
import at.gv.egiz.pdfas.lib.impl.pdfbox2.placeholder.SignaturePlaceholderExtractor;
//import at.gv.egiz.pdfas.lib.impl.pdfbox2.placeholder.


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

	private DataSourceSelectComposite selectionComposite = null;

	private DataSourceSelectComposite getSelectionComposite() {
		if (this.selectionComposite == null) {
			this.selectionComposite =
					getStateMachine().getGUIProvider().createComposite(DataSourceSelectComposite.class, SWT.RESIZE, this);
		}
		return this.selectionComposite;
	}

	@Override
	public void run() {
		Status status = getStateMachine().getStatus();
		if (!(status.getPreviousState() instanceof PrepareConfigurationState) &&
			!(status.getPreviousState() instanceof OpenState))
		{
			ConfigProvider config = getStateMachine().getConfigProvider();
			status.setBKU(config.getDefaultBKU());
			status.setDocument(null);
			status.setSignaturePosition(config.getDefaultSignaturePosition());
		}
		
		if (status.getDocument() == null) {
			DataSourceSelectComposite selection = this
					.getSelectionComposite();

			getStateMachine().getGUIProvider().display(selection);
			selection.layout();

			status.setDocument(selection.getSelected());

			if (status.getDocument() == null) {
				// Not selected yet
				return;
			} 
		}
		log.debug("Got Datasource: " + getStateMachine().getStatus().getDocument().getAbsolutePath()); //$NON-NLS-1$

		// scan for signature placeholders
		// - see if we want to scan for placeholders in the settings
		if (getStateMachine().getConfigProvider().getUseMarker()) {
			try {

				// - scan for placeholders
				PDDocument pddocument = PDDocument.load(getStateMachine().getStatus().getDocument());
				
				// test other placeholders 
				List<String> fields = SignatureFieldsExtractor.findEmptySignatureFields(pddocument);
				for (String string : fields) {
					System.out.println(string);
				}
				
				SignaturePlaceholderData signaturePlaceholderData = SignaturePlaceholderExtractor.extract(pddocument,
						"1", 3); //$NON-NLS-1$

				if (null != signaturePlaceholderData || fields.size() > 0) {
					// create a dialog with ok and cancel buttons and a question
					// icon
					MessageBox dialog = new MessageBox(getStateMachine().getGUIProvider().getMainShell(),
							SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					dialog.setText(Messages.getString("dataSourceSelection.usePlaceholderTitle")); //$NON-NLS-1$
					dialog.setMessage(Messages.getString("dataSourceSelection.usePlaceholderText")); //$NON-NLS-1$

					// open dialog and await user selection
					if (SWT.YES == dialog.open()) {
						
						if (fields.size() > 0) {
							
							PlaceholderSelectionGui gui = new PlaceholderSelectionGui(getStateMachine().getGUIProvider().getMainShell(), 
									65570, "text","select the fields", fields); //$NON-NLS-1$ //$NON-NLS-2$
							int res = gui.open();
							if (res != -1) {
								
								System.out.println("ok pressed"); //$NON-NLS-1$
								getStateMachine().getStatus().setSearchForPlaceholderSignature(true);
								//TODO configure and skip placing 
								//TODO fix this
								try {
									String cfgPath = Constants.CONFIG_DIRECTORY + File.separator + "/cfg/advancedconfig.properties"; //$NON-NLS-1$
								FileInputStream in = new FileInputStream(cfgPath); 
								Properties props = new Properties();
								props.load(in);
								in.close();

								FileOutputStream out = new FileOutputStream(cfgPath); //$NON-NLS-1$
								props.setProperty("signature_field_name", fields.get(res)); //$NON-NLS-1$ //$NON-NLS-2$
								props.store(out, null);
								out.close();
								
								} catch (Exception e) {
									System.err.println(e.getMessage());
									System.err.println(e.getStackTrace());
								}
								
								this.setNextState(new BKUSelectionState(getStateMachine()));
								return; 
								
							}
							getStateMachine().getStatus().setSearchForPlaceholderSignature(false);
							
						} else {
						
						// if the user chooses to use the signature placeholder
						// - fill the position information so that we skip to
						// the
						// next stages without breaking stuff
						SignaturePosition position = new SignaturePosition(
								signaturePlaceholderData.getTablePos().getPosX(),
								signaturePlaceholderData.getTablePos().getPosY(),
								signaturePlaceholderData.getTablePos().getPage());
						status.setSignaturePosition(position);

						getStateMachine().getStatus().setSearchForPlaceholderSignature(true);
					} 
						} else {
						getStateMachine().getStatus().setSearchForPlaceholderSignature(false);
					}
				}
			} catch (PdfAsException e) {
				// fail silently. In case we got here no dialog has been shown.
				// Just
				// proceed with the usual process.
			} catch (IOException e) {
				// fail silently. In case we got here no dialog has been shown.
				// Just
				// proceed with the usual process.
			}
		}

		this.setNextState(new PositioningState(getStateMachine()));
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
		MainWindowBehavior behavior = getStateMachine().getStatus().getBehavior();
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
