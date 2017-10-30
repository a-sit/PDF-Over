/*
 * Copyright 2017 by A-SIT, Secure Information Technology Center Austria
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
//Imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.exceptions.InitializationException;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.utils.SWTLoader;
import at.asit.pdfover.gui.workflow.StateMachineImpl;
import at.asit.pdfover.gui.workflow.config.ConfigProvider;
import at.asit.pdfover.gui.workflow.config.ConfigProviderImpl;
import at.gv.egiz.sl.schema.ToBeEncryptedType.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;


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
	 * @throws ParserConfigurationException 
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
			
		} catch (Exception e) {
			//if file can not be downloaded, try to create it//
			 try {
			   DocumentBuilderFactory dbFactory =
				         DocumentBuilderFactory.newInstance();
				         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				         Document doc = dBuilder.newDocument();
				         
				         // root element
				         Node rootElement = doc.createElement("certificates");
				         doc.appendChild(rootElement);
				         TransformerFactory transformerFactory = TransformerFactory.newInstance();
				         Transformer transformer = transformerFactory.newTransformer();
				         DOMSource source = new DOMSource(doc);
				         StreamResult result = new StreamResult(new File(Constants.RES_CERT_LIST_ADDED));
				        
							transformer.transform(source, result);
						} catch (TransformerException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (ParserConfigurationException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
			
			
			e.printStackTrace();} //$NON-NLS-1$

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
			log.info("===== Starting to download accepted certificates =====");
			
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

					if (!certificateNode.getTextContent().equals(""))
					{
					String certResource = Constants.CERTIFICATE_DOWNLOAD_XML_URL + certificateNode.getTextContent();	
					
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
					}
				} catch (Exception ex) {
					log.debug(ex.toString()); //$NON-NLS-1$
				}
			
			}	}
			else{
			log.info("Certificates-File could not be downloaded, will be created");} //$NON-NLS-1$
		}

		 catch (IOException e) {
			
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
