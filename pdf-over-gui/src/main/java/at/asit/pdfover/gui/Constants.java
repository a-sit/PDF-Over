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

import java.io.File;
import java.util.Locale;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * Various constants
 */
public class Constants {

	/** Current display - used for Colors */
	private static Display display = Display.getCurrent();

	/** Supported locales */
	public static final Locale[] SUPPORTED_LOCALES = { Locale.GERMAN, Locale.ENGLISH };

	/** Main window height */
	public static final int MAINWINDOW_HEIGHT = 780;

	/** Main window width */
	public static final int MAINWINDOW_WIDTH = 600;

	/** Main bar height */
	public static final int MAINBAR_HEIGHT = 60;

	/** Main bar active background - light start of gradient */
	public static final Color MAINBAR_ACTIVE_BACK_LIGHT = new Color(display, 0xB4, 0xCD, 0xEC);

	/** Main bar active background - dark end of gradient */
	public static final Color MAINBAR_ACTIVE_BACK_DARK = new Color(display, 0x6B, 0xA5, 0xD9);

	/** Main bar inactive background */
	public static final Color MAINBAR_INACTIVE_BACK = new Color(display, 0xD4, 0xE7, 0xF1);

	/** Main bar active text color */
	public static final Color MAINBAR_ACTIVE_TEXTCOLOR = new Color(display, 0x00, 0x00, 0x00);

	/** Main bar inactive text color */
	public static final Color MAINBAR_INACTIVE_TEXTCOLOR = new Color(display, 0x40, 0x40, 0x40);

	/** Drop background color */
	public static final Color DROP_BACK = new Color(display, 0xFF, 0xFF, 0xFF);
	
	/** Drop border color */
	public static final Color DROP_BORDER_COLOR = MAINBAR_ACTIVE_BACK_LIGHT;

	/** Normal text size */
	public static final int TEXT_SIZE_NORMAL = 12;

	/** Button text size */
	public static final int TEXT_SIZE_BUTTON = 12;

	/** Small text size */
	public static final int TEXT_SIZE_SMALL = 10;

	/** Big text size */
	public static final int TEXT_SIZE_BIG = 14;

	/** How far to displace the signature with the arrow keys */
	public static final int SIGNATURE_KEYBOARD_POSITIONING_OFFSET = 15;

	/** File suffix for the signed document */
	public final static String SIGNED_SUFFIX = "_signed"; //$NON-NLS-1$

	/** Default Mobile BKU URL */
	public static final String DEFAULT_MOBILE_BKU_URL = "https://www.a-trust.at/mobile/https-security-layer-request/default.aspx"; //$NON-NLS-1$

	/** Configuration directory */
	public static String CONFIG_DIRECTORY = System.getProperty("user.home") + File.separator + ".pdfover"; //$NON-NLS-1$ //$NON-NLS-2$


	/** The default configuration file name */
	public static final String DEFAULT_CONFIG_FILENAME = "PDFOver.config"; //$NON-NLS-1$

	/** The default log4j file name */
	public static final String DEFAULT_LOG4J_FILENAME = "log4j.properties"; //$NON-NLS-1$

}
