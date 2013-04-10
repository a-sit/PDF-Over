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
package at.asit.pdfover.gui.workflow.states.mobilebku;

// Imports
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 */
public class ASITTrustManager implements X509TrustManager {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(ASITTrustManager.class);

	/*
	 * The default X509TrustManager returned by SunX509. We'll delegate
	 * decisions to it, and fall back to the logic in this class if the default
	 * X509TrustManager doesn't trust it.
	 */
	X509TrustManager sunJSSEX509TrustManager;

	/**
	 * Trust Manager for A-Trust Certificates
	 */
	X509TrustManager atrustTrustManager;

	/**
	 * Constructs the TrustManager
	 * 
	 * @throws Exception
	 */
	public ASITTrustManager() throws Exception {
		// create a "default" JSSE X509TrustManager.

		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509"); //$NON-NLS-1$
		tmf.init((KeyStore) null);

		TrustManager tms[] = tmf.getTrustManagers();

		/*
		 * Iterate over the returned trustmanagers, look for an instance of
		 * X509TrustManager. If found, use that as our "default" trust manager.
		 */
		for (int i = 0; i < tms.length; i++) {
			if (tms[i] instanceof X509TrustManager) {
				this.sunJSSEX509TrustManager = (X509TrustManager) tms[i];
				break;
			}
		}

		/*
		 * A-Trust Certificates
		 */

		KeyStore atrustKeyStore = KeyStore.getInstance(KeyStore
				.getDefaultType());

		atrustKeyStore.load(null);

		String usedCertificates = "/certificates/used_certificates.xml"; //$NON-NLS-1$

		Document doc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder()
				.parse(this.getClass().getResourceAsStream(usedCertificates));

		Node certificates = doc.getFirstChild();

		if (!certificates.getNodeName().equals("certificates")) { //$NON-NLS-1$
			throw new Exception(
					"Used certificates xml is invalid! no certificates node"); //$NON-NLS-1$
		}

		NodeList certificateList = certificates.getChildNodes();

		for (int i = 0; i < certificateList.getLength(); i++) {
			try {

				Node certificateNode = certificateList.item(i);

				if (certificateNode.getNodeName().equals("#text")) { //$NON-NLS-1$
					continue; // Ignore dummy text node ..
				}

				if (!certificateNode.getNodeName().equals("certificate")) { //$NON-NLS-1$
					log.warn("Ignoring XML node: " + certificateNode.getNodeName()); //$NON-NLS-1$
					continue;
				}

				String certResource = "/certificates/" + certificateNode.getTextContent() + ".crt"; //$NON-NLS-1$ //$NON-NLS-2$

				X509Certificate cert = (X509Certificate) CertificateFactory
						.getInstance("X509"). //$NON-NLS-1$
						generateCertificate(
								this.getClass().getResourceAsStream(
										certResource));

				atrustKeyStore.setCertificateEntry(certificateNode.getTextContent(), cert);

				log.debug("Loaded certificate : " + certResource); //$NON-NLS-1$

			} catch (Exception ex) {
				log.error("Failed to load certificate [" + "]", ex); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		tmf.init(atrustKeyStore);

		tms = tmf.getTrustManagers();

		/*
		 * Iterate over the returned trustmanagers, look for an instance of
		 * X509TrustManager. If found, use that as our "default" trust manager.
		 */
		for (int i = 0; i < tms.length; i++) {
			if (tms[i] instanceof X509TrustManager) {
				this.atrustTrustManager = (X509TrustManager) tms[i];
				break;
			}
		}

		if (this.sunJSSEX509TrustManager != null
				&& this.atrustTrustManager != null) {
			return;
		}

		/*
		 * Find some other way to initialize, or else we have to fail the
		 * constructor.
		 */
		throw new Exception("Couldn't initialize ASITTrustManager"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.
	 * X509Certificate[], java.lang.String)
	 */
	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		try {
			this.atrustTrustManager.checkServerTrusted(arg0, arg1);
		} catch (CertificateException ex) {
			try {
				this.sunJSSEX509TrustManager.checkClientTrusted(arg0, arg1);
			} catch (CertificateException ex2) {
				log.info("checkClientTrusted: ", ex2); //$NON-NLS-1$
				throw ex2;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.
	 * X509Certificate[], java.lang.String)
	 */
	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		try {
			this.atrustTrustManager.checkServerTrusted(arg0, arg1);
		} catch (CertificateException ex) {
			try {
				this.sunJSSEX509TrustManager.checkServerTrusted(arg0, arg1);
			} catch (CertificateException ex2) {
				log.info("checkServerTrusted: ", ex2); //$NON-NLS-1$
				throw ex2;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
	 */
	@Override
	public X509Certificate[] getAcceptedIssuers() {

		X509Certificate[] default_certs = this.sunJSSEX509TrustManager.getAcceptedIssuers();

		X509Certificate[] atrust_cerst = this.atrustTrustManager.getAcceptedIssuers();
		
		return (X509Certificate[]) ArrayUtils.addAll(default_certs, atrust_cerst);
	}

}
