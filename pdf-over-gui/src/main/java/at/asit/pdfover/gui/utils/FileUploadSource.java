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
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.methods.multipart.PartSource;

import at.asit.pdfover.signator.DocumentSource;

/**
 * 
 */
public class FileUploadSource implements PartSource {

	private DocumentSource source;

	/**
	 * Constructor
	 * 
	 * @param source
	 *            the source
	 */
	public FileUploadSource(DocumentSource source) {
		this.source = source;
	}

	@Override
	public long getLength() {
		return this.source.getLength();
	}

	@Override
	public String getFileName() {
		return "sign.pdf"; //$NON-NLS-1$
	}

	@Override
	public InputStream createInputStream() throws IOException {
		return this.source.getInputStream();
	}
}
