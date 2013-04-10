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
public class PostTanThread implements Runnable {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(PostTanThread.class);

	private MobileBKUState state;

	private MobileBKUHandler handler;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public PostTanThread(MobileBKUState state) {
		this.state = state;
		this.handler = state.getHandler();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			String responseData = this.handler.postTAN();

			// Now we have received some data lets check it:
			log.debug("Response from mobile BKU: " + responseData); //$NON-NLS-1$

			this.handler.handleTANResponse(responseData);

		} catch (Exception ex) {
			log.error("Error in PostTanThread", ex); //$NON-NLS-1$
			this.state.setThreadException(ex);
		} finally {
			this.state.invokeUpdate();
		}
	}

}
