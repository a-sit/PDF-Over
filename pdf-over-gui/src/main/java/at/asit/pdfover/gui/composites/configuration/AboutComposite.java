package at.asit.pdfover.gui.composites.configuration;

import java.awt.Desktop;
import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.utils.SWTUtils;
import at.asit.pdfover.gui.utils.UpdateCheckManager;
import at.asit.pdfover.gui.workflow.config.ConfigurationDataInMemory;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;
import at.asit.pdfover.gui.workflow.states.State;

public class AboutComposite extends ConfigurationCompositeBase {
	static final Logger log = LoggerFactory.getLogger(AboutComposite.class);
	private Link lnkAbout;
	private Link lnkDataProtection;
	private Label lblDataProtection;
	private Link lnkUpdateCheckStatus;
	private Button btnUpdateCheck;
	private Button btnOpenLogDirectory;
	private UpdateCheckManager.Status latestUpdateStatus;
	/**
 * @param parent
 * @param style
	 */
	public AboutComposite(Composite parent, int style, State state, ConfigurationDataInMemory container) {
		super(parent, style, state, container);

		setLayout(new FormLayout());

		this.lnkAbout = new Link(this, SWT.WRAP);
		SWTUtils.anchor(lnkAbout).top(0,5).right(100,-5).left(0,5);
		SWTUtils.setFontHeight(lnkAbout, Constants.TEXT_SIZE_NORMAL);
		SWTUtils.addSelectionListener(lnkAbout, (e) -> { SWTUtils.openURL(Messages.getString("config.LicenseURL")); });

		this.lblDataProtection = new Label(this, SWT.WRAP);
		SWTUtils.anchor(lblDataProtection).top(lnkAbout, 15).left(0,5).right(100,-5);
		SWTUtils.setFontHeight(lblDataProtection, Constants.TEXT_SIZE_BIG);
		SWTUtils.setFontStyle(lblDataProtection, SWT.BOLD);

		this.lnkDataProtection = new Link(this, SWT.WRAP);
		SWTUtils.anchor(lnkDataProtection).top(lblDataProtection,10).left(0,5).right(100,-5);
		SWTUtils.setFontHeight(lnkDataProtection, Constants.TEXT_SIZE_NORMAL);
		SWTUtils.addSelectionListener(lnkDataProtection, (e) -> { SWTUtils.openURL(Messages.getString("config.DataProtectionURL")); });

		this.lnkUpdateCheckStatus = new Link(this, SWT.NONE);
		SWTUtils.anchor(lnkUpdateCheckStatus).bottom(100, -5).left(0,5);
		SWTUtils.setFontHeight(lnkUpdateCheckStatus, Constants.TEXT_SIZE_BUTTON);
		UpdateCheckManager.registerStatusCallback((s) -> { latestUpdateStatus = s; getDisplay().asyncExec(() -> { RefreshUpdateStatusText(); }); });
		SWTUtils.addSelectionListener(lnkUpdateCheckStatus, (e) -> {
			if (latestUpdateStatus == UpdateCheckManager.Status.OUTDATED)
				Program.launch(Constants.UPDATE_URL);
			else
				UpdateCheckManager.checkNow(getShell());
		});

		this.btnUpdateCheck = new Button(this, SWT.CHECK);
		SWTUtils.anchor(btnUpdateCheck).bottom(lnkUpdateCheckStatus,-5).left(0,5);
		SWTUtils.setFontHeight(btnUpdateCheck, Constants.TEXT_SIZE_BUTTON);
		SWTUtils.addSelectionListener(btnUpdateCheck, (e) -> {
			boolean enabled = btnUpdateCheck.getSelection();
			this.configurationContainer.updateCheck = enabled;
			if (enabled)
				UpdateCheckManager.checkNow(getShell());
		});

		this.btnOpenLogDirectory = new Button(this, SWT.NONE);
		SWTUtils.anchor(btnOpenLogDirectory).bottom(100, -5).right(100, -5);
		SWTUtils.setFontHeight(btnOpenLogDirectory, Constants.TEXT_SIZE_BUTTON);

		SWTUtils.reanchor(lnkDataProtection).bottom(btnUpdateCheck,-5);

		SWTUtils.addSelectionListener(btnOpenLogDirectory, (e) -> {
			try
			{
				if (Desktop.isDesktopSupported())
					Desktop.getDesktop().open(new File(Constants.CONFIG_DIRECTORY + File.separator + "logs"));
			} catch (Exception ex) {
				log.warn("Failed to open log directory: ", ex);
			}
		});

		// Load localized strings
		reloadResources();
	}

	private void RefreshUpdateStatusText() {
		SWTUtils.setLocalizedText(lnkUpdateCheckStatus, "config.UpdateStatus." + latestUpdateStatus.name());
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		SWTUtils.setLocalizedText(lnkAbout, "config.AboutText");
		SWTUtils.setLocalizedText(lblDataProtection, "config.DataProtection");
		SWTUtils.setLocalizedText(lnkDataProtection, "config.DataProtectionStatement");
		SWTUtils.setLocalizedText(btnUpdateCheck, "advanced_config.UpdateCheck");
		SWTUtils.setLocalizedToolTipText(btnUpdateCheck, "advanced_config.UpdateCheck_ToolTip");
		SWTUtils.setLocalizedText(btnOpenLogDirectory, "config.ShowLogDirectory");
		RefreshUpdateStatusText();
	}

	@Override
	public void initConfiguration(ConfigurationManager provider) {
		this.configurationContainer.updateCheck = provider.getUpdateCheck();
	}

	@Override
	public void loadConfiguration() {
		btnUpdateCheck.setSelection(this.configurationContainer.updateCheck);
	}

	@Override
	public void storeConfiguration(ConfigurationManager store) {
		store.setUpdateCheckPersistent(this.configurationContainer.updateCheck);
	}

	@Override
	public void validateSettings(int resumeFrom) throws Exception {}
}