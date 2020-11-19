package at.asit.pdfover.signer.pdfas;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import javax.activation.DataSource;

import at.asit.pdfover.commons.Profile;
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
import at.gv.egiz.pdfas.lib.api.sign.IPlainSigner;
import at.gv.egiz.pdfas.lib.api.sign.SignParameter;
import at.gv.egiz.pdfas.sigs.pades.PAdESSigner;
import at.gv.egiz.sl.util.ISLConnector;
import at.knowcenter.wag.egov.egiz.pdf.TablePos;

/**
 * PDF AS Signer Implementation
 */
public class PdfAs4Signer implements Signer {

	
	

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
		String sigPos = null; 
		if (sign_para.getSignaturePosition() != null) {
			sigPos = sign_para.getPdfAsSignaturePosition();
		}
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
		if (sigPos != null) {
			param.setSignaturePosition(sigPos);
		}
		param.setSignatureProfileId(sigProfile);
		String id = UUID.randomUUID().toString();
		param.setTransactionId(id);

		if (parameter.isSearchForPlaceholderSignatures()) {
			param.getConfiguration().setValue(IConfigurationConstants.PLACEHOLDER_MODE, "3");
			param.getConfiguration().setValue(IConfigurationConstants.PLACEHOLDER_SEARCH_ENABLED, IConfigurationConstants.TRUE);
		}
		


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
			config.setValue(IConfigurationConstants.SL_REQUEST_TYPE,
					sstate.getUseBase64Request() ?
							IConfigurationConstants.SL_REQUEST_TYPE_BASE64 :
								IConfigurationConstants.SL_REQUEST_TYPE_UPLOAD);

			IPlainSigner signer;
			if (sstate.hasBKUConnector()) {
				ISLConnector connector = new PdfAs4BKUSLConnector(sstate.getBKUConnector());
				signer = new PAdESSigner(connector);
			} else if (sstate.hasKSSigner()) {
				signer = sstate.getKSSigner();
			} else {
				throw new SignatureException("SigningState doesn't have a signer");
			}
			param.setPlainSigner(signer);

			pdfas.sign(param);

			SignResultImpl result = new SignResultImpl();
			
			if (param.getSignaturePosition() != null) {
				TablePos tp = new TablePos(param.getSignaturePosition());
				SignaturePosition sp;
				if (tp.isXauto() && tp.isYauto())
					sp = new SignaturePosition();
				else if (tp.isPauto())
					sp = new SignaturePosition(tp.getPosX(), tp.getPosY());
				else if (param.getSignatureProfileId().contains(Profile.AMTSSIGNATURBLOCK.getName()))
					sp = new SignaturePosition();
				else
					sp = new SignaturePosition(tp.getPosX(), tp.getPosY(), tp.getPage());
				result.setSignaturePosition(sp);
			}

			result.setSignedDocument(new ByteArrayDocumentSource(sstate.getOutput().toByteArray()));
			return result;
		} catch (PdfAsException | PDFASError e) {
			throw new SignatureException(e);
		}
	}

	@Override
	public SignatureParameter newParameter() {
		return new PdfAs4SignatureParameter();
	}
}
