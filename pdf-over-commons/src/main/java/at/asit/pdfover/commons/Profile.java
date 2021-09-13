package at.asit.pdfover.commons;

import java.util.Locale;

public enum Profile {

    SIGNATURBLOCK_SMALL,  //$NON-NLS-1$
    AMTSSIGNATURBLOCK,  //$NON-NLS-1$
    BASE_LOGO, //$NON-NLS-1$
    INVISIBLE;

    public static int length = Profile.values().length;

    public static Profile getProfile(String name) {
         if (SIGNATURBLOCK_SMALL.name().equals(name)) {
            return SIGNATURBLOCK_SMALL;
        } else if (AMTSSIGNATURBLOCK.name().equals(name)) {
            return AMTSSIGNATURBLOCK;
        } else if (BASE_LOGO.name().equals(name)) {
            return BASE_LOGO;
        } else if (INVISIBLE.name().equals(name)){
            return INVISIBLE;
        }
        return null;
    }

    public static String getDefaultProfile(){
        return SIGNATURBLOCK_SMALL.name();
    }

    public static String getSignatureBlockNoteTextAccordingToProfile(Profile profile, Locale locale){

        if (profile.equals(Profile.SIGNATURBLOCK_SMALL)){
            return Messages.getString("simple_config.Note_Default_Standard", locale);
        } else if (profile.equals(Profile.AMTSSIGNATURBLOCK)){
            return Messages.getString("simple_config.Note_Default_OfficialSignature", locale);
        } else {
            return "";
        }
    }

}
