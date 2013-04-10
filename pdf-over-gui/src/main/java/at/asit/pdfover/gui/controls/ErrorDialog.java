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
import org.eclipse.swt.widgets.Shell;

import at.asit.pdfover.gui.utils.Messages;

/**
 * An error dialog
 */
public class ErrorDialog extends Dialog {
	/**
	 * @param parent The parent shell
	 * @param message The error message
	 * @param button The buttons to be shown
	 */
	public ErrorDialog(Shell parent, String message, BUTTONS button) {
		super(parent, Messages.getString("error.Title"), //$NON-NLS-1$
				message, button, ICON.ERROR);
	}
}
