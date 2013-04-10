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
package at.asit.pdfover.gui.workflow.states.mobilebku;

// Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.workflow.states.MobileBKUState;

/**
 * 
 */
public class PostSLRequestThread implements Runnable {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(PostSLRequestThread.class);

	private MobileBKUState state;

	private String mobileBKUUrl;

	private MobileBKUHandler mobileBKUHandler;

	/**
	 * Constructor
	 * 
	 * @param state the MobileBKUState
	 */
	public PostSLRequestThread(MobileBKUState state) {
		this.state = state;
		this.mobileBKUUrl = state.getURL();
		this.mobileBKUHandler = state.getHandler();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {

			String responseData = this.mobileBKUHandler.postSLRequest(this.mobileBKUUrl);

			// Now we have received some data lets check it:
			log.debug("Response from mobile BKU: " + responseData); //$NON-NLS-1$

			this.mobileBKUHandler.handleSLRequestResponse(responseData);

			/*
			 * If all went well we can set the communication state to the new
			 * state
			 */
			this.state.setCommunicationState(MobileBKUCommunicationState.POST_NUMBER);
		} catch (Exception ex) {
			log.error("Error in PostSLRequestThread", ex); //$NON-NLS-1$
			this.state.setThreadException(ex);
		} finally {
			this.state.invokeUpdate();
		}
	}

}
