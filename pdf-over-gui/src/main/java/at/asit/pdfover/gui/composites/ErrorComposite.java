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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import at.asit.pdfover.gui.workflow.states.State;

/**
 * 
 */
public class ErrorComposite extends StateComposite {
	/**
	 * 
	 */
	private final class OkSelectionListener implements SelectionListener {
		@Override
		public void widgetSelected(SelectionEvent e) {
			ErrorComposite.this.userOk = true;
			ErrorComposite.this.state.updateStateMachine();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// Nothing to do
		}
	}

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(ErrorComposite.class);

	boolean userOk = false;
	
	/**
	 * Checks if the user has clicked OK
	 * @return
	 */
	public boolean isUserOk() {
		return userOk;
	}

	private Exception exception;
	
	
	/**
	 * Sets the Exception to present
	 * @param exception the exception
	 */
	public void setException(Exception exception) {
		this.exception = exception;
	}

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ErrorComposite(Composite parent, int style, State state) {
		super(parent, style, state);

		this.setLayout(new FormLayout());

		Label lbl_message = new Label(this, SWT.NATIVE | SWT.RESIZE);
		FormData fd_lbl_message = new FormData();
		fd_lbl_message.left = new FormAttachment(10, 0);
		fd_lbl_message.right = new FormAttachment(90, 0);
		fd_lbl_message.top = new FormAttachment(40, 0);
		fd_lbl_message.bottom = new FormAttachment(50, 0);
		lbl_message.setLayoutData(fd_lbl_message);
		lbl_message.setText(this.exception.getMessage());
		
		Button btn_ok = new Button(this, SWT.NATIVE | SWT.RESIZE);
		btn_ok.setText("OK");
		// Point mobile_size = btn_mobile.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		FormData fd_btn_ok = new FormData();
		fd_btn_ok.left = new FormAttachment(45, 0);
		fd_btn_ok.right = new FormAttachment(55, 0);
		fd_btn_ok.top = new FormAttachment(70, 0);
		fd_btn_ok.bottom = new FormAttachment(75, 0);
		btn_ok.setLayoutData(fd_btn_ok);
		btn_ok.addSelectionListener(new OkSelectionListener());
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		// TODO Auto-generated method stub
	}

}
