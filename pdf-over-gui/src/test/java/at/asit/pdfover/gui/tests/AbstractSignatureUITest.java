package at.asit.pdfover.gui.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.commons.Profile;
import at.asit.pdfover.gui.Main;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;
import lombok.NonNull;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractSignatureUITest {

    private static Thread uiThread;
    private static Shell shell;
    private static StateMachine sm;
    private SWTBot bot;

    private static final File inputFile = new File("src/test/resources/TestFile.pdf");
    private static String outputDir = inputFile.getAbsoluteFile().getParent();
    private Profile currentProfile;
    private final String postFix = "_superSigned";
    private static final List<Profile> profiles = new ArrayList<>();

    private static final Logger logger = LoggerFactory
            .getLogger(AbstractSignatureUITest.class);

    protected String str(String k) { return Messages.getString(k); }

    @BeforeAll
    public static void prepareTestEnvironment() throws IOException {
        deleteTempDir();
        createTempDir();
        setSignatureProfiles();
    }

    private static void deleteTempDir() throws IOException {
        String root = inputFile.getAbsoluteFile().getParent();
        File dir = new File(root);
        for (File f : Objects.requireNonNull(dir.listFiles())) {
            if (f.getName().startsWith("output_")) {
                FileUtils.deleteDirectory(f);
            }
        }
    }

    private static void createTempDir() throws IOException {
        Path tmpDir = Files.createTempDirectory(Paths.get(inputFile.getAbsoluteFile().getParent()), "output_");
        tmpDir.toFile().deleteOnExit();
        outputDir = FilenameUtils.separatorsToSystem(tmpDir.toString());
    }


    @BeforeEach
    public final void setupUITest() throws InterruptedException, BrokenBarrierException {
        final CyclicBarrier swtBarrier = new CyclicBarrier(2);

        if (uiThread == null) {
            uiThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    Display.getDefault().syncExec(() -> {
                        currentProfile = getCurrentProfile();
                        setConfig(currentProfile);
                        sm = Main.setup(new String[]{inputFile.getAbsolutePath()});
                        shell = sm.getMainShell();

                        try {
                            swtBarrier.await();
                        } catch (InterruptedException | BrokenBarrierException e) {
                            throw new RuntimeException(e);
                        }
                        sm.start();
                    });
                }
            });
            uiThread.setDaemon(true);
            uiThread.start();
        }
        swtBarrier.await();
        bot =  new SWTBot(shell);
    }

    @AfterEach
    public void reset() throws InterruptedException {
        deleteOutputFile();
        closeShell();
    }

    public void closeShell() throws InterruptedException {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                shell.close();
            }
        });
        uiThread.join();
        uiThread = null;
    }


    protected void setCredentials() {
        try {
            ICondition widgetExists = new WidgetExistsCondition(str("mobileBKU.number"));
            bot.waitUntil(widgetExists, 20000);
            bot.textWithLabel(str("mobileBKU.number")).setText("TestUser-1902503362");
            bot.textWithLabel(str("mobileBKU.password")).setText("123456789");
            bot.button(str("common.Ok")).click();
        }
        catch (WidgetNotFoundException wnf) {
            bot.button(str("common.Cancel")).click();
            fail(wnf.getMessage());
        }

        File output = new File(getPathOutputFile());
        ICondition outputExists = new FileExistsCondition(output);
        bot.waitUntil(outputExists, 20000);

        if(!output.exists()) {
            bot.button(str("common.Cancel")).click();
        }
        assertTrue(output.exists(), "Received signed PDF");
    }

    private void deleteOutputFile() {
        if (getPathOutputFile() != null) {
            File outputFile = new File(getPathOutputFile());
            outputFile.delete();
            assertFalse(outputFile.exists());
            logger.info("Deleted output file");
        }
    }

    protected void testSignature(boolean negative, boolean captureRefImage) throws IOException {
        String outputFile = getPathOutputFile();
        assertNotNull(currentProfile);
        assertNotNull(outputFile);

        try (SignaturePositionValidator provider = new SignaturePositionValidator(negative, captureRefImage, currentProfile, outputFile)) {
            provider.verifySignaturePosition();
        } catch (Exception e) {
            logger.error("Error verifiying signature position", e);
        }
    }

    private static void setProperty(@NonNull Properties props, @NonNull String key, @NonNull String value) { props.setProperty(key, value); }

    private void setConfig(Profile currentProfile) {
        ConfigurationManager cm = new ConfigurationManager();
        Point size = cm.getMainWindowSize();

        Map<String, String> testParams = Map.ofEntries(
                Map.entry(Constants.CFG_BKU, cm.getDefaultBKUPersistent().name()),
                Map.entry(Constants.CFG_KEYSTORE_PASSSTORETYPE, "memory"),
                Map.entry(Constants.CFG_LOCALE, cm.getInterfaceLocale().toString()),
                Map.entry(Constants.CFG_LOGO_ONLY_SIZE, Double.toString(cm.getLogoOnlyTargetSize())),
                Map.entry(Constants.CFG_MAINWINDOW_SIZE, size.x + "," + size.y),
                Map.entry(Constants.CFG_OUTPUT_FOLDER, outputDir),
                Map.entry(Constants.CFG_POSTFIX, postFix),
                Map.entry(Constants.CFG_SIGNATURE_NOTE, currentProfile.getDefaultSignatureBlockNote(Locale.GERMANY)),
                Map.entry(Constants.CFG_SIGNATURE_POSITION, "auto"),
                Map.entry(Constants.SIGNATURE_PROFILE, currentProfile.toString()),
                Map.entry(Constants.CFG_SIGNATURE_LOCALE, cm.getSignatureLocale().toString())
        );

        File pdfOverConfig = new File(Constants.CONFIG_DIRECTORY + File.separator + Constants.DEFAULT_CONFIG_FILENAME);
        Properties props = new Properties();
        testParams.forEach((k, v) -> setProperty(props, k, v));

        try {
            FileOutputStream outputStream = new FileOutputStream(pdfOverConfig, false);
            props.store(outputStream, "TEST Configuration file was generated!");
        } catch (IOException e) {
            logger.warn("Failed to create configuration file.");
        }
    }

    public static void setSignatureProfiles() {
        Collections.addAll(profiles, Profile.values());
        assert(profiles.containsAll(EnumSet.allOf(Profile.class)));
    }

    public Profile getCurrentProfile() {
        currentProfile = profiles.get(0);
        profiles.remove(0);
        if (profiles.isEmpty()) {
            setSignatureProfiles();
        }
        return currentProfile;
    }

    /**
     * Returns path of the signed document.
     */
    private String getPathOutputFile() {
        String fileNameSigned = inputFile
                .getName()
                .substring(0, inputFile.getName().lastIndexOf('.'))
                .concat(postFix)
                .concat(".pdf");
        String pathOutputFile = FilenameUtils.separatorsToSystem(outputDir
                .concat("/")
                .concat(fileNameSigned));
        assertNotNull(pathOutputFile);
        return pathOutputFile;
    }

}