package at.asit.pdfover.gui.bku.mobile;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static at.asit.pdfover.commons.Constants.ISNOTNULL;

public class ATrustParser {
    private static final Logger log = LoggerFactory.getLogger(ATrustParser.class);

    private static class ComponentParseFailed extends Exception {}

    private static class TopLevelFormBlock {
        protected final @Nonnull org.jsoup.nodes.Document htmlDocument;
        protected final @Nonnull Map<String, String> formOptions;
        protected TopLevelFormBlock(@Nonnull org.jsoup.nodes.Document d, @Nonnull Map<String,String> fO) { this.htmlDocument = d; this.formOptions = fO; }

        protected void abortIfElementMissing(@Nonnull String selector) throws ComponentParseFailed {
            if (this.htmlDocument.selectFirst(selector) != null) return;
            log.debug("Tested for element {} -- not found.", selector);
            throw new ComponentParseFailed();
        }
        protected @Nonnull org.jsoup.nodes.Element getElementEnsureNotNull(@Nonnull String selector) throws ComponentParseFailed {
            var elm = this.htmlDocument.selectFirst(selector);
            if (elm == null) { log.warn("Expected element not found in response: {}", selector); throw new ComponentParseFailed(); }
            return elm;
        }
        protected @Nonnull String getAttributeEnsureNotNull(@Nonnull String selector, @Nonnull String attribute) throws ComponentParseFailed {
            var elm = getElementEnsureNotNull(selector);
            if (!elm.hasAttr(attribute)) { log.warn("Element {} is missing expected attribute '{}'.", selector, attribute); throw new ComponentParseFailed(); }
            return ISNOTNULL(elm.attr(attribute));
        }
        protected @Nonnull URI getURIAttributeEnsureNotNull(@Nonnull String selector, @Nonnull String attribute) throws ComponentParseFailed {
            String value = getAttributeEnsureNotNull(selector, attribute);
            try {
                return new URI(value);
            } catch (URISyntaxException e) {
                if (attribute.startsWith("abs:"))
                    attribute = ISNOTNULL(attribute.substring(4));
                log.warn("Element {} attribute {} is '{}', could not be parsed as URI", selector, attribute, getAttributeEnsureNotNull(selector, attribute));
                throw new ComponentParseFailed();
            }
        }
        protected @Nonnull URI getLongPollURI() throws ComponentParseFailed {
            var pollingScriptElm = getElementEnsureNotNull("#jsLongPoll script");
            String pollingScript = pollingScriptElm.data();
            int startIdx = pollingScript.indexOf("qrpoll(\"");
            if (startIdx < 0) { log.warn("Failed to find 'qrpoll(\"' in jsLongPoll script:\n{}", pollingScript); throw new ComponentParseFailed(); }
            startIdx += 8;

            int endIdx = pollingScript.indexOf("\");", startIdx);
            if (endIdx < 0) { log.warn("Failed to find qrpoll terminator '\");' in jsLongPoll script:\n{}", pollingScript); throw new ComponentParseFailed(); }

            String pollingUriString = pollingScript.substring(startIdx, endIdx);
            try {
                return ISNOTNULL(new URI(pollingScriptElm.baseUri()).resolve(pollingUriString));
            } catch (URISyntaxException e) {
                log.warn("Long-poll URI '{}' could not be parsed", pollingUriString);
                throw new ComponentParseFailed();
            }
        }
    }

    public static class InterstitialBlock extends TopLevelFormBlock {
        public final @Nonnull String submitButton;
        public final @Nonnull String interstitialMessage;

        private InterstitialBlock(@Nonnull org.jsoup.nodes.Document htmlDocument, @Nonnull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            if (htmlDocument.baseUri().contains("/ExpiresInfo.aspx")) {
                this.interstitialMessage = ISNOTNULL(getElementEnsureNotNull("#Label2").ownText());
                this.submitButton = "#Button_Next";
            } else { throw new ComponentParseFailed(); }
        }
    }

    public static class ErrorBlock extends TopLevelFormBlock {
        public final boolean isRecoverable;
        public final @Nonnull String errorText;

        private ErrorBlock(@Nonnull org.jsoup.nodes.Document htmlDocument, @Nonnull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            if (!htmlDocument.baseUri().contains("/error.aspx"))
                throw new ComponentParseFailed();
            
            this.isRecoverable = (htmlDocument.selectFirst("#Button_Back") != null);

            String errorText = getElementEnsureNotNull("#Label1").ownText();
            if (errorText.startsWith("Fehler:"))
                errorText = errorText.substring(7);
            this.errorText = ISNOTNULL(errorText.trim());
        }
    }

    public static class UsernamePasswordBlock extends TopLevelFormBlock {
        private final @Nonnull String usernameKey;
        private final @Nonnull String passwordKey;
        public final @CheckForNull String errorMessage;

        public void setUsernamePassword(String username, String password) {
            formOptions.put(usernameKey, username); formOptions.put(passwordKey, password);
        }

        private UsernamePasswordBlock(@Nonnull org.jsoup.nodes.Document htmlDocument, @Nonnull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            abortIfElementMissing("#handynummer");
            this.usernameKey = getAttributeEnsureNotNull("#handynummer", "name");
            this.passwordKey = getAttributeEnsureNotNull("#signaturpasswort", "name");
            this.errorMessage = null;
        }
    }

    public static class SMSTanBlock extends TopLevelFormBlock {
        private final @Nonnull String tanKey;
        public final @Nonnull String referenceValue;
        public final @CheckForNull String errorMessage;

