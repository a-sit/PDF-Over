package at.asit.pdfover.signator;

import java.util.EnumMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PDF Signator Interface
 */
public class Signator {

	private static Logger log = LoggerFactory.getLogger(Signator.class);

	/**
	 * List of available PDF signing libraries
	 */
	public enum Signers {
		/**
		 * PDF-AS
		 */
		PDFAS
	};

	private static Map<Signers, SignerFactory> factoryMap;

	static {
		factoryMap = new EnumMap<Signers, SignerFactory>(Signers.class);

		try {
			Class<?> pdfAsClass = Class.forName("at.asit.pdfover.signer.pdfas.PDFASSignerFactory");
			SignerFactory factory = (SignerFactory)pdfAsClass.newInstance();
			registerSigner(Signers.PDFAS, factory);
		} catch (ClassNotFoundException e) {
			log.error("PDF Signer Factory not found", e);
			throw new RuntimeException("PDF Signer Factory not found", e);
		} catch (InstantiationException e) {
			log.error("PDF Signer Factory could not be instantiated", e);
			throw new RuntimeException("PDF Signer Factory could not be instantiated", e);
		} catch (IllegalAccessException e) {
			log.error("PDF Signer Factory could not accessed", e);
			throw new RuntimeException("PDF Signer Factory could not accessed", e);
		}
	}

	private static void registerSigner(Signers signer, SignerFactory factory) {
		factoryMap.put(signer, factory);
	}

	/**
	 * Gets a PDF Signer according to the chosen signer library
	 * @param signer the chosen Signer type
	 * @return the PDF Signer
	 */
	public static Signer getSigner(Signers signer) {
		return factoryMap.get(signer).createSigner();
	}
}
