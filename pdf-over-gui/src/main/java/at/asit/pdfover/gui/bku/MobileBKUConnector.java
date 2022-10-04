package at.asit.pdfover.gui.bku;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.bku.mobile.ATrustParser;
import at.asit.pdfover.gui.workflow.states.MobileBKUState;
import at.asit.pdfover.gui.workflow.states.MobileBKUState.UsernameAndPassword;
import at.asit.pdfover.signer.BkuSlConnector;
import at.asit.pdfover.signer.SignatureException;
import at.asit.pdfover.signer.UserCancelledException;
import at.asit.pdfover.signer.pdfas.PdfAs4SLRequest;

import static at.asit.pdfover.commons.Constants.ISNOTNULL;

public class MobileBKUConnector implements BkuSlConnector {
    private static final Logger log = LoggerFactory.getLogger(MobileBKUConnector.class);
    
    private final @Nonnull MobileBKUState state;
    public MobileBKUConnector(@Nonnull MobileBKUState state) {
        this.state = state;
        state.storeRememberedCredentialsTo(this.credentials);
    }

    public @Nonnull UsernameAndPassword credentials = new UsernameAndPassword();

    /**
     * This method takes the SLRequest from PDF-AS, and blocks until it has obtained a response
     */
    @Override
	public String handleSLRequest(PdfAs4SLRequest slRequest) throws SignatureException, UserCancelledException {
        log.debug("Got security layer request: (has file part: {})\n{}", (slRequest.signatureData != null), slRequest.xmlRequest);
        try (final CloseableHttpClient httpClient = HttpClients.custom().disableRedirectHandling().build()) {
            ClassicHttpRequest currentRequest = buildInitialRequest(slRequest);
            ATrustParser.Result response;
            while ((response = sendHTTPRequest(httpClient, currentRequest)).slResponse == null)
                currentRequest = presentResponseToUserAndReturnNextRequest(ISNOTNULL(response.html));
            log.debug("Returning security layer response:\n{}", response.slResponse);
            return response.slResponse;
        } catch (UserCancelledException e) {
            throw e;
        } catch (Exception e) {
            throw new SignatureException(e);
        }
    }

