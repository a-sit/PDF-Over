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
package at.asit.pdfover.gui.composites;

// Imports
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Messages;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.DocumentSource;

/**
 * GUI component for Output State
 */
public class OutputComposite extends StateComposite {

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(OutputComposite.class);

	private File inputFile;

	/**
	 * Sets the input file
	 * 
	 * @param inputFile
	 *            the input file
	 */
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	/**
	 * Gets the input file
	 * 
	 * @return the input file
	 */
	public File getInputFile() {
		return this.inputFile;
	}

	/**
	 * Saves the file
	 * 
	 * @throws IOException
	 */
	void saveFile() throws IOException {
		FileDialog save = new FileDialog(OutputComposite.this.getShell(),
				SWT.SAVE | SWT.NATIVE);
		save.setFilterExtensions(new String[] { "*.pdf" }); //$NON-NLS-1$
		save.setFilterNames(new String[] { Messages
				.getString("common.PDFExtension_Description") }); //$NON-NLS-1$

		String proposed = OutputComposite.this.getInputFile().getAbsolutePath();

		String extension = FilenameUtils.getExtension(proposed);

		proposed = FilenameUtils.removeExtension(proposed);

		proposed = proposed + "_signed." + extension; //$NON-NLS-1$

		save.setFileName(proposed);

		String target = save.open();

		if (target != null) {
			File targetFile = new File(target);

			DocumentSource source = OutputComposite.this.getSignedDocument();

			FileOutputStream outstream = new FileOutputStream(targetFile);
			outstream.write(source.getByteArray(), 0,
					source.getByteArray().length);
			outstream.close();

			OutputComposite.this.savedFile = targetFile;
		}
	}

	/**
	 * SelectionListener for save button
	 */
	private final class SaveSelectionListener extends SelectionAdapter {
		/**
		 * Empty constructor
		 */
		public SaveSelectionListener() {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				OutputComposite.this.saveFile();
			} catch (Exception ex) {
				log.error("SaveSelectionListener: ", ex); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Selection Listener for open button
	 */
	private final class OpenSelectionListener extends SelectionAdapter {
		/**
		 * Empty constructor
		 */
		public OpenSelectionListener() {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				DocumentSource source = OutputComposite.this
						.getSignedDocument();

				if (source != null) {
					File open = OutputComposite.this.savedFile;
					if (open == null) {
						// Save as temp file ...
						java.util.Date date = new java.util.Date();
						String fileName = String.format(
								"%d_tmp_signed.pdf", date.getTime()); //$NON-NLS-1$
						open = new File(OutputComposite.this.tempDirectory
								+ "/" + fileName); //$NON-NLS-1$
						FileOutputStream outstream = new FileOutputStream(open);
						outstream.write(source.getByteArray(), 0,
								source.getByteArray().length);
						outstream.close();
					}

					if (open.exists()) {
						// Desktop supported check allready done in constructor
						Desktop.getDesktop().open(open);
						return;
					}
				} else {
					log.error("OutputComposite:OpenSelectionListener:widgetSelected -> source is null!!"); //$NON-NLS-1$
					ErrorDialog dialog = new ErrorDialog(
							getShell(),
							SWT.NONE,
							Messages.getString("error.FailedToGetSignedDocument"),//$NON-NLS-1$
							"", false); //$NON-NLS-1$
					dialog.open();
				}
			} catch (Exception ex) {
				log.error("OpenSelectionListener: ", ex); //$NON-NLS-1$
			}
		}
	}

	File savedFile = null;

	private DocumentSource signedDocument;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 *            The parent composite
	 * @param style
	 *            The swt style
	 * @param state
	 *            The owning state
	 */
	public OutputComposite(Composite parent, int style, State state) {
		super(parent, style, state);

		this.setLayout(new FormLayout());

		Button btn_open = new Button(this, SWT.NATIVE | SWT.RESIZE);
		btn_open.setText(Messages.getString("common.open")); //$NON-NLS-1$
		// Point mobile_size = btn_mobile.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		FormData fd_btn_open = new FormData();
		// fd_btn_open.left = new FormAttachment(40, 0);
		fd_btn_open.right = new FormAttachment(50, -5);
		fd_btn_open.top = new FormAttachment(40, 0);
		// fd_btn_open.bottom = new FormAttachment(55, 0);
		btn_open.setLayoutData(fd_btn_open);
		btn_open.addSelectionListener(new OpenSelectionListener());

		if (!Desktop.isDesktopSupported()) {
			btn_open.setEnabled(false);
		}

		Button btn_save = new Button(this, SWT.NATIVE | SWT.RESIZE);
		btn_save.setText(Messages.getString("common.Save")); //$NON-NLS-1$
		// Point card_size = btn_card.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		FormData fd_btn_save = new FormData();
		fd_btn_save.left = new FormAttachment(50, 5);
		// fd_btn_save.right = new FormAttachment(60, 0);
		fd_btn_save.top = new FormAttachment(40, 0);
		// fd_btn_save.bottom = new FormAttachment(55, 0);
		btn_save.setLayoutData(fd_btn_save);
		btn_save.addSelectionListener(new SaveSelectionListener());

		//this.pack();
	}

	String tempDirectory;

	/**
	 * @param tempDirectory
	 */
	public void setTempDirectory(String tempDirectory) {
		this.tempDirectory = tempDirectory;
	}

	/**
	 * Gets the signed document
	 * 
	 * @return the signed document
	 */
	public DocumentSource getSignedDocument() {
		return this.signedDocument;
	}

	/**
	 * Sets the signed document
	 * 
	 * @param signedDocument
	 *            the signed document
	 */
	public void setSignedDocument(final DocumentSource signedDocument) {
		this.signedDocument = signedDocument;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	private boolean save_showed = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.composites.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		// Nothing to do
		try {
			if (!this.save_showed) {
				OutputComposite.this.saveFile();
				this.save_showed = true;
			}
		} catch (Exception ex) {
			log.error("SaveSelectionListener: ", ex); //$NON-NLS-1$
		}
	}

}
