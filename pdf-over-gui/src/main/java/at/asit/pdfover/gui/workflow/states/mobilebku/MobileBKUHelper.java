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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.exceptions.InvalidNumberException;
import at.asit.pdfover.gui.exceptions.InvalidPasswordException;
import at.asit.pdfover.gui.exceptions.PasswordTooLongException;
import at.asit.pdfover.gui.exceptions.PasswordTooShortException;

/**
 * 
 */
public class MobileBKUHelper {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(MobileBKUHelper.class);

	/**
	 * Regular expression for mobile phone numbers: this allows the entry of
	 * mobile numbers in the following formats:
	 * 
	 * +(countryCode)99999999999 00(countryCode)99999999999 099999999999
	 * 1030199999999999 (A-Trust Test bku)
	 */
	private static final String NUMBER_REGEX = "^((\\+[\\d]{2})|(00[\\d]{2})|(0)|(10301))([1-9][\\d]+)$"; //$NON-NLS-1$

	/**
	 * Extracts a substring from data starting after start and ending with end
	 * 
	 * @param data
	 *            the whole data string
	 * @param start
	 *            the start marker
	 * @param end
	 *            the end marker
	 * @return the substring
	 * @throws Exception
	 */
	public static String extractTag(String data, String start, String end)
			throws Exception {
		int startidx = data.indexOf(start);
		if (startidx > 0) {
			startidx = startidx + start.length();
			int endidx = data.indexOf(end, startidx);
			if (endidx > startidx) {
				return data.substring(startidx, endidx);
			}
			log.error("extracting Tag: end tag not valid!: " + start + " ... " + end); //$NON-NLS-1$//$NON-NLS-2$
			throw new Exception("end tag not available!"); //$NON-NLS-1$
		}
		log.error("extracting Tag: start tag not valid!: " + start + " ... " + end); //$NON-NLS-1$//$NON-NLS-2$
		throw new Exception("start tag not available!"); //$NON-NLS-1$
	}

	/**
	 * Validates the Mobile phone number
	 * 
	 * @param number
	 * @return the normalized Phone number
	 * @throws InvalidNumberException
	 */
	public static String normalizeMobileNumber(String number)
			throws InvalidNumberException {
		// Verify number and normalize

		// Compile and use regular expression
		Pattern pattern = Pattern.compile(NUMBER_REGEX);
		Matcher matcher = pattern.matcher(number);

		if (!matcher.find()) {
			throw new InvalidNumberException();
		}

		if (matcher.groupCount() != 6) {
			throw new InvalidNumberException();
		}

		String countryCode = matcher.group(1);

		String normalNumber = matcher.group(6);

		if (countryCode.equals("10301")) { //$NON-NLS-1$
			// A-Trust Testnumber! Don't change
			return number;
		}

		countryCode = countryCode.replace("00", "+"); //$NON-NLS-1$ //$NON-NLS-2$

		if (countryCode.equals("0")) { //$NON-NLS-1$
			countryCode = "+43"; //$NON-NLS-1$
		}

		return countryCode + normalNumber;
	}

	/**
	 * Validate given Password for Mobile BKU
	 * 
	 * @param password
	 * @throws InvalidPasswordException
	 */
	public static void validatePassword(String password)
			throws InvalidPasswordException {
		if (password.length() < 6 || password.length() > 20) {
			if (password.length() < 6) {
				throw new PasswordTooShortException();
			}
			throw new PasswordTooLongException();
		}
	}

	/**
	 * Removes file extension from URL
	 * 
	 * @param query
	 *            the url string
	 * @return the stripped url
	 */
	public static String stripQueryString(String query) {
		int pathidx = query.lastIndexOf('/');
		if (pathidx > 0) {
			return query.substring(0, pathidx);
		}
		return query;
	}

	/**
	 * Get a HTTP Client instance
	 * @return the HttpClient
	 */
	public static HttpClient getHttpClient() {
		HttpClient client = new HttpClient();
		client.getParams().setParameter("http.useragent", //$NON-NLS-1$
				Constants.USER_AGENT_STRING);

		String host = System.getProperty("http.proxyHost"); //$NON-NLS-1$
		String port = System.getProperty("http.proxyPort"); //$NON-NLS-1$
		if (host != null && !host.isEmpty() &&
				port != null && !port.isEmpty()) {
			int p = Integer.parseInt(port);
			client.getHostConfiguration().setProxy(host, p);
			String user = System.getProperty("http.proxyUser"); //$NON-NLS-1$
			String pass = System.getProperty("http.proxyPassword"); //$NON-NLS-1$
			if (user != null && !user.isEmpty() && pass != null) {
				client.getState().setProxyCredentials(new AuthScope(host, p),
						new UsernamePasswordCredentials(user, pass));
			}
		}

		return client;
	}

	/**
	 * Register our TrustedSocketFactory for https connections
	 */
	@SuppressWarnings("deprecation")
	public static void registerTrustedSocketFactory() {
		Protocol.registerProtocol("https", //$NON-NLS-1$
				new Protocol("https", new TrustedSocketFactory(), 443)); //$NON-NLS-1$
	}
}
