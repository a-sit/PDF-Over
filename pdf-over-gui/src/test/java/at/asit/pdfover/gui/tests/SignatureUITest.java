package at.asit.pdfover.gui.tests;

import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import at.asit.pdfover.commons.Profile;

public class SignatureUITest extends AbstractSignatureUITest {

    @ParameterizedTest
    @EnumSource(Profile.class)
    public void testSignatureAutoPosition() throws IOException {
        setCredentials();
        testSignature(false, false);
    }

    @ParameterizedTest
    @EnumSource(Profile.class)
    public void testSignatureAutoPositionNegative() throws IOException {
        setCredentials();
        testSignature(true, false);
    }
}