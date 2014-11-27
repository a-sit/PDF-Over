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
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.MainWindow.Buttons;
import at.asit.pdfover.gui.MainWindowBehavior;
import at.asit.pdfover.gui.composites.PositioningComposite;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.utils.SignaturePlaceholderCache;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.Status;
import at.asit.pdfover.signator.Emblem;
import at.asit.pdfover.signator.FileNameEmblem;
import at.asit.pdfover.signator.SignatureParameter;
import at.asit.pdfover.signator.SignaturePosition;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.decrypt.PDFAuthenticationFailureException;

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
	private static final Logger log = LoggerFactory
			.getLogger(PositioningState.class);

	private PositioningComposite positionComposite = null;

	private SignaturePosition previousPosition = null;

	private File previousDocument = null;

	private PDFFile document = null;

	private PDFFile getPDFDocument() throws IOException {
		PDFFile pdf = null;
		RandomAccessFile rafile = new RandomAccessFile(getStateMachine().getStatus().getDocument(), "r"); //$NON-NLS-1$
		FileChannel chan = rafile.getChannel();
		ByteBuffer buf = chan
				.map(FileChannel.MapMode.READ_ONLY, 0, chan.size());
		chan.close();
		rafile.close();
		try
		{
			pdf = new PDFFile(buf);
		}
		catch (PDFAuthenticationFailureException e) {
			throw new IOException(Messages.getString("error.PDFPwdProtected"), e); //$NON-NLS-1$
		}
		catch (IOException e) {
			throw new IOException(Messages.getString("error.MayNotBeAPDF"), e); //$NON-NLS-1$
		}
		if (pdf.getDefaultDecrypter().isEncryptionPresent())
			throw new IOException(Messages.getString("error.PDFProtected")); //$NON-NLS-1$
		return pdf;
	}

	private PositioningComposite getPositioningComposite(PDFFile document) {
		if (this.positionComposite == null) {
			this.positionComposite =
					getStateMachine().getGUIProvider().createComposite(PositioningComposite.class, SWT.RESIZE, this);
			log.debug("Displaying " +  getStateMachine().getStatus().getDocument()); //$NON-NLS-1$
			this.positionComposite.displayDocument(document);
		}
		// Update possibly changed values
		SignatureParameter param = getStateMachine().getPDFSigner().getPDFSigner().newParameter();
		Emblem emblem = new FileNameEmblem(getStateMachine().getConfigProvider().getDefaultEmblem());
		param.setEmblem(emblem);
		if(getStateMachine().getConfigProvider().getSignatureNote() != null && !getStateMachine().getConfigProvider().getSignatureNote().isEmpty()) {
			param.setProperty("SIG_NOTE", getStateMachine().getConfigProvider().getSignatureNote()); //$NON-NLS-1$
		}
		
		param.setSignatureLanguage(getStateMachine().getConfigProvider().getSignLocale().getLanguage());
		
		this.positionComposite.setPlaceholder(
				SignaturePlaceholderCache.getPlaceholder(param),
				param.getPlaceholderDimension().getWidth(),
				param.getPlaceholderDimension().getHeight(),
				getStateMachine().getConfigProvider().getPlaceholderTransparency());
		if (this.previousPosition != null && !this.previousPosition.useAutoPositioning())
			this.positionComposite.setPosition(
					this.previousPosition.getX(),
					this.previousPosition.getY(),
					this.previousPosition.getPage());
		
		return this.positionComposite;
	}

	@Override
	public void run() {
		Status status = getStateMachine().getStatus();
		if (!(status.getPreviousState() instanceof PositioningState) &&
			!(status.getPreviousState() instanceof OpenState))
		{
			this.previousPosition = status.getSignaturePosition();
			status.setSignaturePosition(null);
		}

		if ((this.document == null) ||
				(this.previousDocument != getStateMachine().getStatus().getDocument())) {
			this.document = null;
			log.debug("Checking PDF document for encryption"); //$NON-NLS-1$
			try {
				this.document = getPDFDocument();
				this.previousDocument = getStateMachine().getStatus().getDocument();
			} catch (IOException e) { 
				this.positionComposite = null;
				log.error("Failed to display PDF document", e); //$NON-NLS-1$
				String message = e.getLocalizedMessage();
				if (message == null)
					message = Messages.getString("error.IOError"); //$NON-NLS-1$
				ErrorDialog dialog = new ErrorDialog(
						getStateMachine().getGUIProvider().getMainShell(),
						message, BUTTONS.RETRY_CANCEL);
				if(dialog.open() == SWT.RETRY) {
					run();
				} else {
					setNextState(new OpenState(getStateMachine()));
				}
				return;
			}
		}

		if (status.getSignaturePosition() == null) {
			PositioningComposite position = null;
			try {
				position = this.getPositioningComposite(this.document);
			} catch(Exception ex) {
				log.error("Failed to create composite (probably a mac...)", ex); //$NON-NLS-1$
				ErrorDialog dialog = new ErrorDialog(
						getStateMachine().getGUIProvider().getMainShell(),
						Messages.getString("error.PositioningNotPossible"), BUTTONS.OK); //$NON-NLS-1$
				dialog.open();
				status.setSignaturePosition(new SignaturePosition());
				this.setNextState(new BKUSelectionState(getStateMachine()));
				return;
			}

			getStateMachine().getGUIProvider().display(position);
			
			status.setSignaturePosition(position.getPosition());
			
			if(status.getSignaturePosition() != null) {
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
		behavior.setActive(Buttons.POSITION, true);
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
