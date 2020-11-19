package at.asit.pdfover.commons;

public enum Profile {

    SIGNATURBLOCK_SMALL("Signaturblock Normal"),  //$NON-NLS-1$
    AMTSSIGNATURBLOCK("Amtssignatur"),  //$NON-NLS-1$
    BASE_LOGO("Nur Bildmarke"), //$NON-NLS-1$
    INVISIBLE("Unsichtbar");

    public static int length = 4;
    private String name;

    Profile(String profile){
        this.name = profile;
    }

    public static String[] getProfileStrings() {
        String[] profiles = new String[Profile.length];
        int i = 0;
        for (Profile profile : Profile.values()) {
            profiles[i] = profile.getName();
            i++;
        }
        return profiles;
    }

    public static Profile getProfileByIndex(int index) {
        String[] profiles = getProfileStrings();
        if (profiles.length < index) {
            return null;
        }
        return getProfile(profiles[index]);
    }

    public String getName() {
        return this.name;
    }

    public static Profile getProfile(String profile) {
         if (SIGNATURBLOCK_SMALL.getName().equals(profile)) {
            return SIGNATURBLOCK_SMALL;
        } else if (AMTSSIGNATURBLOCK.getName().equals(profile)) {
            return AMTSSIGNATURBLOCK;
        } else if (BASE_LOGO.getName().equals(profile)) {
            return BASE_LOGO;
        } else if (INVISIBLE.getName().equals(profile)){
            return INVISIBLE;
        }
        return null;
    }


}
