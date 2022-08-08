package at.asit.pdfover.signer.pdfas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.function.Consumer;

import at.asit.pdfover.commons.utils.ImageUtil;

/**
 * caches placeholders for signature parameters (placeholder generation is pretty slow)
 */
public final class PdfAs4SignaturePlaceholder implements Runnable {
    private static TreeMap<PdfAs4SignatureParameter, PdfAs4SignaturePlaceholder> cache = new TreeMap<>(
        Comparator
          .comparing(PdfAs4SignatureParameter::getPdfAsSignatureProfileId)
          .thenComparing((p) -> { return (p.emblem != null) ? p.emblem.getOriginalFileHash() : ""; })
          .thenComparing((p) -> { return p.signatureNote; }, Comparator.nullsFirst(String::compareTo))
    );

    /**
     * request a placeholder for the specified parameter asynchronously
     * @param callback the callback to be invoked on completion (may also be invoked before this function returns!)
     */
    public static void For(PdfAs4SignatureParameter param, Consumer<PdfAs4SignaturePlaceholder> callback) {
        synchronized(cache) {
            cache.computeIfAbsent(param, (p) -> new PdfAs4SignaturePlaceholder(p)).AddCallback(callback);
        }
    }

    private final PdfAs4SignatureParameter param;
    private PdfAs4SignaturePlaceholder(PdfAs4SignatureParameter param) {
        this.param = param;
        new Thread(this).start();
    }

    private java.awt.image.BufferedImage awtImageData;
    /** AWT image data for the placeholder */
    public java.awt.image.BufferedImage getAWTImage() { return this.awtImageData; }
    private org.eclipse.swt.graphics.ImageData swtImageData;
    /** SWT image data for the placeholder */
    public org.eclipse.swt.graphics.ImageData getSWTImage() { return this.swtImageData; }

    private ArrayList<Consumer<PdfAs4SignaturePlaceholder>> callbacks = new ArrayList<>();
    private void AddCallback(Consumer<PdfAs4SignaturePlaceholder> c) {
        synchronized (this) {
            if (this.callbacks != null)
            {
                this.callbacks.add(c);
                return;
            } /* else... */
        }
        /* ... else, not synchronized */
        c.accept(this);
    }


    @Override
    public void run() {
        this.awtImageData = (java.awt.image.BufferedImage) this.param.getPlaceholder();
        this.swtImageData = ImageUtil.convertToSWT(this.awtImageData);
        ArrayList<Consumer<PdfAs4SignaturePlaceholder>> _callbacks;
        synchronized (this) {
            _callbacks = this.callbacks;
            this.callbacks = null;
        }
        _callbacks.forEach((c) -> c.accept(this));
    }
    
}
