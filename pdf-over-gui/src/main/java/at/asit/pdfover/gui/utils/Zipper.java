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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * Zipper/unzipper to backup/extract configuration
 */
@Slf4j
public class Zipper {

	/**
	 * Compresses the source path to Zip File output stream
	 * @param sourcePath
	 * @param os
	 * @throws IOException
	 */
	public static void zip(String sourcePath, OutputStream os) throws IOException {
		zip(sourcePath, os, false);
	}

		/**
	 * Compresses the source path to Zip File output stream
	 * @param sourcePath
	 * @param os
	 * @param doDelete whether to delete content after compression
	 * @throws IOException
	 */
	public static void zip(String sourcePath, OutputStream os, boolean doDelete) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(os);
		File dir = new File(sourcePath);
		zip(dir, dir.toURI(), zos, doDelete);
		zos.close();
	}

	private static void zip(File f, URI root, ZipOutputStream zos, boolean doDelete) throws IOException {
		if (f.isDirectory()) {
			File[] subDirs = f.listFiles();
			for (File subDir : subDirs) {
				zip(subDir, root, zos, doDelete);
				if (doDelete && !f.toURI().equals(root))
					subDir.delete();
			}
		} else {
			URI path = root.relativize(f.toURI());
			ZipEntry entry = new ZipEntry(path.toString());
			zos.putNextEntry(entry);
			byte[] buffer = new byte[4 * 1024 * 1024];
			int len;
			BufferedInputStream is = new BufferedInputStream(new FileInputStream(f));
			while ((len = is.read(buffer)) >= 0)
				zos.write(buffer, 0, len);
			is.close();
			zos.closeEntry();
			if (doDelete)
				f.delete();
		}
	}

	private static Path sanitizePath(String targetPath, String zipEntryName) throws IOException {
		Path targetPathP = Path.of(targetPath).toAbsolutePath().normalize();
		Path targetFile = Path.of(targetPath, zipEntryName).toAbsolutePath().normalize();
		if (targetFile.startsWith(targetPathP))
			return targetFile;
		else
			throw new IOException("Bad zip entry");
	}

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
			log.debug("entry: " + entry.getName() + ", "  ////
					+ entry.getSize());
			// consume all the data from this entry

			if (entry.isDirectory()) {
				log.debug("Extracting directory: " + entry.getName());

				File nDir = sanitizePath(targetPath, entry.getName()).toFile();
				if(!nDir.exists()) {
					if(!nDir.mkdir()) {
						throw new IOException("Failed to create dir: " + entry.getName());
					}
				}
				continue;
			}
			byte[] buffer = new byte[4 * 1024 * 1024];
			int len;

			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(
				sanitizePath(targetPath, entry.getName()).toFile()));

			while ((len = zis.read(buffer)) >= 0)
				out.write(buffer, 0, len);

			out.close();
		}
	}
}
