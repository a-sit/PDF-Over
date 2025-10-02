package at.asit.pdfover.gui.demo;

import at.asit.pdfover.gui.demo.SignWidget;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SignWidgetSWTBotTest {
    private Shell shell;
    private SWTBot bot;
    private SignWidget widget;

    //@Before
    public void setUp() {
        Display display = Display.getDefault();
        shell = new Shell(display);
        shell.setLayout(new FillLayout());
        widget = new SignWidget(shell);
        shell.setSize(400, 200);
        shell.open();
        bot = new SWTBot(shell);
    }

    //@After
    public void tearDown() { Display.getDefault().syncExec(() -> shell.dispose()); }

    //@Test
    public void transitionsToSuccess() throws Exception {
        bot.button("Start1").click();
        bot.waitUntil(new DefaultCondition() {
            public boolean test() { return "SUCCESS".equals(widget.statusText()); }
            public String getFailureMessage() { return "Did not reach SUCCESS"; }
        }, 10000);
        assertEquals("SUCCESS", widget.statusText());
    }
}