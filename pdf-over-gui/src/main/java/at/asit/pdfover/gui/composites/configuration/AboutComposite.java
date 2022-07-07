package at.asit.pdfover.gui.composites.configuration;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import at.asit.pdfover.gui.composites.StateComposite;
import at.asit.pdfover.gui.workflow.config.ConfigProviderImpl;

public class AboutComposite extends ConfigurationCompositeBase {
	static final Logger log = LoggerFactory.getLogger(AboutComposite.class);
	private Link lnkAbout;
	private Link lnkDataProtection;
	private Label lblDataProtection;
	private Button btnOpenLogDirectory;
	/**
 * @param parent
 * @param style
	 */
	public AboutComposite(Composite parent, int style) {
		super(parent, style, null, null);

		setLayout(new FormLayout());

		this.lnkAbout = new Link(this, SWT.WRAP);
		StateComposite.anchor(lnkAbout).top(0,5).right(100,-5).left(0,5).set();
		StateComposite.setFontHeight(lnkAbout, Constants.TEXT_SIZE_NORMAL);

		this.lblDataProtection = new Label(this, SWT.WRAP);
		StateComposite.anchor(lblDataProtection).top(lnkAbout, 15).left(0,5).right(100,-5).set();
		StateComposite.setFontHeight(lblDataProtection, Constants.TEXT_SIZE_BIG);
		StateComposite.setFontStyle(lblDataProtection, SWT.BOLD);

		this.lnkDataProtection = new Link(this, SWT.WRAP);
		StateComposite.anchor(lnkDataProtection).top(lblDataProtection,10).left(0,5).right(100,-5).set();
		StateComposite.setFontHeight(lnkDataProtection, Constants.TEXT_SIZE_NORMAL);

		this.btnOpenLogDirectory = new Button(this, SWT.NONE);
		StateComposite.anchor(btnOpenLogDirectory).bottom(100, -5).right(100, -5).set();
		StateComposite.setFontHeight(btnOpenLogDirectory, Constants.TEXT_SIZE_BUTTON);

		this.lnkAbout.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					URI url = new URI(Messages.getString("config.LicenseURL"));
					log.debug("Trying to open " + url.toString());
					if (Desktop.isDesktopSupported()) {
						Desktop.getDesktop().browse(url);
					} else {
						log.info("AWT Desktop is not supported on this platform");
						Program.launch(url.toString());
					}
				} catch (IOException ex) {
					log.error("AboutComposite: ", ex);
				} catch (URISyntaxException ex) {
					log.error("AboutComposite: ", ex);
				}
			}
		});

		this.lnkDataProtection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					URI url = new URI(Messages.getString("config.DataProtectionURL"));
					log.debug("Trying to open " + url.toString());
					if (Desktop.isDesktopSupported()) {
						Desktop.getDesktop().browse(url);
					} else {
						log.info("AWT Desktop is not supported on this platform");
						Program.launch(url.toString());
					}
				} catch (IOException ex) {
					log.error("AboutComposite: ", ex);
				} catch (URISyntaxException ex) {
					log.error("AboutComposite: ", ex);
				}
			}
		});

		this.btnOpenLogDirectory.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try
				{
					if (Desktop.isDesktopSupported())
						Desktop.getDesktop().open(new File(Constants.CONFIG_DIRECTORY + File.separator + "logs"));
				} catch (Exception ex) {
					log.warn("Failed to open log directory: ", ex);
				}
			}
		});

		// Load localized strings
		reloadResources();
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		// Nothing to do here
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		this.lnkAbout.setText(Messages.getString("config.AboutText"));
		this.lblDataProtection.setText(Messages.getString("config.DataProtection"));
		this.lnkDataProtection.setText(Messages.getString("config.DataProtectionStatement"));
		this.btnOpenLogDirectory.setText(Messages.getString("config.ShowLogDirectory"));
	}


	@Override
	protected void signerChanged() {}

	@Override
	public void initConfiguration(ConfigProviderImpl provider) {}

	@Override
	public void loadConfiguration() {}

	@Override
	public void storeConfiguration(ConfigProviderImpl store) {}

	@Override
	public void validateSettings(int resumeFrom) throws Exception {}
}