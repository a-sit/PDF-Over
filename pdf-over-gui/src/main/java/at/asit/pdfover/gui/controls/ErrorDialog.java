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
	 * @param parent
	 * @param message
	 * @param canRetry
	 */
	public ErrorDialog(Shell parent, String message, boolean canRetry) {
		int boxstyle = SWT.ICON_ERROR ;
		if(canRetry) {
			boxstyle |= SWT.RETRY| SWT.CANCEL;
		} else {
			boxstyle |= SWT.OK;
		}
		
		this.box = new MessageBox(parent, boxstyle);
		this.box.setMessage(message);
		this.box.setText(Messages.getString("error.title")); //$NON-NLS-1$
	}

	/**
	 * Open error dialog
	 * 
	 * @return if the user wants to retry the action which caused the error
	 */
	public boolean open() {
		int rc = this.box.open();
		if(rc == SWT.RETRY) {
			return true;
		} 
		return false;
	}
}
