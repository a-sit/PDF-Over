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
import org.eclipse.swt.widgets.Shell;

/**
 * 
 */
public class PasswordInputDialog extends InputDialog {

	/**
	 * @param parent
	 * @param title
	 * @param prompt
	 */
	public PasswordInputDialog(Shell parent, String title, String prompt) {
		super(parent, title, prompt);
		TEXT_FLAGS = SWT.BORDER | SWT.PASSWORD;
	}
}
