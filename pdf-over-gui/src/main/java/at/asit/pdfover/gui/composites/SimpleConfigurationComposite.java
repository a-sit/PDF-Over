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
import java.awt.image.BufferedImage;
import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.Messages;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.controls.ErrorMarker;
import at.asit.pdfover.gui.controls.ErrorDialog.ERROR_BUTTONS;
import at.asit.pdfover.gui.exceptions.InvalidNumberException;
import at.asit.pdfover.gui.exceptions.InvalidPortException;
import at.asit.pdfover.gui.utils.ImageConverter;
import at.asit.pdfover.gui.workflow.ConfigurationContainer;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.FileNameEmblem;
import at.asit.pdfover.signator.SignatureParameter;

/**
 * 
 */
public class SimpleConfigurationComposite extends BaseConfigurationComposite {

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

		this.grpHandySignatur = new Group(this, SWT.NONE | SWT.RESIZE);
		FormData fd_grpHandySignatur = new FormData();
		fd_grpHandySignatur.right = new FormAttachment(100, -5);
		fd_grpHandySignatur.left = new FormAttachment(0, 5);
		fd_grpHandySignatur.top = new FormAttachment(0, 5);
		this.grpHandySignatur.setLayoutData(fd_grpHandySignatur);
		this.grpHandySignatur.setLayout(new GridLayout(2, false));
		this.grpHandySignatur.setText(Messages
				.getString("simple_config.MobileBKU_Title")); //$NON-NLS-1$

