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
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.composites.PositioningComposite;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;
import at.asit.pdfover.signator.Emblem;
import at.asit.pdfover.signator.SignaturePosition;
import at.asit.pdfover.signer.pdfas.PdfAs4SignatureParameter;

/**
 * Decides where to position the signature block
 */
public class PositioningState extends State {

	/**
	 * @param stateMachine
	 */
	public PositioningState(StateMachine stateMachine) {
		super(stateMachine);
	}

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(PositioningState.class);

	private PositioningComposite positionComposite = null;

	private SignaturePosition previousPosition = null;


	private File loadedDocumentPath = null;
	private PDDocument document = null;

	private void closePDFDocument() {

		if (this.document != null)
		{
			try { this.document.close(); } catch (IOException e) { log.warn("Failed to close PDF", e); }
			this.document = null;
		}
		this.loadedDocumentPath = null;
	}

	private void openPDFDocument() throws IOException {
		closePDFDocument();
		File documentPath = getStateMachine().status.document;
		PDDocument pdf = null;
		try
		{
			pdf = PDDocument.load(documentPath);
			if (pdf.getNumberOfPages() > 0)
				pdf.getPage(0);
			else
				throw new IOException();
		}
		catch (InvalidPasswordException e) {
			throw new IOException(Messages.getString("error.PDFPwdProtected"), e);
		}
		catch (IOException e) {
			throw new IOException(Messages.getString("error.MayNotBeAPDF"), e);
		}
		this.document = pdf;
		this.loadedDocumentPath = documentPath;
	}

	private PositioningComposite getPositioningComposite(PDDocument document) {
		StateMachine stateMachine = getStateMachine();
		if (this.positionComposite == null) {
			this.positionComposite =
					stateMachine.createComposite(PositioningComposite.class, SWT.RESIZE, this);
			log.debug("Displaying " +  stateMachine.status.document);
			this.positionComposite.displayDocument(document);
		}

		ConfigurationManager config = stateMachine.configProvider;

		PdfAs4SignatureParameter param = new PdfAs4SignatureParameter();
		param.signatureProfile = config.getSignatureProfile();

		Emblem emblem = new Emblem(config.getDefaultEmblemPath());
		param.emblem = emblem;
		if (config.getSignatureNote() != null && !config.getSignatureNote().isEmpty())
			param.signatureNote = config.getSignatureNote();

		param.signatureLanguage = config.getSignatureLocale().getLanguage();
		param.enablePDFACompat = config.getSignaturePdfACompat();

		this.positionComposite.setPlaceholder(
				param.getPlaceholder(),
				config.getPlaceholderTransparency());

		if (this.previousPosition != null && !this.previousPosition.useAutoPositioning())
		{
			this.positionComposite.setPosition(
					this.previousPosition.getX(),
					this.previousPosition.getY(),
					this.previousPosition.getPage());
		}

		return this.positionComposite;
	}

	@Override
	public void run() {
		Status status = getStateMachine().status;
		if (!(status.getPreviousState() instanceof PositioningState) &&
			!(status.getPreviousState() instanceof OpenState))
		{
			this.previousPosition = status.signaturePosition;
			status.signaturePosition = null;
		}

		if ((this.document == null) ||
				(this.loadedDocumentPath != getStateMachine().status.document)) {
			log.debug("Checking PDF document for encryption");
			try {
				openPDFDocument();
			} catch (IOException e) {
				this.positionComposite = null;
				log.error("Failed to display PDF document", e);
				String message = e.getLocalizedMessage();
				if (message == null)
					message = Messages.getString("error.IOError");
				ErrorDialog dialog = new ErrorDialog(
						getStateMachine().getMainShell(),
						message, BUTTONS.RETRY_CANCEL);
				if(dialog.open() == SWT.RETRY) {
					run();
				} else {
					setNextState(new OpenState(getStateMachine()));
				}
				return;
			}
		}

		if (status.signaturePosition == null) {
			PositioningComposite position = null;
			try {
				position = this.getPositioningComposite(this.document);
			} catch(Exception ex) {
				log.error("Failed to create composite (probably a mac...)", ex);
				ErrorDialog dialog = new ErrorDialog(
						getStateMachine().getMainShell(),
						Messages.getString("error.PositioningNotPossible"), BUTTONS.OK);
				dialog.open();
				status.signaturePosition = new SignaturePosition();
				this.setNextState(new BKUSelectionState(getStateMachine()));
				return;
			}

			getStateMachine().display(position);

			status.signaturePosition = position.getPosition();

			if(status.signaturePosition != null) {
				this.setNextState(new BKUSelectionState(getStateMachine()));
			}

			this.positionComposite.requestFocus();
		} else {
			this.setNextState(new BKUSelectionState(getStateMachine()));
		}
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.State#cleanUp()
	 */
	@Override
	public void cleanUp() {
		if (this.positionComposite != null)
			this.positionComposite.dispose();
		closePDFDocument();
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
		behavior.setActive(Buttons.POSITION, true);
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
