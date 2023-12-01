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
package at.asit.pdfover.gui.bku;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
// Imports
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.http.client.config.CookieSpecs;

import at.asit.pdfover.commons.Constants;

/**
 *
 */
public class BKUHelper {

	/* public static HttpClient getHttpClient(boolean useProxy) {
	 HttpClient client = new HttpClient();
	 client.getParams().setParameter("http.useragent",
	 Constants.APP_NAME_VERSION);

	  if (useProxy) {
	 String host = System.getProperty("http.proxyHost");
	 String port = System.getProperty("http.proxyPort");
	 if (host != null && !host.isEmpty() &&
	 port != null && !port.isEmpty()) {
	 int p = Integer.parseInt(port);
	 client.getHostConfiguration().setProxy(host, p);
	 String user = System.getProperty("http.proxyUser");
	 String pass = System.getProperty("http.proxyPassword");
	 if (user != null && !user.isEmpty() && pass != null) {
	 client.getState().setProxyCredentials(new AuthScope(host, p),
	 new UsernamePasswordCredentials(user, pass));
	 }
	 }
	 }

	 return client;
	 }*/

	/**
	 * Get a HTTP Client instance
	 *
	 * @param useProxy
	 *            whether to use a potentially set proxy
	 * @return the HttpClient
	 */
	@SuppressWarnings("deprecation")
	public static HttpClient getHttpClient(boolean useProxy) {
		HttpClient client = new HttpClient();
		client.getParams().setParameter("http.useragent",
				Constants.APP_NAME_VERSION);


		client.getParams().setParameter("http.protocol.cookie-policy", CookieSpecs.BROWSER_COMPATIBILITY);

		if (useProxy) {
			String host = System.getProperty("http.proxyHost");//
			String port = System.getProperty("http.proxyPort");//
			if (host != null && !host.isEmpty() && port != null && !port.isEmpty()) {
				int p = Integer.parseInt(port);
				client.getHostConfiguration().setProxy(host, p);
				String user = System.getProperty("http.proxyUser");//
				String pass = System.getProperty("http.proxyPassword");//
				if (user != null && !user.isEmpty() && pass != null) {
					client.getState().setProxyCredentials(new AuthScope(host, p),
							new UsernamePasswordCredentials(user, pass));
				}
			}
		}

		return client;

	}

	/**
	 * Get a HTTP Client instance
	 *
	 * @return the HttpClient
	 */
	public static HttpClient getHttpClient() {
		return getHttpClient(false);
	}
}
