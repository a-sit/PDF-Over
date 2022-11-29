package at.asit.pdfover.gui.utils;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;

public final class HttpClientUtils {
    public static HttpClientBuilder builderWithSettings() {
        return HttpClients.custom().useSystemProperties();
    }
}
