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
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.controls.ErrorMarker;
import at.asit.pdfover.gui.exceptions.InvalidEmblemFile;
import at.asit.pdfover.gui.exceptions.InvalidNumberException;
import at.asit.pdfover.gui.exceptions.InvalidPortException;
import at.asit.pdfover.gui.workflow.ConfigurationContainer;
import at.asit.pdfover.gui.workflow.states.State;
import org.eclipse.swt.layout.FillLayout;

/**
 * 
 */
public class SimpleConfigurationComposite extends BaseConfigurationComposite {

	/**
	 * 
	 */
	private final class ImageFileBrowser extends SelectionAdapter {
		/**
		 * 
		 */
		public ImageFileBrowser() {
			// Nothing to do
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			FileDialog dialog = new FileDialog(
					SimpleConfigurationComposite.this.getShell(), SWT.OPEN);
			dialog.setFilterExtensions(new String[] { "*.jpg", "*.gif" }); //$NON-NLS-1$ //$NON-NLS-2$
			dialog.setFilterNames(new String[] { "JPG Dateien", "Gif Dateien" });
			String fileName = dialog.open();
			File file = null;
			if (fileName != null) {
				file = new File(fileName);
				if (file.exists()) {
					/*
					 * SimpleConfigurationComposite.this.txtEmblemFile
					 * .setText(fileName);
					 */
					processEmblemChanged(fileName);
				}
			}
		}
	}

	private Label lblEmblem;
	private Text txtProxyHost;
	Text txtProxyPort;
	Text txtMobileNumber;
	// Text txtEmblemFile;
	String emblemFile;
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

			if (betterFactor == 0.0) {
				betterFactor = 1.0f;
			}

			Image emblem = new Image(this.getDisplay(), this.origEmblem
					.getImageData().scaledTo((int) (width * betterFactor),
							(int) (height * betterFactor)));

			Image old = this.lblEmblem.getImage();

			if (old != null) {
				old.dispose();
			}

