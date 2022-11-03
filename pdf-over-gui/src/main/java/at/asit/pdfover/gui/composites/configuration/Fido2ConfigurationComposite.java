package at.asit.pdfover.gui.composites.configuration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.utils.SWTUtils;
import at.asit.pdfover.gui.workflow.config.ConfigurationDataInMemory;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;
import at.asit.pdfover.gui.workflow.states.State;

public class Fido2ConfigurationComposite extends ConfigurationCompositeBase {
    private static final Logger log = LoggerFactory.getLogger(Fido2ConfigurationComposite.class);

    private Link lnkInfoText;
    // TODO information about unsupported/installing deps?

    private Group grpFidoSettings;
    private Button btnFido2ByDefault;

    public Fido2ConfigurationComposite(Composite parent, int style, State state, ConfigurationDataInMemory container) {
        super(parent, style, state, container);
        setLayout(new FormLayout());

        this.lnkInfoText = new Link(this, SWT.WRAP);
        SWTUtils.anchor(lnkInfoText).top(0,5).left(0,5).right(100,-5);
        SWTUtils.setFontHeight(lnkInfoText, Constants.TEXT_SIZE_NORMAL);
        SWTUtils.addSelectionListener(lnkInfoText, () -> { SWTUtils.openURL(Messages.getString("config.fido2.InfoURL")); });

        FormLayout STANDARD_LAYOUT = new FormLayout();
		STANDARD_LAYOUT.marginHeight = 10;
		STANDARD_LAYOUT.marginWidth = 5;

        this.grpFidoSettings = new Group(this, SWT.NONE);
        this.grpFidoSettings.setLayout(STANDARD_LAYOUT);
        SWTUtils.anchor(grpFidoSettings).top(lnkInfoText,5).left(0,5).right(100,-5);
        SWTUtils.setFontHeight(grpFidoSettings, Constants.TEXT_SIZE_NORMAL);

        this.btnFido2ByDefault = new Button(this.grpFidoSettings, SWT.CHECK);
        SWTUtils.anchor(btnFido2ByDefault).top(0).left(0,5).right(100,-5);
        SWTUtils.setFontHeight(btnFido2ByDefault, Constants.TEXT_SIZE_BUTTON);
        SWTUtils.addSelectionListener(btnFido2ByDefault, () -> { this.configurationContainer.fido2ByDefault = btnFido2ByDefault.getSelection(); });

        reloadResources();
    }

    @Override
	public void reloadResources() {
        SWTUtils.setLocalizedText(lnkInfoText, "config.fido2.AboutFIDO");
        SWTUtils.setLocalizedText(grpFidoSettings, "config.fido2.Group");
        SWTUtils.setLocalizedText(btnFido2ByDefault, "config.fido2.ByDefault");
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
