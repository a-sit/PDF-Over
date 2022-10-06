package at.asit.pdfover.signer.pdfas;

import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.UUID;

import javax.activation.DataSource;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Profile;
import at.asit.pdfover.signer.ByteArrayDocumentSource;
import at.asit.pdfover.signer.SignResult;
import at.asit.pdfover.signer.SignatureException;
import at.asit.pdfover.signer.SignaturePosition;
import at.asit.pdfover.signer.UserCancelledException;
import at.gv.egiz.pdfas.common.exceptions.PDFASError;
import at.gv.egiz.pdfas.common.exceptions.PdfAsException;
import at.gv.egiz.pdfas.common.exceptions.SLPdfAsException;
import at.gv.egiz.pdfas.lib.api.ByteArrayDataSource;
import at.gv.egiz.pdfas.lib.api.Configuration;
import at.gv.egiz.pdfas.lib.api.IConfigurationConstants;
import at.gv.egiz.pdfas.lib.api.PdfAs;
import at.gv.egiz.pdfas.lib.api.PdfAsFactory;
import at.gv.egiz.pdfas.lib.api.sign.IPlainSigner;
import at.gv.egiz.pdfas.lib.api.sign.SignParameter;
import at.gv.egiz.pdfas.sigs.pades.PAdESSigner;
import at.gv.egiz.sl.util.ISLConnector;
import at.knowcenter.wag.egov.egiz.pdf.TablePos;

/**
 * PDF AS Signer Implementation
 */
public class PdfAs4Signer {

	/**
	 * The template URL
	 */
	protected static final String URL_TEMPLATE = "http://pdfover.4.gv.at/template";

	/**
	 * Location reference string
	 */
	protected static final String LOC_REF = "<sl:LocRefContent>" + URL_TEMPLATE
			+ "</sl:LocRefContent>";

	public static PdfAs4SigningState prepare(PdfAs4SignatureParameter parameter) throws SignatureException {

		if (parameter == null) {
			throw new SignatureException("Incorrect SignatureParameter!");
		}

		String sigProfile = parameter.getPdfAsSignatureProfileId();
		String sigEmblem = (parameter.emblem == null ? null : parameter.emblem.getCachedFileName());
		String sigNote = parameter.signatureNote;
		String sigPos = null;
		if (parameter.signaturePosition != null) {
			sigPos = parameter.getPdfAsSignaturePosition();
		}

		PdfAs pdfas = PdfAs4Helper.getPdfAs();
		synchronized (PdfAs4Helper.class) {
			Configuration config = pdfas.getConfiguration();
			if (sigEmblem != null && !sigEmblem.trim().isEmpty()) {
				config.setValue("sig_obj." + sigProfile + ".value.SIG_LABEL", sigEmblem);
			}

			if(sigNote != null) {
				config.setValue("sig_obj." + sigProfile + ".value.SIG_NOTE", sigNote);
			}

			// TODO encapsulate this parameter magic in PdfAs4SignatureParameter
			if (parameter.signatureProfile == Profile.BASE_LOGO)
			{
				int emblemWidth = (parameter.emblem != null) ? parameter.emblem.getWidth() : 1;
				int emblemHeight = (parameter.emblem != null) ? parameter.emblem.getHeight() : 1;
				double aspectRatio = ((double)emblemWidth) / emblemHeight;
				double targetWidth = parameter.targetLogoSize * Constants.PDF_UNITS_PER_MM;
				double targetHeight = parameter.targetLogoSize * Constants.PDF_UNITS_PER_MM;
				if (aspectRatio < 1)
					targetWidth *= aspectRatio;
				else
					targetHeight /= aspectRatio;
				config.setValue("sig_obj." + sigProfile + ".table.main.Style.padding", "0");
				config.setValue("sig_obj." + sigProfile + ".pos", "w:"+targetWidth+";f:0");
				config.setValue("sig_obj." + sigProfile + ".table.main.Style.imagescaletofit", targetWidth+";"+targetHeight);
			}

			PdfAs4SigningState state = new PdfAs4SigningState();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			DataSource input = new ByteArrayDataSource(parameter.inputDocument.getByteArray());
			SignParameter param = PdfAsFactory.createSignParameter(config, input, output);
			if (sigPos != null) {
				param.setSignaturePosition(sigPos);
			}
			param.setSignatureProfileId(sigProfile);
			String id = UUID.randomUUID().toString();
			param.setTransactionId(id);

			if (parameter.searchForPlaceholderSignatures) {
				param.getConfiguration().setValue(IConfigurationConstants.PLACEHOLDER_MODE, "1");
				param.getConfiguration().setValue(IConfigurationConstants.PLACEHOLDER_SEARCH_ENABLED, IConfigurationConstants.TRUE);
			}

			state.signParameter = param;
			state.output = output;
			return state;
		}
	}

	public static SignResult sign(PdfAs4SigningState state) throws SignatureException, UserCancelledException {
		try {
			if (state == null) {
				throw new SignatureException("Incorrect SigningState!");
			}

			PdfAs pdfas = PdfAs4Helper.getPdfAs();
			synchronized (PdfAs4Helper.class) {
				// Retrieve objects
				SignParameter param = state.signParameter;

				Configuration config = param.getConfiguration();
				config.setValue(IConfigurationConstants.SL_REQUEST_TYPE,
						state.useBase64Request ?
								IConfigurationConstants.SL_REQUEST_TYPE_BASE64 :
									IConfigurationConstants.SL_REQUEST_TYPE_UPLOAD);

				IPlainSigner signer;
				if (state.bkuConnector != null) {
					ISLConnector connector = new PdfAs4BKUSLConnector(state.bkuConnector);
					signer = new PAdESSigner(connector);
				} else if (state.hasKeystoreSigner()) {
					signer = state.getKeystoreSigner();
				} else {
					throw new SignatureException("SigningState doesn't have a signer");
				}
				param.setPlainSigner(signer);

				pdfas.sign(param);

				SignResult result = new SignResult();

				if (param.getSignaturePosition() != null) {
					TablePos tp = new TablePos(param.getSignaturePosition());
					SignaturePosition sp;
					if (tp.isXauto() && tp.isYauto())
						sp = new SignaturePosition();
					else if (tp.isPauto())
						sp = new SignaturePosition(tp.getPosX(), tp.getPosY());
					else if (param.getSignatureProfileId().contains(Profile.AMTSSIGNATURBLOCK.name()))
						sp = new SignaturePosition();
					else
						sp = new SignaturePosition(tp.getPosX(), tp.getPosY(), tp.getPage());
					result.setSignaturePosition(sp);
				}

				result.setSignedDocument(new ByteArrayDocumentSource(state.output.toByteArray()));
				return result;
			}
		} catch (PdfAsException | PDFASError ex) {
			// workaround for PDF-AS nullpointerexception intercepting the actual exception
			// cf. issue #52
			// this is a bit of a hack...
			Exception e = ex;
			{
				if ((e instanceof PDFASError) && (e.getCause() instanceof NullPointerException))
					e = Objects.requireNonNullElse(PdfAs4BKUSLConnector.originalExceptionSwallowedByPDFASNPE, e);
			}

			{
				Throwable rootCause = e;
				while (rootCause.getCause() != null)
					rootCause = rootCause.getCause();
				try { /* error code 60xx is user cancellation */
					int errorCode = ((SLPdfAsException)rootCause).getCode();
					if ((6000 <= errorCode) && (errorCode <= 6099))
						throw new UserCancelledException();
				} catch (ClassCastException e2) { /* fall through to wrapped throw */}
			}
			
			throw new SignatureException(e);
		}
	}
}
