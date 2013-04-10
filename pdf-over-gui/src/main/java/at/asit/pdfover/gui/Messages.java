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
package at.asit.pdfover.gui;

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
	 * SFL4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(Messages.class);
	
	private static final String BUNDLE_NAME = "at.asit.pdfover.gui.messages"; //$NON-NLS-1$

	private static HashMap<Locale, ResourceBundle> bundles = new HashMap<Locale, ResourceBundle>();
	
	//private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
	//		.getBundle(BUNDLE_NAME);

	private static Locale currentLocale = Locale.getDefault();
	
	private Messages() {
	}

	/**
	 * Sets the currently used locals
	 * @param locale
	 */
	public static void setLocale(Locale locale) {
		currentLocale = locale;
	}
	
	private static ResourceBundle getBundle() {
		if(!bundles.containsKey(currentLocale)) {
			ResourceBundle tmp = null;
			try {
				tmp = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
			} catch(Exception e) {
				log.error("NO RESOURCE BUNDLE FOR " + currentLocale.toString(), e); //$NON-NLS-1$
				tmp = ResourceBundle.getBundle(BUNDLE_NAME);
			}
			if(tmp == null) {
				log.error("NO RESOURCE BUNDLE FOR " + currentLocale.toString()); //$NON-NLS-1$
				tmp = ResourceBundle.getBundle(BUNDLE_NAME);
			}
			bundles.put(currentLocale, tmp);
			return tmp;
		}
		return bundles.get(currentLocale);
	}
	
	/**
	 * Gets the localized message
	 * @param key
	 * @return the localized message
	 */
	public static String getString(String key) {
		try {
			return getBundle().getString(key);
			//return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
