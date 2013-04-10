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
package at.asit.pdfover.gui.utils;

// Imports
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.exceptions.InitializationException;

/**
 * 
 */
@SuppressWarnings("nls")
public class SWTLoader {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(SWTLoader.class);

	/**
	 * Load the SWT library for this OS
	 * @throws InitializationException Loading failed
	 */
	public static void loadSWT() throws InitializationException {
		try {
			URLClassLoader cl = (URLClassLoader)SWTLoader.class.getClassLoader();
			Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			addUrlMethod.setAccessible(true);

			String swtLibPath = getSwtJarPath() + getSwtJarName();
			log.debug("loading " + swtLibPath);
			File swtLib = new File(swtLibPath);
			if (!swtLib.isFile())
				throw new SWTLoadFailedException("Library " + swtLibPath + " not found");
			log.debug("Adding " + swtLib + " to ClassLoader...");
			addUrlMethod.invoke(cl, swtLib.toURI().toURL());
			log.debug("Success.");
		} catch (Exception e) {
			throw new InitializationException("SWT loading failed", e);
		}
	}

	private static int getArchBits() {
		String arch = System.getProperty("os.arch");
		return arch.contains("64") ? 64 : 32;
	}

	private static String getSwtJarName() throws SWTLoadFailedException {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win"))
			os = "windows";
		else if (os.contains("mac"))
			os = "mac";
		else if (os.contains("linux") || os.contains("nix"))
			os = "linux";
		else {
			log.error("Unknown OS: " + os);
			throw new SWTLoadFailedException("Unknown OS: " + os);
		}
		return "swt-" + os + "-" + getArchBits() + ".jar";
	}

	private static String getSwtJarPath() {
		String path = "";
		try {
			path = URLDecoder.decode(SWTLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
			int idx = path.lastIndexOf('/');
			idx = path.lastIndexOf('/', idx - 1);
			path = path.substring(0, idx + 1);
		} catch (UnsupportedEncodingException e) {
			// Ignore
		}
		return path + "lib-swt/";
	}

	private static class SWTLoadFailedException extends Exception {
		private static final long serialVersionUID = 1L;

		SWTLoadFailedException(String msg) {
			super(msg);
		}
	}

}
