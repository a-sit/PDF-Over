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
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.utils.Messages;

/**
 * 
 */
public class TrustedSocketFactory implements ProtocolSocketFactory {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(TrustedSocketFactory.class);

	private static SSLSocketFactory getFactory() throws NoSuchAlgorithmException,
			KeyManagementException, Exception {
		SSLContext sslContext = SSLContext.getInstance("TLS"); //$NON-NLS-1$
		sslContext.init(null, new TrustManager[] { new ASITTrustManager() },
				new java.security.SecureRandom());

		return sslContext.getSocketFactory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket
	 * (java.lang.String, int)
	 */
	@Override
	public Socket createSocket(String host, int port) throws IOException,
			UnknownHostException {
		try {
			SSLSocket sslSocket = (SSLSocket) getFactory().createSocket(host,
					port);

			return sslSocket;
		} catch (Exception ex) {
			log.error("TrustedSocketFactory: ", ex); //$NON-NLS-1$
			if (ex instanceof IOException) {
				throw (IOException) ex;
			} else if (ex instanceof UnknownHostException) {
				throw (UnknownHostException) ex;
			} else {
				throw new IOException(
						Messages.getString("TrustedSocketFactory.FailedToCreateSecureConnection"), ex); //$NON-NLS-1$
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket
	 * (java.lang.String, int, java.net.InetAddress, int)
	 */
	@Override
	public Socket createSocket(String host, int port, InetAddress clientHost,
			int clientPort) throws IOException, UnknownHostException {
		try {
			SSLSocket sslSocket = (SSLSocket) getFactory().createSocket(host,
					port, clientHost, clientPort);

			return sslSocket;
		} catch (Exception ex) {
			log.error("TrustedSocketFactory: ", ex); //$NON-NLS-1$
			if (ex instanceof IOException) {
				throw (IOException) ex;
			} else if (ex instanceof UnknownHostException) {
				throw (UnknownHostException) ex;
			} else {
				throw new IOException(
						Messages.getString("TrustedSocketFactory.FailedToCreateSecureConnection"), ex); //$NON-NLS-1$
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket
	 * (java.lang.String, int, java.net.InetAddress, int,
	 * org.apache.commons.httpclient.params.HttpConnectionParams)
	 */
	@Override
	public Socket createSocket(String host, int port, InetAddress clientHost,
			int clientPort, HttpConnectionParams params) throws IOException,
			UnknownHostException, ConnectTimeoutException {
		try {
			if (params == null) {
				throw new IllegalArgumentException("Parameters may not be null"); //$NON-NLS-1$
			}
			int timeout = params.getConnectionTimeout();
			Socket socket = null;

			SSLSocketFactory socketfactory = getFactory();
			if (timeout == 0) {
				socket = socketfactory.createSocket(host, port, clientHost,
						clientPort);
			} else {
				socket = socketfactory.createSocket();
				SocketAddress localaddr = new InetSocketAddress(clientHost,
						clientPort);
				SocketAddress remoteaddr = new InetSocketAddress(host, port);
				socket.bind(localaddr);
				socket.connect(remoteaddr, timeout);
			}
			return socket;
		} catch (Exception ex) {
			log.error("TrustedSocketFactory: ", ex); //$NON-NLS-1$
			if (ex instanceof IOException) {
				throw (IOException) ex;
			} else if (ex instanceof UnknownHostException) {
				throw (UnknownHostException) ex;
			} else {
				throw new IOException(
						Messages.getString("TrustedSocketFactory.FailedToCreateSecureConnection"), ex); //$NON-NLS-1$
			}
		}
	}

}
