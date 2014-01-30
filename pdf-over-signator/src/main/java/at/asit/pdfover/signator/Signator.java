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

//Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.EnumMap;
import java.util.Map;

/**
 * PDF Signator Interface
 */
public class Signator {

	/**
	 * SLF4J Logger instance
	 **/
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
