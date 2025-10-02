package at.asit.pdfover.gui.demo;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public final class SignWidgetApp {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SignWidget Demo");
        shell.setLayout(new FillLayout());

        new SignWidget(shell); // create your widget

        shell.setSize(480, 240);
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}