package at.asit.pdfover.gui.utils;

import java.awt.Desktop;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.bku.BKUHelper;
import at.asit.pdfover.gui.controls.Dialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.Dialog.ICON;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;

public final class UpdateCheckManager {
    static final Logger log = LoggerFactory.getLogger(UpdateCheckManager.class);
    private static Thread updateCheckThread = null;
    private static boolean needsCheck = false;

    public static void checkNow(Shell shell) {
        if (Constants.APP_VERSION == null)
            return;

        synchronized (UpdateCheckManager.class) {
            if (updateCheckThread != null)
                return;

            needsCheck = true;
            updateCheckThread = new Thread(() -> {
                while (true) {
                    synchronized (UpdateCheckManager.class) {
                        if (!needsCheck) {
                            UpdateCheckManager.updateCheckThread = null;
                            return;
                        }
                        needsCheck = false;
                    }
                    HttpClient client = (HttpClient) BKUHelper.getHttpClient();
                    GetMethod method = new GetMethod(Constants.CURRENT_RELEASE_URL);
                    try {
                        client.executeMethod(method);
                        final String version = method.getResponseBodyAsString().trim();
                        if (!VersionComparator.before(Constants.APP_VERSION, version))
                            return;

                        // wait 500ms before invoke the GUI message, because GUI had to be started from
                        // main thread
                        try { Thread.sleep(500); } catch (InterruptedException e1) { }
                        // invoke GUI message in main thread
                        shell.getDisplay().asyncExec(() -> {
                            Dialog info = new Dialog(shell,
                                    Messages.getString("version_check.UpdateTitle"),
                                    String.format(Messages.getString("version_check.UpdateText"), version),
                                    BUTTONS.OK_CANCEL, ICON.INFORMATION);

                            if (info.open() == SWT.OK)
                            {
                                if (Desktop.isDesktopSupported()) {
                                    try {
                                        Desktop.getDesktop().browse(new URI(Constants.UPDATE_URL));
                                    } catch (Exception e) {
                                        log.error("Error opening update location ", e);
                                    }
                                } else {
                                    log.info("SWT Desktop is not supported on this platform");
                                    Program.launch(Constants.UPDATE_URL);
                                }
                            }
                        });
                    } catch (Exception e) {
                        log.error("Error downloading update information: ", e);
                    }
                }
            });
            updateCheckThread.start();
        }
    }
}
