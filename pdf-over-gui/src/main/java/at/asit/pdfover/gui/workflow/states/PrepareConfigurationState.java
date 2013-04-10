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
package at.asit.pdfover.gui.workflow.states;

//Imports
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.State;
import at.asit.pdfover.gui.workflow.states.BKUSelectionState.BKUs;
import at.asit.pdfover.signator.Signator;
import at.asit.pdfover.signator.SignaturePosition;

/**
 * Starting state of workflow proccess
 * 
 * Reads configuration, command arguments and initializes configured variables
 */
public class PrepareConfigurationState extends State {

	public final static String BKU_SELECTION_CONFIG = "DEFAULT_BKU";

	/**
	 * SFL4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(PrepareConfigurationState.class);

	@Override
	public void run(StateMachine stateMachine) {
		// TODO: Read config file and command line arguments
		// Set usedSignerLib ...

		// Create PDF Signer
		workflow.setPdfSigner(Signator.getSigner(workflow.getUsedSignerLib()));

		workflow.setParameter(workflow.getPdfSigner().newParameter());

		workflow.setSelectedBKU(PrepareConfigurationState.readSelectedBKU(workflow.getConfigurationValues()));
		
		workflow.getParameter().setSignaturePosition(readDefinedPosition(workflow.getConfigurationValues()));
		
		this.setNextState(new DataSourceSelectionState());
	}

	@Override
	public String toString() {
		return "PrepareConfigurationState";
	}

	/**
	 * Gets BKUS value from Properties
	 * @param props
	 * @return The BKUS value
	 */
	public static BKUs readSelectedBKU(final Properties props) {
		if (props.containsKey(BKU_SELECTION_CONFIG)) {
			String value = props.getProperty(BKU_SELECTION_CONFIG);
			value = value.trim().toLowerCase();

			if (value.equals(BKUs.LOCAL.toString().trim().toLowerCase())) {

				return BKUs.LOCAL;
			} else if (value
					.equals(BKUs.MOBILE.toString().trim().toLowerCase())) {
				return BKUs.MOBILE;
			}
		}
		return BKUs.NONE;
	}
	
	/**
	 * Gets BKUS value from Properties
	 * @param props
	 * @return The BKUS value
	 */
	public static SignaturePosition readDefinedPosition(final Properties props) {
		// TODO
		return null;
	}
}
