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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.states.State;

/**
 * 
 */
public class WaitingForAppComposite extends StateComposite {
	private Label lbl_description;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 * @param state 
	 */
	public WaitingForAppComposite(Composite parent, int style, State state) {
		super(parent, style, state);
		setLayout(new FormLayout());
		
		this.lbl_description = new Label(this, SWT.NATIVE);
		FormData fd_lbl_description = new FormData();
		fd_lbl_description.bottom = new FormAttachment(50, -10);
		fd_lbl_description.left = new FormAttachment(0, +10);
		fd_lbl_description.right = new FormAttachment(100, -10);
		this.lbl_description.setLayoutData(fd_lbl_description);
		this.lbl_description.setAlignment(SWT.CENTER);
		this.lbl_description.setText(Messages.getString("waiting_for_app.message")); //$NON-NLS-1$
		
		ProgressBar progressBar = new ProgressBar(this, SWT.HORIZONTAL | SWT.INDETERMINATE);
		FormData fd_progressBar = new FormData();
		fd_progressBar.top = new FormAttachment(50, +10);
		fd_progressBar.bottom = new FormAttachment(50, +40);
		fd_progressBar.left = new FormAttachment(50, -100);
		fd_progressBar.right = new FormAttachment(50, +100);
		progressBar.setLayoutData(fd_progressBar);
		
		reloadResources();

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
		// Nothing to do here
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		this.lbl_description.setText(Messages.getString("waiting_for_app.message")); //$NON-NLS-1$
	}
}
