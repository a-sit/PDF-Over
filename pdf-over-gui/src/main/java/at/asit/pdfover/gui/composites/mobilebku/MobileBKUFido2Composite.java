package at.asit.pdfover.gui.composites.mobilebku;

import java.net.URI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.composites.StateComposite;
import at.asit.pdfover.gui.utils.SWTUtils;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.webauthnclient.PublicKeyCredential;
import at.asit.webauthnclient.WebAuthN;
import at.asit.webauthnclient.exceptions.WebAuthNUserCancelled;
import at.asit.webauthnclient.responsefields.AuthenticatorAssertionResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MobileBKUFido2Composite extends StateComposite {    
    private @NonNull String fido2OptionsString = "";

    private PublicKeyCredential<AuthenticatorAssertionResponse> credential;
    private boolean userCancel;
    private boolean userSms;

    private Button btn_authenticate;
    private Button btn_cancel;
    private Button btn_sms;
    public void setSMSEnabled(boolean state) { this.btn_sms.setEnabled(state); }

    private URI signatureDataURI;
    private Link lnk_sigData;
    public void setSignatureDataURI(URI uri) { this.signatureDataURI = uri; this.lnk_sigData.setVisible(uri != null); }

    public void initialize(@NonNull String fido2Options) {
        this.fido2OptionsString = fido2Options;
        this.credential = null;
        this.userCancel = this.userSms = false;
    }

    public boolean isDone() { return ((this.credential != null) || this.userCancel || this.userSms); }
    public PublicKeyCredential<AuthenticatorAssertionResponse> getResultingCredential() { return this.credential; }
    public boolean wasUserCancelClicked() { return userCancel; }
    public boolean wasUserSMSClicked() { return userSms; }

    public void beginAuthentication() {
        if (!btn_authenticate.isEnabled()) return;

        SWTUtils.setLocalizedText(btn_authenticate, "common.working");
        btn_authenticate.setEnabled(false);
        new Thread(() -> {
            try {
                this.credential = WebAuthN.buildGetFromJSON(this.fido2OptionsString).get("https://service.a-trust.at");
            } catch (Throwable t) {
                if (!(t instanceof WebAuthNUserCancelled)) {
                    log.warn("webauthn operation failed", t);
                }
            } finally {
                this.getDisplay().syncExec(() -> { btn_authenticate.setEnabled(true); this.reloadResources(); });
                this.getDisplay().wake();
            }
        }).start();
    }    

    public MobileBKUFido2Composite(Composite parent, int style, State state) {
        super(parent, style, state);
        setLayout(new FormLayout());

        final Composite containerComposite = new Composite(this, SWT.NATIVE);
        containerComposite.addPaintListener((e) -> {
            Rectangle clientArea = containerComposite.getClientArea();
            e.gc.setForeground(Constants.MAINBAR_ACTIVE_BACK_DARK);
            e.gc.setLineWidth(3);
            e.gc.setLineStyle(SWT.LINE_SOLID);
            e.gc.drawRoundRectangle(clientArea.x, clientArea.y+25,
                    clientArea.width - 2, clientArea.height - 27, 10, 10);
        
		});
        containerComposite.setLayout(new FormLayout());
        SWTUtils.anchor(containerComposite).top(50, -145).bottom(50, 120).left(50, -200).right(50, 200);

        ImageData webauthnLogoImg = new ImageData(this.getClass().getResourceAsStream(Constants.RES_IMG_WEBAUTHN));
        Label webauthnLogo = new Label(containerComposite, SWT.NATIVE);
        SWTUtils.anchor(webauthnLogo).top(0,0).left(0, 10).height(50).width(187);
        webauthnLogo.setBackground(getBackground());
        webauthnLogo.setImage(new Image(getDisplay(), webauthnLogoImg.scaledTo(187, 50)));

        ImageData fidoLogoImg = new ImageData(this.getClass().getResourceAsStream(Constants.RES_IMG_FIDO2));
        Label fidoLogo = new Label(containerComposite, SWT.NATIVE);
        SWTUtils.anchor(fidoLogo).left(0, 10).bottom(100, -10).height(50).width(81);
        fidoLogo.setImage(new Image(getDisplay(), fidoLogoImg.scaledTo(81, 50)));

        this.btn_authenticate = new Button(containerComposite, SWT.NATIVE);
        SWTUtils.anchor(btn_authenticate).top(50, -15).left(0, 90).right(100, -90);
        SWTUtils.addSelectionListener(btn_authenticate, this::beginAuthentication);
        btn_authenticate.setEnabled(WebAuthN.isAvailable());

        this.btn_cancel = new Button(containerComposite, SWT.NATIVE);
        SWTUtils.anchor(btn_cancel).bottom(100, -10).right(100, -10);
        SWTUtils.addSelectionListener(btn_cancel, () -> { this.userCancel = true; });

        this.btn_sms = new Button(containerComposite, SWT.NATIVE);
        SWTUtils.anchor(btn_sms).bottom(100, -10).right(btn_cancel, -10);
        SWTUtils.addSelectionListener(btn_sms, () -> { this.userSms = true; });

        this.lnk_sigData = new Link(containerComposite, SWT.NATIVE | SWT.RESIZE);
		SWTUtils.anchor(lnk_sigData).top(0, 45).right(100, -20);
		SWTUtils.addSelectionListener(lnk_sigData, (e) -> { SWTUtils.openURL(this.signatureDataURI); });
    }

    @Override public void onDisplay() { getShell().setDefaultButton(this.btn_authenticate); beginAuthentication(); }

    @Override
    public void reloadResources() {
        SWTUtils.setLocalizedText(btn_authenticate, WebAuthN.isAvailable() ? "mobileBKU.authorize" : "common.unavailable");
        SWTUtils.setLocalizedText(btn_cancel, "common.Cancel");
        SWTUtils.setLocalizedText(btn_sms, "tanEnter.SMS");
        SWTUtils.setLocalizedText(lnk_sigData, "mobileBKU.show");
        SWTUtils.setLocalizedToolTipText(lnk_sigData, "mobileBKU.show_tooltip");
    }
}