    /**
     * Sends the specified request, following redirects (including meta-tag redirects) recursively
     * @return The JSOUP document retrieved
     * @throws IOException on HTTP error codes
     * @throws ProtocolException
     * @throws URISyntaxException
     * @throws InterruptedException
     */
    private @Nonnull ATrustParser.Result sendHTTPRequest(CloseableHttpClient httpClient, ClassicHttpRequest request) throws IOException, ProtocolException, URISyntaxException {
        log.debug("Sending request to '{}'...", request.getUri().toString());
        try (final CloseableHttpResponse response = httpClient.execute(request)) {
            int httpStatus = response.getCode();
            if ((httpStatus == HttpStatus.SC_MOVED_PERMANENTLY) || (httpStatus == HttpStatus.SC_MOVED_TEMPORARILY)) {
                Header redirectPath = response.getHeader("location");
                if (redirectPath == null)
                    throw new IOException("Received HTTP redirect, but no Location header.");
                return sendHTTPRequest(httpClient, buildRedirectedRequest(request.getUri(), redirectPath.getValue()));
            }

            if (httpStatus != HttpStatus.SC_OK) {
                throw new IOException("Got HTTP status " + httpStatus + " " + Optional.ofNullable(response.getReasonPhrase()).orElse("(null)"));
            }
                        
            Header refreshHeader = response.getHeader("refresh");
            if (refreshHeader != null)
                return sendHTTPRequest(httpClient, buildRefreshHeaderRequest(request.getUri(), refreshHeader.getValue()));

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity == null)
                throw new IOException("Did not get a HTTP body (entity == null)");
            
            ContentType contentType = ContentType.parse(responseEntity.getContentType());
            String entityBody = EntityUtils.toString(response.getEntity(),contentType.getCharset());
            if (entityBody == null)
                throw new IOException("Did not get a HTTP body (entity content == null)");

            if ("text/html".equals(contentType.getMimeType())) {
                Document resultDocument = Jsoup.parse(entityBody, request.getUri().toASCIIString());
                if (resultDocument == null)
                {
                    log.error("Failed to parse A-Trust server response as HTML:\n{}", entityBody);
                    throw new IOException("Failed to parse HTML");
                }

                Element metaRefresh = resultDocument.selectFirst("meta[http-equiv=\"refresh\"i]");
                if (metaRefresh != null) {
                    String refreshContent = metaRefresh.attr("content");
                    if (!refreshContent.isEmpty())
                        return sendHTTPRequest(httpClient, buildRefreshHeaderRequest(request.getUri(), refreshContent));
                }
                return ATrustParser.Parse(resultDocument);
            } else {
                return ATrustParser.Parse(request.getUri(), contentType.getMimeType(), entityBody);
            }
        }
    }

    /**
     * Builds a HttpRequest for the given base URI and (potentially relative) redirect path
     */
    private static @Nonnull ClassicHttpRequest buildRedirectedRequest(URI baseURI, String redirectLocation) {
        log.debug("following redirect: {}", redirectLocation);
        return new HttpGet(baseURI.resolve(redirectLocation));
    }

    /**
     * Builds a HttpRequest for redirection to a given Refresh header value
     */
    private static @Nonnull ClassicHttpRequest buildRefreshHeaderRequest(URI baseURI, String refreshHeader) throws IOException {
        // refresh value is delay in seconds, semicolon, URL=, url
        Pattern pattern = Pattern.compile("^\\s*[0-9\\.]+\\s*;\\s*(?:[uU][rR][lL]\s*=\s*)(.+)$");
        Matcher matcher = pattern.matcher(refreshHeader);
        if (!matcher.matches())
            throw new IOException("Got invalid Refresh header with value \"" + refreshHeader + "\".");
        String redirectURL = matcher.group(1);
        return buildRedirectedRequest(baseURI, redirectURL);
    }

    /**
     * Builds the initial request to A-Trust based on the specified SL request
     */
    private static @Nonnull ClassicHttpRequest buildInitialRequest(PdfAs4SLRequest slRequest) {
        HttpPost post = new HttpPost(Constants.MOBILE_BKU_URL);
        if (slRequest.signatureData != null) {
            post.setEntity(MultipartEntityBuilder.create()
                .addBinaryBody("fileupload", slRequest.signatureData.getByteArray(), ContentType.APPLICATION_PDF, "sign.pdf")
                .addTextBody("XMLRequest", slRequest.xmlRequest)
                .build());
        } else {
            post.setEntity(UrlEncodedFormEntityBuilder.create()
                .add("XMLRequest", slRequest.xmlRequest)
                .build());
        }
        return post;
    }

    private static @Nonnull ClassicHttpRequest buildFormSubmit(@Nonnull ATrustParser.HTMLResult html, @Nonnull String submitButtonId) {
        HttpPost post = new HttpPost(html.formTarget);
        
        var builder = MultipartEntityBuilder.create();
        for (var pair : html.iterateFormOptions())
            builder.addTextBody(pair.getKey(), pair.getValue());
        if (html.submitButtons.containsKey(submitButtonId)) {
            var submitInfo = html.submitButtons.get(submitButtonId);
            builder.addTextBody(submitInfo.name, submitInfo.value);
        } else {
            log.warn("Attempted to use submit button #{} which does not exist. Omitting.", submitButtonId);
        }

        post.setEntity(builder.build());
        return post;
    }

    /**
     * Main lifting function for MobileBKU UX
     * @return the next request to make, or null if the current response should be returned
     */
    private @Nonnull ClassicHttpRequest presentResponseToUserAndReturnNextRequest(@Nonnull ATrustParser.HTMLResult html) throws UserCancelledException {
        if (html.usernamePasswordBlock != null) {
            try {
                while ((this.credentials.username == null) || (this.credentials.password == null)) {
                    this.state.getCredentialsFromUserTo(this.credentials, null); // TODO error message
                }
                html.usernamePasswordBlock.setUsernamePassword(this.credentials.username, this.credentials.password);
                return buildFormSubmit(html, "Button_Identification");
            } catch (UserCancelledException e) {
                return buildFormSubmit(html, "Button_Cancel");
            }
        }
        if (html.qrCodeBlock != null) {
            try (final CloseableHttpClient httpClient = HttpClients.custom().disableRedirectHandling().build()) {
                final HttpGet request = new HttpGet(html.qrCodeBlock.qrCodeURI);
                boolean[] done = new boolean[1];
                done[0] = false;
                Thread longPollThread = new Thread(() -> {
                    log.debug("longPollThread hello");
                    while (!done[0]) {
                        try (final CloseableHttpResponse response = httpClient.execute(new HttpGet(html.qrCodeBlock.pollingURI))) {
                            JSONObject jsonResponse = new JSONObject(EntityUtils.toString(response.getEntity()));
                            if (jsonResponse.getBoolean("Fin"))
                                state.signalQRScanned();
                            else if (jsonResponse.getBoolean("Wait"))
                            {
                                log.debug("longPollThread continue...");
                                continue;
                            }
                            else if (jsonResponse.getBoolean("Error"))
                                state.signalQRScanned(); /* will trigger reload and find error; this is the same thing a-trust does */
                            else {
                                log.warn("Unknown long poll response:\n{}", jsonResponse.toString(2));
                                break;
                            }
                        } catch (NoHttpResponseException e) {
                            continue;
                        } catch (IOException | ParseException | IllegalStateException e) {
                            log.warn("QR code long polling exception", e);
                            /* sleep so we don't hammer a-trust too hard in case this goes wrong */
                            try { Thread.sleep(5000); } catch (InterruptedException e2) {}
                        }
                    }
                    log.debug("longPollThread goodbye");
                });
                try {
                    longPollThread.start();
                    MobileBKUState.QRResult result = this.state.showQRCode(html.qrCodeBlock.referenceValue, html.qrCodeBlock.qrCodeURI, null);
                    switch (result) {
                        case UPDATE: return new HttpGet(html.htmlDocument.baseUri());
                        case TO_FIDO2: throw new IllegalStateException();
                        case TO_SMS: throw new IllegalStateException();
                    }
                } finally {
                    done[0] = true;
                    request.abort();
                    try { longPollThread.join(1000); } catch (InterruptedException e) {}
                }
            } catch (IOException e) {
                log.warn("closing CloseableHttpClient threw exception", e);
            }
        }
        throw new IllegalStateException("No top-level block is set? Something has gone terribly wrong.");
    }

    private static class UrlEncodedFormEntityBuilder {
        private UrlEncodedFormEntityBuilder() {}
        private List<NameValuePair> values = new ArrayList<>();
        public static @Nonnull UrlEncodedFormEntityBuilder create() { return new UrlEncodedFormEntityBuilder(); }
        public @Nonnull UrlEncodedFormEntityBuilder add(String key, String value) { values.add(new BasicNameValuePair(key, value)); return this; }
        public @Nonnull UrlEncodedFormEntity build() { return new UrlEncodedFormEntity(values, Charset.forName("utf-8")); }
    }
}
