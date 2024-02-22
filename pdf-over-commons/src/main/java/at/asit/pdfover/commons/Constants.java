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

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import lombok.extern.slf4j.Slf4j;


/**
 * Various constants
 */
@Slf4j
public class Constants {

	/** Application name */
	public static final String APP_NAME = "PDF-Over";

	/** Application version */
	public static final String APP_VERSION;
	static
	{
		String v = null;
		try {
			Properties props = new Properties();
			props.load(Constants.class.getClassLoader().getResourceAsStream("version.properties"));
			v = props.getProperty("version");
		} catch (Exception e) {
			log.warn("Failed to load project version", e);
			v = "UNKNOWN-SNAPSHOT";
		}
		APP_VERSION = v;
	}

	/** Application name + version */
	public static final String APP_NAME_VERSION = (APP_NAME + " v" + APP_VERSION);

	public static final String SIGNATURE_PROFILE = "SIGNATURE_PROFILE";
    public static final String DEFAULT_POSTFIX = "_signed";

    static {
		// Has to be set before (implicitly) initializing Display
		Display.setAppName(APP_NAME);
	}

	/**
	 *
	 */
	public static final String SIGNATURE_FIELD_NAME_CONF = "signature_field_name";

	/** Current display - used for Colors */
	private static Display display = Display.getCurrent();

	/** Supported locales */
	public static final Locale[] SUPPORTED_LOCALES = { Locale.GERMAN, Locale.ENGLISH };

	/** Configuration directory */
	public static final String CONFIG_DIRECTORY = System.getProperty("user.home") + File.separator + ".pdf-over";

	/** Directory for config backup on factory reset */
	public static final String CONFIG_BACKUP_DIRECTORY = System.getProperty("user.home") + File.separator + ".pdf-over.old";

	/** The default configuration file name */
	public static final String DEFAULT_CONFIG_FILENAME = "PDF-Over.config";

	/** The configuration version file name */
	public static final String CONFIG_VERSION_FILENAME = ".version";

	/**
	 * The minimum PDF-Over version that does not need a PDF-AS update */
	public static final String MIN_PDF_AS_CONFIG_VERSION = "4.4.6";

	/** The configuration backup filename */
	public static final String PDF_AS_CONFIG_BACKUP_FILENAME = "cfg_backup";

	/** File suffix for the signed document */
	public final static String SIGNED_SUFFIX = "_signed";

	public final static double PDF_UNITS_PER_MM = (595.0 / 210.0);

	/** The default target size for logo-only signatures (in mm) */
	public final static double DEFAULT_LOGO_ONLY_SIZE = 23;

	/** Local BKU URL */
	public static final String LOCAL_BKU_URL = "http://127.0.0.1:3495/http-security-layer-request";

	/** Default Mobile BKU URL */
	public static final URI MOBILE_BKU_URL = URI.create("https://service.a-trust.at/mobile/https-security-layer-request/default.aspx");
	public static final URI MOBILE_BKU_URL_TEST = URI.create("https://hs-abnahme.a-trust.at/mobile/https-security-layer-request/default.aspx");

	/** How far to displace the signature with the arrow keys */
	public static final int SIGNATURE_KEYBOARD_POSITIONING_OFFSET = 15;

	/** Current release file */
	public static final String CURRENT_RELEASE_URL = "https://updates.a-sit.at/pdf-over/Release.txt";

	/** Update URL */
	public static final String UPDATE_URL = "https://technology.a-sit.at/en/pdf-over/";

	/** True */
	public static final String TRUE = "true";

	/** False */
	public static final String FALSE = "false";

	/* Configuration parameters */

	/** The bku config parameter */
	public static final String CFG_BKU = "BKU";

	/**
	 * The value for the Signature position in the configuration file values for
	 * this entry are:
	 *
	 * x=vx;y=vy;p=vp or auto
	 *
	 * vx:= float value vy:= float value vp:= integer value
	 */
	public static final String CFG_SIGNATURE_POSITION = "SIGNATURE_POSITION";

	/** The use marker parameter (true/false) */
	public static final String CFG_USE_MARKER = "USE_MARKER";

	/** The use of signature fields instead of QR codes */
	public static final String CFG_USE_SIGNATURE_FIELDS = "USE_FIELDS";

	/** */
	public static final String CFG_ENABLE_PLACEHOLDER = "USE_PLACEHOLDER_SEARCH";

	/** The signature placeholder transparency config parameter (0-255) */
	public static final String CFG_SIGNATURE_PLACEHOLDER_TRANSPARENCY = "SIGNATURE_PLACEHOLDER_TRANSPARENCY";

	/** The mobile number config parameter */
	public static final String CFG_MOBILE_NUMBER = "MOBILE_NUMBER";
	public static final String CFG_MOBILE_PASSWORD_REMEMBER = "MOBILE_REMEMBER_PASSWORD";

