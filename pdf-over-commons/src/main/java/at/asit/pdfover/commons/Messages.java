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

import java.nio.charset.StandardCharsets;
// Imports
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;


/**
 * Localizes string messages for PDFOver GUI
 */
@Slf4j
public class Messages {

	private static final String BUNDLE_NAME = "at.asit.pdfover.gui.messages";

	private static HashMap<Locale, ResourceBundle> bundles = new HashMap<>();

	private static Locale currentLocale = getDefaultLocale();

	private Messages() {
	}

	/**
	 * Get the closest match to the system default Locale out of the supported locales
	 * @return the default locale
	 */
	public static @NonNull Locale getDefaultLocale() {
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
			log.debug("Loading resource bundle for {}", locale);
			ResourceBundle tmp = null;
			try {
				tmp = ResourceBundle.getBundle(BUNDLE_NAME, locale);
				log.debug("Received bundle for {}", tmp.getLocale());
			} catch(Exception e) {
				log.error("NO RESOURCE BUNDLE FOR {} {}", locale, e);
				tmp = ResourceBundle.getBundle(BUNDLE_NAME);
			}
			if(tmp == null) {
				log.error("NO RESOURCE BUNDLE FOR {}", locale);
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
	public static @NonNull String getString(String key) {
		return getString(key, currentLocale);
	}

	/**
	 * Gets the localized message
	 * @param key the key
	 * @param locale the locale to use
	 * @return the localized message
	 */
	public static @NonNull String getString(String key, Locale locale) {
		try {
			String value = getBundle(locale).getString(key);

			/* DIRTY HACK: this recognizes java 8 ("1.8") and older; these versions read .properties files as ISO-8859-1 instead of UTF-8 */
			if (System.getProperty("java.version").startsWith("1."))
				value = new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

			log.trace("[{}] {}: {} -> {}", new Object[]{System.getProperty("java.version"), currentLocale, key, value});
			return value;
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public static @NonNull String formatString(String key, Object... values) {
		return String.format(getString(key), values);
	}
}
