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
package at.asit.pdfover.gui.exceptions;

/**
 * 
 */
public class ResumableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -607216270516492225L;

	private int resumeIndex = 0;
	
	/**
	 * Create a new resumable exception, thrown by a validator which can be ignored
	 * @param msg Error message
	 * @param resumeIndex Validator index to resume from
	 */
	public ResumableException(String msg, int resumeIndex) {
		super(msg);
		this.resumeIndex = resumeIndex;
	}

	/**
	 * Create a new resumable exception, thrown by a validator which can be ignored
	 * @param msg Error message
	 * @param cause Exception causing this one
	 * @param resumeIndex Validator index to resume from
	 */
	public ResumableException(String msg, Throwable cause, int resumeIndex) {
		super(msg, cause);
		this.resumeIndex = resumeIndex;
	}

	/**
	 * Return the validator index to resume from
	 * @return the resumeIndex
	 */
	public int getResumeIndex() {
		return this.resumeIndex;
	}

}