	/** The signature note config parameter */
	public static final String CFG_SIGNATURE_NOTE = "SIGNATURE_NOTE";

	/** The signature locale config parameter */
	public static final String CFG_SIGNATURE_LOCALE = "SIGNLOCALE";

	/** The PDF/A-compatibility config parameter */
	public static final String CFG_SIGNATURE_PDFA_COMPAT = "SIGNATURE_PDFA_COMPAT";

	/** KeyStore enabled config parameter */
	public static final String CFG_KEYSTORE_ENABLED = "KEYSTORE_ENABLED";

	/** KeyStore file config parameter */
	public static final String CFG_KEYSTORE_FILE = "KEYSTORE_FILE";

	/** KeyStore type config parameter */
	public static final String CFG_KEYSTORE_TYPE = "KEYSTORE_TYPE";

	/** KeyStore alias config parameter */
	public static final String CFG_KEYSTORE_ALIAS = "KEYSTORE_ALIAS";

	public static final String CFG_KEYSTORE_PASSSTORETYPE = "KEYSTORE_PASS_STORE_TYPE";

	/** KeyStore store password config parameter */
	public static final String CFG_KEYSTORE_STOREPASS = "KEYSTORE_STOREPASS";

	/** KeyStore key password config parameter */
	public static final String CFG_KEYSTORE_KEYPASS = "KEYSTORE_KEYPASS";

	/** The emblem config parameter */
	public static final String CFG_EMBLEM = "EMBLEM";

	/** The emblem size config parameter */
	public static final String CFG_LOGO_ONLY_SIZE = "LOGO_ONLY_SIZE";

	/** The locale config parameter */
	public static final String CFG_LOCALE = "LOCALE";

	/** The update check config parameter */
	public static final String CFG_UPDATE_CHECK = "UPDATE_CHECK";

	/** The proxy host config parameter */
	public static final String CFG_PROXY_HOST = "PROXY_HOST";

	/** The proxy port config parameter */
	public static final String CFG_PROXY_PORT = "PROXY_PORT";

	/** The proxy username config parameter */
	public static final String CFG_PROXY_USER = "PROXY_USER";

	/** The proxy password config parameter */
	public static final String CFG_PROXY_PASS = "PROXY_PASS";

	/** The output folder config parameter */
	public static final String CFG_OUTPUT_FOLDER = "OUTPUT_FOLDER";

	public static final String CFG_POSTFIX = "SAVE_FILE_POSTFIX";

	/** The main window size (Format: width,height) */
	public static final String CFG_MAINWINDOW_SIZE = "MAINWINDOW_SIZE";

	/** The theme */
	public static final String CFG_THEME = "THEME";

	public static final String CFG_FIDO2_BY_DEFAULT = "FIDO2_DEFAULT";

	/** Download URL for accepted Certificates*/
	public static final String CFG_DOWNLOAD_URL="DOWNLOAD_URL";

	public static final String CFG_LAST_DIRECTORY = "LAST_SOURCE_DIRECTORY";

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
		if (f.canRead()) {
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
	public static final String RES_ICON = "/icons/icon.png";

	/** Config image resource */
	public static final String RES_IMG_CONFIG = getResImgConfig();

	private static String getResImgConfig() {
		switch (THEME) {
		default:
		case DEFAULT:
			return "/img/config.png";
		case GEMPLUSH:
			return "/img/config_p.png";
		}
	}

	/** Config inactive image resource */
	public static final String RES_IMG_CONFIG_DISABLED = "/img/config_disabled.png";

	/** Error image resource */
	public static final String RES_IMG_ERROR = "/img/error.png";

	/** Card image resource */
	public static final String RES_IMG_CARD = "/img/karte.png";

	/** Mobile phone image resource */
	public static final String RES_IMG_MOBILE = "/img/id_austria.png";

	/** FIDO2 logo resource */
	public static final String RES_IMG_FIDO2 = "/img/fido_logo.png";

	/** WebAuthN logo resource */
	public static final String RES_IMG_WEBAUTHN = "/img/webauthn-logo.png";

	/** Package resource path */
	public static String RES_PKG_PATH = "/at/asit/pdfover/gui/";

	/** Zipped configuration resource */
	public static final String RES_CFG_ZIP = "/cfg/PDFASConfig.zip";

	/** Accepted certificate resource path */
	public static final String RES_CERT_PATH = "/certificates/";

	/** Accepted certificate list resource */
	public static final String RES_CERT_LIST = RES_CERT_PATH + "certificates.xml";

	public static final String LABEL_TAN = "input_tan";

	public static final String LABEL_SIGN_BTN = "SignButton";

	public static final String LABEL_PHONE_NUMBER = "handynummer";

	public static final String LABEL_SIGN_PASS = "signaturpasswort";

	public static final String LABEL_BTN_IDF = "Button_Identification";

}
