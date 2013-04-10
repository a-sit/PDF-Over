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
package at.asit.pdfover.gui.workflow;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import at.asit.pdfover.gui.workflow.states.State;


/**
 * 
 */
public interface GUIProvider {
	/**
	 * Create a new Composite
	 * @param compositeClass The class of the Composite to create
	 * @param style the SWT style
	 * @param state the State this Composite belongs to
	 * @return the new Composite
	 */
	public <T> T createComposite(Class<T> compositeClass, int style, State state);

	/**
	 * Display the composite as top most in main window
	 * @param composite the composite
	 */
	public void display(final Composite composite);
	
	/**
	 * Gets the main window shell
	 * @return the main window shell
	 */
	public Shell getMainShell();
	
	/**
	 * Reloads the resources on active gui components
	 */
	public void reloadResources();
}
