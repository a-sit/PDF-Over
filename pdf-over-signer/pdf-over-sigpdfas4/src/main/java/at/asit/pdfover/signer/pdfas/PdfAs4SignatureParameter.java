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


import at.asit.pdfover.signator.SignatureDimension;
import at.asit.pdfover.signator.SignatureParameter;
import at.asit.pdfover.signator.SignaturePosition;
import at.gv.egiz.pdfas.lib.api.Configuration;
import at.gv.egiz.pdfas.lib.api.PdfAs;
import at.gv.egiz.pdfas.lib.api.PdfAsFactory;
import at.gv.egiz.pdfas.lib.api.sign.SignParameter;
import at.asit.pdfover.commons.Profile;

/**
 * Implementation of SignatureParameter for PDF-AS 4 Library
 */
public class PdfAs4SignatureParameter extends SignatureParameter {
    /**
     * The profile ID extension for the German signature block
     */
    private static final String PROFILE_ID_LANG_DE = "_DE";
    /**
     * The profile ID extension for the English signature block
     */
    private static final String PROFILE_ID_LANG_EN = "_EN";
    /**
     * The profile ID extension for the signature note
     */
    private static final String PROFILE_ID_NOTE = "_NOTE";
    /**
     * The profile ID extension for PDF/A compatibility
     */
    private static final String PROFILE_ID_PDFA = "_PDFA";

    private static final String PROFILE_ID_RECOMMENDED = "_RECOMMENDED";

    /**
     * Visibility of signature block
     */
    public static boolean PROFILE_VISIBILITY = true;

    private HashMap<String, String> genericProperties = new HashMap<String, String>();

    /**
     * This parameters are defining the signature block size
     */
    private int sig_w = 229;
    private int sig_h = 77;

    /**
     * SLF4J Logger instance
     **/
    static final Logger log = LoggerFactory
            .getLogger(PdfAs4SignatureParameter.class);
    private String profile = Profile.getDefaultProfile();

    /* (non-Javadoc)
     * @see at.asit.pdfover.signator.SignatureParameter#getPlaceholderDimension()
     */
    @Override
    public SignatureDimension getPlaceholderDimension() {
        return new SignatureDimension(this.sig_w, this.sig_h);
    }

    /* (non-Javadoc)
     * @see at.asit.pdfover.signator.SignatureParameter#getPlaceholder()
     */
    @Override
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
    @Override
    public void setProperty(String key, String value) {
        this.genericProperties.put(key, value);
    }

    /* (non-Javadoc)
     * @see at.asit.pdfover.signator.SignatureParameter#getProperty(java.lang.String)
     */
    @Override
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
                        "p:new;x:%f;y:%f", in_pos.getX(),
                        in_pos.getY());
            } else {
                out_pos = String.format(
                        (Locale) null,
                        "p:%d;x:%f;y:%f", in_pos.getPage(), in_pos.getX(),
                        in_pos.getY());
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
                profileId += PROFILE_ID_RECOMMENDED;
                return profileId;
            default:
                profileId = this.profile;
                profileId += getLangProfilePart(lang);
                break;
        }

        if (useNote)
            profileId += PROFILE_ID_NOTE;

        if (usePdfACompat)
            profileId += PROFILE_ID_PDFA;

        log.debug("Profile ID: {}", profileId);
        return profileId;
    }

    private String getProfileName() {
        if (this.profile == null) {
            this.profile = Profile.SIGNATURBLOCK_SMALL.name();
        }
        return Profile.getProfile(this.profile).name();
    }

    private String getLangProfilePart(String lang) {
        return (lang != null && lang.equals("en")) ? PROFILE_ID_LANG_EN : PROFILE_ID_LANG_DE;
    }

    @Override
    public void setSignatureProfile(String profile) {
        this.profile = profile;
    }

    @Override
    public String getSignatureProfile() {
        return this.profile;
    }

}



