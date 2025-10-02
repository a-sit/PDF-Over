package at.asit.pdfover.gui.demo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public final class SignWidget {
    private final SignStateMachine fsm = new SignStateMachine();
    private final Label status;
    public final Button start;

    public SignWidget(Composite parent) {
        parent.setLayout(new org.eclipse.swt.layout.RowLayout(SWT.VERTICAL));
        start = new Button(parent, SWT.PUSH);
        start.setText("Start");
        status = new Label(parent, SWT.NONE);
        status.setText("IDLE");

        java.util.concurrent.Executor exec =
                java.util.concurrent.Executors.newCachedThreadPool();

        fsm.onChange(s ->
                Display.getDefault().asyncExec(() -> status.setText(s.name())));

        start.addListener(SWT.Selection, e -> fsm.startAsync(exec));
    }

    public String statusText() { return status.getText(); }
}