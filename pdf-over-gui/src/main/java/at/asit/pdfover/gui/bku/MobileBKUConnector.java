package at.asit.pdfover.gui.bku;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.bku.mobile.ATrustParser;
import at.asit.pdfover.gui.utils.HttpClientUtils;
import at.asit.pdfover.gui.workflow.states.MobileBKUState;
import at.asit.pdfover.gui.workflow.states.MobileBKUState.UsernameAndPassword;
import at.asit.pdfover.signer.BkuSlConnector;
import at.asit.pdfover.signer.SignatureException;
import at.asit.pdfover.signer.UserCancelledException;
import at.asit.pdfover.signer.pdfas.PdfAs4SLRequest;
import at.asit.webauthnclient.WebAuthN;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MobileBKUConnector implements BkuSlConnector {    
    private final @NonNull MobileBKUState state;
    public MobileBKUConnector(@NonNull MobileBKUState state) {
        this.state = state;
        this.wantsFido2Default = WebAuthN.isAvailable() && state.getConfig().getFido2ByDefault();
        state.storeRememberedCredentialsTo(this.credentials);
    }

    private static class UserDisplayedError extends Exception {
        private final @NonNull String msg;
        @Override public @NonNull String getMessage() { return this.msg; }
        private UserDisplayedError(@NonNull String s) { this.msg = s; }
    }

    public @NonNull UsernameAndPassword credentials = new UsernameAndPassword();

    /**
     * This method takes the SLRequest from PDF-AS, and blocks until it has obtained a response
     */
    @Override
	public String handleSLRequest(PdfAs4SLRequest slRequest) throws SignatureException, UserCancelledException {
        log.debug("Got security layer request: (has file part: {})\n{}", (slRequest.signatureData != null), slRequest.xmlRequest);
        try (final CloseableHttpClient httpClient = HttpClientUtils.builderWithSettings().disableRedirectHandling().build()) {
            ClassicHttpRequest currentRequest = buildInitialRequest(slRequest);
            ATrustParser.Result response;
            while ((response = sendHTTPRequest(httpClient, currentRequest)).slResponse == null)
                currentRequest = presentResponseToUserAndReturnNextRequest(response.html);
            log.debug("Returning security layer response:\n{}", response.slResponse);
            return response.slResponse;
        } catch (UserDisplayedError e) {
            state.showUnrecoverableError(e.getMessage());
            throw new IllegalStateException("unreachable", e); /* showUnrecoverableError always throws */
        } catch (UserCancelledException e) {
            throw e;
        } catch (Exception e) {
            throw new SignatureException(e);
        }
    }

    /* some anti-infinite-loop safeguards so we don't murder the atrust servers by accident */
    private int loopHTTPRequestCounter = 0;
    private Long lastHTTPRequestTime = null;
    /**
     * Sends the specified request, following redirects (including meta-tag redirects) recursively
     * @return The JSOUP document retrieved
     * @throws IOException on HTTP error codes
     * @throws ProtocolException
     * @throws URISyntaxException
     * @throws InterruptedException
     */
    private @NonNull ATrustParser.Result sendHTTPRequest(CloseableHttpClient httpClient, ClassicHttpRequest request) throws IOException, ProtocolException, URISyntaxException, UserDisplayedError {
        while (loopHTTPRequestCounter < 50) {
	        long now = System.nanoTime();
	        if ((lastHTTPRequestTime != null) && ((now - lastHTTPRequestTime) < 2e+9)) { /* less than 2s since last request */
	            ++loopHTTPRequestCounter;
	        } else {
	            loopHTTPRequestCounter = 0;
	            lastHTTPRequestTime = now;
	        }
	
	        log.debug("Sending {} request to '{}'...", request.getMethod(), request.getUri().toString());
	        
	        try (final CloseableHttpResponse response = httpClient.execute(request)) {
	            int httpStatus = response.getCode();
	            if ((httpStatus == HttpStatus.SC_MOVED_PERMANENTLY) || (httpStatus == HttpStatus.SC_MOVED_TEMPORARILY)) {
	                Header redirectPath = response.getHeader("location");
	                if (redirectPath == null)
	                    throw new IOException("Received HTTP redirect, but no Location header.");
	                request = buildRedirectedRequest(request.getUri(), redirectPath.getValue());
	                continue;
	            }
	
	            if (httpStatus != HttpStatus.SC_OK) {
	                switch (httpStatus) {
	                    case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED: throw new UserDisplayedError(Messages.getString("error.ProxyAuthRequired"));
	                    case HttpStatus.SC_REQUEST_TOO_LONG: throw new UserDisplayedError(Messages.getString("atrusterror.http_413"));
	                    default: throw new UserDisplayedError(Messages.formatString("atrusterror.http_generic", httpStatus, Optional.ofNullable(response.getReasonPhrase()).orElse("(null)")));
	                }
	            }
	                        
	            Header refreshHeader = response.getHeader("refresh");
	            if (refreshHeader != null) {
	                request = buildRefreshHeaderRequest(request.getUri(), refreshHeader.getValue());
	                continue;
	            }
	
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
	                    log.debug("Failed to parse A-Trust server response as HTML:\n{}", entityBody);
	                    throw new IOException("Failed to parse HTML");
	                }
	
	                Element metaRefresh = resultDocument.selectFirst("meta[http-equiv=\"refresh\"i]");
	                if (metaRefresh != null) {
	                    String refreshContent = metaRefresh.attr("content");
	                    if (!refreshContent.isEmpty()) {
	                        request = buildRefreshHeaderRequest(request.getUri(), refreshContent);
	                        continue;
	                    }
	                }
	                return ATrustParser.Parse(resultDocument);
	            } else {
	                return ATrustParser.Parse(request.getUri(), contentType.getMimeType(), entityBody);
	            }
	        } catch (Exception e) {
	            log.debug("Failed to connect:", e);
	            e.printStackTrace();
	            throw new UserDisplayedError(Messages.formatString("error.FailedToConnect", e.getLocalizedMessage()));
	        }
        }
        throw new IOException("Infinite loop protection triggered");
    }

    /**
     * Builds a HttpRequest for the given base URI and (potentially relative) redirect path
     */
    private static @NonNull ClassicHttpRequest buildRedirectedRequest(URI baseURI, String redirectLocation) {
        log.debug("following redirect: {}", redirectLocation);
        return new HttpGet(baseURI.resolve(redirectLocation));
    }

    /**
     * Builds a HttpRequest for redirection to a given Refresh header value
     */
    private static @NonNull ClassicHttpRequest buildRefreshHeaderRequest(URI baseURI, String refreshHeader) throws IOException {
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
    private static final ContentType TEXT_UTF8 = ContentType.TEXT_PLAIN.withCharset("UTF-8");
    private static @NonNull ClassicHttpRequest buildInitialRequest(PdfAs4SLRequest slRequest) {
        HttpPost post = new HttpPost(Constants.MOBILE_BKU_URL);
        if (slRequest.signatureData != null) {
            post.setEntity(MultipartEntityBuilder.create()
                .addBinaryBody("fileupload", slRequest.signatureData.getByteArray(), ContentType.APPLICATION_PDF, "sign.pdf")
                .addTextBody("XMLRequest", slRequest.xmlRequest, TEXT_UTF8)
                .build());
        } else {
            post.setEntity(UrlEncodedFormEntityBuilder.create()
                .add("XMLRequest", slRequest.xmlRequest)
                .build());
        }
        return post;
    }

    private static @NonNull ClassicHttpRequest buildFormSubmit(@NonNull ATrustParser.HTMLResult html, String submitButton) {
        HttpPost post = new HttpPost(html.formTarget);

        var builder = MultipartEntityBuilder.create();
        for (var pair : html.iterateFormOptions())
            builder.addTextBody(pair.getKey(), pair.getValue(), TEXT_UTF8);

        if (submitButton != null) {
            var submitButtonElm = html.htmlDocument.selectFirst(submitButton);
            if (submitButtonElm != null) {
                if ("input".equalsIgnoreCase(submitButtonElm.tagName())) {
                    if ("submit".equalsIgnoreCase(submitButtonElm.attr("type"))) {
                        String name = submitButtonElm.attr("name");
                        if (!name.isEmpty())
                            builder.addTextBody(name, submitButtonElm.attr("value"), TEXT_UTF8);
                    } else {
                        log.warn("Skipped specified submitButton {}, type is {} (not submit)", submitButton, submitButtonElm.attr("type"));
                    }
                } else {
                    log.warn("Skipped specified submitButton {}, tag name is {} (not input)", submitButton, submitButtonElm.tagName());
                }
            } else {
                log.warn("Skipped specified submitButton {}, element not found", submitButton);
            }
        }

        post.setEntity(builder.build());
        return post;
    }

    @Slf4j
    private static class LongPollThread extends Thread implements AutoCloseable {
        
        private final CloseableHttpClient httpClient = HttpClientUtils.builderWithSettings().build();
        private final HttpGet request;
        private final Runnable signal;
        private boolean done = false;

        @Override
        public void run() {
            long timeout = System.nanoTime() + (300l * 1000l * 1000l * 1000l); /* a-trust timeout is 5 minutes */
            while (!done) {
                log.debug("LongPollThread Making request to {}...", request.getRequestUri());
                try (final CloseableHttpResponse response = httpClient.execute(request)) {
                    String jsonResponseStr = EntityUtils.toString(response.getEntity());
                    JSONObject jsonResponse = null;
                    try {
                        jsonResponse = new JSONObject(jsonResponseStr);
                        log.debug("Got long poll response:\n{}", jsonResponse.toString(2));
                    } catch (JSONException e) {
                        log.warn("Failed to parse long poll response:\n\"{}\"", jsonResponseStr);
                        throw e;
                    }
                    if (jsonResponse.getBoolean("Fin")) {
                        signalProbablyDone();
                    } else if (jsonResponse.getBoolean("Wait")) {
                        continue;
                    } else if (jsonResponse.getBoolean("Error")) {
                        signalProbablyDone(); /* will trigger reload and find error; this is the same thing a-trust does */
                    } else {
                        log.warn("Unknown long poll response:\n{}", jsonResponse.toString(2));
                        break;
                    }
                } catch (NoHttpResponseException e) {
                    if (timeout <= System.nanoTime()) {
                        log.debug("LongPollThread no response, expecting A-Trust timeout, triggering reload...");
                        signalProbablyDone(); /* reload main page to find the timeout error */
                    }
                    continue; /* httpclient timeout */
                } catch (Exception e) {
                    if (done) break;
                    log.warn("QR code long polling exception", e);

                    /* A-Trust does excepting handling this way, so we copy it (they might mask errors, so we should too...) */
                    try { Thread.sleep(10000); } catch (InterruptedException e2) {}
                    signalProbablyDone();
                }
            }
            log.debug("LongPollThread goodbye");
        }

        private void signalProbablyDone() {
            this.signal.run();
            /* so we don't send a second request that immediately gets aborted */
            try { Thread.sleep(500); } catch (InterruptedException e2) {}
        }

        public LongPollThread(URI uri, Runnable signal) {
            log.debug("LongPollThread setup for '{}'", uri);
            this.request = new HttpGet(uri);
            this.signal = signal;
        }

        @Override
        public void close() {
            done = true;

            if (this.request != null)
                this.request.abort();

            if (this.isAlive()) {
                this.interrupt();
                try { this.join(1000); } catch (InterruptedException e) {}
            }
            
            if (this.httpClient != null)
                try { this.httpClient.close(); } catch (IOException e) { log.warn("Auto-close of long-poll HTTP client threw exception", e); }
        }
        
    }

    private boolean wantsFido2Default;
    /**
     * Main lifting function for MobileBKU UX
     * @return the next request to make
     */
    private @NonNull ClassicHttpRequest presentResponseToUserAndReturnNextRequest(@NonNull ATrustParser.HTMLResult html) throws UserCancelledException {
        if ((html.errorBlock == null) && (html.usernamePasswordBlock == null)) { /* successful username/password auth */
            if ((this.credentials.username != null) && (this.credentials.password != null))
                state.rememberCredentialsIfNecessary(this.credentials);
        }

        if (wantsFido2Default && (html.fido2Link != null)) {
            wantsFido2Default = false;
            return new HttpGet(html.fido2Link);
        }

        if (html.autoSkipBlock != null) {
            return buildFormSubmit(html, html.autoSkipBlock.submitButton);
        }
        if (html.interstitialBlock != null) {
            this.state.showInformationMessage(html.interstitialBlock.interstitialMessage);
            return buildFormSubmit(html, html.interstitialBlock.submitButton);
        }
        if (html.errorBlock != null) {
            try {
                this.credentials.password = null;
                this.state.clearRememberedPassword();

                if (html.errorBlock.isRecoverable)
                    this.state.showRecoverableError(html.errorBlock.errorText);
                else
                    this.state.showUnrecoverableError(html.errorBlock.errorText);
                return buildFormSubmit(html, "#Button_Back");
            } catch (UserCancelledException e) {
                if (html.errorBlock.requiresResponse)
                    return buildFormSubmit(html, "#Button_Cancel");
                else
                    throw e;
            }
        }
        if (html.usernamePasswordBlock != null) {
            try {
                while ((this.credentials.username == null) || (this.credentials.password == null)) {
                    this.state.getCredentialsFromUserTo(this.credentials, html.usernamePasswordBlock.errorMessage);
                }
                html.usernamePasswordBlock.setUsernamePassword(this.credentials.username, this.credentials.password);
                return buildFormSubmit(html, "#Button_Identification");
            } catch (UserCancelledException e) {
                return buildFormSubmit(html, "#Button_Cancel");
            }
        }
        if (html.smsTanBlock != null) {
            MobileBKUState.SMSTanResult result = this.state.getSMSTanFromUser(
                html.smsTanBlock.referenceValue, html.signatureDataLink,
                html.fido2Link != null, html.smsTanBlock.errorMessage);
            
            switch (result.type) {
                case TO_FIDO2: if (html.fido2Link != null) return new HttpGet(html.fido2Link);
                case SMSTAN: html.smsTanBlock.setTAN(result.smsTan); return buildFormSubmit(html, "#SignButton");
            }
            return new HttpGet(html.htmlDocument.baseUri());
        }
        if (html.qrCodeBlock != null) {
            try (LongPollThread longPollThread = new LongPollThread(html.qrCodeBlock.pollingURI, () -> { this.state.signalQRScanned(); })) {
                this.state.showQRCode(html.qrCodeBlock.referenceValue, html.qrCodeBlock.qrCodeURI, html.signatureDataLink, html.smsTanLink != null, html.fido2Link != null, html.qrCodeBlock.errorMessage);
                longPollThread.start();
                var result = this.state.waitForQRCodeResult();
                switch (result) {
                    case UPDATE: break;
                    case TO_FIDO2: if (html.fido2Link != null) return new HttpGet(html.fido2Link); break;
                    case TO_SMS: if (html.smsTanLink != null) return new HttpGet(html.smsTanLink); break;
                }
                return new HttpGet(html.htmlDocument.baseUri());
            }
        }
        if (html.waitingForAppBlock != null) {
            try (LongPollThread longPollThread = new LongPollThread(html.waitingForAppBlock.pollingURI, () -> { this.state.signalAppOpened(); })) {
                this.state.showWaitingForAppOpen(html.waitingForAppBlock.referenceValue, html.signatureDataLink, html.smsTanLink != null, html.fido2Link != null);
                longPollThread.start();
                var result = this.state.waitForAppOpen();
                switch (result) {
                    case UPDATE: break;
                    case TO_FIDO2: if (html.fido2Link != null) return new HttpGet(html.fido2Link); break;
                    case TO_SMS: if (html.smsTanLink != null) return new HttpGet(html.smsTanLink); break;
                }
                return new HttpGet(html.htmlDocument.baseUri());
            }
        }
        if (html.waitingForBiometryBlock != null) {
            try (LongPollThread longPollThread = new LongPollThread(html.waitingForBiometryBlock.pollingURI, () -> { this.state.signalAppBiometryDone(); })) {
                this.state.showWaitingForAppBiometry(html.waitingForBiometryBlock.referenceValue, html.signatureDataLink, html.smsTanLink != null, html.fido2Link != null);
                longPollThread.start();
                var result = this.state.waitForAppBiometry();
                switch (result) {
                    case UPDATE: break;
                    case TO_FIDO2: if (html.fido2Link != null) return new HttpGet(html.fido2Link); break;
                    case TO_SMS: if (html.smsTanLink != null) return new HttpGet(html.smsTanLink); break;
                }
                return new HttpGet(html.htmlDocument.baseUri());
            }
        }
        if (html.fido2Block != null) {
            
            var fido2Result = this.state.promptUserForFIDO2Auth(html.fido2Block.fidoOptions, html.signatureDataLink, html.smsTanLink != null);

            switch (fido2Result.type) {
                case TO_SMS: return new HttpGet(html.smsTanLink);
                case CREDENTIAL: break;
            }

            var fido2Assertion = fido2Result.credential;

            Base64.Encoder base64 = Base64.getEncoder();
            
            JSONObject aTrustAssertion = new JSONObject();
            aTrustAssertion.put("id", fido2Assertion.id);
            aTrustAssertion.put("rawId", base64.encodeToString(fido2Assertion.rawId));
            aTrustAssertion.put("type", fido2Assertion.type);
            aTrustAssertion.put("extensions", new JSONObject()); // TODO fix extensions in library

            JSONObject aTrustAssertionResponse = new JSONObject();
            aTrustAssertion.put("response", aTrustAssertionResponse);
            aTrustAssertionResponse.put("authenticatorData", base64.encodeToString(fido2Assertion.response.authenticatorData));
            aTrustAssertionResponse.put("clientDataJson", base64.encodeToString(fido2Assertion.response.clientDataJSON));
            aTrustAssertionResponse.put("signature", base64.encodeToString(fido2Assertion.response.signature));
            if (fido2Assertion.response.userHandle != null)
                aTrustAssertionResponse.put("userHandle", base64.encodeToString(fido2Assertion.response.userHandle));
            else
                aTrustAssertionResponse.put("userHandle", JSONObject.NULL);
            
            html.fido2Block.setFIDOResult(aTrustAssertion.toString());
            return buildFormSubmit(html, "#FidoContinue");
        }
        throw new IllegalStateException("No top-level block is set? Something has gone terribly wrong.");
    }

    private static class UrlEncodedFormEntityBuilder {
        private UrlEncodedFormEntityBuilder() {}
        private List<NameValuePair> values = new ArrayList<>();
        public static @NonNull UrlEncodedFormEntityBuilder create() { return new UrlEncodedFormEntityBuilder(); }
        public @NonNull UrlEncodedFormEntityBuilder add(String key, String value) { values.add(new BasicNameValuePair(key, value)); return this; }
        public @NonNull UrlEncodedFormEntity build() { return new UrlEncodedFormEntity(values, Charset.forName("utf-8")); }
    }
}
