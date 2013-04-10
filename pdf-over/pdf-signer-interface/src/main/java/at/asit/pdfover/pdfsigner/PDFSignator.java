package at.asit.pdfover.pdfsigner;

import java.util.HashMap;
import java.util.Map;

/**
 * PDF Signator Interface
 * @author afitzek
 */
public class PDFSignator {

	protected static Map<PDFSigner, PDFSignerFactory> _factory;

	// Let Factory choose if instance can be cached or not
	//protected static Map<PDFSigner, PDFSignerInterface> _signer;
	
	static {
		//_signer = new HashMap<PDFSigner, PDFSignerInterface>();
		_factory = new HashMap<PDFSigner, PDFSignerFactory>();
		
		
		try {
			@SuppressWarnings("rawtypes")
			Class pdfAsClass = Class.forName("at.asit.pdfover.pdfsigner.pdfas.PDFASSignerFactory");
			PDFSignerFactory factory = (PDFSignerFactory)pdfAsClass.newInstance();
		    RegisterPDFSigner(factory.GetPDFSignerType(), factory);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}

	public static void RegisterPDFSigner(PDFSigner signer, PDFSignerFactory factory) {
		_factory.put(signer, factory);
	}
	
	protected static PDFSignerInterface GetSigner(PDFSigner signer) throws PDFSignatureException {
		/*if (_signer.containsKey(signer)) {
			return _signer.get(signer);
		}*/
		
		if(_factory.containsKey(signer)) {
			//_signer.put(signer, _factory.get(signer).CreatePDFSigner());
			return _factory.get(signer).CreatePDFSigner();
		} else {
			throw new PDFSignatureException("Unknown PDF Library: " + signer.toString());
		}

		//return _signer.get(signer);
	}

	/**
	 * Gets a PDF Signer according to the chosen pdf signer library
	 * @param signer
	 * @return
	 * @throws PDFSignatureException 
	 */
	public static PDFSignerInterface GetPDFSigner(PDFSigner signer) throws PDFSignatureException {
		return GetSigner(signer);
	}
}
