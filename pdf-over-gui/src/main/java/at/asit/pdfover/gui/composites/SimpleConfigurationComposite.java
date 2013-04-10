/*
 * Copyright 2012 by A-SIT, Secure Information Technology Center Austria
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package at.asit.pdfover.gui.composites;

// Imports
import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.workflow.states.State;

/**
 * 
 */
public class SimpleConfigurationComposite extends BaseConfigurationComposite {
	
	private Label lblEmblem;
	private Text text;
	private Text text_1;
	private Text txtMobileNumber;
	Text txtEmblemFile;
	private Image origEmblem = null;
	
	

	void recalculateEmblemSize() {
		if (this.origEmblem != null) {

			int width = this.origEmblem.getBounds().width;
			int height = this.origEmblem.getBounds().height;

			int scaledWidth = this.lblEmblem.getSize().x;
			int scaledHeight = this.lblEmblem.getSize().y;

			float scaleFactorWidth = (float) scaledWidth / (float) width;
			float scaleFactorHeight = (float) scaledHeight / (float) height;

			float betterFactor = 1;

			int testHeight = (int) (height * scaleFactorWidth);
			int testWidth = (int) (width * scaleFactorHeight);

			// check for better scale factor ...

			if (testHeight > scaledHeight) {
				// width scaling fails!! use Height scaling
				betterFactor = scaleFactorHeight;
			} else if (testWidth > scaledWidth) {
				// height scaling fails!! use Width scaling
				betterFactor = scaleFactorWidth;
			} else {
				// Both are ok test* < scaled*

				int heightDiff = scaledHeight - testHeight;

				int widthDiff = scaledWidth - testWidth;

				if (widthDiff < heightDiff) {
					// width diff better use scaleFactorHeight
					betterFactor = scaleFactorHeight;
				} else {
					// height diff better or equal so use scaleFactorWidth
					betterFactor = scaleFactorWidth;
				}
			}

			log.debug("Scaling factor: " + betterFactor); //$NON-NLS-1$

			Image emblem = new Image(this.getDisplay(), this.origEmblem
					.getImageData().scaledTo((int) (width * betterFactor),
							(int) (height * betterFactor)));

			Image old = this.lblEmblem.getImage();
			
			if(old != null) {
				old.dispose();
			}
			
			this.lblEmblem.setText(""); //$NON-NLS-1$
			this.lblEmblem.setImage(emblem);
		}
	}

	private void setEmblemFileInternal(final String filename) throws Exception {
		if (this.configurationContainer.getEmblem() != null) {
			if (this.configurationContainer.getEmblem().equals(filename)) {
				return; // Ignore ...
			}
		}

		try {
			this.configurationContainer.setEmblem(filename);
			
			this.txtEmblemFile.setText(this.configurationContainer.getEmblem());

			if(this.origEmblem != null) {
				this.origEmblem.dispose();
			}
			
			this.origEmblem = new Image(this.getDisplay(), new ImageData(
					filename));			

			this.lblEmblem.setText(""); //$NON-NLS-1$
			
			this.recalculateEmblemSize();
		} catch (Exception e) {
			this.lblEmblem.setText("No Image");
			this.lblEmblem.setImage(null);
			if(this.origEmblem != null) {
				this.origEmblem.dispose();
			}
			this.origEmblem = null;
			throw e;
		}

		this.lblEmblem.pack();
		this.lblEmblem.getParent().pack();
		this.doLayout();
	}

	void processEmblemChanged() {
		try {
			String filename = this.txtEmblemFile.getText();
			this.setEmblemFileInternal(filename);
		} catch (Exception ex) {
			// TODO: Show error message!
			log.error("processEmblemChanged: ", ex); //$NON-NLS-1$
		}
	}
	
	ConfigurationComposite configurationComposite;
	
	/**
	 * @return the configurationComposite
	 */
	public ConfigurationComposite getConfigurationComposite() {
		return this.configurationComposite;
	}

	/**
	 * @param configurationComposite the configurationComposite to set
	 */
	public void setConfigurationComposite(
			ConfigurationComposite configurationComposite) {
		this.configurationComposite = configurationComposite;
	}

