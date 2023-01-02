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

import java.util.ArrayList;
// Imports
import java.util.Comparator;
import java.util.List;

/**
 *
 */
public class VersionComparator implements Comparator<String> {
	private static final class Version {
		final List<Integer> parts;
		final boolean isSnapshot;
		private Version(String v) {
			if ((this.isSnapshot = v.endsWith("-SNAPSHOT")))
				v = v.substring(0, v.length() - 9);
			
			List<Integer> l = new ArrayList<Integer>();
			for (String partStr : v.split("\\.")) {
				int part = 0;
				try { part = Integer.parseInt(partStr); } catch (NumberFormatException e) {}
				l.add(part);
			}

			this.parts = l;
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(String v1s, String v2s) {

		Version v1 = new Version(v1s), v2 = new Version(v2s);

		for (int i = 0, n = Math.max(v1.parts.size(), v2.parts.size()); i < n; ++i) {
			int v1Part = ((i < v1.parts.size()) ? v1.parts.get(i) : 0);
			int v2Part = ((i < v2.parts.size()) ? v2.parts.get(i) : 0);

			if (v1Part < v2Part)
				return -1;
			if (v1Part > v2Part)
				return 1;
		}
		if (v1.isSnapshot != v2.isSnapshot)
			return v1.isSnapshot ? -1 : 1;
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
	public static boolean lessThan(String v1, String v2) {
		return compare_s(v1, v2) < 0;
	}

	/**
	 * Check two version strings for order
	 * @param v1 version 1
	 * @param v2 version 2
	 * @return v1 &gt; v2
	 */
	public static boolean greaterThan(String v1, String v2) {
		return compare_s(v1, v2) > 0;
	}
}
