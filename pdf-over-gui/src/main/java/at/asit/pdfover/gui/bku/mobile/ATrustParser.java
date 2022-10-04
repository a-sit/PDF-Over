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
        protected @Nonnull org.jsoup.nodes.Document htmlDocument;
        protected @Nonnull Map<String, String> formOptions;
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
    }

    public static class UsernamePasswordBlock extends TopLevelFormBlock {
        private @Nonnull String usernameKey;
        private @Nonnull String passwordKey;
        public @CheckForNull String errorMessage;

        public void setUsernamePassword(String username, String password) {
            formOptions.put(usernameKey, username); formOptions.put(passwordKey, password);
        }

        private UsernamePasswordBlock(@Nonnull org.jsoup.nodes.Document htmlDocument, @Nonnull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            abortIfElementMissing("#handynummer");
            this.usernameKey = getAttributeEnsureNotNull("#handynummer", "name");
            this.passwordKey = getAttributeEnsureNotNull("#signaturpasswort", "name");
        }
    }

    public static class QRCodeBlock extends TopLevelFormBlock {
        public @Nonnull String referenceValue;
        public @Nonnull URI qrCodeURI;
        public @Nonnull URI pollingURI;
        public @Nullable String errorMessage;

        private QRCodeBlock(@Nonnull org.jsoup.nodes.Document htmlDocument, @Nonnull Map<String, String> formOptions) throws ComponentParseFailed {
            super(htmlDocument, formOptions);
            abortIfElementMissing("#qrimage");
            
            this.referenceValue = ISNOTNULL(getElementEnsureNotNull("#vergleichswert").ownText());
            this.qrCodeURI = getURIAttributeEnsureNotNull("#qrimage", "abs:src");

            var pollingScriptElm = getElementEnsureNotNull("#jsLongPoll script");
            String pollingScript = pollingScriptElm.data();
            int startIdx = pollingScript.indexOf("qrpoll(\"");
            if (startIdx < 0) { log.warn("Failed to find 'qrpoll(\"' in jsLongPoll script:\n{}", pollingScript); throw new ComponentParseFailed(); }
            startIdx += 8;

            int endIdx = pollingScript.indexOf("\");", startIdx);
            if (endIdx < 0) { log.warn("Failed to find qrpoll terminator '\");' in jsLongPoll script:\n{}", pollingScript); throw new ComponentParseFailed(); }

            String pollingUriString = pollingScript.substring(startIdx, endIdx);
            try {
                this.pollingURI = ISNOTNULL(new URI(pollingScriptElm.baseUri()).resolve(pollingUriString));
            } catch (URISyntaxException e) {
                log.warn("URI '{}' could not be parsed", pollingUriString);
                throw new ComponentParseFailed();
            }
        }
    }

    public static class Fido2Block extends TopLevelFormBlock {
        private @Nonnull String fidoOptions;
        private @Nonnull String credentialResultKey;

        public @Nonnull String getFIDOOptions() { return fidoOptions; }
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

        public static class NameValuePair {
            public final @Nonnull String name;
            public final @Nonnull String value;
            public NameValuePair(@Nonnull String n, @Nonnull String v) { name = n; value = v; }
        }
        /**
         * map: id -> (name, value)
         */
        public final @Nonnull Map<String, @CheckForNull NameValuePair> submitButtons = new HashMap<>();

        public @Nonnull Iterable<Map.Entry<String, String>> iterateFormOptions() { return ISNOTNULL(formOptions.entrySet()); }

        /* optional mode switch links (any number may or may not be null) */
        public final @CheckForNull URI fido2Link;

        /* top-level blocks (exactly one is not null) */
        public final @CheckForNull UsernamePasswordBlock usernamePasswordBlock;
        public final @CheckForNull QRCodeBlock qrCodeBlock;
        public final @CheckForNull Fido2Block fido2Block;

        private void validate() {
            Set<String> populated = new HashSet<>();

            if (usernamePasswordBlock != null) populated.add("usernamePasswordBlock");
            if (qrCodeBlock != null) populated.add("qrCodeBlock");
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

                /* special handling for submit inputs, they only get sent if they are "clicked" */
                if ("submit".equals(input.attr("type"))) {
                    if (name.isEmpty())
                        this.submitButtons.put(input.attr("id"), null);
                    else
                        this.submitButtons.put(input.attr("id"), new NameValuePair(name, ISNOTNULL(input.attr("value"))));
                } else {
                    if (!name.isEmpty())
                        this.formOptions.put(name, input.attr("value"));
                }
            }

            this.fido2Link = getHrefIfExists("#FidoButton");

            this.usernamePasswordBlock = TryParseMainBlock(UsernamePasswordBlock.class);
            this.qrCodeBlock = TryParseMainBlock(QRCodeBlock.class);
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
