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

/**
 * 
 */
public interface StateMachine {
	/**
	 * Get the ConfigProvider
	 * @return the ConfigProvider
	 */
	public ConfigProvider getConfigProvider();
	
	/**
	 * Get the container Composite
	 * @return the container Composite
	 */
	public Composite getComposite();

	//public void display(Composite composite)
	/**
	 * Get the Status
	 * @return the Status
	 */
	public Status getStatus();

	/**
	 * Update state machine
	 * Calls the next state.
	 */
	public void update();

	/**
	 * Exit state machine execution
	 */
	public void exit();
}
