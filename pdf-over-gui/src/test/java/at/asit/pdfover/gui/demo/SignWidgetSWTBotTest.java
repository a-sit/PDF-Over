package at.asit.pdfover.gui.demo;

import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.Main;
import at.asit.pdfover.gui.demo.SignWidget;
import at.asit.pdfover.gui.tests.FileExistsCondition;
import at.asit.pdfover.gui.tests.WidgetExistsCondition;
import at.asit.pdfover.gui.workflow.StateMachine;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SignWidgetSWTBotTest {
    private Shell shell;
    private SWTBot bot;
    private SignWidget widget;
    private StateMachine sm;
    private static final File inputFile = new File("src/test/resources/TestFile.pdf");
    protected String str(String k) { return Messages.getString(k); }

    @Before
    public void setUp() {
        Display display = Display.getDefault();
        sm = Main.setup(new String[]{inputFile.getAbsolutePath()});
        shell = sm.getMainShell();
        sm.start();
        bot = new SWTBot(shell);
    }

    @After
    public void tearDown() { Display.getDefault().syncExec(() -> shell.dispose()); }

    @Test
    public void transitionsToSuccess() throws Exception {
        bot.button("Start1").click();
        bot.waitUntil(new DefaultCondition() {
            public boolean test() { return "SUCCESS".equals(widget.statusText()); }
            public String getFailureMessage() { return "Did not reach SUCCESS"; }
        }, 10000);
        assertEquals("SUCCESS", widget.statusText());
    }

    @Test
    public void setCredentials() {
        try {
            ICondition widgetExists = new WidgetExistsCondition(str("mobileBKU.number"));
            bot.waitUntil(widgetExists, 50000);
            bot.textWithLabel(str("mobileBKU.number")).setText("TestUser-1902503362");
            bot.textWithLabel(str("mobileBKU.password")).setText("123456789");
            bot.button(str("common.Ok")).click();
        }
        catch (WidgetNotFoundException wnf) {
            bot.button(str("common.Cancel")).click();
            fail(wnf.getMessage());
        }

    }
}