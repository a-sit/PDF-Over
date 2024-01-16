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

import at.asit.pdfover.commons.BKUs;
import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Profile;
import at.asit.pdfover.signer.DocumentSource;
import at.asit.pdfover.signer.Emblem;
import at.asit.pdfover.signer.SignaturePosition;
import at.gv.egiz.pdfas.lib.api.Configuration;
import at.gv.egiz.pdfas.lib.api.PdfAs;
import at.gv.egiz.pdfas.lib.api.PdfAsFactory;
import at.gv.egiz.pdfas.lib.api.sign.SignParameter;
import iaik.x509.X509Certificate;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Locale;

/**
 * Implementation of SignatureParameter for PDF-AS 4 Library
 */
@Slf4j
public class PdfAs4SignatureParameter {

    /**
     * This value scales the preview image that is generated, we then downscale
     * it with filtering to get a cleaner image
     */
    public static final int SIG_PREVIEW_SCALING_FACTOR = 2;

    /**
     * this is set by CliArguments.InvisibleProfile
     * TODO: this is a no good, very bad, hack
     */
    public static boolean PROFILE_VISIBILITY = true;

    /** The Signature Position */
	public SignaturePosition signaturePosition = null;

	/** The Signature language */
	public String signatureLanguage = null;

	/** The key identifier */
	public String keyIdentifier = null;

	/** The input document */
	public DocumentSource inputDocument = null;

	/** Holds the emblem */
	public Emblem emblem;

	/** Whether to use PDF/A compatibility */
	public boolean enablePDFACompat;

	/** The signature device */
	public BKUs signatureDevice;

    /** The siganture note, if any */
    public String signatureNote = null;

	/** Whether so look for placeholder signatures or not. */
	public boolean searchForPlaceholderSignatures = false;

    /** The size to scale the logo to (in mm) */
    public double targetLogoSize = Constants.DEFAULT_LOGO_ONLY_SIZE;

    /** The signature profile in use */
    public Profile signatureProfile = Profile.getDefaultProfile();

    Image getPlaceholder() {
        String sigProfile = getPdfAsSignatureProfileId();

        String sigEmblem = (this.emblem == null ? null : this.emblem.getCachedFileName());
        String sigNote = this.signatureNote;

        try {
            X509Certificate cert = new X509Certificate(PdfAs4SignatureParameter.class.getResourceAsStream("/example.cer"));
            
            PdfAs pdfas = PdfAs4Helper.getPdfAs();
            synchronized (PdfAs4Helper.class) {
                Configuration conf = pdfas.getConfiguration();
                if (sigEmblem != null && !sigEmblem.trim().equals("")) {
                    conf.setValue("sig_obj." + sigProfile + ".value.SIG_LABEL", sigEmblem);
                }
                if (sigNote != null) {
                    conf.setValue("sig_obj." + sigProfile + ".value.SIG_NOTE", sigNote);
                }
                if (this.signatureProfile == Profile.BASE_LOGO)
                {
                    int emblemWidth = (this.emblem != null) ? this.emblem.getWidth() : 1;
                    int emblemHeight = (this.emblem != null) ? this.emblem.getHeight() : 1;
                    double aspectRatio = ((double)emblemWidth) / emblemHeight;
                    double targetWidth = this.targetLogoSize * Constants.PDF_UNITS_PER_MM;
                    double targetHeight = this.targetLogoSize * Constants.PDF_UNITS_PER_MM;
                    if (aspectRatio < 1)
                        targetWidth *= aspectRatio;
                    else
                        targetHeight /= aspectRatio;
                    conf.setValue("sig_obj." + sigProfile + ".table.main.Style.padding", "0");
                    conf.setValue("sig_obj." + sigProfile + ".pos", "w:"+targetWidth+";f:0");
                    conf.setValue("sig_obj." + sigProfile + ".table.main.Style.imagescaletofit", targetWidth+";"+targetHeight);
                }
                SignParameter param = PdfAsFactory.createSignParameter(conf, null, null);
                param.setSignatureProfileId(sigProfile);

                // 72 is the number of typography dots in an inch
                Image placeholder = pdfas.generateVisibleSignaturePreview(param, cert, 72 * SIG_PREVIEW_SCALING_FACTOR);

                // WORKAROUND for #5, manually paint a black border
                if ((placeholder != null) && !this.signatureProfile.equals(Profile.BASE_LOGO))
                {
                    Graphics2D ctx = (Graphics2D)placeholder.getGraphics();
                    ctx.setColor(Color.BLACK);
                    ctx.drawRect(0, 0, placeholder.getWidth(null)-1, placeholder.getHeight(null)-1);
                }

                return placeholder;
            }
        } catch (Exception e) {
            log.error("Failed to get signature placeholder", e);
            // these hardcoded values come from pdfas.generateVisibleSignaturePreview(param, cert, 72);
            // we then scale them so the calculations in SignaturePanel.setSignaturePlaceholder are correct
            return new BufferedImage(229 * SIG_PREVIEW_SCALING_FACTOR, 82 * SIG_PREVIEW_SCALING_FACTOR, BufferedImage.TYPE_INT_RGB);
        }
    }

    /**
     * Gets the Signature Position String for PDF-AS
     *
     * @return Signature Position String
     */
    public String getPdfAsSignaturePosition() {
        SignaturePosition in_pos = this.signaturePosition;

        if (in_pos.useAutoPositioning())
			return "p:auto;x:auto;y:auto";

		if (in_pos.getPage() < 1)
		{
			return String.format(
					(Locale) null,
					"p:new;x:%f;y:%f", in_pos.getX(), in_pos.getY());
		} else {
			return String.format(
					(Locale) null,
					"p:%d;x:%f;y:%f", in_pos.getPage(), in_pos.getX(), in_pos.getY());
		}
    }

    /**
     * Get the Signature Profile ID for this set of parameters
     *
     * @return the Signature Profile ID
     */
    public String getPdfAsSignatureProfileId() {

        //Add Signature Param here//
        String profileId;

        if (!PROFILE_VISIBILITY) {
            log.debug("Profile visibility was set to false");
            return Profile.INVISIBLE.name();
        }

        switch (this.signatureProfile) {
            case BASE_LOGO:
            case INVISIBLE:
                return this.signatureProfile.name();
            case AMTSSIGNATURBLOCK:
                profileId = this.signatureProfile.name();
                profileId += getLangProfilePart(this.signatureLanguage);
                profileId += "_RECOMMENDED";
                return profileId;
            default:
                profileId = this.signatureProfile.name();
                profileId += getLangProfilePart(this.signatureLanguage);
                break;
        }

        if (this.signatureNote != null)
            profileId += "_NOTE";

        if (this.enablePDFACompat)
            profileId += "_PDFA";

        log.debug("Profile ID: {}", profileId);
        return profileId;
    }

    private static String getLangProfilePart(String lang) {
        return ("en".equals(lang)) ? "_EN" : "_DE";
    }
}



