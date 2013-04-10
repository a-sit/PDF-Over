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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unzipper to extract default configuration
 */
public class Unzipper {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(Unzipper.class);

	/**
	 * Extracts Zip File input stream to target path 
	 * @param is
	 * @param targetPath
	 * @throws IOException
	 */
	public static void unzip(InputStream is, String targetPath) throws IOException {
		ZipInputStream zis = new ZipInputStream(is);
		ZipEntry entry;
		// while there are entries I process them
		while ((entry = zis.getNextEntry()) != null) {
			log.debug("entry: " + entry.getName() + ", "  //$NON-NLS-1$//$NON-NLS-2$
					+ entry.getSize());
			// consume all the data from this entry

			if (entry.isDirectory()) {
				log.debug("Extracting directory: " + entry.getName()); //$NON-NLS-1$
				
				File nDir =new File(targetPath + "/" + entry.getName()); //$NON-NLS-1$
				if(!nDir.exists()) {
					if(!nDir.mkdir()) {
						throw new IOException("Failed to create dir: " + entry.getName()); //$NON-NLS-1$
					}
				}
				continue;
			}
			byte[] buffer = new byte[1024];
			int len;
			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(targetPath + "/" + entry.getName())); //$NON-NLS-1$
			while ((len = zis.read(buffer)) >= 0)
				out.write(buffer, 0, len);
			
			out.close();
		}
	}
}
