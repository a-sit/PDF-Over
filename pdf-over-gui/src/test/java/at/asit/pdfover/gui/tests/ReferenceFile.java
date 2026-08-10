package at.asit.pdfover.gui.tests;


import at.asit.pdfover.commons.Profile;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enum representing reference files for signature profiles.
 */
@Getter
public enum ReferenceFile {
    AMTSSIGNATUR("refFileAmtssignatur.png", Profile.AMTSSIGNATURBLOCK),
    SIGNATURBLOCK_SMALL("refFileSignaturblockSmallNote.png", Profile.SIGNATURBLOCK_SMALL),
    BASE_LOGO("refFileBaseLogo.png", Profile.BASE_LOGO),
    INVISIBLE("refFileInvisible.png", Profile.INVISIBLE),
    TEST_NEGATIVE("refFileTestNegative.png", null);

    private final String fileName;
    private final Profile profile;
    private static final Map<Profile, String> REFERENCE_FILES;

    static {
        REFERENCE_FILES = Arrays.stream(values())
                .filter(ref -> ref.profile != null)
                .collect(Collectors.toMap(
                        ref -> ref.profile,
                        ref -> ref.fileName
                ));
    }

    ReferenceFile(String fileName, Profile profile) {
        this.fileName = fileName;
        this.profile = profile;
    }

    /**
     * Gets the reference file for a specific profile.
     * @param profile the signature profile
     * @return the corresponding reference file name
     */
    public static String getFileNameForProfile(Profile profile) {
        String fileName = REFERENCE_FILES.get(profile);
        if (fileName == null) {
            throw new IllegalArgumentException("No reference file defined for profile: " + profile);
        }
        return fileName;
    }

    /**
     * Gets the reference file name for negative test cases.
     * @return the negative test reference file name
     */
    public static String getNegativeTestFileName() {
        return TEST_NEGATIVE.fileName;
    }
}