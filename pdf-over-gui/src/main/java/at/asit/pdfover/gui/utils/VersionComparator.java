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
package at.asit.pdfover.gui.utils;

// Imports
import java.util.Comparator;

/**
 *
 */
public class VersionComparator implements Comparator<String> {
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(String v1, String v2) {
		String[] v1Parts = v1.split("\\.|-"); //
		String[] v2Parts = v2.split("\\.|-"); //

		int length = Math.max(v1Parts.length, v2Parts.length);
		for (int i = 0; i < length; ++i) {
			int v1Part = 0;
			try {
				if (i < v1Parts.length)
					v1Part = Integer.parseInt(v1Parts[i]);
			} catch (NumberFormatException e) {
				if (v1Parts[i].equals("SNAPSHOT")) //
					v1Part = Integer.MAX_VALUE;
			}

			int v2Part = 0;
			try {
				if (i < v2Parts.length)
					v2Part = Integer.parseInt(v2Parts[i]);
			} catch (NumberFormatException e) {
				if (v2Parts[i].equals("SNAPSHOT")) //
					v2Part = Integer.MAX_VALUE;
			}

			if (v1Part < v2Part)
				return -1;
			if (v1Part > v2Part)
				return 1;
		}
		return 0;
	}

	/**
	 * Compare two version strings (static version)
	 * @param v1 version 1
	 * @param v2 version 2
	 * @return -1 if v1 &lt; v2, 0 if v1 = v2, 1 if v1 &gt; v2
	 */
	public static int compare_s(String v1, String v2) {
		VersionComparator vc = new VersionComparator();
		return vc.compare(v1, v2);
	}

	/**
	 * Check two version strings for equality
	 * @param v1 version 1
	 * @param v2 version 2
	 * @return v1 == v2
	 */
	public static boolean equals(String v1, String v2) {
		return compare_s(v1, v2) == 0;
	}

	/**
	 * Check two version strings for order
	 * @param v1 version 1
	 * @param v2 version 2
	 * @return v1 &lt; v2
	 */
	public static boolean before(String v1, String v2) {
		return compare_s(v1, v2) < 0;
	}

	/**
	 * Check two version strings for order
	 * @param v1 version 1
	 * @param v2 version 2
	 * @return v1 &gt; v2
	 */
	public static boolean after(String v1, String v2) {
		return compare_s(v1, v2) > 0;
	}
}
