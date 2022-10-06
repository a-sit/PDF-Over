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
package at.asit.pdfover.gui.bku.mobile;

// Imports
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import at.asit.pdfover.gui.exceptions.InvalidPasswordException;
import at.asit.pdfover.gui.exceptions.PasswordTooLongException;
import at.asit.pdfover.gui.exceptions.PasswordTooShortException;

/**
 *
 */
public class MobileBKUValidator {

	/**
	 * Regular expression for mobile phone numbers: this allows the entry of
	 * mobile numbers in the following formats:
	 *
	 * +(countryCode)99999999999 00(countryCode)99999999999 099999999999
	 * 1030199999999999 (A-Trust Test bku)
	 */
	private static final String NUMBER_REGEX = "^((\\+[\\d]{2})|(00[\\d]{2})|(0)|(10301))([1-9][\\d]+)$";

	/**
	 * Validates the Mobile phone number
	 *
	 * @param number
	 * @return the normalized Phone number
	 */
	public static String normalizeMobileNumber(String number) {
		// Verify number and normalize

		number = number.trim();
		
		String numberWithoutWhitespace = number.replaceAll("\\s","");
		// Compile and use regular expression
		Pattern pattern = Pattern.compile(NUMBER_REGEX);
		Matcher matcher = pattern.matcher(numberWithoutWhitespace);

		if (!matcher.find())
			return number; /* might be an idA username, return unchanged */

		if (matcher.groupCount() != 6) {
			return number;
		}

		String countryCode = matcher.group(1);

		String normalNumber = matcher.group(6);

		if (countryCode.equals("10301")) {
			// A-Trust Testnumber! Don't change
			return numberWithoutWhitespace;
		}

		countryCode = countryCode.replace("00", "+");

		if (countryCode.equals("0")) {
			countryCode = "+43";
		}

		return countryCode + normalNumber;
	}

	/**
	 * Validate given Password for Mobile BKU
	 *
	 * @param password
	 * @throws InvalidPasswordException
	 */
	public static void validatePassword(String password)
			throws InvalidPasswordException {
		if (password.length() < 5)
			throw new PasswordTooShortException();
		if (password.length() > 200)
			throw new PasswordTooLongException();
	}
}
