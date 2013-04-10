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
package at.asit.pdfover.signator;

// Imports
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class PDFFileDocumentSource implements DocumentSource {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(PDFFileDocumentSource.class);

	private File file;
	
	private byte[] data = null;
	
	private int len = 0;
	
	/**
	 * Default constructor
	 * @param file
	 */
	public PDFFileDocumentSource(File file) {
		this.file = file;
	}
	
	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.DocumentSource#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(this.file);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.DocumentSource#getLength()
	 */
	@Override
	public int getLength() {
		if(this.file.length() > Integer.MAX_VALUE) {
			// TODO: Handle error somehow or use long ... (Ignore Files larger than 2 GB)
		}
		this.len = (int) this.file.length();
		return this.len;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.DocumentSource#getByteArray()
	 */
	@Override
	public byte[] getByteArray() {
		if(this.data == null) {
			try {
				InputStream stream = this.getInputStream();
				this.data = new byte[this.getLength()];
				stream.read(this.data);
				stream.close();
			} catch(IOException ex) {
				log.error("Failed to read file!", ex);
			}
		}
		return this.data;
	}

}
