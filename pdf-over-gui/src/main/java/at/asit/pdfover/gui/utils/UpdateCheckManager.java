package at.asit.pdfover.gui.utils;

import java.util.ArrayList;
import java.util.function.Consumer;

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
import org.eclipse.swt.widgets.Shell;

public final class UpdateCheckManager {
    private static final Logger log = LoggerFactory.getLogger(UpdateCheckManager.class);
    private static Thread updateCheckThread = null;
    private static boolean needsCheck = false;

    public enum Status { NOT_CHECKED, CHECKING, OUTDATED, UP_TO_DATE, FAILED };
    private static Status currentStatus = Status.NOT_CHECKED;
    public static Status getCurrentStatus() {
        synchronized (UpdateCheckManager.class) {
            return currentStatus;
        }
    }

    private static ArrayList<Consumer<Status>> statusCallbacks = new ArrayList<>();
    public static void registerStatusCallback(Consumer<Status> f) {
        synchronized (UpdateCheckManager.class) {
            statusCallbacks.add(f);
            f.accept(currentStatus);
        }
    }

    private static void setStatus(Status status) {
        synchronized(UpdateCheckManager.class) {
            currentStatus = status;
            for (Consumer<Status> f : statusCallbacks)
                f.accept(status);
        }
    }

    private static String latestVersionNotified = null;
    private static Status runCheck(Shell shell) {
        HttpClient client = (HttpClient) BKUHelper.getHttpClient();
        GetMethod method = new GetMethod(Constants.CURRENT_RELEASE_URL);
        try {
            client.executeMethod(method);
            final String version = method.getResponseBodyAsString().trim();
            if (!VersionComparator.before(Constants.APP_VERSION, version))
                return Status.UP_TO_DATE;

            if ((latestVersionNotified == null) || VersionComparator.before(latestVersionNotified, version)) {
                latestVersionNotified = version;
                // invoke GUI message in main thread
                shell.getDisplay().asyncExec(() -> {
                    Dialog info = new Dialog(shell,
                            Messages.getString("version_check.UpdateTitle"),
                            String.format(Messages.getString("version_check.UpdateText"), version),
                            BUTTONS.OK_CANCEL, ICON.INFORMATION);

                    if (info.open() == SWT.OK)
                        SWTUtils.openURL(Constants.UPDATE_URL);
                });
            }

            return Status.OUTDATED;
        } catch (Exception e) {
            log.error("Error downloading update information: ", e);
            return Status.FAILED;
        }
    }

    public static void checkNow(Shell shell) {
        if (Constants.APP_VERSION == null)
            return;

        synchronized (UpdateCheckManager.class) {
            if (updateCheckThread != null)
                return;

            needsCheck = true;
            updateCheckThread = new Thread(() -> {
                synchronized(UpdateCheckManager.class) {
                    setStatus(Status.CHECKING);
                }
                while (true) {
                    Status status = runCheck(shell);
                    synchronized (UpdateCheckManager.class) {
                        if (!UpdateCheckManager.needsCheck) {
                            setStatus(status);
                            UpdateCheckManager.updateCheckThread = null;
                            return;
                        }
                        UpdateCheckManager.needsCheck = false;
                        setStatus(Status.CHECKING);
                    }
                }
            });
            updateCheckThread.start();
        }
    }
}
