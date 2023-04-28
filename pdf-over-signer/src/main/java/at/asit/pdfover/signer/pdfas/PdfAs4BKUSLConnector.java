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
package at.asit.pdfover.signer.pdfas;

// Imports
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.signer.BkuSlConnector;
import at.asit.pdfover.signer.SignatureException;
import at.asit.pdfover.signer.UserCancelledException;
import at.asit.pdfover.signer.pdfas.exceptions.PdfAs4SLRequestException;
import at.gv.egiz.pdfas.common.exceptions.PDFIOException;
import at.gv.egiz.pdfas.common.exceptions.PdfAsException;
import at.gv.egiz.pdfas.common.exceptions.SLPdfAsException;
import at.gv.egiz.pdfas.common.utils.PDFUtils;
import at.gv.egiz.pdfas.lib.api.IConfigurationConstants;
import at.gv.egiz.pdfas.lib.api.sign.SignParameter;
import at.gv.egiz.sl.schema.CreateCMSSignatureResponseType;
import at.gv.egiz.sl.schema.ErrorResponseType;
import at.gv.egiz.sl.schema.InfoboxReadRequestType;
import at.gv.egiz.sl.schema.InfoboxReadResponseType;
import at.gv.egiz.sl.util.BaseSLConnector;
import at.gv.egiz.sl.util.RequestPackage;
import at.gv.egiz.sl.util.SLMarschaller;

/**
 *
 */
public class PdfAs4BKUSLConnector extends BaseSLConnector {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(PdfAs4BKUSLConnector.class);

	private BkuSlConnector connector;

	/**
	 * Constructor
	 * @param connector the BKU SL Connector
	 */
	public PdfAs4BKUSLConnector(BkuSlConnector connector) {
		this.connector = connector;
	}


	/* (non-Javadoc)
	 * @see at.gv.egiz.sl.util.ISLConnector#sendInfoboxReadRequest(at.gv.egiz.sl.schema.InfoboxReadRequestType, at.gv.egiz.pdfas.lib.api.sign.SignParameter)
	 */
	@Override
	public InfoboxReadResponseType sendInfoboxReadRequest(
			InfoboxReadRequestType request, SignParameter parameter)
			throws PdfAsException {
		JAXBElement<?> element = null;
		try {
			try {
				String slRequestString = SLMarschaller.marshalToString(this.of.createInfoboxReadRequest(request));

				String slResponse = this.connector.handleSLRequest(new PdfAs4SLRequest(slRequestString, null));

				element = (JAXBElement<?>) SLMarschaller.unmarshalFromString(slResponse);
			} catch (SignatureException e) {
				Throwable c = e;
				while (c.getCause() != null)
					c = c.getCause();
				if (c instanceof IllegalStateException) // TODO: this is a legacy hack, remove it?
					throw new UserCancelledException(e);
				else
					throw e;
			}
		} catch (JAXBException e) {
			throw new PDFIOException("error.pdf.io.03", e);
		} catch (PdfAs4SLRequestException e) {
			throw new PDFIOException("error.pdf.io.03", e);
		} catch (SignatureException e) {
			throw new PDFIOException("error.pdf.io.03", e);
		} catch (UserCancelledException e) {
			throw new SLPdfAsException(6001, "Vorgang durch den Benutzer abgebrochen.");
		}

		if (element == null) {
			throw new PDFIOException("error.pdf.io.04");
		}

		if (element.getValue() instanceof InfoboxReadResponseType) {
			return (InfoboxReadResponseType) element.getValue();
		} else if (element.getValue() instanceof ErrorResponseType) {
			ErrorResponseType errorResponseType = (ErrorResponseType)element.getValue();
			throw new SLPdfAsException(errorResponseType.getErrorCode(), errorResponseType.getInfo());
		}
		throw new PdfAsException("error.pdf.io.03");
	}

	/* (non-Javadoc)
	 * @see at.gv.egiz.sl.util.ISLConnector#sendCMSRequest(at.gv.egiz.sl.util.RequestPackage, at.gv.egiz.pdfas.lib.api.sign.SignParameter)
	 */
	@Override
	public CreateCMSSignatureResponseType sendCMSRequest(RequestPackage pack,
			SignParameter parameter) throws PdfAsException {
		JAXBElement<?> element = null;
		try {
			
			String slRequestString = SLMarschaller.marshalToString(this.of.createCreateCMSSignatureRequest(pack.getRequestType()));

			byte[] signatureData = pack.getSignatureData();
			if (IConfigurationConstants.SL_REQUEST_TYPE_UPLOAD.equals(parameter.getConfiguration().getValue(IConfigurationConstants.SL_REQUEST_TYPE)))
				signatureData = PDFUtils.blackOutSignature(signatureData, pack.getByteRange());

			PdfAs4SLRequest slRequest = new PdfAs4SLRequest(slRequestString, signatureData);

			try {
				String slResponse = this.connector.handleSLRequest(slRequest);

				element = (JAXBElement<?>) SLMarschaller.unmarshalFromString(slResponse);
			} catch (SignatureException e) {
				Throwable c = e;
				while (c.getCause() != null)
					c = c.getCause();
				if (c instanceof IllegalStateException) // TODO: this is a legacy hack, remove it?
					throw new UserCancelledException(e);
				else
					throw e;
			}
		} catch (JAXBException e) {
			throw new PDFIOException("error.pdf.io.03", e);
		} catch (PdfAs4SLRequestException e) {
			throw new PDFIOException("error.pdf.io.03", e);
		} catch (SignatureException e) {
			throw new PDFIOException("error.pdf.io.03", e);
		} catch (UserCancelledException e) {
			throw new SLPdfAsException(6001, "Vorgang durch den Benutzer abgebrochen.");
		}

		if (element == null) {
			throw new PDFIOException("error.pdf.io.05");
		}

		if (element.getValue() instanceof CreateCMSSignatureResponseType) {
			CreateCMSSignatureResponseType createCMSSignatureResponseType = (CreateCMSSignatureResponseType) element
					.getValue();
			log.trace(createCMSSignatureResponseType.toString());
			return createCMSSignatureResponseType;
		} else if (element.getValue() instanceof ErrorResponseType) {
			ErrorResponseType errorResponseType = (ErrorResponseType) element
					.getValue();
			throw new SLPdfAsException(errorResponseType.getErrorCode(), errorResponseType.getInfo());
		}
		throw new PdfAsException("error.pdf.io.03");
	}

}
