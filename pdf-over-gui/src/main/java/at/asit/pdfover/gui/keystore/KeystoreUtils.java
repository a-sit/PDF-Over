package at.asit.pdfover.gui.keystore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class KeystoreUtils {
    public static KeyStore tryLoadKeystore(File location, String storeType, String storePass) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException {
		KeyStore ks = KeyStore.getInstance(storeType);
		FileInputStream fis = new FileInputStream(location);
        try
        {
		    ks.load(fis, storePass.toCharArray());
        } catch (IOException e) {
            UnrecoverableKeyException keyCause = (UnrecoverableKeyException)e.getCause();
            if (keyCause != null)
                throw keyCause;
            else
                throw e;
        }
        return ks;
	}
}
