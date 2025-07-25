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
package at.asit.pdfover.gui.bku;

// Imports
import java.io.IOException;
import java.net.Socket;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.utils.FileUploadSource;
import at.asit.pdfover.signer.BkuSlConnector;
import at.asit.pdfover.signer.SignatureException;
import at.asit.pdfover.signer.pdfas.PdfAs4SLRequest;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
public class LocalBKUConnector implements BkuSlConnector {

	private static boolean isAvailable = false;
	public static boolean IsAvailable() { return isAvailable; }
	private static Thread pollingThread = new Thread(() -> {
		while (true) {
			try (Socket socket = new Socket("127.0.0.1", 3495)) {
				isAvailable = true;
			} catch (IOException e) {
				isAvailable = false;
			}
			try { Thread.sleep(isAvailable ? 30000 : 5000); } catch (InterruptedException e) {}
		}
	}, "LocalBKUProbeThread");
	static {
		pollingThread.setDaemon(true);
		pollingThread.start();
	}
	

	/**
	 * HTTP Response server HEADER
	 */
	public final static String BKU_RESPONSE_HEADER_SERVER = "server";

	/**
	 * HTTP Response user-agent HEADER
	 */
	public final static String BKU_RESPONSE_HEADER_USERAGENT = "user-agent";

	/**
	 * HTTP Response SignatureLayout HEADER
	 */
	public final static String BKU_RESPONSE_HEADER_SIGNATURE_LAYOUT = "SignatureLayout";

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.BkuSlConnector#handleSLRequest(java.lang.String)
	 */
	@Override
	public String handleSLRequest(PdfAs4SLRequest request) throws SignatureException {
		try {
			HttpClient client = BKUHelper.getHttpClient();
			PostMethod method = new PostMethod(Constants.LOCAL_BKU_URL);

			String sl_request = request.xmlRequest;
			if (request.signatureData == null) {
				method.addParameter("XMLRequest", sl_request);
			} else {
				StringPart xmlpart = new StringPart(
						"XMLRequest", sl_request, "UTF-8");

				FilePart filepart = new FilePart("fileupload", new FileUploadSource(request.signatureData));

				Part[] parts = { xmlpart, filepart };

				method.setRequestEntity(new MultipartRequestEntity(parts, method
						.getParams()));
			}
			log.trace("SL REQUEST: " + sl_request);

			int returnCode = client.executeMethod(method);

			if (returnCode != HttpStatus.SC_OK) {
				throw new HttpException(
						method.getResponseBodyAsString());
			}

			return method.getResponseBodyAsString();
		} catch (HttpException e) {
			log.error("LocalBKUConnector: ", e);
			throw new SignatureException(e);
		} catch (IOException e) {
			log.error("LocalBKUConnector: ", e);
			throw new SignatureException(e);
		}
	}
}
