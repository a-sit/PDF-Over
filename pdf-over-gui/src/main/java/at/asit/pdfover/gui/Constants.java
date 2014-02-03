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
import java.io.FileInputStream;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUs;

/**
 * Various constants
 */
public class Constants {

	/** Application name */
	public static final String APP_NAME = "PDF-Over"; //$NON-NLS-1$

	/** Application version */
	public static final String APP_VERSION = Constants.class.getPackage().getImplementationVersion();

	/** Application name + version */
	public static final String APP_NAME_VERSION = (APP_VERSION == null ? APP_NAME : APP_NAME + " v" + APP_VERSION); //$NON-NLS-1$

	static {
		// Has to be set before (implicitly) initializing Display
		Display.setAppName(APP_NAME);
	}

	/** Current display - used for Colors */
	private static Display display = Display.getCurrent();

	/** Supported locales */
	public static final Locale[] SUPPORTED_LOCALES = { Locale.GERMAN, Locale.ENGLISH };

	/** Configuration directory */
	public static String CONFIG_DIRECTORY = System.getProperty("user.home") + File.separator + ".pdf-over"; //$NON-NLS-1$ //$NON-NLS-2$

	/** The default configuration file name */
	public static final String DEFAULT_CONFIG_FILENAME = "PDF-Over.config"; //$NON-NLS-1$

	/** The default log4j file name */
	public static final String DEFAULT_LOG4J_FILENAME = "log4j.properties"; //$NON-NLS-1$

	/** File suffix for the signed document */
	public final static String SIGNED_SUFFIX = "_signed"; //$NON-NLS-1$

	/** Default Mobile BKU URL */
	public static final String DEFAULT_MOBILE_BKU_URL = "https://www.a-trust.at/mobile/https-security-layer-request/default.aspx"; //$NON-NLS-1$

	/** Default Mobile BKU type */
	public static final MobileBKUs DEFAULT_MOBILE_BKU_TYPE = MobileBKUs.A_TRUST;

	/** Default signature placeholder transparency */
	public static final int DEFAULT_SIGNATURE_PLACEHOLDER_TRANSPARENCY = 170;

	/** How far to displace the signature with the arrow keys */
	public static final int SIGNATURE_KEYBOARD_POSITIONING_OFFSET = 15;

	/** PDF-Over User Agent string */
	public static final String USER_AGENT_STRING = "PDF-Over " + (APP_VERSION == null ? "4.0" : APP_VERSION); //$NON-NLS-1$ //$NON-NLS-2$


	/* Configuration parameters */

	/** The bku config parameter */
	public static final String CFG_BKU = "BKU"; //$NON-NLS-1$

	/** The value for the Signature position in the configuration file
	 * values for this entry are:
	 *
	 * x=vx;y=vy;p=vp or auto
	 *
	 * vx:= float value
	 * vy:= float value
	 * vp:= integer value
	 */
	public static final String CFG_SIGNATURE_POSITION = "SIGNATURE_POSITION"; //$NON-NLS-1$

	/** This signature placeholder transparency config parameter (0-255) */
	public static final String  CFG_SIGNATURE_PLACEHOLDER_TRANSPARENCY = "SIGNATURE_PLACEHOLDER_TRANSPARENCY"; //$NON-NLS-1$

	/** The mobile number config parameter */
	public static final String CFG_MOBILE_NUMBER = "MOBILE_NUMBER"; //$NON-NLS-1$

	/** The signature note config parameter */
	public static final String CFG_SIGNATURE_NOTE = "SIGNATURE_NOTE"; //$NON-NLS-1$

	/** Mobile bku url config parameter */
	public static final String CFG_MOBILE_BKU_URL = "MOBILE_BKU_URL"; //$NON-NLS-1$

	/** Mobile bku type config parameter */
	public static final String CFG_MOBILE_BKU_TYPE = "MOBILE_BKU_TYPE"; //$NON-NLS-1$

	/** The emblem config parameter */
	public static final String CFG_EMBLEM = "EMBLEM"; //$NON-NLS-1$

	/** The locale config parameter */
	public static final String CFG_LOCALE = "LOCALE"; //$NON-NLS-1$

	/** The signature locale config parameter */
	public static final String CFG_SIGN_LOCALE = "SIGNLOCALE"; //$NON-NLS-1$

	/** The proxy host config parameter */
	public static final String CFG_PROXY_HOST = "PROXY_HOST"; //$NON-NLS-1$

	/** The proxy port config parameter */
	public static final String CFG_PROXY_PORT = "PROXY_PORT"; //$NON-NLS-1$