			this.lblEmblem.setText(""); //$NON-NLS-1$
			this.lblEmblem.setImage(emblem);
		}
	}

	private void setEmblemFileInternal(final String filename) throws Exception {
		this.setEmblemFileInternal(filename, false);
	}

	private void setEmblemFileInternal(final String filename, boolean force)
			throws Exception {
		if (!force) {
			if (this.configurationContainer.getEmblem() != null) {
				if (this.configurationContainer.getEmblem().equals(filename)) {
					return; // Ignore ...
				}
			}
		}

		try {
			this.configurationContainer.setEmblem(filename);

			if (filename == null || filename.trim().equals("")) { //$NON-NLS-1$
				return;
			}

			// this.txtEmblemFile.setText();

			this.emblemFile = this.configurationContainer.getEmblem();
			if (this.origEmblem != null) {
				this.origEmblem.dispose();
			}

			this.origEmblem = new Image(this.getDisplay(), new ImageData(
					filename));

			this.lblEmblem.setText(""); //$NON-NLS-1$

			this.recalculateEmblemSize();
		} catch (Exception e) {
			this.lblEmblem
					.setText("No Image. Drag and Drop a Image. Or use the browse button to select an emblem.");
			this.lblEmblem.setImage(null);
			if (this.origEmblem != null) {
				this.origEmblem.dispose();
			}
			this.origEmblem = null;
			throw e;
		}

		// this.lblEmblem.pack();
		// this.lblEmblem.getParent().pack();
		this.doLayout();
	}

	void processEmblemChanged(String filename) {
		try {
			// String filename = this.txtEmblemFile.getText();
			plainEmblemSetter(filename);
		} catch (Exception ex) {
			log.error("processEmblemChanged: ", ex); //$NON-NLS-1$
			ErrorDialog dialog = new ErrorDialog(getShell(), SWT.NONE, "Failed to load the emblem", ex);
			dialog.open();
		}
	}

	/**
	 * @param filename
	 * @throws Exception
	 */
	private void plainEmblemSetter(String filename) throws Exception {
		this.emblemFile = filename;
		this.setEmblemFileInternal(filename);
		this.btnUseImage.setSelection(true);
	}

	void processNumberChanged() {
		try {
			this.txtMobileNumberErrorMarker.setVisible(false);
			plainMobileNumberSetter();
		} catch (Exception ex) {
			this.txtMobileNumberErrorMarker.setVisible(true);
			this.txtMobileNumberErrorMarker
					.setToolTipText("Phone number is invalid! Please provide in the form: +43676123456789");
			log.error("processNumberChanged: ", ex); //$NON-NLS-1$
		}
	}

	/**
	 * @throws InvalidNumberException
	 */
	private void plainMobileNumberSetter() throws InvalidNumberException {
		String number = this.txtMobileNumber.getText();
		this.configurationContainer.setNumber(number);
		number = this.configurationContainer.getNumber();
		if (number == null) {
			this.txtMobileNumber.setText(""); //$NON-NLS-1$
			return;
		}
		this.txtMobileNumber.setText(number);
	}

	void processProxyHostChanged() {
		try {
			this.proxyHostErrorMarker.setVisible(false);
			plainProxyHostSetter();
		} catch (Exception ex) {
			this.proxyHostErrorMarker.setVisible(true);
			this.proxyHostErrorMarker.setToolTipText(ex.getMessage());
			log.error("processProxyHost: ", ex); //$NON-NLS-1$
		}
	}

	/**
	 * 
	 */
	private void plainProxyHostSetter() {
		String host = this.txtProxyHost.getText();
		this.configurationContainer.setProxyHost(host);
	}

	void processProxyPortChanged() {
		try {
			this.txtProxyPortErrorMarker.setVisible(false);
			plainProxyPortSetter();
		} catch (Exception ex) {
			this.txtProxyPortErrorMarker.setVisible(true);
			this.txtProxyPortErrorMarker.setToolTipText(ex.getMessage());
			log.error("processProxyPort: ", ex); //$NON-NLS-1$
		}
	}

	/**
	 * @throws InvalidPortException
	 */
	private void plainProxyPortSetter() throws InvalidPortException {
		String portString = this.txtProxyPort.getText();
		int port = -1;
		if (portString == null || portString.trim().equals("")) { //$NON-NLS-1$
			port = -1;
		} else {
			port = Integer.parseInt(portString);
		}
		this.configurationContainer.setProxyPort(port);
	}

	ConfigurationComposite configurationComposite;
	FormData fd_txtProxyPortErrorMarker;

	/**
	 * @return the configurationComposite
	 */
	public ConfigurationComposite getConfigurationComposite() {
		return this.configurationComposite;
	}

	/**
	 * @param configurationComposite
	 *            the configurationComposite to set
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
			org.eclipse.swt.widgets.Composite parent, int style, State state,
			ConfigurationContainer container) {
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

		Composite composite_2 = new Composite(grpHandySignatur, SWT.NONE);
		composite_2.setLayout(new FormLayout());
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));

		this.txtMobileNumber = new Text(composite_2, SWT.BORDER | SWT.RESIZE);
		this.fd_txtMobileNumber = new FormData();
		this.fd_txtMobileNumber.top = new FormAttachment(0);
		this.fd_txtMobileNumber.left = new FormAttachment(0, 5);
		this.fd_txtMobileNumber.bottom = new FormAttachment(100);
		this.fd_txtMobileNumber.right = new FormAttachment(100, -42);
		this.txtMobileNumber.setLayoutData(this.fd_txtMobileNumber);

		this.txtMobileNumberErrorMarker = new ErrorMarker(composite_2,
				SWT.NATIVE, null, "", this.txtMobileNumber); //$NON-NLS-1$
		this.txtMobileNumberErrorMarker.setVisible(false);
		this.fd_txtMobileNumberErrorMarker = new FormData();
		this.fd_txtMobileNumberErrorMarker.top = new FormAttachment(0);
		this.fd_txtMobileNumberErrorMarker.left = new FormAttachment(100, -32);
		this.fd_txtMobileNumberErrorMarker.bottom = new FormAttachment(100);
		this.fd_txtMobileNumberErrorMarker.right = new FormAttachment(100);
		this.txtMobileNumberErrorMarker
				.setLayoutData(this.fd_txtMobileNumberErrorMarker);

		this.txtMobileNumber.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					processNumberChanged();
				}
			}
		});

		this.txtMobileNumber.setMessage("+43676123456789");

		this.txtMobileNumber.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				processNumberChanged();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// Nothing to do here!
			}
		});

		Group grpBildmarke = new Group(this, SWT.NONE);
		FormData fd_grpBildmarke = new FormData();
		fd_grpBildmarke.left = new FormAttachment(0, 5);
		fd_grpBildmarke.right = new FormAttachment(100, -5);
		fd_grpBildmarke.bottom = new FormAttachment(65, -5);
		fd_grpBildmarke.top = new FormAttachment(20, 5);
		grpBildmarke.setLayoutData(fd_grpBildmarke);
		grpBildmarke.setLayout(new GridLayout(5, false));
		grpBildmarke.setText("Bildmarke");
		new Label(grpBildmarke, SWT.NONE);
		new Label(grpBildmarke, SWT.NONE);

		this.lblEmblem = new Label(grpBildmarke, SWT.BORDER | SWT.RESIZE);
		this.lblEmblem.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 3, 1));
		this.lblEmblem.setAlignment(SWT.CENTER);
		this.lblEmblem
				.setText("No Image. Drag and Drop a Image. Or use the browse button to select an emblem.");
		this.lblEmblem.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event event) {
				SimpleConfigurationComposite.this.recalculateEmblemSize();
			}
		});

		DropTarget dnd_target = new DropTarget(this.lblEmblem, DND.DROP_DEFAULT
				| DND.DROP_COPY);
		final FileTransfer fileTransfer = FileTransfer.getInstance();
		Transfer[] types = new Transfer[] { fileTransfer };
		dnd_target.setTransfer(types);

		dnd_target.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				if (fileTransfer.isSupportedType(event.currentDataType)) {
					String[] files = (String[]) event.data;
					if (files.length > 0) {
						// Only taking first file ...
						File file = new File(files[0]);
						if (!file.exists()) {
							log.error("File: " + files[0] + " does not exist!"); //$NON-NLS-1$//$NON-NLS-2$
							return;
						}
						processEmblemChanged(file.getAbsolutePath());
					}
				}
			}

			@Override
			public void dragOperationChanged(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_COPY;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
			}

			@Override
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_COPY;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
				// Only drop one item!
				if (event.dataTypes.length > 1) {
					event.detail = DND.DROP_NONE;
					return;
				}
				// will accept text but prefer to have files dropped
				for (int i = 0; i < event.dataTypes.length; i++) {
					if (fileTransfer.isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
						// files should only be copied
						if (event.detail != DND.DROP_COPY) {
							event.detail = DND.DROP_NONE;
						}
						break;
					}
				}
			}
		});

		new Label(grpBildmarke, SWT.NONE);
		new Label(grpBildmarke, SWT.NONE);

		this.btnUseImage = new Button(grpBildmarke, SWT.CHECK);
		this.btnUseImage.setText("Use Image");
		this.btnUseImage.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (SimpleConfigurationComposite.this.btnUseImage
						.getSelection()) {
					processEmblemChanged(SimpleConfigurationComposite.this.emblemFile);
				} else {
					try {
						SimpleConfigurationComposite.this.configurationContainer
								.setEmblem(null);
					} catch (InvalidEmblemFile e1) {
						log.error("THIS EXCEPTION IS IMPOSSIBLE! ", e1); //$NON-NLS-1$
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Nothing to do
			}
		});
		new Label(grpBildmarke, SWT.NONE);

		Button btnBrowseEmblem = new Button(grpBildmarke, SWT.NONE);
		btnBrowseEmblem.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				false, false, 1, 1));
		btnBrowseEmblem.addSelectionListener(new ImageFileBrowser());
		btnBrowseEmblem.setText("Browse");

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

		Composite composite = new Composite(grpProxy, SWT.NONE);
		composite.setLayout(new FormLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));
		this.txtProxyHost = new Text(composite, SWT.BORDER);
		FormData fd_txtProxyHost = new FormData();
		fd_txtProxyHost.right = new FormAttachment(100, -42);
		fd_txtProxyHost.bottom = new FormAttachment(100);
		fd_txtProxyHost.top = new FormAttachment(0);
		fd_txtProxyHost.left = new FormAttachment(0, 5);

		this.proxyHostErrorMarker = new ErrorMarker(composite, SWT.NONE, null,
				"", this.txtProxyHost); //$NON-NLS-1$

		FormData fd_marker = new FormData();
		fd_marker.right = new FormAttachment(100, -32);
		fd_marker.bottom = new FormAttachment(100);
		fd_marker.top = new FormAttachment(0);

		this.proxyHostErrorMarker.setLayoutData(fd_marker);
		this.proxyHostErrorMarker.setVisible(false);
		this.txtProxyHost.setLayoutData(fd_txtProxyHost);

		this.txtProxyHost.setMessage("Hostname or IP of proxy server");

		this.txtProxyHost.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				processProxyHostChanged();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// Nothing to do here!
			}
		});

		this.txtProxyHost.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					processProxyHostChanged();
				}
			}
		});

		Label lblNewLabel_1 = new Label(grpProxy, SWT.NONE);
		lblNewLabel_1.setBounds(0, 0, 57, 15);
		lblNewLabel_1.setText("Port:");

		Composite composite_1 = new Composite(grpProxy, SWT.NONE);
		composite_1.setLayout(new FormLayout());
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));

		this.txtProxyPort = new Text(composite_1, SWT.BORDER);
		this.fd_txtProxyPort = new FormData();
		this.fd_txtProxyPort.top = new FormAttachment(0, 0);
		this.fd_txtProxyPort.left = new FormAttachment(0, 5);
		this.fd_txtProxyPort.right = new FormAttachment(100, -42);
		this.fd_txtProxyPort.bottom = new FormAttachment(100);
		this.txtProxyPort.setLayoutData(this.fd_txtProxyPort);
		this.txtProxyPort.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					processProxyPortChanged();
				}
			}
		});

		this.txtProxyPortErrorMarker = new ErrorMarker(composite_1, SWT.NATIVE,
				null, "", this.txtProxyPort); //$NON-NLS-1$
		this.fd_txtProxyPortErrorMarker = new FormData();
		this.fd_txtProxyPortErrorMarker.top = new FormAttachment(0);
		this.fd_txtProxyPortErrorMarker.left = new FormAttachment(100, -32);
		this.fd_txtProxyPortErrorMarker.bottom = new FormAttachment(100);
		this.txtProxyPortErrorMarker
				.setLayoutData(this.fd_txtProxyPortErrorMarker);
		this.txtProxyPortErrorMarker.setVisible(false);

		this.txtProxyPort.setMessage("port proxy server [1-65535]");

		this.txtProxyPort.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				processProxyPortChanged();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// Nothing to do here!
			}
		});

		this.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event event) {

				// Number resize with error Marker

				Point numberSize = new Point(
						SimpleConfigurationComposite.this.txtMobileNumber
								.getSize().y,
						SimpleConfigurationComposite.this.txtMobileNumber
								.getSize().y);
				SimpleConfigurationComposite.this.txtMobileNumberErrorMarker
						.resize(numberSize);
				SimpleConfigurationComposite.this.fd_txtMobileNumberErrorMarker.left = new FormAttachment(
						100, -1 * numberSize.y);
				SimpleConfigurationComposite.this.fd_txtMobileNumber.right = new FormAttachment(
						100, -1 * (numberSize.y + 10));

				Point portSize = new Point(
						SimpleConfigurationComposite.this.txtProxyPort
								.getSize().y,
						SimpleConfigurationComposite.this.txtProxyPort
								.getSize().y);
				SimpleConfigurationComposite.this.txtProxyPortErrorMarker
						.resize(numberSize);
				SimpleConfigurationComposite.this.fd_txtProxyPortErrorMarker.left = new FormAttachment(
						100, -1 * portSize.y);
				SimpleConfigurationComposite.this.fd_txtProxyPort.right = new FormAttachment(
						100, -1 * (portSize.y + 10));
			}
		});
	}

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(SimpleConfigurationComposite.class);
	private ErrorMarker proxyHostErrorMarker;
	ErrorMarker txtMobileNumberErrorMarker;
	FormData fd_txtMobileNumberErrorMarker;
	FormData fd_txtMobileNumber;
	FormData fd_txtProxyPort;
	ErrorMarker txtProxyPortErrorMarker;
	Button btnUseImage;

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.composites.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		// Nothing to do here
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#loadConfiguration
	 * ()
	 */
	@Override
	public void loadConfiguration() {
		// Initialize form fields from configuration Container
		String number = this.configurationContainer.getNumber();
		if (number != null) {
			this.txtMobileNumber.setText(number);
		}

		String emblemFile = this.configurationContainer.getEmblem();
		if (emblemFile != null && !emblemFile.trim().equals("")) { //$NON-NLS-1$
			// this.txtEmblemFile.setText(emblemFile);
			this.emblemFile = emblemFile;
			try {
				this.setEmblemFileInternal(emblemFile, true);
				this.btnUseImage.setSelection(true);
			} catch (Exception e1) {
				log.error("Failed to load emblem: ", e1); //$NON-NLS-1$
				ErrorDialog dialog = new ErrorDialog(getShell(), SWT.NONE, "Failed to load emblem.", e1);
				dialog.open();
			}
		}

		int port = this.configurationContainer.getProxyPort();
		if (port > 0) {
			this.txtProxyPort.setText(Integer.toString(port));
		}

		String host = this.configurationContainer.getProxyHost();

		if (host != null) {
			this.txtProxyHost.setText(host);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#validateSettings
	 * ()
	 */
	@Override
	public void validateSettings() throws Exception {
		this.plainMobileNumberSetter();
		this.plainEmblemSetter(this.emblemFile);
		this.plainProxyHostSetter();
		this.plainProxyPortSetter();
	}
}
