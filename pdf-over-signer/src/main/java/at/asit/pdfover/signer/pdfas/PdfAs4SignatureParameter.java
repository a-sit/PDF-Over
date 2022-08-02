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
package at.asit.pdfover.signer.pdfas;

//Imports

import iaik.x509.X509Certificate;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.signator.BKUs;
import at.asit.pdfover.signator.DocumentSource;
import at.asit.pdfover.signator.Emblem;
import at.asit.pdfover.signator.SignatureDimension;
import at.asit.pdfover.signator.SignaturePosition;
import at.gv.egiz.pdfas.lib.api.Configuration;
import at.gv.egiz.pdfas.lib.api.PdfAs;
import at.gv.egiz.pdfas.lib.api.PdfAsFactory;
import at.gv.egiz.pdfas.lib.api.sign.SignParameter;
import at.asit.pdfover.commons.Profile;

/**
 * Implementation of SignatureParameter for PDF-AS 4 Library
 */
public class PdfAs4SignatureParameter {

	// TODO nuke getters/setters from this class
    /**
     * SLF4J Logger instance
     **/
    static final Logger log = LoggerFactory.getLogger(PdfAs4SignatureParameter.class);
    
    /**
     * this is set by CliArguments.InvisibleProfile
     * TODO: this is a no good, very bad, hack
     */
    public static boolean PROFILE_VISIBILITY = true;

    /** The Signature Position */
	protected SignaturePosition signaturePosition = null;

	/** The Signature language */
	protected String signatureLanguage = null;

	/** The key identifier */
	protected String keyIdentifier = null;

	/** The input document */
	protected DocumentSource documentSource = null;

	/** Holds the emblem */
	protected Emblem emblem;

	/** Whether to use PDF/A compatibility */
	protected boolean pdfACompat;

	/** The signature device */
	protected BKUs signatureDevice;

	/** Whether so look for placeholder signatures or not. */
	protected boolean searchForPlaceholderSignatures = false;

	/**
	 * @return the searchForPlaceholderSignatures
	 */
	public boolean isSearchForPlaceholderSignatures() {
		return this.searchForPlaceholderSignatures;
	}

	/**
	 * @param value
	 *            the searchForPlaceholderSignatures to set
	 */
	public void setSearchForPlaceholderSignatures(boolean value) {
		this.searchForPlaceholderSignatures = value;
	}

	/**
	 * @return the signatureDevice
	 */
	public BKUs getSignatureDevice() {
		return this.signatureDevice;
	}

	/**
	 * @param signatureDevice
	 *            the signatureDevice to set
	 */
	public void setSignatureDevice(BKUs signatureDevice) {
		this.signatureDevice = signatureDevice;
	}

	/**
	 * Getter of the property <tt>signaturePosition</tt>
	 *
	 * @return Returns the signaturePosition.
	 */
	public SignaturePosition getSignaturePosition() {
		return this.signaturePosition;
	}

	/**
	 * Setter of the property <tt>signaturePosition</tt>
	 *
	 * @param signaturePosition
	 *            The signaturePosition to set.
	 */
	public void setSignaturePosition(SignaturePosition signaturePosition) {
		this.signaturePosition = signaturePosition;
	}

	/**
	 * Getter of the property <tt>signatureLanguage</tt>
	 *
	 * @return Returns the signatureLanguage.
	 */
	public String getSignatureLanguage() {
		return this.signatureLanguage;
	}

	/**
	 * Setter of the property <tt>signatureLanguage</tt>
	 *
	 * @param signatureLanguage
	 *            The signatureLanguage to set.
	 */
	public void setSignatureLanguage(String signatureLanguage) {
		this.signatureLanguage = signatureLanguage;
	}

	/**
	 * Getter of the property <tt>signaturePdfACompat</tt>
	 *
	 * @return Returns the PDF/A compatibility setting.
	 */
	public boolean getSignaturePdfACompat() {
		return this.pdfACompat;
	}

	/**
	 * Setter of the property <tt>signaturePdfACompat</tt>
	 *
	 * @param compat
	 *            The the PDF/A compatibility setting to set.
	 */
	public void setSignaturePdfACompat(boolean compat) {
		this.pdfACompat = compat;
	}

	/**
	 * Getter of the property <tt>keyIdentifier</tt>
	 *
	 * @return Returns the keyIdentifier.
	 */
	public String getKeyIdentifier() {
		return this.keyIdentifier;
	}

	/**
	 * Setter of the property <tt>keyIdentifier</tt>
	 *
	 * @param keyIdentifier
	 *            The keyIdentifier to set.
	 */
	public void setKeyIdentifier(String keyIdentifier) {
		this.keyIdentifier = keyIdentifier;
	}

	/**
	 * Getter of the property <tt>documentSource</tt>
	 *
	 * @return Returns the documentSource.
	 */
	public DocumentSource getInputDocument() {
		return this.documentSource;
	}

	/**
	 * Setter of the property <tt>documentSource</tt>
	 *
	 * @param inputDocument
	 *            The documentSource to set.
	 */
	public void setInputDocument(DocumentSource inputDocument) {
		this.documentSource = inputDocument;
	}

