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

/**
 * PDF Signer base Class
 * This class should be extended to support libraries such as PDF-AS or PADES.
 */
public interface Signer {

	/**
	 * Prepare a signature
	 * Defines signature parameters, the pdf library prepares the pdf document to sign and
	 * creates a Security Layer Request.
	 * @param parameter The signature parameters
	 * @return The signing state (contains the prepared document and the signature request
	 * @throws SignatureException
	 */
	public SigningState prepare(SignatureParameter parameter) throws SignatureException;

	/**
	 * Adds the signature to the document.
	 * The SL Response has to be set in the state
	 * @param state The signing state
	 * @return The signature Result
	 * @throws SignatureException
	 */
	public SignResult sign(SigningState state) throws SignatureException;

	/**
	 * Creates new signing profile
	 * @return The new Profile
	 */
	public SignatureParameter newParameter();
}
