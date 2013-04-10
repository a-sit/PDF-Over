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
package at.asit.pdfover.gui.controls;

// Imports
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import at.asit.pdfover.gui.Messages;

/**
 * 
 */
public class ErrorDialog {

	private MessageBox box;
	
	/**
	 * Message box buttons
	 */
	public enum ERROR_BUTTONS {
		/**
		 * Display only ok button
		 */
		OK,
		/**
		 * Display retry and cancel buttons
		 */
		RETRY_CANCEL,
		/**
		 * Display abort, retry and ignore buttons
		 */
		ABORT_RETRY_IGNORE
	};
	
	/**
	 * @param parent The parent shell
	 * @param message The error message
	 * @param button The buttons to be shown
	 */
	public ErrorDialog(Shell parent, String message, ERROR_BUTTONS button) {
		this.initialize(parent, message, button);
	}
	
	private void initialize(Shell parent, String message, ERROR_BUTTONS button) {
		int boxstyle = SWT.ICON_ERROR ;
		switch(button) {
		case OK:
			boxstyle |= SWT.OK;
			break;
		case RETRY_CANCEL:
			boxstyle |= SWT.RETRY| SWT.CANCEL;
			break;
		case ABORT_RETRY_IGNORE:
			boxstyle |= SWT.RETRY| SWT.ABORT | SWT.IGNORE;
			break;
		}
		
		this.box = new MessageBox(parent, boxstyle);
		this.box.setMessage(message);
		this.box.setText(Messages.getString("error.Title")); //$NON-NLS-1$
	}

	/**
	 * Open error dialog
	 * 
	 * @return SWT.OK | SWT.IGNORE | SWT.ABORT | SWT.RETRY | SWT.CANCEL
	 */
	public int open() {
		return this.box.open();
	}
}
