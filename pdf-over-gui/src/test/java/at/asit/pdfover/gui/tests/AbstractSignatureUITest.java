package at.asit.pdfover.gui.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
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
    private SWTBotShell swtbs;
    private SWTBot bot;

    private static File inputFile = new File("src/test/resources/TestFile.pdf");
    private static String outputDir = inputFile.getAbsoluteFile().getParent();
    private String postFix = "_superSigned";
    private Profile currentProfile;
    private static final String UNIX_SEPARATOR = "/";

    private static final Logger logger = LoggerFactory
            .getLogger(AbstractSignatureUITest.class);

    SignaturePositionTestProvider provider = new SignaturePositionTestProvider();
    private static Path tmpDir;


    private static List<Profile> profiles = new ArrayList<>();

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
        for (File f : dir.listFiles()) {
            if (f.getName().startsWith("output_")) {
                FileUtils.deleteDirectory(f);
            }
        }
    }

    private static void createTempDir() throws IOException {
        tmpDir = Files.createTempDirectory(Paths.get(inputFile.getAbsoluteFile().getParent()), "output_");
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
                	currentProfile = getCurrentProfile();
                	setConfig(currentProfile);

                    sm = Main.setup(new String[]{inputFile.getAbsolutePath()});
                    shell = sm.getMainShell();

                    try {
                        swtBarrier.await();
                        sm.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            uiThread.setDaemon(true);
            uiThread.start();
        }
        swtBarrier.await();

        bot =  new SWTBot(shell);
        swtbs = bot.activeShell();
        swtbs.activate();
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
            ICondition widgetExists = new WidgetExitsCondition(str("mobileBKU.numb3r"));
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
            assertTrue(!outputFile.exists());
            logger.info("Deleted output file");
        }
    }

    protected void testSignature(boolean negative, boolean captureRefImage) throws IOException {
        String outputFile = getPathOutputFile();
        assertNotNull(currentProfile);
        assertNotNull(outputFile);
        provider.checkSignaturePosition(currentProfile, negative, getPathOutputFile(), captureRefImage);
    }

    private String getPathOutputFile() {
        String inputFileName = inputFile.getName();
        String pathOutputFile = inputFileName
                .substring(0, inputFileName.lastIndexOf('.'))
                .concat(postFix)
                .concat(".pdf");
        pathOutputFile = FilenameUtils.separatorsToSystem(outputDir
                .concat(UNIX_SEPARATOR)
                .concat(pathOutputFile));
        assertNotNull(pathOutputFile);
        return pathOutputFile;
    }

    private static void setProperty(@NonNull Properties props, @NonNull String key, @NonNull String value) { props.setProperty(key, value); }

    private void setConfig(Profile currentProfile) {
        ConfigurationManager cm = new ConfigurationManager();
        Point size = cm.getMainWindowSize();

        Map<String,String> testParams = Map.ofEntries(
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
            FileOutputStream outputstream = new FileOutputStream(pdfOverConfig, false);
	        props.store(outputstream, "TEST Configuration file was generated!");
        } catch (IOException e) {
            logger.warn("Failed to create configuration file.");
		}
    }

    public static void setSignatureProfiles() {
        for (Profile p : Profile.values()) {
            profiles.add(p);
        }
        assert(EnumSet.allOf(Profile.class).stream().allMatch(profiles::contains));
    }

    public Profile getCurrentProfile() {
        currentProfile = profiles.get(0);
        profiles.remove(0);
        if (profiles.isEmpty()) {
           	setSignatureProfiles();
           }
        return currentProfile;
    }

}
