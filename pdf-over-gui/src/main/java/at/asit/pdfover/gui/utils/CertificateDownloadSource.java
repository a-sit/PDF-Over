package at.asit.pdfover.gui.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
//Imports
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.security.auth.login.Configuration;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.exceptions.InitializationException;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.utils.SWTLoader;
import at.asit.pdfover.gui.workflow.StateMachineImpl;
import at.asit.pdfover.gui.workflow.config.ConfigProvider;
import at.asit.pdfover.gui.workflow.config.ConfigProviderImpl;


/**
 * Download of accepted certificates
 */
public class CertificateDownloadSource {
	
	/**
	 * SLF4J Logger instance
	 **/
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(CertificateDownloadSource.class);
	private static URL url=null;
	
	/**
	 * 
	 */
	public static void getAcceptedCertificates()
	{
	try {
			
			URL url = new URL(Constants.CERTIFICATE_DOWNLOAD_XML_URL+Constants.CERTIFICATE_XML_FILE);
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();

				BufferedInputStream bis = new BufferedInputStream(is);
				FileOutputStream fis2 = new FileOutputStream(new File(Constants.RES_CERT_LIST_ADDED));
				
				
				byte[] buffer = new byte[1024];
				int count = 0;
				while ((count = bis.read(buffer, 0, 1024)) != -1) {
					fis2.write(buffer, 0, count);
				}
				fis2.close();
				bis.close();
				downloadCertificatesFromServer();
			
		} catch (IOException e) {
			log.debug("File not found");}

		
	}
	
	/**
	 * Download accepted Certificates from Server
	 */
	public static void downloadCertificatesFromServer()
	{

		BufferedReader br = null;
		FileReader fr = null;

		try {
			
		
			File added_cert = new File(Constants.RES_CERT_LIST_ADDED);
	
			if (added_cert.exists())
			{		
			Document doc_added = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(added_cert);
			
			
			Node certificates_added = doc_added.getFirstChild();
			NodeList certificates_added_list = certificates_added.getChildNodes();
			
			//identify the certificate that has to be downloaded
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

					ConfigProviderImpl cpi = new ConfigProviderImpl();
					
					String certResource = Constants.CERTIFICATE_DOWNLOAD_XML_URL + certificateNode.getTextContent();	
					log.info("===== Starting to download accepted certificates =====");
					URL url = new URL(certResource);
					URLConnection connection = url.openConnection();					
					InputStream is = connection.getInputStream();
					BufferedInputStream bis = new BufferedInputStream(is);
			        FileOutputStream fis = new FileOutputStream(new File(Constants.RES_CERT_PATH_ADDED+certificateNode.getTextContent()));
			        byte[] buffer = new byte[1024];
			        int count=0;  
			        while((count = bis.read(buffer,0,1024)) != -1)
			        {
			            fis.write(buffer, 0, count);
			        }
			        fis.close();
			        bis.close();


				} catch (Exception ex) {
					log.debug(ex.toString()); //$NON-NLS-1$
				}
			}
			
			}
			

		} catch (IOException e) {

			e.printStackTrace();

		} catch (SAXException e) {

			e.printStackTrace();
		} catch (ParserConfigurationException e) {

			e.printStackTrace();
		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}

	}

}
