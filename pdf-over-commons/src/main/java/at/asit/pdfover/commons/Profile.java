package at.asit.pdfover.commons;

public enum Profile {

    SIGNATURBLOCK_SMALL,  //$NON-NLS-1$
    AMTSSIGNATURBLOCK,  //$NON-NLS-1$
    BASE_LOGO, //$NON-NLS-1$
    INVISIBLE;

    public static int length = 4;

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


}
