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
package at.asit.pdfover.gui.bku.mobile;

// Imports
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.protocol.Protocol;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.bku.BKUHelper;
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
	 * @return    the substring
	 * @throws Exception
	 *            not found
	 */
	public static String extractSubstring(String data, String start, String end)
			throws Exception {
		int startidx = data.indexOf(start);
		if (startidx > 0) {
			startidx = startidx + start.length();
			int endidx = data.indexOf(end, startidx);
			if (endidx > startidx) {
				return data.substring(startidx, endidx);
			}
			log.error("extracting substring: end not valid!: " + start + " ... " + end); //$NON-NLS-1$//$NON-NLS-2$
			throw new Exception("End string not available! Mobile BKU site changed?"); //$NON-NLS-1$
		}
		log.error("extracting substring: start not valid!: " + start + " ... " + end); //$NON-NLS-1$//$NON-NLS-2$
		throw new Exception("Start string not available! Mobile BKU site changed?"); //$NON-NLS-1$
	}

	/**
	 * Extracts an XML tag from data with the given param="value"
	 * 
	 * @param data
	 *            the whole data string
	 * @param tag
	 *            the tag name (empty string to match all tags)
	 * @param param
	 *            the parameter to look for
	 * @param value
	 *            the parameter value to look for
	 * @return    the found tag
	 * @throws Exception
	 *            not found
	 */
	public static String extractTagWithParam(String data, String tag,
			String param, String value) throws Exception {
		String start = '<' + tag;
		int startidx, endidx = 0;	
		while ((startidx = data.indexOf(start, endidx)) != -1) {
			endidx = data.indexOf('>', startidx);
			if (endidx == -1) {
				log.error("extracting tag: unterminated tag! " + tag + " (" + param + "=" + value + ")"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				throw new Exception("Tag not found! Mobile BKU site changed?"); //$NON-NLS-1$
			}
			String found = data.substring(startidx, endidx + 1);
			if (found.contains(param + "='" + value + "'") || //$NON-NLS-1$ //$NON-NLS-2$
			    found.contains(param + "=\"" + value + "\"")) //$NON-NLS-1$ //$NON-NLS-2$
				return found;
		}
		log.error("extracting tag: not found!: " + tag + " (" + param + "='" + value + "')"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		throw new Exception("Tag not found! Mobile BKU site changed?"); //$NON-NLS-1$
	}

	/**
	 * Extracts a parameter value from an XML tag from data with the given param="value"
	 * 
	 * @param data
	 *            the whole data string
	 * @param tag
	 *            the tag name (empty string to match all tags)
	 * @param param
	 *            the parameter to look for
	 * @param value
	 *            the parameter value to look for
	 * @param returnparam
	 *            the parameter whose value to return
	 * @return    the found tag
	 * @throws Exception
	 *            not found
	 */
	public static String extractValueFromTagWithParam(String data, String tag,
			String param, String value, String returnparam) throws Exception {
		String found = extractTagWithParam(data, tag, param, value);
		int startidx = found.indexOf(returnparam + '=');
		if (startidx == -1) {
			log.error("extracting tag: param not found! " + tag + " (" + param + "=" + value + ") - " + returnparam); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			throw new Exception("Tag not found! Mobile BKU site changed?"); //$NON-NLS-1$
		}
		startidx += returnparam.length() + 1;
		int endidx = found.indexOf(found.charAt(startidx), startidx + 1);
		if (endidx == -1) {
			log.error("extracting tag: unterminated param value! " + tag + " (" + param + "=" + value + ") - " + returnparam); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			throw new Exception("Tag not found! Mobile BKU site changed?"); //$NON-NLS-1$
		}
		return found.substring(startidx + 1, endidx);
	}
	
	/**
	 * This method is the same as the non optional method but instead of throwing the exception it returns null 
	 * @return the string or null 
	 */
	public static String extractValueFromTagWithParamOptional(String data, String tag,
			String param, String value, String returnparam) {
		String str; 
		try {
			str = extractValueFromTagWithParam(data, tag, param, value, returnparam);
		} catch (Exception e) {
			log.debug("Optional value is not available");
			str = null;
		}
		return str;
		
	}

	/**
	 * Extracts the content from an XML tag from data with the given param="value"
	 * 
	 * @param data
	 *            the whole data string
	 * @param tag
	 *            the tag name
	 * @param param
	 *            the parameter to look for
	 * @param value
	 *            the parameter value to look for
	 * @return    the found tag's content
	 * @throws Exception
	 *            not found
	 */
	public static String extractContentFromTagWithParam(String data, String tag,
			String param, String value) throws Exception {
		String found = extractTagWithParam(data, tag, param, value);
		int startidx = data.indexOf(found) + found.length();
		int endidx = data.indexOf("</" + tag + ">", startidx); //$NON-NLS-1$ //$NON-NLS-2$
		if (endidx == -1) {
			log.error("extracting tag: closing tag not found! " + tag + " (" + param + "=" + value + ")"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			throw new Exception("Tag not found! Mobile BKU site changed?"); //$NON-NLS-1$
		}
		return data.substring(startidx, endidx);
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

		number = number.trim();
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
		if (password.length() < 5 || password.length() > 200) {
			if (password.length() < 6) {
				throw new PasswordTooShortException();
			}
			throw new PasswordTooLongException();
		}
	}

	/**
	 * Removes file extension from URL
	 * 
	 * @param url
	 *            the url string
	 * @return the stripped url
	 */
	public static String stripQueryString(String url) {
		int pathidx = url.lastIndexOf('/');
		if (pathidx > 0) {
			return url.substring(0, pathidx);
		}
		return url;
	}

	/**
	 * Build a fully qualified URL out of a base URL plus a URL fragment
	 * @param fragment the URL fragment
	 * @param base the base URL
	 * @return the fully qualified URL
	 */
	public static String getQualifiedURL(String fragment, URL base) {
		if (fragment.startsWith("http:") || fragment.startsWith("https:")) //$NON-NLS-1$ //$NON-NLS-2$
			return fragment;
		int p = base.getPort();
		String port = ((p != -1) && (p != base.getDefaultPort())) ? ":" + p : ""; //$NON-NLS-1$ //$NON-NLS-2$
		if (fragment.startsWith("/")) { //$NON-NLS-1$
			return base.getProtocol() + "://" + base.getHost() + port + fragment; //$NON-NLS-1$
		}
		return stripQueryString(base.toString()) + "/" + fragment; //$NON-NLS-1$
	}

	/**
	 * Register our TrustedSocketFactory for https connections
	 */
	@SuppressWarnings("deprecation")
	public static void registerTrustedSocketFactory() {
		Protocol.registerProtocol("https", //$NON-NLS-1$
				new Protocol("https", new TrustedSocketFactory(), 443)); //$NON-NLS-1$
	}

	/**
	 * Get a HTTP Client instance
	 * @param status the mobile BKU status
	 * @return the HttpClient
	 */
	public static HttpClient getHttpClient(MobileBKUStatus status) {
		HttpClient client = BKUHelper.getHttpClient(true);
		Cookie[] cookies = status.getCookies();
		if (cookies != null) {
			HttpState state = new HttpState();
			for (Cookie cookie : cookies) {
				state.addCookie(cookie);
			}
			client.setState(state);
		}
		return client;
	}
	
	/***
	 * 
	 * @param htmlString describes the html data in String representation 
	 * @param attributeName is the attribute which should be selected 
	 * @return returns the attribute name or null otherswise
	 */
	public static String getDynamicNameAttribute(String htmlString, String attributeName) {
		
		Document doc = Jsoup.parse(htmlString);
		Elements inputs = doc.select("div input#" + attributeName);
		
		if (inputs.size() == 0 ) return null; 
		
		String name = inputs.get(0).attr("name"); 
		return name; 
	}
}
