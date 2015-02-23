package at.asit.pdfover.signer.pdfas;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import javax.activation.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.signator.ByteArrayDocumentSource;
import at.asit.pdfover.signator.SignResult;
import at.asit.pdfover.signator.SignResultImpl;
import at.asit.pdfover.signator.SignatureException;
import at.asit.pdfover.signator.SignatureParameter;
import at.asit.pdfover.signator.SignaturePosition;
import at.asit.pdfover.signator.Signer;
import at.asit.pdfover.signator.SigningState;
import at.gv.egiz.pdfas.common.exceptions.PDFASError;
import at.gv.egiz.pdfas.common.exceptions.PdfAsException;
import at.gv.egiz.pdfas.lib.api.ByteArrayDataSource;
import at.gv.egiz.pdfas.lib.api.Configuration;
import at.gv.egiz.pdfas.lib.api.IConfigurationConstants;
import at.gv.egiz.pdfas.lib.api.PdfAs;
import at.gv.egiz.pdfas.lib.api.PdfAsFactory;
import at.gv.egiz.pdfas.lib.api.sign.SignParameter;
import at.gv.egiz.pdfas.sigs.pades.PAdESSigner;
import at.gv.egiz.sl.util.ISLConnector;
import at.knowcenter.wag.egov.egiz.pdf.TablePos;

/**
 * PDF AS Signer Implementation
 */
public class PdfAs4Signer implements Signer {

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(PdfAs4Signer.class);

	/**
	 * The template URL
	 */
	protected static final String URL_TEMPLATE = "http://pdfover.4.gv.at/template";

	/**
	 * Location reference string
	 */
	protected static final String LOC_REF = "<sl:LocRefContent>" + URL_TEMPLATE
			+ "</sl:LocRefContent>";

	@Override
	public SigningState prepare(SignatureParameter parameter)
			throws SignatureException {
		PdfAs4SignatureParameter sign_para = null;

		if (PdfAs4SignatureParameter.class.isInstance(parameter)) {
			sign_para = PdfAs4SignatureParameter.class.cast(parameter);
		}

		if (sign_para == null) {
			throw new SignatureException("Incorrect SignatureParameter!");
		}

		String sigProfile = sign_para.getPdfAsSignatureProfileId();
		String sigEmblem = (sign_para.getEmblem() == null ? null : sign_para.getEmblem().getFileName());
		String sigNote = sign_para.getProperty("SIG_NOTE");
		String sigPos = sign_para.getPdfAsSignaturePosition();

		PdfAs pdfas = PdfAs4Helper.getPdfAs();
		Configuration config = pdfas.getConfiguration();
		if (sigEmblem != null && !sigEmblem.trim().isEmpty()) {
			config.setValue("sig_obj." + sigProfile + ".value.SIG_LABEL", sigEmblem);
		}

		if(sigNote != null) {
			config.setValue("sig_obj." + sigProfile + ".value.SIG_NOTE", sigNote);
		}

		PdfAs4SigningState state = new PdfAs4SigningState();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		DataSource input = new ByteArrayDataSource(parameter.getInputDocument().getByteArray());
		SignParameter param = PdfAsFactory.createSignParameter(config, input, output);
		param.setSignaturePosition(sigPos);
		param.setSignatureProfileId(sigProfile);
		String id = UUID.randomUUID().toString();
		param.setTransactionId(id);
		state.setSignParameter(param);
		state.setOutput(output);
		return state;
	}

	@Override
	public SignResult sign(SigningState state) throws SignatureException {
		try {
			PdfAs4SigningState sstate = null;

			if (PdfAs4SigningState.class.isInstance(state)) {
				sstate = PdfAs4SigningState.class.cast(state);
			}

			if (sstate == null) {
				throw new SignatureException("Incorrect SigningState!");
			}

			// Retrieve objects
			PdfAs pdfas = PdfAs4Helper.getPdfAs();

			SignParameter param = sstate.getSignParameter();

			Configuration config = param.getConfiguration();
			log.debug("Use base64 request? " + sstate.getUseBase64Request());
			config.setValue(IConfigurationConstants.SL_REQUEST_TYPE,
					sstate.getUseBase64Request() ?
							IConfigurationConstants.SL_REQUEST_TYPE_BASE64 :
								IConfigurationConstants.SL_REQUEST_TYPE_UPLOAD);

			ISLConnector connector = new PdfAs4BKUSLConnector(sstate.getBKUConnector());
			param.setPlainSigner(new PAdESSigner(connector));

			pdfas.sign(param);

			// Preparing Result Response
			SignResultImpl result = new SignResultImpl();

//			// Set Signer Certificate
//			result.setSignerCertificate(..);
			
			// Set Signature position
			TablePos tp = new TablePos(param.getSignaturePosition());
			SignaturePosition sp;
			if (tp.isXauto() && tp.isYauto())
				sp = new SignaturePosition();
			else if (tp.isPauto())
				sp = new SignaturePosition(tp.getPosX(), tp.getPosY());
			else
				sp = new SignaturePosition(tp.getPosX(), tp.getPosY(), tp.getPage());
			result.setSignaturePosition(sp);

			// Set signed Document
			result.setSignedDocument(new ByteArrayDocumentSource(sstate.getOutput().toByteArray()));
			return result;
		} catch (PdfAsException e) {
			throw new SignatureException(e);
		} catch (PDFASError e) {
			throw new SignatureException(e);
		}
	}

	@Override
	public SignatureParameter newParameter() {
		return new PdfAs4SignatureParameter();
	}
}
