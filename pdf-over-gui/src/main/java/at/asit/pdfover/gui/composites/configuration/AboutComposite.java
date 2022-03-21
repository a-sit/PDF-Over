package at.asit.pdfover.gui.composites.configuration;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.config.ConfigManipulator;
import at.asit.pdfover.gui.workflow.config.PersistentConfigProvider;

public class AboutComposite extends ConfigurationCompositeBase {
	static final Logger log = LoggerFactory.getLogger(AboutComposite.class);
	private Link lnkAbout;
	private Link lnkDataProtection;
	private Label lblDataProtection;
	/**
 * @param parent
 * @param style
	 */
	public AboutComposite(Composite parent, int style) {
		super(parent, style, null, null);

		setLayout(new FormLayout());

		this.lnkAbout = new Link(this, SWT.NONE);
		ConfigurationCompositeBase.anchor(lnkAbout).right(100,-5).left(0,5).top(0,5).width(100).set();
		ConfigurationCompositeBase.setFontHeight(lnkAbout, Constants.TEXT_SIZE_NORMAL);

		this.lnkAbout.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					URI url = new URI(Messages.getString("config.LicenseURL")); //$NON-NLS-1$
					log.debug("Trying to open " + url.toString()); //$NON-NLS-1$
					if (Desktop.isDesktopSupported()) {
						Desktop.getDesktop().browse(url);
					} else {
						log.info("AWT Desktop is not supported on this platform"); //$NON-NLS-1$
						Program.launch(url.toString());
					}
				} catch (IOException ex) {
					log.error("AboutComposite: ", ex); //$NON-NLS-1$
				} catch (URISyntaxException ex) {
					log.error("AboutComposite: ", ex); //$NON-NLS-1$
				}
			}
		});

		this.lblDataProtection = new Label(this, SWT.NONE);
		ConfigurationCompositeBase.anchor(lblDataProtection).top(lnkAbout, 15).right(100,-5).left(0,5).width(100).set();
		ConfigurationCompositeBase.setFontHeight(lblDataProtection, Constants.TEXT_SIZE_BIG);

		this.lnkDataProtection = new Link(this, SWT.NONE);
		ConfigurationCompositeBase.anchor(lnkDataProtection).right(100,-5).left(0,5).top(lblDataProtection,10).bottom(100,-5).width(100).height(120).set();
		ConfigurationCompositeBase.setFontHeight(lnkDataProtection, Constants.TEXT_SIZE_NORMAL);

		this.lnkDataProtection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					URI url = new URI(Messages.getString("config.DataProtectionURL")); //$NON-NLS-1$
					log.debug("Trying to open " + url.toString()); //$NON-NLS-1$
					if (Desktop.isDesktopSupported()) {
						Desktop.getDesktop().browse(url);
					} else {
						log.info("AWT Desktop is not supported on this platform"); //$NON-NLS-1$
						Program.launch(url.toString());
					}
				} catch (IOException ex) {
					log.error("AboutComposite: ", ex); //$NON-NLS-1$
				} catch (URISyntaxException ex) {
					log.error("AboutComposite: ", ex); //$NON-NLS-1$
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
		this.lnkAbout.setText(Messages.getString("config.AboutText")); //$NON-NLS-1$
		this.lnkDataProtection.setText(Messages.getString("config.DataProtectionStatement"));
		this.lblDataProtection.setText(Messages.getString("config.DataProtection"));
	}


	@Override
	protected void signerChanged() {}

	@Override
	public void initConfiguration(PersistentConfigProvider provider) {}

	@Override
	public void loadConfiguration() {}

	@Override
	public void storeConfiguration(ConfigManipulator store, PersistentConfigProvider provider) {}

	@Override
	public void validateSettings(int resumeFrom) throws Exception {}
}