		FontData[] fD_grpHandySignatur = this.grpHandySignatur.getFont()
				.getFontData();
		fD_grpHandySignatur[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpHandySignatur.setFont(new Font(Display.getCurrent(),
				fD_grpHandySignatur[0]));

		this.lblMobileNumber = new Label(this.grpHandySignatur, SWT.NONE
				| SWT.RESIZE);
		this.lblMobileNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				false, false, 1, 1));
		this.lblMobileNumber.setText(Messages
				.getString("simple_config.PhoneNumber")); //$NON-NLS-1$

		FontData[] fD_lblMobileNumber = this.lblMobileNumber.getFont()
				.getFontData();
		fD_lblMobileNumber[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblMobileNumber.setFont(new Font(Display.getCurrent(),
				fD_lblMobileNumber[0]));

		Composite composite_2 = new Composite(this.grpHandySignatur, SWT.NONE);
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
		this.txtMobileNumber.setToolTipText(Messages
				.getString("simple_config.ExampleNumber_ToolTip")); //$NON-NLS-1$

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

		FontData[] fD_txtMobileNumber = this.txtMobileNumber.getFont()
				.getFontData();
		fD_txtMobileNumber[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.txtMobileNumber.setFont(new Font(Display.getCurrent(),
				fD_txtMobileNumber[0]));

		this.txtMobileNumber.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					processNumberChanged();
				}
			}
		});

		this.txtMobileNumber.setMessage(Messages
				.getString("simple_config.ExampleNumber")); //$NON-NLS-1$

		this.txtMobileNumber.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				processNumberChanged();
			}
		});

		this.grpBildmarke = new Group(this, SWT.NONE);
		FormData fd_grpBildmarke = new FormData();
		fd_grpBildmarke.left = new FormAttachment(0, 5);
		fd_grpBildmarke.right = new FormAttachment(100, -5);
		fd_grpBildmarke.top = new FormAttachment(this.grpHandySignatur, 5);
		this.grpBildmarke.setLayoutData(fd_grpBildmarke);
		this.grpBildmarke.setLayout(new FormLayout());
		this.grpBildmarke.setText(Messages
				.getString("simple_config.Emblem_Title")); //$NON-NLS-1$

		FontData[] fD_grpBildmarke = this.grpBildmarke.getFont().getFontData();
		fD_grpBildmarke[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpBildmarke.setFont(new Font(Display.getCurrent(),
				fD_grpBildmarke[0]));

		Composite containerComposite = new Composite(this.grpBildmarke,
				SWT.NONE);
		containerComposite.setLayout(new FormLayout());
		FormData fd_containerComposite = new FormData();
		fd_containerComposite.left = new FormAttachment(0);
		fd_containerComposite.right = new FormAttachment(100);
		fd_containerComposite.top = new FormAttachment(0);
		fd_containerComposite.bottom = new FormAttachment(100);
		containerComposite.setLayoutData(fd_containerComposite);

		Composite controlComposite = new Composite(containerComposite, SWT.NONE);
		controlComposite.setLayout(new FormLayout());
		FormData fd_controlComposite = new FormData();
		fd_controlComposite.left = new FormAttachment(0, 20);
		fd_controlComposite.right = new FormAttachment(0, 300);
		fd_controlComposite.top = new FormAttachment(0, 20);
		fd_controlComposite.bottom = new FormAttachment(100, -20);
		controlComposite.setLayoutData(fd_controlComposite);
		controlComposite.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				// e.gc.setForeground();
				e.gc.setForeground(Constants.DROP_BORDER_COLOR);
				e.gc.setLineWidth(3);
				e.gc.setLineStyle(SWT.LINE_DASH);
				e.gc.drawRoundRectangle(e.x, e.y, e.width - 2, e.height - 2,
						10, 10);
			}
		});

		this.lblEmblem = new Label(containerComposite, SWT.RESIZE);

		this.btnBrowseEmblem = new Button(controlComposite, SWT.NONE);

		this.lbl_drop = new Label(controlComposite, SWT.NATIVE);

		this.lbl_logo = new Label(controlComposite, SWT.NATIVE);
		this.lbl_logo.setAlignment(SWT.CENTER);
		FormData fd_lbl_logo = new FormData();
		fd_lbl_logo.left = new FormAttachment(0, 20);
		fd_lbl_logo.right = new FormAttachment(100, -20);
		fd_lbl_logo.top = new FormAttachment(0, 20);
		fd_lbl_logo.bottom = new FormAttachment(this.lbl_drop, -20);

		this.lbl_logo.setLayoutData(fd_lbl_logo);

		this.lbl_drop.setText(Messages.getString("simple_config.EmblemEmpty")); //$NON-NLS-1$
		this.btnClearImage = new Button(controlComposite, SWT.NATIVE);

		FormData fd_lbl_drop = new FormData();
		fd_lbl_drop.left = new FormAttachment(0, 20);
		fd_lbl_drop.right = new FormAttachment(100, -20);
		// fd_lbl_drop.top = new FormAttachment(50, -20);
		fd_lbl_drop.bottom = new FormAttachment(this.btnBrowseEmblem, -20);

		this.lbl_drop.setLayoutData(fd_lbl_drop);

		FormData fd_lblEmblem = new FormData();
		fd_lblEmblem.left = new FormAttachment(controlComposite, 20);
		fd_lblEmblem.right = new FormAttachment(100, -20);
		fd_lblEmblem.top = new FormAttachment(0, 20);
		fd_lblEmblem.bottom = new FormAttachment(100, -20);

		this.lblEmblem.setLayoutData(fd_lblEmblem);
		this.lblEmblem.setAlignment(SWT.CENTER);

		this.lblEmblem.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event event) {
				SimpleConfigurationComposite.this.recalculateEmblemSize();
			}
		});

		FontData[] fD_lblEmblem = this.lblEmblem.getFont().getFontData();
		fD_lblEmblem[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblEmblem.setFont(new Font(Display.getCurrent(), fD_lblEmblem[0]));

		DropTarget dnd_target = new DropTarget(controlComposite,
				DND.DROP_DEFAULT | DND.DROP_COPY);
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

		this.btnClearImage.setText(Messages
				.getString("simple_config.ClearEmblem")); //$NON-NLS-1$
		this.btnClearImage.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				SimpleConfigurationComposite.this.processEmblemChanged(null);
			}
		});

		FontData[] fD_btnUseImage = this.btnClearImage.getFont().getFontData();
		fD_btnUseImage[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnClearImage.setFont(new Font(Display.getCurrent(),
				fD_btnUseImage[0]));

		FormData fd_btnUseImage = new FormData();

		fd_btnUseImage.bottom = new FormAttachment(100, -20);
		fd_btnUseImage.right = new FormAttachment(this.btnBrowseEmblem, -10);

		this.btnClearImage.setLayoutData(fd_btnUseImage);

		FormData fd_btnBrowseEmblem = new FormData();

		fd_btnBrowseEmblem.bottom = new FormAttachment(100, -20);
		fd_btnBrowseEmblem.right = new FormAttachment(100, -20);

		this.btnBrowseEmblem.setLayoutData(fd_btnBrowseEmblem);
		this.btnBrowseEmblem.addSelectionListener(new ImageFileBrowser());
		this.btnBrowseEmblem.setText(Messages.getString("common.browse")); //$NON-NLS-1$

		FontData[] fD_btnBrowseEmblem = this.btnBrowseEmblem.getFont()
				.getFontData();
		fD_btnBrowseEmblem[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btnBrowseEmblem.setFont(new Font(Display.getCurrent(),
				fD_btnBrowseEmblem[0]));

		this.grpProxy = new Group(this, SWT.NONE);
		FormData fd_grpProxy = new FormData();
		fd_grpProxy.right = new FormAttachment(100, -5);
		fd_grpProxy.top = new FormAttachment(this.grpBildmarke, 5);
		fd_grpProxy.left = new FormAttachment(0, 5);
		this.grpProxy.setLayoutData(fd_grpProxy);
		this.grpProxy.setLayout(new GridLayout(2, false));
		this.grpProxy.setText(Messages.getString("simple_config.Proxy_Title")); //$NON-NLS-1$

		FontData[] fD_grpProxy = this.grpProxy.getFont().getFontData();
		fD_grpProxy[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpProxy.setFont(new Font(Display.getCurrent(), fD_grpProxy[0]));

		this.lblNewLabel = new Label(this.grpProxy, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_lblNewLabel.widthHint = 66;
		this.lblNewLabel.setLayoutData(gd_lblNewLabel);
		this.lblNewLabel.setBounds(0, 0, 57, 15);
		this.lblNewLabel.setText(Messages.getString("simple_config.ProxyHost")); //$NON-NLS-1$

		FontData[] fD_lblNewLabel = this.lblNewLabel.getFont().getFontData();
		fD_lblNewLabel[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblNewLabel.setFont(new Font(Display.getCurrent(),
				fD_lblNewLabel[0]));

		Composite composite = new Composite(this.grpProxy, SWT.NONE);
		composite.setLayout(new FormLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));
		this.txtProxyHost = new Text(composite, SWT.BORDER);
		FormData fd_txtProxyHost = new FormData();
		fd_txtProxyHost.right = new FormAttachment(100, -42);
		fd_txtProxyHost.bottom = new FormAttachment(100);
		fd_txtProxyHost.top = new FormAttachment(0);
		fd_txtProxyHost.left = new FormAttachment(0, 5);

		FontData[] fD_txtProxyHost = this.txtProxyHost.getFont().getFontData();
		fD_txtProxyHost[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.txtProxyHost.setFont(new Font(Display.getCurrent(),
				fD_txtProxyHost[0]));

		this.proxyHostErrorMarker = new ErrorMarker(composite, SWT.NONE, null,
				"", this.txtProxyHost); //$NON-NLS-1$

		FormData fd_marker = new FormData();
		fd_marker.right = new FormAttachment(100, -32);
		fd_marker.bottom = new FormAttachment(100);
		fd_marker.top = new FormAttachment(0);

		this.proxyHostErrorMarker.setLayoutData(fd_marker);
		this.proxyHostErrorMarker.setVisible(false);
		this.txtProxyHost.setLayoutData(fd_txtProxyHost);
		this.txtProxyHost.setToolTipText(Messages
				.getString("simple_config.ProxyHost_ToolTip")); //$NON-NLS-1$
		this.txtProxyHost.setMessage(Messages
				.getString("simple_config.ProxyHostTemplate")); //$NON-NLS-1$

		this.txtProxyHost.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				processProxyHostChanged();
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

		this.lblNewLabel_1 = new Label(this.grpProxy, SWT.NONE);
		this.lblNewLabel_1.setBounds(0, 0, 57, 15);
		this.lblNewLabel_1.setText(Messages
				.getString("simple_config.ProxyPort")); //$NON-NLS-1$

		FontData[] fD_lblNewLabel_1 = this.lblNewLabel_1.getFont()
				.getFontData();
		fD_lblNewLabel_1[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblNewLabel_1.setFont(new Font(Display.getCurrent(),
				fD_lblNewLabel_1[0]));

		Composite composite_1 = new Composite(this.grpProxy, SWT.NONE);
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
		this.txtProxyPort.setToolTipText(Messages
				.getString("simple_config.ProxyPort_ToolTip")); //$NON-NLS-1$

		FontData[] fD_txtProxyPort = this.txtProxyPort.getFont().getFontData();
		fD_txtProxyPort[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.txtProxyPort.setFont(new Font(Display.getCurrent(),
				fD_txtProxyPort[0]));

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

		this.txtProxyPort.setMessage(Messages
				.getString("simple_config.ProxyPortTemplate")); //$NON-NLS-1$

		this.txtProxyPort.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				processProxyPortChanged();
			}
		});

		this.grpSignatureNote = new Group(this, SWT.NONE);
		FormData fd_grpSignatureNote = new FormData();
		fd_grpSignatureNote.right = new FormAttachment(100, -5);
		fd_grpSignatureNote.top = new FormAttachment(this.grpProxy, 5);
		fd_grpSignatureNote.left = new FormAttachment(0, 5);
		this.grpSignatureNote.setLayoutData(fd_grpSignatureNote);
		this.grpSignatureNote.setLayout(new GridLayout(2, false));
		this.grpSignatureNote.setText(Messages
				.getString("simple_config.Note_Title")); //$NON-NLS-1$

		FontData[] fD_grpSignatureNote = this.grpSignatureNote.getFont()
				.getFontData();
		fD_grpSignatureNote[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.grpSignatureNote.setFont(new Font(Display.getCurrent(),
				fD_grpSignatureNote[0]));

		this.lblSignatureNote = new Label(this.grpSignatureNote, SWT.NONE);
		GridData gd_lblSignatureNote = new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1);
		gd_lblSignatureNote.widthHint = 66;
		this.lblSignatureNote.setLayoutData(gd_lblSignatureNote);
		this.lblSignatureNote.setBounds(0, 0, 57, 15);
		this.lblSignatureNote.setText(Messages.getString("simple_config.Note")); //$NON-NLS-1$

		FontData[] fD_lblSignatureNote = this.lblSignatureNote.getFont()
				.getFontData();
		fD_lblSignatureNote[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lblSignatureNote.setFont(new Font(Display.getCurrent(),
				fD_lblSignatureNote[0]));

		this.txtSignatureNote = new Text(this.grpSignatureNote, SWT.BORDER);
		this.txtSignatureNote.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, false, 1, 1));
		this.txtSignatureNote.setToolTipText(Messages
				.getString("simple_config.Note_Tooltip")); //$NON-NLS-1$

		FontData[] fD_txtSignatureNote = this.txtProxyHost.getFont()
				.getFontData();
		fD_txtSignatureNote[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.txtSignatureNote.setFont(new Font(Display.getCurrent(),
				fD_txtSignatureNote[0]));

		this.txtSignatureNote.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				processSignatureNoteChanged();
			}
		});

		this.txtSignatureNote.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					processSignatureNoteChanged();
				}
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
			dialog.setFilterExtensions(new String[] {
					"*.jpg;*.png;*.gif", "*.jpg", "*.png", "*.gif", "*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			dialog.setFilterNames(new String[] {
					Messages.getString("common.ImageExtension_Description"), //$NON-NLS-1$
					Messages.getString("common.JPGExtension_Description"), //$NON-NLS-1$
					Messages.getString("common.PNGExtension_Description"), //$NON-NLS-1$
					Messages.getString("common.GIFExtension_Description"), //$NON-NLS-1$
					Messages.getString("common.AllExtension_Description") }); //$NON-NLS-1$
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

	Label lblEmblem;
	private Text txtProxyHost;
	Text txtProxyPort;
	Text txtSignatureNote;
	Text txtMobileNumber;
	// Text txtEmblemFile;
	String emblemFile;
	private Image origEmblem = null;
	private Image origlogo = null;

	void recalculateEmblemSize() {
		this.recalculateEmblemSize(this.origEmblem, this.lblEmblem);
		this.recalculateEmblemSize(this.origlogo, this.lbl_logo);
	}

	void recalculateEmblemSize(Image image, Label parent) {
		if (image != null) {

			int width = image.getBounds().width;
			int height = image.getBounds().height;

			int scaledWidth = parent.getSize().x;
			int scaledHeight = parent.getSize().y;

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

			BufferedImage awt_image = ImageConverter.convertToAWT(image
					.getImageData());

			java.awt.Image scaled_awt = awt_image.getScaledInstance(
					(int) (width * betterFactor),
					(int) (height * betterFactor), java.awt.Image.SCALE_SMOOTH);

			BufferedImage scaled_buffered = new BufferedImage(
					(int) (width * betterFactor),
					(int) (height * betterFactor), BufferedImage.TYPE_INT_RGB);
			scaled_buffered.getGraphics().drawImage(scaled_awt, 0, 0, null);

			Image emblem = new Image(this.getDisplay(),
					ImageConverter.convertToSWT(scaled_buffered));

			Image old = parent.getImage();

			if (old != null) {
				old.dispose();
			}

			parent.setText(""); //$NON-NLS-1$
			parent.setImage(emblem);
		} else {
			parent.setImage(null);
		}
	}

	private void setEmblemFile(final String filename) throws Exception {
		setEmblemFileInternal(filename, false);
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

		this.configurationContainer.setEmblem(filename);
		this.setVisibleImage();
		this.doLayout();
	}

	void setVisibleImage() {
		String image = this.configurationContainer.getEmblem();
		ImageData img = null;
		ImageData logo = null;

		try {

			if (image == null || image.trim().equals("")) { //$NON-NLS-1$
				if (this.signer != null) {
					img = ImageConverter
							.convertToSWT((BufferedImage) this.signer
									.getPDFSigner().newParameter()
									.getPlaceholder());
				}
			} else {
				if (this.signer != null) {
					SignatureParameter param = this.signer.getPDFSigner()
							.newParameter();
					param.setEmblem(new FileNameEmblem(image));
					img = ImageConverter.convertToSWT((BufferedImage) param
							.getPlaceholder());
				} else {
					img = new ImageData(image);
				}
				logo = new ImageData(image);
			}
		} catch (Exception e) {
			log.error("Failed to load image for display...", e); //$NON-NLS-1$
		}

		if (img != null) {
			this.origEmblem = new Image(this.getDisplay(), img);
		} else {
			this.origEmblem = null;
		}

		if (logo != null) {
			this.origlogo = new Image(this.getDisplay(), logo);
		} else {
			this.origlogo = null;
		}

		this.recalculateEmblemSize();
	}

	void processEmblemChanged(String filename) {
		try {
			setEmblemFile(filename);
		} catch (Exception ex) {
			log.error("processEmblemChanged: ", ex); //$NON-NLS-1$
			ErrorDialog dialog = new ErrorDialog(
					getShell(),
					Messages.getString("error.FailedToLoadEmblem"), ERROR_BUTTONS.OK); //$NON-NLS-1$
			dialog.open();
		}
	}

	void processNumberChanged() {
		try {
			this.txtMobileNumberErrorMarker.setVisible(false);
			plainMobileNumberSetter();
		} catch (Exception ex) {
			this.txtMobileNumberErrorMarker.setVisible(true);
			this.txtMobileNumberErrorMarker.setToolTipText(Messages
					.getString("error.InvalidPhoneNumber")); //$NON-NLS-1$
			log.error("processNumberChanged: ", ex); //$NON-NLS-1$
			this.redraw();
			this.doLayout();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#signerChanged()
	 */
	@Override
	protected void signerChanged() {
		this.setVisibleImage();
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

	void processSignatureNoteChanged() {
		String note = this.txtSignatureNote.getText();
		this.configurationContainer.setSignatureNote(note);
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
			try {
				port = Integer.parseInt(portString);
			} catch (NumberFormatException e) {
				throw new InvalidPortException(portString, e);
			}
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
	Button btnClearImage;
	private Label lbl_logo;
	private Group grpHandySignatur;
	private Label lblMobileNumber;
	private Group grpBildmarke;
	private Label lbl_drop;
	private Button btnBrowseEmblem;
	private Group grpProxy;
	private Label lblNewLabel;
	private Label lblNewLabel_1;
	private Label lblSignatureNote;
	private Group grpSignatureNote;

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.composites.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		this.layout(true, true);
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
				setEmblemFileInternal(emblemFile, true);
				this.btnClearImage.setSelection(true);
			} catch (Exception e1) {
				log.error("Failed to load emblem: ", e1); //$NON-NLS-1$
				ErrorDialog dialog = new ErrorDialog(
						getShell(),
						Messages.getString("error.FailedToLoadEmblem"), ERROR_BUTTONS.OK); //$NON-NLS-1$
				dialog.open();
			}
		}

		int port = this.configurationContainer.getProxyPort();
		if (port > 0) {
			this.txtProxyPort.setText(Integer.toString(port));
		}

		String note = this.configurationContainer.getSignatureNote();

		if (note != null) {
			this.txtSignatureNote.setText(note);
		}

		String host = this.configurationContainer.getProxyHost();

		if (host != null) {
			this.txtProxyHost.setText(host);
		}

		this.setVisibleImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#validateSettings
	 * ()
	 */
	@Override
	public void validateSettings(int resumeFrom) throws Exception {
		switch (resumeFrom) {
		case 0:
			this.plainMobileNumberSetter();
			// Fall through
		case 1:
			this.plainProxyHostSetter();
			// Fall through
		case 2:
			this.plainProxyPortSetter();
		case 3:
			this.processSignatureNoteChanged();
		}
		// this.plainEmblemSetter(this.emblemFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		this.grpHandySignatur.setText(Messages
				.getString("simple_config.MobileBKU_Title")); //$NON-NLS-1$
		this.lblMobileNumber.setText(Messages
				.getString("simple_config.PhoneNumber")); //$NON-NLS-1$
		this.txtMobileNumber.setToolTipText(Messages
				.getString("simple_config.ExampleNumber_ToolTip")); //$NON-NLS-1$
		this.txtMobileNumber.setMessage(Messages
				.getString("simple_config.ExampleNumber")); //$NON-NLS-1$
		this.grpBildmarke.setText(Messages
				.getString("simple_config.Emblem_Title")); //$NON-NLS-1$
		this.lbl_drop.setText(Messages.getString("simple_config.EmblemEmpty")); //$NON-NLS-1$
		this.btnClearImage.setText(Messages
				.getString("simple_config.ClearEmblem")); //$NON-NLS-1$
		this.btnBrowseEmblem.setText(Messages.getString("common.browse")); //$NON-NLS-1$
		this.grpProxy.setText(Messages.getString("simple_config.Proxy_Title")); //$NON-NLS-1$
		this.lblNewLabel.setText(Messages.getString("simple_config.ProxyHost")); //$NON-NLS-1$
		this.txtProxyHost.setToolTipText(Messages
				.getString("simple_config.ProxyHost_ToolTip")); //$NON-NLS-1$
		this.lblNewLabel_1.setText(Messages
				.getString("simple_config.ProxyPort")); //$NON-NLS-1$
		this.txtProxyPort.setToolTipText(Messages
				.getString("simple_config.ProxyPort_ToolTip")); //$NON-NLS-1$
		this.txtProxyPort.setMessage(Messages
				.getString("simple_config.ProxyPortTemplate")); //$NON-NLS-1$
		this.grpSignatureNote.setText(Messages
				.getString("simple_config.Note_Title")); //$NON-NLS-1$
		this.lblSignatureNote.setText(Messages.getString("simple_config.Note")); //$NON-NLS-1$
		this.txtSignatureNote.setToolTipText(Messages
				.getString("simple_config.Note_Tooltip")); //$NON-NLS-1$
	}
}