        public void setTAN(String tan) {
            formOptions.put(tanKey, tan);
        }

        private SMSTanBlock(@Nonnull org.jsoup.nodes.Document htmlDocument, @Nonnull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            abortIfElementMissing("#div_tan");
            this.tanKey = getAttributeEnsureNotNull("#input_tan", "name");
            this.referenceValue = ISNOTNULL(getElementEnsureNotNull("#vergleichswert").ownText());
            this.errorMessage = null;
        }
    }

    public static class QRCodeBlock extends TopLevelFormBlock {
        public final @Nonnull String referenceValue;
        public final @Nonnull URI qrCodeURI;
        public final @Nonnull URI pollingURI;
        public final @Nullable String errorMessage;

        private QRCodeBlock(@Nonnull org.jsoup.nodes.Document htmlDocument, @Nonnull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            abortIfElementMissing("#qrimage");
            
            this.referenceValue = ISNOTNULL(getElementEnsureNotNull("#vergleichswert").ownText());
            this.qrCodeURI = getURIAttributeEnsureNotNull("#qrimage", "abs:src");
            this.pollingURI = getLongPollURI();

            this.errorMessage = null;
        }
    }

    public static class WaitingForAppBlock extends TopLevelFormBlock {
        public final @Nonnull String referenceValue;
        public final @Nonnull URI pollingURI;

        private WaitingForAppBlock(@Nonnull org.jsoup.nodes.Document htmlDocument, @Nonnull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            abortIfElementMissing("#smartphoneAnimation");

            this.referenceValue = ISNOTNULL(getElementEnsureNotNull("#vergleichswert").ownText());
            this.pollingURI = getLongPollURI();            
        }
    }

    public static class WaitingForBiometryBlock extends TopLevelFormBlock {
        public final @Nonnull String referenceValue;
        public final @Nonnull URI pollingURI;

        private WaitingForBiometryBlock(@Nonnull org.jsoup.nodes.Document htmlDocument, @Nonnull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            abortIfElementMissing("#biometricimage");

            this.referenceValue = ISNOTNULL(getElementEnsureNotNull("#vergleichswert").ownText());
            this.pollingURI = getLongPollURI();
        }
    }

    public static class Fido2Block extends TopLevelFormBlock {
        public final @Nonnull String fidoOptions;
        private final @Nonnull String credentialResultKey;

        public void setFIDOResult(String result) { formOptions.put(credentialResultKey, result); }

        private Fido2Block(@Nonnull org.jsoup.nodes.Document htmlDocument, @Nonnull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            abortIfElementMissing("#fidoBlock");
            this.fidoOptions = getAttributeEnsureNotNull("#credentialOptions", "value");
            this.credentialResultKey = getAttributeEnsureNotNull("#credentialResult", "name");
        }
    }

    public static class HTMLResult {
        public final @Nonnull org.jsoup.nodes.Document htmlDocument;
        public final @Nonnull URI formTarget;
        public final @Nonnull Map<String, String> formOptions = new HashMap<>();

        public @Nonnull Iterable<Map.Entry<String, String>> iterateFormOptions() { return ISNOTNULL(formOptions.entrySet()); }

        /* optional links (any number may or may not be null) */
        public final @CheckForNull URI signatureDataLink;
        public final @CheckForNull URI smsTanLink;
        public final @CheckForNull URI fido2Link;

        /* top-level blocks (exactly one is not null) */
        public final @CheckForNull InterstitialBlock interstitialBlock;
        public final @CheckForNull ErrorBlock errorBlock;
        public final @CheckForNull UsernamePasswordBlock usernamePasswordBlock;
        public final @CheckForNull SMSTanBlock smsTanBlock;
        public final @CheckForNull QRCodeBlock qrCodeBlock;
        public final @CheckForNull WaitingForAppBlock waitingForAppBlock;
        public final @CheckForNull WaitingForBiometryBlock waitingForBiometryBlock;
        public final @CheckForNull Fido2Block fido2Block;

        private void validate() {
            Set<String> populated = new HashSet<>();

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

        private @Nullable URI getHrefIfExists(String selector) {
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
        private <T extends TopLevelFormBlock> @Nullable T TryParseMainBlock(Class<T> clazz) {
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

        private HTMLResult(@Nonnull org.jsoup.nodes.Document htmlDocument) {
            log.trace("Now parsing:\n{}", htmlDocument.toString());
            this.htmlDocument = htmlDocument;

            var forms = htmlDocument.getElementsByTag("form");
            if (forms.size() != 1) {
                log.error("Found {} forms in A-Trust response document, expected 1. Document:\n{}", forms.size(), htmlDocument.toString());
                throw new IllegalArgumentException("Failed to parse A-Trust response page");
            }

            var mainForm = ISNOTNULL(forms.first()); /* size check above */
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
        public final @CheckForNull String slResponse;
        public final @CheckForNull HTMLResult html;

        private Result(@Nonnull String slResponse) { this.slResponse = slResponse; this.html = null; }
        private Result(@Nonnull org.jsoup.nodes.Document htmlDocument) { this.slResponse = null; this.html = new HTMLResult(htmlDocument); }
    }

    public static @Nonnull Result Parse(@Nonnull org.jsoup.nodes.Document htmlDocument) { return new Result(htmlDocument); }

    public static @Nonnull Result Parse(URI baseURI, String contentType, @Nonnull String content) {
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
