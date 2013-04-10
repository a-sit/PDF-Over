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
package at.asit.pdfover.gui.components;

// Imports
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.workflow.Workflow;

/**
 * 
 *
 */
public class PositioningComposite extends Composite implements StateComposite {

	/**
	 * SFL4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(PositioningComposite.class);

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public PositioningComposite(Composite parent, int style, Workflow workflow) {
		super(parent, style);
		Label test = new Label(this, SWT.NATIVE);
		test.setBounds(10, 20, 100, 50);
		test.setText("POSITIONING ----");
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.components.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		this.layout(true, true);
	}
}
