package at.asit.pdfover.commons;

import java.util.Locale;

public enum Profile {

    SIGNATURBLOCK_SMALL,
    AMTSSIGNATURBLOCK,
    BASE_LOGO,
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

    public static Profile getDefaultProfile(){
        return SIGNATURBLOCK_SMALL;
    }

    public String getDefaultSignatureBlockNote(Locale locale){

        if (this == Profile.SIGNATURBLOCK_SMALL){
            return Messages.getString("simple_config.Note_Default_Standard", locale);
        } else if (this == Profile.AMTSSIGNATURBLOCK) {
            return Messages.getString("simple_config.Note_Default_OfficialSignature", locale);
        } else {
            return "";
        }
    }

}
