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

import java.io.File;
import java.io.FileInputStream;
// Imports
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.asit.pdfover.commons.Constants;

/**
 * 
 */
public class SimpleXMLTrustManager implements X509TrustManager {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(SimpleXMLTrustManager.class);

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
	public SimpleXMLTrustManager() throws Exception {
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
		 * Certificates
		 */

		KeyStore myKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		myKeyStore.load(null);

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(this.getClass().getResourceAsStream(Constants.RES_CERT_LIST));
		Node certificates = doc.getFirstChild();
		NodeList certificateList = certificates.getChildNodes();

		try {
			if (!certificates.getNodeName().equals("certificates")) { //$NON-NLS-1$
				throw new Exception("Used certificates xml is invalid! no certificates node"); //$NON-NLS-1$
			}

			//add trusted certificates to certStore//
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

					String certResource = Constants.RES_CERT_PATH + certificateNode.getTextContent();

					X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X509"). //$NON-NLS-1$
							generateCertificate(this.getClass().getResourceAsStream(certResource));

					myKeyStore.setCertificateEntry(certificateNode.getTextContent(), cert);

					log.debug("Loaded certificate : " + certResource); //$NON-NLS-1$

				} catch (Exception ex) {
					log.error("Failed to load certificate [" + "]", ex); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

		}

		catch (Exception e) {
			e.toString();
		}

		File added_cert = new File(Constants.RES_CERT_LIST_ADDED);
		
		//check if the additional certificates.xml file exists//

		if (added_cert.exists()) {
			Node certificates_added = null;

			Document doc_added = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(added_cert);

			certificates_added = doc_added.getFirstChild();

			NodeList certificates_added_list = certificates_added.getChildNodes();

			//if exists, add trusted certificates to cert-Store
			for (int i = 0; i < certificates_added_list.getLength(); i++) {
				try {

					Node certificateNode = certificates_added_list.item(i);

					if (certificateNode.getNodeName().equals("#text")) { //$NON-NLS-1$
						continue; // Ignore dummy text node ..
					}

					if (!certificateNode.getNodeName().equals("certificate")) { //$NON-NLS-1$
						log.warn("Ignoring XML node: " + certificateNode.getNodeName()); //$NON-NLS-1$
						continue;
					}

					if (!certificateNode.getTextContent().equals("")) {
						String certResource = Constants.RES_CERT_PATH_ADDED + certificateNode.getTextContent();

						FileInputStream addedNode = new FileInputStream(certResource);

						X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X509"). //$NON-NLS-1$
								generateCertificate(addedNode);

						myKeyStore.setCertificateEntry(certificateNode.getTextContent(), cert);

						log.debug("Loaded certificate : " + certResource); //$NON-NLS-1$
					}
				} catch (Exception ex) {
					log.error("Failed to load certificate [" + "]", ex); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}

		tmf.init(myKeyStore);

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

		if (this.sunJSSEX509TrustManager != null && this.atrustTrustManager != null) {
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
	public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
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
	public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
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

		X509Certificate[] atrust_certs = this.atrustTrustManager.getAcceptedIssuers();

		X509Certificate[] all_certs = Arrays.copyOf(default_certs, default_certs.length + atrust_certs.length);
		System.arraycopy(atrust_certs, 0, all_certs, default_certs.length, atrust_certs.length);
		return all_certs;
	}

}
