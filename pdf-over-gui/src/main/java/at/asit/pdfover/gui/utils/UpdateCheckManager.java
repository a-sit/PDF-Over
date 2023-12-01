package at.asit.pdfover.gui.utils;

import java.util.ArrayList;
import java.util.function.Consumer;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.controls.Dialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.Dialog.ICON;
import lombok.extern.slf4j.Slf4j;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

@Slf4j
public final class UpdateCheckManager {
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
        try (final CloseableHttpClient httpClient = HttpClientUtils.builderWithSettings().build()) {
            try (final CloseableHttpResponse httpResponse = httpClient.execute(new HttpGet(Constants.CURRENT_RELEASE_URL))) {
                final String latestVersion = EntityUtils.toString(httpResponse.getEntity()).trim();
                if (!VersionComparator.lessThan(Constants.APP_VERSION, latestVersion))
                    return Status.UP_TO_DATE;

                if ((latestVersionNotified == null) || VersionComparator.lessThan(latestVersionNotified, latestVersion)) {
                    latestVersionNotified = latestVersion;
                    // invoke GUI message in main thread
                    shell.getDisplay().asyncExec(() -> {
                        Dialog info = new Dialog(shell, Messages.getString("version_check.UpdateTitle"), Messages.formatString("version_check.UpdateText", latestVersion),
                                                    BUTTONS.OK_CANCEL, ICON.INFORMATION);
                        if (info.open() == SWT.OK)
                            SWTUtils.openURL(Constants.UPDATE_URL);
                    });
                }

                return Status.OUTDATED;
            }
        } catch (Exception e) {
            log.warn("Error downloading update information: ", e);
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
