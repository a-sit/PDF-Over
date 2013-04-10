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

/**
 * 
 */
public class ATrustHelper {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(ATrustHelper.class);

	/**
	 * @param data
	 * @param start
	 * @param end
	 * @return
	 * @throws Exception
	 */
	public static String extractTag(String data, String start, String end) throws Exception {
		int startidx = data.indexOf(start);
		if(startidx > 0) {
			startidx = startidx+start.length();
			int endidx = data.indexOf(end, startidx);
			if(endidx > startidx) {
				return data.substring(startidx, endidx);
			} else {
				// TODO: throw exception
				throw new Exception("end tag not available!");
			}
		} else {
			// TODO: throw exception
			throw new Exception("start tag not available!");
		}
	}
	
	/**
	 * @param query
	 * @return
	 */
	public static String stripQueryString(String query) {
		int pathidx = query.lastIndexOf('/');
		if(pathidx > 0) {
			return query.substring(0, pathidx);
		}
		return query;
	}
}