	/**
	 * @param parent
	 * @param style
	 * @param state
	 * @param container 
	 */
	public SimpleConfigurationComposite(
			org.eclipse.swt.widgets.Composite parent, int style, State state, ConfigurationContainer container) {
		super(parent, style, state, container);
		setLayout(new FormLayout());

		Group grpHandySignatur = new Group(this, SWT.NONE | SWT.RESIZE);
		FormData fd_grpHandySignatur = new FormData();
		fd_grpHandySignatur.right = new FormAttachment(100, -5);
		fd_grpHandySignatur.left = new FormAttachment(0, 5);
		fd_grpHandySignatur.top = new FormAttachment(0, 5);
		fd_grpHandySignatur.bottom = new FormAttachment(20, -5);
		grpHandySignatur.setLayoutData(fd_grpHandySignatur);
		grpHandySignatur.setText("Handy Signatur");
		grpHandySignatur.setLayout(new GridLayout(2, false));

		Label lblMobileNumber = new Label(grpHandySignatur, SWT.NONE
				| SWT.RESIZE);
		lblMobileNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		lblMobileNumber.setText("Handy Nummer:");

		this.txtMobileNumber = new Text(grpHandySignatur, SWT.BORDER
				| SWT.RESIZE);
		this.txtMobileNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));

		Group grpBildmarke = new Group(this, SWT.NONE);
		FormData fd_grpBildmarke = new FormData();
		fd_grpBildmarke.left = new FormAttachment(0, 5);
		fd_grpBildmarke.right = new FormAttachment(100, -5);
		fd_grpBildmarke.bottom = new FormAttachment(65, -5);
		fd_grpBildmarke.top = new FormAttachment(20, 5);
		grpBildmarke.setLayoutData(fd_grpBildmarke);
		grpBildmarke.setLayout(new GridLayout(3, false));
		grpBildmarke.setText("Bildmarke");

		this.lblEmblem = new Label(grpBildmarke, SWT.BORDER | SWT.RESIZE);
		this.lblEmblem.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 4));
		this.lblEmblem.setAlignment(SWT.CENTER);
		this.lblEmblem.setText("No Image");
		this.lblEmblem.addListener(SWT.Resize, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				SimpleConfigurationComposite.this.recalculateEmblemSize();
			}
		});

		Label lblDateiname = new Label(grpBildmarke, SWT.NONE);
		lblDateiname.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1));
		lblDateiname.setText("Dateiname:");
		new Label(grpBildmarke, SWT.NONE);

		this.txtEmblemFile = new Text(grpBildmarke, SWT.BORDER);
		GridData gd_txtEmblemFile = new GridData(SWT.FILL, SWT.FILL, false,
				false, 2, 1);
		gd_txtEmblemFile.widthHint = 123;
		this.txtEmblemFile.setLayoutData(gd_txtEmblemFile);
		this.txtEmblemFile.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				processEmblemChanged();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// Nothing to do here!
			}
		});
		this.txtEmblemFile.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					processEmblemChanged();
				}
			}
		});
		new Label(grpBildmarke, SWT.NONE);

		Button btnBrowseEmblem = new Button(grpBildmarke, SWT.NONE);
		btnBrowseEmblem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(SimpleConfigurationComposite.this
						.getShell(), SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.jpg", "*.gif" }); //$NON-NLS-1$ //$NON-NLS-2$
				dialog.setFilterNames(new String[] { "JPG Dateien",
						"Gif Dateien" });
				String fileName = dialog.open();
				File file = null;
				if (fileName != null) {
					file = new File(fileName);
					if (file.exists()) {
						SimpleConfigurationComposite.this.txtEmblemFile
								.setText(fileName);
						processEmblemChanged();
					}
				}
			}
		});
		btnBrowseEmblem.setText("Browse");

		Label label = new Label(grpBildmarke, SWT.NONE);
		GridData gd_label = new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1);
		gd_label.widthHint = 189;
		label.setLayoutData(gd_label);

		Group grpProxy = new Group(this, SWT.NONE);
		FormData fd_grpProxy = new FormData();
		fd_grpProxy.right = new FormAttachment(100, -5);
		fd_grpProxy.top = new FormAttachment(65, 5);
		fd_grpProxy.left = new FormAttachment(0, 5);
		fd_grpProxy.bottom = new FormAttachment(90, -5);
		grpProxy.setLayoutData(fd_grpProxy);
		grpProxy.setText("Proxy");
		grpProxy.setLayout(new GridLayout(2, false));

		Label lblNewLabel = new Label(grpProxy, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_lblNewLabel.widthHint = 66;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		lblNewLabel.setBounds(0, 0, 57, 15);
		lblNewLabel.setText("Host:");

		this.text = new Text(grpProxy, SWT.BORDER);
		this.text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));

		Label lblNewLabel_1 = new Label(grpProxy, SWT.NONE);
		lblNewLabel_1.setBounds(0, 0, 57, 15);
		lblNewLabel_1.setText("Port:");

		this.text_1 = new Text(grpProxy, SWT.BORDER);
		this.text_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));

	}

	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(SimpleConfigurationComposite.class);

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		// TODO Auto-generated method stub
		
	}

}
