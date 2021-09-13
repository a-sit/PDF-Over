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
package at.asit.pdfover.commons;

// Imports
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Localizes string messages for PDFOver GUI
 */
public class Messages {
	
	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(Messages.class);
	
	private static final String BUNDLE_NAME = "at.asit.pdfover.gui.messages"; //$NON-NLS-1$

	private static HashMap<Locale, ResourceBundle> bundles = new HashMap<>();

	private static Locale currentLocale = getDefaultLocale();
	
	private Messages() {
	}

	/**
	 * Get the closest match to the system default Locale out of the supported locales
	 * @return the default locale
	 */
	public static Locale getDefaultLocale() {
		Locale ld = Locale.getDefault();
		for (Locale l : Constants.SUPPORTED_LOCALES) {
			if (l.equals(ld) || l.getLanguage().equals(ld.getLanguage()))
				return l;
		}
		return Constants.SUPPORTED_LOCALES[0];
	}

	/**
	 * Sets the currently used locals
	 * @param locale
	 */
	public static void setLocale(Locale locale) {
		currentLocale = locale;
	}
	
	private static ResourceBundle getBundle(Locale locale) {
		if(!bundles.containsKey(locale)) {
			log.debug("Loading resource bundle for {}", locale); //$NON-NLS-1$
			ResourceBundle tmp = null;
			try {
				tmp = ResourceBundle.getBundle(BUNDLE_NAME, locale);
				log.debug("Received bundle for {}", tmp.getLocale()); //$NON-NLS-1$
			} catch(Exception e) {
				log.error("NO RESOURCE BUNDLE FOR {} {}", locale, e); //$NON-NLS-1$
				tmp = ResourceBundle.getBundle(BUNDLE_NAME);
			}
			if(tmp == null) {
				log.error("NO RESOURCE BUNDLE FOR {}", locale); //$NON-NLS-1$
				tmp = ResourceBundle.getBundle(BUNDLE_NAME);
			}
			bundles.put(locale, tmp);
			return tmp;
		}
		return bundles.get(locale);
	}
	
	/**
	 * Gets the localized message
	 * @param key
	 * @return the localized message
	 */
	public static String getString(String key) {
		try {
			return getBundle(currentLocale).getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	/**
	 * Gets the localized message
	 * @param key the key
	 * @param locale the locale to use
	 * @return the localized message
	 */
	public static String getString(String key, Locale locale) {
		try {
			return getBundle(locale).getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
