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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.controls.Dialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.Dialog.ICON;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.utils.SWTUtils;
import at.asit.pdfover.commons.Messages;
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

	private Link lnk_saved_file;

	private Button btn_save;

	private Label lbl_success_message;

	private DocumentSource signedDocument;

	private File inputFile;

	String outputDir = null;

	String tempDirectory = null;

	File outputFile = null;

	private boolean saveFailed = false;

	private String postFix = null;

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

		this.lbl_success_message = new Label(this, SWT.NATIVE | SWT.RESIZE);
		FormData fd_lbl_success_message = new FormData();
		fd_lbl_success_message.top = new FormAttachment(40, 0);
		fd_lbl_success_message.left = new FormAttachment(0);
		fd_lbl_success_message.right = new FormAttachment(100);
		this.lbl_success_message.setLayoutData(fd_lbl_success_message);
		this.lbl_success_message.setAlignment(SWT.CENTER);

		FontData[] fD1 = this.lbl_success_message.getFont().getFontData();
		fD1[0].setHeight(Constants.TEXT_SIZE_BIG);
		this.lbl_success_message.setFont(new Font(Display.getCurrent(), fD1[0]));

		this.lnk_saved_file = new Link(this, SWT.NATIVE | SWT.RESIZE);
		FormData fd_lnk_saved_file = new FormData();
		fd_lnk_saved_file.top = new FormAttachment(this.lbl_success_message, 10);
		fd_lnk_saved_file.left = new FormAttachment(this.lbl_success_message, 0,
				SWT.CENTER);
		// fd_lnk_saved_file.right = new FormAttachment(100);
		this.lnk_saved_file.setLayoutData(fd_lnk_saved_file);

		this.lnk_saved_file.addSelectionListener(new OpenSelectionListener());

		FontData[] fD2 = this.lnk_saved_file.getFont().getFontData();
		fD2[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lnk_saved_file.setFont(new Font(Display.getCurrent(), fD2[0]));

		this.btn_save = new Button(this, SWT.NATIVE | SWT.RESIZE);

		FontData[] fD_btn_save = this.btn_save.getFont().getFontData();
		fD_btn_save[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btn_save.setFont(new Font(Display.getCurrent(), fD_btn_save[0]));

		FormData fd_btn_save = new FormData();
		fd_btn_save.top = new FormAttachment(this.lnk_saved_file, 10);
		fd_btn_save.left = new FormAttachment(this.lnk_saved_file, 0,
				SWT.CENTER);
		this.btn_save.setLayoutData(fd_btn_save);

		this.btn_save.addSelectionListener(new SaveSelectionListener());
		enableSaveButton(false);

		reloadResources();
	}

	/**
	 * @param outputDir
	 *            the outputDir to set
	 */
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	/**
	 * @return the outputDir
	 */
	public String getOutputDir() {
		return this.outputDir;
	}

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
	 * @param tempDirectory
	 */
	public void setTempDir(String tempDirectory) {
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

	/**
	 * Return whether the last save was successful
	 * @return whether the last save was successful
	 */
	public boolean getSaveSuccessful() {
		return !this.saveFailed;
	}

	private void enableSaveButton(boolean doEnable)
	{
		this.btn_save.setEnabled(doEnable);
		this.btn_save.setVisible(doEnable);
	}

	public void setSaveFilePostFix(String postFix){
		this.postFix = postFix;
	}

	public String getSaveFilePostFix(){
		if (this.postFix == null){
			this.postFix = Constants.DEFAULT_POSTFIX;
		}
		return this.postFix;
	}

	/**
	 * Saves the signed document.
	 *
	 * If user has a default output directory set, try to save there.
	 * If not (or if directory unavailable), ask user for location.
	 */
	public void saveDocument() {
		File inputFolder = getInputFile().getAbsoluteFile().getParentFile();
		String fileName = getInputFile().getName();
		String proposedName = getSignedFileName(fileName);
		String outputFileName;

		String outputFolder = getOutputDir();
		if (!this.saveFailed && outputFolder != null && !outputFolder.trim().isEmpty()) {
			// Output folder configured, try to save there

			File f = new File(outputFolder);
			if (f.isDirectory()) {
				if (!outputFolder.endsWith(File.separator)) {
					outputFolder += File.separator;
				}
				outputFileName = outputFolder + proposedName;
			} else {
				outputFileName = outputFolder;
			}
		} else {
			// Ask user where to save

			FileDialog save = new FileDialog(this.getShell(),
					SWT.SAVE | SWT.NATIVE);
			save.setFilterExtensions(new String[] { "*.pdf", "*" });
			save.setFilterNames(new String[] {
					Messages.getString("common.PDFExtension_Description"),
					Messages.getString("common.AllExtension_Description")});
			save.setFilterPath(inputFolder.getAbsolutePath());
			save.setFileName(proposedName);

			outputFileName = save.open();
			inputFolder = null;
		}
		log.debug("Trying to save to '" + outputFileName + "'");

		this.outputFile = saveResultAsFile(inputFolder, outputFileName);
		this.saveFailed = (this.outputFile == null);

		// If saving failed, enable save button
		enableSaveButton(this.saveFailed);
		reloadResources();
		layout(true);
	}

	/**
	 * Save the signed document under the given filename
	 * @param inputFolder the Folder the original document is located at
	 * @param target the filename to save the document as
	 *
	 * @return saved File (or null if unsuccessful)
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private File saveResultAsFile(File inputFolder, String target) {
		if (target == null)
			return null;

		File targetFile = new File(target);
		if (!targetFile.isAbsolute())
			targetFile = new File(inputFolder, target);

		if (targetFile.exists()) {
			Dialog dialog = new Dialog(getShell(), Messages.getString("common.warning"),
					String.format(Messages.getString("output.file_ask_overwrite"), targetFile.getName()),
					BUTTONS.OK_CANCEL, ICON.QUESTION);
			if (dialog.open() == SWT.CANCEL)
			{
				return null;
			}
		}

		DocumentSource source = this.getSignedDocument();
		boolean retry;

		do {
			retry = false;
			try {
				FileOutputStream outstream = new FileOutputStream(targetFile);
				outstream.write(source.getByteArray(), 0,
						source.getByteArray().length);
				outstream.close();
			} catch (FileNotFoundException e) {
				log.warn("Failed to open output file", e);
				ErrorDialog dialog = new ErrorDialog(getShell(),
						String.format(Messages.getString("output.save_failed"),
								targetFile.getName(), e.getLocalizedMessage()),
						BUTTONS.RETRY_CANCEL);
				if (dialog.open() == SWT.CANCEL)
					return null;
				retry = true;
			} catch (IOException e) {
				log.error("I/O Error", e);
				ErrorDialog dialog = new ErrorDialog(getShell(),
						String.format(Messages.getString("output.save_failed"),
								targetFile.getName(), e.getLocalizedMessage()),
						BUTTONS.RETRY_CANCEL);
				if (dialog.open() == SWT.CANCEL)
					return null;
				retry = true;
			}
		} while (retry);

		if (!targetFile.exists())
		{
			log.error("Tried to save file " + targetFile.getName() +
					", but it doesn't exist");
			return null;
		}
		return targetFile;
	}

	/**
	 * Get the proposed filename of a signed document for a given input filename
	 * @param name input filename
	 * @return proposed output filename
	 */
	private String getSignedFileName(String name) {
		name = FilenameUtils.getName(name);
		String extension = FilenameUtils.getExtension(name);
		name = FilenameUtils.removeExtension(name);
		return name + getSaveFilePostFix() + FilenameUtils.EXTENSION_SEPARATOR  + extension;
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
				OutputComposite.this.saveDocument();
			} catch (Exception ex) {
				log.error("SaveSelectionListener: ", ex);
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
				if (OutputComposite.this.outputFile == null)
					return;

				if (!OutputComposite.this.outputFile.exists())
					return;

				// Normalize filename
				File f = new File(FilenameUtils.normalize(
						OutputComposite.this.outputFile.getAbsolutePath()));
				log.debug("Trying to open " + f.toString());
				// work around for the case of Linux and Java version 8
				if (isSpecialCase()) {
					reReloadResources(f.toString());
					return;
				}
				else if (Desktop.isDesktopSupported()) {
					Desktop.getDesktop().open(f);
				} else {
					log.info("AWT Desktop is not supported on this platform");
					Program.launch(f.getAbsolutePath());
				}
			} catch (IOException ex) {
				log.error("OpenSelectionListener: ", ex);
				ErrorDialog error = new ErrorDialog(getShell(),
						String.format(Messages.getString("error.FailedToOpenDocument"),
								ex.getLocalizedMessage()), BUTTONS.RETRY_CANCEL);
				if (error.open() == SWT.RETRY)
					widgetSelected(e);
			}
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.composites.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		// Nothing to do
		this.layout(true);
	}

	/**
	 * @return true when linux and java version <= 8
	 *
	 */
	public boolean isSpecialCase() {

		boolean isSCase = false;
		try {
			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("linux")) {
				String version = System.getProperty("java.version");
				if (version.contains(".")) {
					String[] parts = version.split("\\.");
					isSCase = Integer.valueOf(parts[0]) <= 8 ? true : false;
				} else {
					isSCase = Integer.valueOf(version) <= 8 ? true : false;
				}
			}
		} catch (Exception e) {
			log.debug("Error: " + e.getMessage());
			isSCase = false;
		}
		return isSCase;
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		this.lbl_success_message.setText(Messages
				.getString("output.success_message"));
		if (this.outputFile == null) {
			this.lnk_saved_file.setText(Messages
					.getString("output.link_save_message"));
		} else {
			this.lnk_saved_file.setText(Messages
					.getString("output.link_open_message"));
		}
		SWTUtils.setLocalizedText(btn_save, "common.Save");
	}

	/**
	 * @param str
	 */
	public void reReloadResources(String str) {
		SWTUtils.setLocalizedText(lbl_success_message, "output.success_message");
		if (this.outputFile == null) {
			this.lnk_saved_file.setText(Messages
					.getString("output.link_save_message"));
		} else {
			String str2 = "File location: " + str;
			this.lbl_success_message.setText(str2);
			this.lnk_saved_file.setText("");
		}
		SWTUtils.setLocalizedText(btn_save, "common.Save");
	}



}
