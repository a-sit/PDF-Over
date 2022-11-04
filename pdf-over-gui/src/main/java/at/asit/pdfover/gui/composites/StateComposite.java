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
import org.eclipse.swt.widgets.Composite;

import at.asit.pdfover.gui.workflow.states.State;

/**
 *	Composite interface for workflow state gui implementations
 */
public abstract class StateComposite extends Composite {

    /**
	 * Current State
	 */
	protected State state;

	/**
	 * The base class for state composites
	 *
	 * @param parent The parent Composite
	 * @param style The Composite style
	 * @param state The current State
	 */
	public StateComposite(Composite parent, int style, State state) {
		super(parent, style);
		this.state = state;
	}

	public final void doLayout() { this.layout(true,true); this.onDisplay(); }

	/**
	 * Reloads the localizeable resources
	 */
	public abstract void reloadResources();

	/**
	 * Called when the control is .display()ed
	 */
	public void onDisplay() {}

	// allow subclassing of SWT components
	@Override protected final void checkSubclass() {}
}
