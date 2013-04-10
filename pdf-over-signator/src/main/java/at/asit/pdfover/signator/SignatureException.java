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

/**
 * Base class for signature exceptions
 */
public class SignatureException extends Exception {

	/**
	 * SFL4J Logger instance
	 **/
	private static Logger log = LoggerFactory.getLogger(SignatureException.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 711578398780816710L;
	

	/**
	 * Empty constructor
	 */
	public SignatureException() {
		super();
	}

	/**
	 * Constructor with causing exception
	 * @param cause the cause
	 */
	public SignatureException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Constructor with message
	 * @param msg the message
	 */
	public SignatureException(String msg) {
		super(msg);
	}

	/**
	 * Constructor with message and causing exception
	 * @param message the message
	 * @param cause the cause
	 */
	public SignatureException(String message, Throwable cause) {
		super(message, cause);
	}
}