	/** The proxy username config parameter */
	public static final String CFG_PROXY_USER = "PROXY_USER"; //$NON-NLS-1$

	/** The proxy password config parameter */
	public static final String CFG_PROXY_PASS = "PROXY_PASS"; //$NON-NLS-1$

	/** The output folder config parameter */
	public static final String CFG_OUTPUT_FOLDER = "OUTPUT_FOLDER"; //$NON-NLS-1$

	/** The main window size (Format: width,height) */
	public static final String CFG_MAINWINDOW_SIZE = "MAINWINDOW_SIZE"; //$NON-NLS-1$

	/** The theme */
	public static final String CFG_THEME = "THEME"; //$NON-NLS-1$


	/* Theme constants */

	/** The available themes */
	public static enum Themes {
		/** Default theme */ DEFAULT,
		/** GemPlush theme */ GEMPLUSH
	};

	/** The used theme */
	public static final Themes THEME = getTheme();
	private static Themes getTheme() {
		File f = new File(CONFIG_DIRECTORY + File.separatorChar + DEFAULT_CONFIG_FILENAME);
		if (f.canRead())
		{
			try {
				Properties config = new Properties();
				config.load(new FileInputStream(f));
				return Themes.valueOf(config.getProperty(CFG_THEME).toUpperCase());
			} catch (Exception e) {
				// Ignore
			}
		}
		return Themes.DEFAULT;
	}

	/** Main window height */
	public static final int DEFAULT_MAINWINDOW_HEIGHT = 780;

	/** Main window width */
	public static final int DEFAULT_MAINWINDOW_WIDTH = 600;

	/** Main bar height */
	public static final int MAINBAR_HEIGHT = 60;

	/** Main bar active background - light start of gradient */
	public static final Color MAINBAR_ACTIVE_BACK_LIGHT = getMainbarActiveBackLight();
	private static Color getMainbarActiveBackLight() {
		switch (THEME) {
			default:
			case DEFAULT:
				return new Color(display, 0xB4, 0xCD, 0xEC);
			case GEMPLUSH:
				return new Color(display, 0xEC, 0xAD, 0xE7);
		}
	}

	/** Main bar active background - dark end of gradient */
	public static final Color MAINBAR_ACTIVE_BACK_DARK = getMainbarActiveBackDark();
	private static Color getMainbarActiveBackDark() {
		switch (THEME) {
			default:
			case DEFAULT:
				return new Color(display, 0x6B, 0xA5, 0xD9);
			case GEMPLUSH:
				return new Color(display, 0xD9, 0x53, 0x9C);
		}
	}

	/** Main bar inactive background */
	public static final Color MAINBAR_INACTIVE_BACK = getMainbarInactiveBack();
	private static Color getMainbarInactiveBack() {
		switch (THEME) {
			default:
			case DEFAULT:
				return new Color(display, 0xD4, 0xE7, 0xF1);
			case GEMPLUSH:
				return new Color(display, 0xF1, 0xD1, 0xE8);
		}
	}

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


	/* Resources */

	/** Shell icon resource */
	public static final String RES_ICON = "/icons/icon.png"; //$NON-NLS-1$

	/** Config image resource */
	public static final String RES_IMG_CONFIG = getResImgConfig();
	private static String getResImgConfig() {
		switch (THEME) {
			default:
			case DEFAULT:
				return "/img/config.png"; //$NON-NLS-1$
			case GEMPLUSH:
				return "/img/config_p.png"; //$NON-NLS-1$
		}
	}

	/** Config inactive image resource */
	public static final String RES_IMG_CONFIG_DISABLED = "/img/config_disabled.png"; //$NON-NLS-1$

	/** Error image resource */
	public static final String RES_IMG_ERROR = "/img/error.png"; //$NON-NLS-1$

	/** Card image resource */
	public static final String RES_IMG_CARD = "/img/karte.gif"; //$NON-NLS-1$

	/** Mobile phone image resource */
	public static final String RES_IMG_MOBILE = "/img/handy.gif"; //$NON-NLS-1$

	/** Package resource path */
	public static String RES_PKG_PATH = "/at/asit/pdfover/gui/"; //$NON-NLS-1$

	/** Zipped configuration resource */
	public static final String RES_CFG_ZIP = "/cfg/PDFASConfig.zip"; //$NON-NLS-1$

	/** Accepted certificate resource path */
	public static final String RES_CERT_PATH = "/certificates/"; //$NON-NLS-1$

	/** Accepted certificate list resource */
	public static final String RES_CERT_LIST = RES_CERT_PATH + "certificates.xml"; //$NON-NLS-1$
}
