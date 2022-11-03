package at.asit.pdfover.gui.composites.configuration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.utils.SWTUtils;
import at.asit.pdfover.gui.workflow.config.ConfigurationDataInMemory;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.webauthn.WebAuthN;

public class Fido2ConfigurationComposite extends ConfigurationCompositeBase {
    private static final Logger log = LoggerFactory.getLogger(Fido2ConfigurationComposite.class);

    private Link lnkInfoText;
    private Link unsupportedText;

    private Group grpFidoSettings;
    private Button btnFido2ByDefault;

    public Fido2ConfigurationComposite(Composite parent, int style, State state, ConfigurationDataInMemory container) {
        super(parent, style, state, container);
        setLayout(new FormLayout());

        this.lnkInfoText = new Link(this, SWT.WRAP);
        SWTUtils.anchor(lnkInfoText).top(0,5).left(0,5).right(100,-5);
        SWTUtils.setFontHeight(lnkInfoText, Constants.TEXT_SIZE_NORMAL);
        SWTUtils.addSelectionListener(lnkInfoText, () -> { SWTUtils.openURL(Messages.getString("config.fido2.InfoURL")); });

        this.unsupportedText = new Link(this, SWT.WRAP);
        SWTUtils.anchor(unsupportedText).top(lnkInfoText,5).left(0,5).right(100,-5);
        SWTUtils.setFontHeight(unsupportedText, Constants.TEXT_SIZE_NORMAL);
        SWTUtils.addSelectionListener(unsupportedText, () -> { SWTUtils.openURL("https://developers.yubico.com/libfido2/#_installation"); });

        FormLayout STANDARD_LAYOUT = new FormLayout();
		STANDARD_LAYOUT.marginHeight = 10;
		STANDARD_LAYOUT.marginWidth = 5;

        this.grpFidoSettings = new Group(this, SWT.NONE);
        this.grpFidoSettings.setLayout(STANDARD_LAYOUT);
        SWTUtils.anchor(grpFidoSettings).top(lnkInfoText,10).left(0,5).right(100,-5);
        SWTUtils.setFontHeight(grpFidoSettings, Constants.TEXT_SIZE_NORMAL);

        this.btnFido2ByDefault = new Button(this.grpFidoSettings, SWT.CHECK);
        SWTUtils.anchor(btnFido2ByDefault).top(0).left(0,5).right(100,-5);
        SWTUtils.setFontHeight(btnFido2ByDefault, Constants.TEXT_SIZE_BUTTON);
        SWTUtils.addSelectionListener(btnFido2ByDefault, () -> { this.configurationContainer.fido2ByDefault = btnFido2ByDefault.getSelection(); });

        if (WebAuthN.isAvailable())
            this.unsupportedText.setVisible(false);
        else
            this.grpFidoSettings.setVisible(false);

        reloadResources();
    }

    @Override
	public void reloadResources() {
        SWTUtils.setLocalizedText(lnkInfoText, "config.fido2.AboutFIDO");
        SWTUtils.setLocalizedText(grpFidoSettings, "config.fido2.Group");
        SWTUtils.setLocalizedText(btnFido2ByDefault, "config.fido2.ByDefault");
        
        if (Platform.isLinux())
            SWTUtils.setLocalizedText(unsupportedText, "config.fido2.UnsupportedTryLibFido2");
        else {
            SWTUtils.setFontStyle(unsupportedText, SWT.BOLD);
            SWTUtils.setLocalizedText(unsupportedText, "config.fido2.UnsupportedPlatform");
        }
    }

    @Override
	public void initConfiguration(ConfigurationManager provider) {
        this.configurationContainer.fido2ByDefault = provider.getFido2ByDefault();
    }

    @Override
	public void loadConfiguration() {
        btnFido2ByDefault.setSelection(this.configurationContainer.fido2ByDefault);
    }

    @Override
	public void storeConfiguration(ConfigurationManager store) {
        store.setFido2ByDefaultPersistent(this.configurationContainer.fido2ByDefault);
    }

    @Override
	public void validateSettings(int resumeIndex) throws Exception {}
}