	/**
	 * Gets the Emblem
	 *
	 * @return the Emblem
	 */
	public Emblem getEmblem() {
		return this.emblem;
	}

	/**
	 * Sets the Emblem
	 *
	 * @param emblem
	 *            The new Emblem
	 */
	public void setEmblem(Emblem emblem) {
		this.emblem = emblem;
	}

    private HashMap<String, String> genericProperties = new HashMap<String, String>();

    /**
     * This parameters are defining the signature block size
     */
    private int sig_w = 229;
    private int sig_h = 77;

    private String profile = Profile.getDefaultProfile();

    /* (non-Javadoc)
     * @see at.asit.pdfover.signator.SignatureParameter#getPlaceholderDimension()
     */
    public SignatureDimension getPlaceholderDimension() {
        return new SignatureDimension(this.sig_w, this.sig_h);
    }

    /* (non-Javadoc)
     * @see at.asit.pdfover.signator.SignatureParameter#getPlaceholder()
     */
    public Image getPlaceholder() {
        String sigProfile = getPdfAsSignatureProfileId();

        String sigEmblem = (getEmblem() == null ? null : getEmblem().getFileName());
        String sigNote = getProperty("SIG_NOTE");

        try {
            X509Certificate cert = new X509Certificate(PdfAs4SignatureParameter.class.getResourceAsStream("/qualified.cer"));
            PdfAs pdfas = PdfAs4Helper.getPdfAs();
            Configuration conf = pdfas.getConfiguration();
            if (sigEmblem != null && !sigEmblem.trim().equals("")) {
                conf.setValue("sig_obj." + sigProfile + ".value.SIG_LABEL", sigEmblem);
            }
            if (sigNote != null) {
                conf.setValue("sig_obj." + sigProfile + ".value.SIG_NOTE", sigNote);
            }
            SignParameter param = PdfAsFactory
                    .createSignParameter(conf, null, null);
            param.setSignatureProfileId(sigProfile);
            Image img = pdfas.generateVisibleSignaturePreview(param, cert, 72 * 4);
            this.sig_w = img.getWidth(null) / 4;
            this.sig_h = img.getHeight(null) / 4;

            return img;
        } catch (Exception e) {
            log.error("Failed to get signature placeholder", e);
            return new BufferedImage(getPlaceholderDimension().getWidth(),
                    getPlaceholderDimension().getHeight(),
                    BufferedImage.TYPE_INT_RGB);
        }
    }

    /* (non-Javadoc)
     * @see at.asit.pdfover.signator.SignatureParameter#setProperty(java.lang.String, java.lang.String)
     */
    public void setProperty(String key, String value) {
        this.genericProperties.put(key, value);
    }

    /* (non-Javadoc)
     * @see at.asit.pdfover.signator.SignatureParameter#getProperty(java.lang.String)
     */
    public String getProperty(String key) {
        return this.genericProperties.get(key);
    }

    /**
     * Gets the Signature Position String for PDF-AS
     *
     * @return Signature Position String
     */
    public String getPdfAsSignaturePosition() {
        SignaturePosition in_pos = getSignaturePosition();
        String out_pos;

        if (!in_pos.useAutoPositioning()) {
            if (in_pos.getPage() < 1) {
                out_pos = String.format(
                        (Locale) null,
                        "p:new;x:%f;y:%f", in_pos.getX(), in_pos.getY());
            } else {
                out_pos = String.format(
                        (Locale) null,
                        "p:%d;x:%f;y:%f", in_pos.getPage(), in_pos.getX(), in_pos.getY());
            }
        } else {
            out_pos = "p:auto;x:auto;y:auto";
        }

        return out_pos;
    }

    /**
     * Get the Signature Profile ID for this set of parameters
     *
     * @return the Signature Profile ID
     */
    public String getPdfAsSignatureProfileId() {
        String lang = getSignatureLanguage();
        boolean useNote = (getProperty("SIG_NOTE") != null);
        boolean usePdfACompat = (getSignaturePdfACompat());

        //Add Signature Param here//
        String profileId;

        if (!PROFILE_VISIBILITY) {
            log.debug("Profile visibility was set to false");
            return Profile.INVISIBLE.name();
        }

        Profile profile = Profile.getProfile(this.profile);
        switch (profile) {
            case BASE_LOGO:
            case INVISIBLE:
                return this.profile;
            case AMTSSIGNATURBLOCK:
                profileId = this.profile;
                profileId += getLangProfilePart(lang);
                profileId += "_RECOMMENDED";
                return profileId;
            default:
                profileId = this.profile;
                profileId += getLangProfilePart(lang);
                break;
        }

        if (useNote)
            profileId += "_NOTE";

        if (usePdfACompat)
            profileId += "_PDFA";

        log.debug("Profile ID: {}", profileId);
        return profileId;
    }

    private static String getLangProfilePart(String lang) {
        return ("en".equals(lang)) ? "_EN" : "_DE";
    }

    public void setSignatureProfile(String profile) {
        this.profile = profile;
    }

    public String getSignatureProfile() {
        return this.profile;
    }

}



