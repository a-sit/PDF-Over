package at.asit.pdfover.gui.bku.mobile;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ATrustParser {
    private static class ComponentParseFailed extends Exception {}

    private static class TopLevelFormBlock {
        protected final @NonNull org.jsoup.nodes.Document htmlDocument;
        protected final @NonNull Map<String, String> formOptions;
        protected TopLevelFormBlock(@NonNull org.jsoup.nodes.Document d, @NonNull Map<String,String> fO) { this.htmlDocument = d; this.formOptions = fO; }

        protected void abortIfElementMissing(@NonNull String selector) throws ComponentParseFailed {
            if (this.htmlDocument.selectFirst(selector) != null) return;
            log.debug("Tested for element {} -- not found.", selector);
            throw new ComponentParseFailed();
        }
        protected @NonNull org.jsoup.nodes.Element getElementEnsureNotNull(@NonNull String selector) throws ComponentParseFailed {
            var elm = this.htmlDocument.selectFirst(selector);
            if (elm == null) { log.warn("Expected element not found in response: {}", selector); throw new ComponentParseFailed(); }
            return elm;
        }
        protected @NonNull String getAttributeEnsureNotNull(@NonNull String selector, @NonNull String attribute) throws ComponentParseFailed {
            var elm = getElementEnsureNotNull(selector);
            if (!elm.hasAttr(attribute)) { log.warn("Element {} is missing expected attribute '{}'.", selector, attribute); throw new ComponentParseFailed(); }
            return elm.attr(attribute);
        }
        protected @NonNull URI getURIAttributeEnsureNotNull(@NonNull String selector, @NonNull String attribute) throws ComponentParseFailed {
            String value = getAttributeEnsureNotNull(selector, attribute);
            try {
                return new URI(value);
            } catch (URISyntaxException e) {
                if (attribute.startsWith("abs:"))
                    attribute = attribute.substring(4);
                log.warn("Element {} attribute {} is '{}', could not be parsed as URI", selector, attribute, getAttributeEnsureNotNull(selector, attribute));
                throw new ComponentParseFailed();
            }
        }
        protected @NonNull URI getLongPollURI() throws ComponentParseFailed {
            var pollingScriptElm = getElementEnsureNotNull("#jsLongPoll script");
            String pollingScript = pollingScriptElm.data();
            int startIdx = pollingScript.indexOf("qrpoll(\"");
            if (startIdx < 0) { log.warn("Failed to find 'qrpoll(\"' in jsLongPoll script:\n{}", pollingScript); throw new ComponentParseFailed(); }
            startIdx += 8;

            int endIdx = pollingScript.indexOf("\");", startIdx);
            if (endIdx < 0) { log.warn("Failed to find qrpoll terminator '\");' in jsLongPoll script:\n{}", pollingScript); throw new ComponentParseFailed(); }

            String pollingUriString = pollingScript.substring(startIdx, endIdx);
            try {
                return new URI(pollingScriptElm.baseUri()).resolve(pollingUriString);
            } catch (URISyntaxException e) {
                log.warn("Long-poll URI '{}' could not be parsed", pollingUriString);
                throw new ComponentParseFailed();
            }
        }
    }

    public static class AutoSkipBlock extends TopLevelFormBlock {
        public final @NonNull String submitButton;

        private AutoSkipBlock(@NonNull org.jsoup.nodes.Document htmlDocument, @NonNull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            if (htmlDocument.baseUri().contains("/tanAppInfo.aspx")) {
                this.submitButton = "#NextBtn";
            } else { throw new ComponentParseFailed(); }
        }
    }

    public static class InterstitialBlock extends TopLevelFormBlock {
        public final @NonNull String submitButton;
        public final @NonNull String interstitialMessage;

        private InterstitialBlock(@NonNull org.jsoup.nodes.Document htmlDocument, @NonNull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            if (htmlDocument.baseUri().contains("/ExpiresInfo.aspx")) {
                this.interstitialMessage = getElementEnsureNotNull("#Label2").ownText();
                this.submitButton = "#Button_Next";
            } else { throw new ComponentParseFailed(); }
        }
    }

    public static class ErrorBlock extends TopLevelFormBlock {
        public final boolean isRecoverable;
        public final boolean requiresResponse;
        public final @NonNull String errorText;

        private ErrorBlock(@NonNull org.jsoup.nodes.Document htmlDocument, @NonNull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);

            try {
                String documentPath = new URI(htmlDocument.baseUri()).getPath();
                String aspxFile = documentPath.substring(documentPath.lastIndexOf('/'));

                // gods this is such a hack, why can't they have a proper error element or something
                if (!(
                    (aspxFile.startsWith("/error") && aspxFile.endsWith(".aspx")) ||
                    (aspxFile.equals("/SessionClosed.aspx"))
                ))
                    throw new ComponentParseFailed();
            } catch (URISyntaxException ex) {
                log.warn("Failed to parse document base URI as URI? ({})", htmlDocument.baseUri());
                throw new ComponentParseFailed();
            }

            this.isRecoverable = (htmlDocument.selectFirst("#Button_Back") != null);
            this.requiresResponse = (htmlDocument.selectFirst("#Button_Cancel") != null);

            StringBuilder errorText = new StringBuilder(getElementEnsureNotNull("#Label1").ownText().trim());
            var detailLabel = this.htmlDocument.selectFirst("#LabelDetail");
            if (detailLabel != null)
                errorText.append("\n").append(detailLabel.ownText().trim());
            this.errorText = errorText.toString();
        }
    }

    public static class UsernamePasswordBlock extends TopLevelFormBlock {
        private final @NonNull String usernameKey;
        private final @NonNull String passwordKey;
        public final String errorMessage;

        public void setUsernamePassword(String username, String password) {
            formOptions.put(usernameKey, username); formOptions.put(passwordKey, password);
        }

        private UsernamePasswordBlock(@NonNull org.jsoup.nodes.Document htmlDocument, @NonNull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            abortIfElementMissing("#handynummer");
            this.usernameKey = getAttributeEnsureNotNull("#handynummer", "name");
            this.passwordKey = getAttributeEnsureNotNull("#signaturpasswort", "name");
            this.errorMessage = null;
        }
    }

    public static class SMSTanBlock extends TopLevelFormBlock {
        private final @NonNull String tanKey;
        public final @NonNull String referenceValue;
        public final String errorMessage;

        public void setTAN(String tan) {
            formOptions.put(tanKey, tan);
        }

        private SMSTanBlock(@NonNull org.jsoup.nodes.Document htmlDocument, @NonNull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            abortIfElementMissing("#input_tan");
            this.tanKey = getAttributeEnsureNotNull("#input_tan", "name");
            this.referenceValue = getElementEnsureNotNull("#vergleichswert").ownText();
            this.errorMessage = null;
        }
    }

    public static class QRCodeBlock extends TopLevelFormBlock {
        public final @NonNull String referenceValue;
        public final @NonNull URI qrCodeURI;
        public final @NonNull URI pollingURI;
        public final String errorMessage;

        private QRCodeBlock(@NonNull org.jsoup.nodes.Document htmlDocument, @NonNull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            abortIfElementMissing("#qrimage");
            
            this.referenceValue = getElementEnsureNotNull("#vergleichswert").ownText();
            this.qrCodeURI = getURIAttributeEnsureNotNull("#qrimage", "abs:src");
            this.pollingURI = getLongPollURI();

            this.errorMessage = null;
        }
    }

    public static class WaitingForAppBlock extends TopLevelFormBlock {
        public final @NonNull String referenceValue;
        public final @NonNull URI pollingURI;

        private WaitingForAppBlock(@NonNull org.jsoup.nodes.Document htmlDocument, @NonNull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            abortIfElementMissing("#smartphoneAnimation");

            this.referenceValue = getElementEnsureNotNull("#vergleichswert").ownText();
            this.pollingURI = getLongPollURI();            
        }
    }

    public static class WaitingForBiometryBlock extends TopLevelFormBlock {
        public final @NonNull String referenceValue;
        public final @NonNull URI pollingURI;

        private WaitingForBiometryBlock(@NonNull org.jsoup.nodes.Document htmlDocument, @NonNull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            abortIfElementMissing("#biometricimage");

            this.referenceValue = getElementEnsureNotNull("#vergleichswert").ownText();
            this.pollingURI = getLongPollURI();
        }
    }

    public static class Fido2Block extends TopLevelFormBlock {
        public final @NonNull String fidoOptions;
        private final @NonNull String credentialResultKey;

        public void setFIDOResult(String result) { formOptions.put(credentialResultKey, result); }

        private Fido2Block(@NonNull org.jsoup.nodes.Document htmlDocument, @NonNull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            abortIfElementMissing("#fidoBlock");
            this.fidoOptions = getAttributeEnsureNotNull("#credentialOptions", "value");
            this.credentialResultKey = getAttributeEnsureNotNull("#credentialResult", "name");
        }
    }

    public static class HTMLResult {
        public final @NonNull org.jsoup.nodes.Document htmlDocument;
        public final @NonNull URI formTarget;
        public final @NonNull Map<String, String> formOptions = new HashMap<>();

        public @NonNull Iterable<Map.Entry<String, String>> iterateFormOptions() { return formOptions.entrySet(); }

        /* optional links (any number may or may not be null) */
        public final URI signatureDataLink;
        public final URI smsTanLink;
        public final URI fido2Link;

        /* top-level blocks (exactly one is not null) */
        public final AutoSkipBlock autoSkipBlock;
        public final InterstitialBlock interstitialBlock;
        public final ErrorBlock errorBlock;
        public final UsernamePasswordBlock usernamePasswordBlock;
        public final SMSTanBlock smsTanBlock;
        public final QRCodeBlock qrCodeBlock;
        public final WaitingForAppBlock waitingForAppBlock;
        public final WaitingForBiometryBlock waitingForBiometryBlock;
        public final Fido2Block fido2Block;

        private void validate() {
            Set<String> populated = new HashSet<>();

            if (autoSkipBlock != null) populated.add("autoSkipBlock");
            if (interstitialBlock != null) populated.add("interstitialBlock");
            if (errorBlock != null) populated.add("errorBlock");
            if (usernamePasswordBlock != null) populated.add("usernamePasswordBlock");
            if (smsTanBlock != null) populated.add("smsTanBlock");
            if (qrCodeBlock != null) populated.add("qrCodeBlock");
            if (waitingForAppBlock != null) populated.add("waitingForAppBlock");
            if (waitingForBiometryBlock != null) populated.add("waitingForBiometryBlock");
            if (fido2Block != null) populated.add("fido2Block");

            switch (populated.size()) {
                case 0: log.error("Did not find any top-level blocks.\n{}", this.htmlDocument.toString()); break;
                case 1: /* passed */ return;
                default: log.error("Found too many top-level blocks: {}\n", String.join(", ", populated), this.htmlDocument.toString()); break;
            }
            throw new IllegalArgumentException("Unknown A-Trust page reached?");
        }

        private URI getHrefIfExists(String selector) {
            var elm = htmlDocument.selectFirst(selector);
            if (elm == null) return null;

            String url = elm.absUrl("href");
            try {
                return new URI(url);
            } catch (Exception e) {
                log.warn("Invalid {} href attribute: {} ({})", selector, elm.attr("href"), url);
                return null;
            }
        }

        /**
         * tries to parse T using its constructor; if ComponentParseFailed is thrown, swallows it
         */
        private <T extends TopLevelFormBlock> T TryParseMainBlock(Class<T> clazz) {
            try {
                return clazz.getDeclaredConstructor(org.jsoup.nodes.Document.class, Map.class).newInstance(this.htmlDocument, this.formOptions);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
                log.error("Internal parser error; check your method signatures?", e);
                return null;
            } catch (InvocationTargetException wrappedE) {
                Throwable e = wrappedE.getCause();
                if (!(e instanceof ComponentParseFailed)) {
                    if (e instanceof RuntimeException)
                        throw (RuntimeException)e;
                    log.warn("Unexpected parser failure.", e);
                }
                return null;
            }
        }

        private HTMLResult(@NonNull org.jsoup.nodes.Document htmlDocument) {
            log.trace("Now parsing:\n{}", htmlDocument.toString());
            this.htmlDocument = htmlDocument;

            var forms = htmlDocument.getElementsByTag("form");
            if (forms.size() != 1) {
                log.error("Found {} forms in A-Trust response document, expected 1. Document:\n{}", forms.size(), htmlDocument.toString());
                throw new IllegalArgumentException("Failed to parse A-Trust response page");
            }

            var mainForm = forms.first(); /* size check above */
            String formAction = mainForm.absUrl("action");
            try {
                this.formTarget = new URI(formAction);
            } catch (URISyntaxException e) {
                log.error("Invalid form target in page: {} ({})", mainForm.attr("action"), formAction, e);
                throw new IllegalArgumentException("Failed to parse A-Trust response page");
            }

            for (var input : mainForm.select("input")) {
                String name = input.attr("name");

                if (name.isEmpty())
                    continue;

                /* submit inputs omitted here, they only get sent if they are "clicked", cf. MobileBKUConnector::buildFormSubmit */
                if ("submit".equalsIgnoreCase(input.attr("type")))
                    continue;
                    
                this.formOptions.put(name, input.attr("value"));
            }

            this.signatureDataLink = getHrefIfExists("#LinkList a[href*=\"ShowSigobj.aspx\"]"); /* grr, they didn't give it an ID */
            this.smsTanLink = getHrefIfExists("#SmsButton");
            this.fido2Link = getHrefIfExists("#FidoButton"); // TODO hide the button if unsupported?

            this.autoSkipBlock = TryParseMainBlock(AutoSkipBlock.class);
            this.interstitialBlock = TryParseMainBlock(InterstitialBlock.class);
            this.errorBlock = TryParseMainBlock(ErrorBlock.class);
            this.usernamePasswordBlock = TryParseMainBlock(UsernamePasswordBlock.class);
            this.smsTanBlock = TryParseMainBlock(SMSTanBlock.class);
            this.qrCodeBlock = TryParseMainBlock(QRCodeBlock.class);
            this.waitingForAppBlock = TryParseMainBlock(WaitingForAppBlock.class);
            this.waitingForBiometryBlock = TryParseMainBlock(WaitingForBiometryBlock.class);
            this.fido2Block = TryParseMainBlock(Fido2Block.class);
            
            validate();
        }
    }

    public static class Result {
        public final String slResponse;
        public final HTMLResult html;

        private Result(@NonNull String slResponse) { this.slResponse = slResponse; this.html = null; }
        private Result(@NonNull org.jsoup.nodes.Document htmlDocument) { this.slResponse = null; this.html = new HTMLResult(htmlDocument); }
    }

    public static @NonNull Result Parse(@NonNull org.jsoup.nodes.Document htmlDocument) { return new Result(htmlDocument); }

    public static @NonNull Result Parse(URI baseURI, String contentType, @NonNull String content) {
        if (contentType.equals("text/html"))
        {
            var document = Jsoup.parse(content, baseURI.toASCIIString());
            if (document == null)
            {
                log.error("Failed to parse HTML (document == null):\n{}", content);
                throw new IllegalArgumentException("A-Trust parsing failed");
            }
            return Parse(document);
        }

        if (contentType.endsWith("/xml"))
            return new Result(content);
        
        log.error("Unknown content-type \"{}\" from URI {}", contentType, baseURI.toString());
        throw new IllegalArgumentException("Unknown A-Trust page reached?");
    }
}
