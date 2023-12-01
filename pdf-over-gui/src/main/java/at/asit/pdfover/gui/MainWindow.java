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
package at.asit.pdfover.gui;

// Imports
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

import at.asit.pdfover.commons.Constants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.composites.StateComposite;
import at.asit.pdfover.gui.controls.Dialog;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.Dialog.ICON;
import at.asit.pdfover.gui.controls.MainBarButton;
import at.asit.pdfover.gui.controls.MainBarEndButton;
import at.asit.pdfover.gui.controls.MainBarMiddleButton;
import at.asit.pdfover.gui.controls.MainBarRectangleButton;
import at.asit.pdfover.gui.controls.MainBarStartButton;
import at.asit.pdfover.gui.osx.CocoaUIEnhancer;
import at.asit.pdfover.gui.utils.SWTUtils;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.states.BKUSelectionState;
import at.asit.pdfover.gui.workflow.states.ConfigurationUIState;
import at.asit.pdfover.gui.workflow.states.OpenState;
import at.asit.pdfover.gui.workflow.states.PositioningState;

/**
 * The Main Window of PDF-Over 4
 */
public class MainWindow {

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(MainWindow.class);

	private Shell shell;
	private Composite mainbar;
	private FormData mainBarFormData;
	private Composite container;
	private FormData containerFormData;
	private StackLayout stack;
	StateMachine stateMachine;
	private MainBarButton btn_sign;
	private MainBarButton btn_position;
	private MainBarButton btn_open;
	private MainBarButton btn_config;

	/**
	 * Main bar Buttons
	 */
	public enum Buttons {
		/** the configuration button */
		CONFIG,

		/** the open button */
		OPEN,

		/** the position button */
		POSITION,

		/** the signature button */
		SIGN,

		/** the final button */
		FINAL
	}

	private Map<Buttons, MainBarButton> buttonMap;

	private MainBarEndButton btn_end;

	/**
	 * Default constructor
	 *
	 * @param stateMachine
	 *            The main workflow
	 */
	public MainWindow(StateMachine stateMachine) {
		super();

		this.stateMachine = stateMachine;

		this.buttonMap = new EnumMap<MainWindow.Buttons, MainBarButton>(
				Buttons.class);
	}

	/**
	 * Sets top level composite for stack layout
	 *
	 * @param ctrl
	 */
	public void setTopControl(Control ctrl) {
		if(ctrl != null)
			log.debug("Top control: " + ctrl.toString());
		this.stack.topControl = ctrl;
		this.doLayout();
	}

	/**
	 * Layout the Main Window
	 */
	public void doLayout() {
		Control ctrl = this.stack.topControl;
		if (this.container.isDisposed()) {
			//Note: if the conainter is disposed, we are finished here
			return;
		}
		this.container.layout(true, true);
		getShell().layout(true, true);
		// Note: SWT only layouts children! No grandchildren!
		if (ctrl instanceof StateComposite) {
			if (!ctrl.isDisposed()) {
				((StateComposite) ctrl).reloadResources();
				((StateComposite) ctrl).doLayout();
			}
		}
	}

	/**
	 * Gets the container composite
	 *
	 * @return the container composite
	 */
	public Composite getContainer() {
		return this.container;
	}

	/**
	 * Open the window.
	 *
	 */
	public void open() {
		createContents();
	}

	/**
	 * Reload the localization
	 */
	public void reloadLocalization() {
		SWTUtils.setLocalizedText(btn_config, "main.configuration");
		SWTUtils.setLocalizedToolTipText(btn_config, "main.configuration");
		SWTUtils.setLocalizedText(btn_open, "common.open");
		SWTUtils.setLocalizedToolTipText(btn_open, "common.open");

		SWTUtils.setLocalizedText(btn_position, "main.position");
		SWTUtils.setLocalizedToolTipText(btn_position, "main.position");

		SWTUtils.setLocalizedText(btn_sign, "main.signature");
		SWTUtils.setLocalizedToolTipText(btn_sign, "main.signature");

		SWTUtils.setLocalizedText(btn_end, "main.done");
		SWTUtils.setLocalizedToolTipText(btn_end, "main.done");

		Control ctrl = this.stack.topControl;
		if (ctrl instanceof StateComposite) {
			if (!ctrl.isDisposed()) {
				((StateComposite) ctrl).reloadResources();
				((StateComposite) ctrl).doLayout();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		this.shell = new Shell();
		getShell().setSize(this.stateMachine.configProvider.getMainWindowSize());
		if (System.getProperty("os.name").toLowerCase().contains("mac")) {
			if (System.getProperty("os.name").contains("OS X")) {
				hookupOSXMenu();
			}
		}
		try {
			Display display = Display.getCurrent();
			Monitor primary = display.getPrimaryMonitor();
			Rectangle bounds = primary.getBounds();
			Rectangle main = getShell().getBounds();
			getShell().setLocation(
					bounds.x + (bounds.width - main.width) / 2,
					bounds.y + (bounds.height - main.height) / 2);
		}
		catch (SWTError e) {
			log.debug("Cannot get display", e);
		}
		Display.setAppVersion(Constants.APP_VERSION);
		getShell().setText(Constants.APP_NAME);

		getShell().addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				log.debug("Closing main window");
				MainWindow.this.stateMachine.configProvider.setMainWindowSizePersistent(getShell().getSize());
				try {
					MainWindow.this.stateMachine.configProvider.saveToDisk();
				} catch (IOException e1) {
					log.error("Error saving configuration", e);
				}
				getShell().dispose();
			}
		});

		ImageData data = new ImageData(this.getClass().getResourceAsStream(Constants.RES_ICON));
		Image shellicon = new Image(getShell().getDisplay(), data);

		getShell().setImage(shellicon);

		getShell().setLayout(new FormLayout());

		this.mainbar = new Composite(getShell(), SWT.NONE);
		this.mainbar.setLayout(new FormLayout());
		this.mainBarFormData = new FormData();
		this.mainBarFormData.left = new FormAttachment(0, 10);
		this.mainBarFormData.right = new FormAttachment(100, -10);
		this.mainBarFormData.top = new FormAttachment(0, 10);
		this.mainBarFormData.bottom = new FormAttachment(0, Constants.MAINBAR_HEIGHT);
		this.mainbar.setLayoutData(this.mainBarFormData);

		this.btn_config = new MainBarRectangleButton(this.mainbar, SWT.NONE);
		SWTUtils.anchor(btn_config).bottom(100).right(0,50).top(0).left(0);
		SWTUtils.setLocalizedText(btn_config, "main.configuration");
		SWTUtils.setLocalizedToolTipText(btn_config, "main.configuration");
		this.btn_config.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				MainWindow.this.stateMachine
						.jumpToState(new ConfigurationUIState(
								MainWindow.this.stateMachine));
			}
		});
		this.buttonMap.put(Buttons.CONFIG, this.btn_config);

		InputStream is = this.getClass().getResourceAsStream(Constants.RES_IMG_CONFIG);
		((MainBarRectangleButton) this.btn_config).setEnabledImage(
				new Image(Display.getDefault(), new ImageData(is)));
		is = this.getClass().getResourceAsStream(Constants.RES_IMG_CONFIG_DISABLED);
		((MainBarRectangleButton) this.btn_config).setDisabledImage(
				new Image(Display.getDefault(), new ImageData(is)));

		Composite mainbarContainer = new Composite(this.mainbar, SWT.NONE);
		mainbarContainer.setLayout(new FormLayout());
		SWTUtils.anchor(mainbarContainer).left(btn_config).right(100).top(0).bottom(100);

		this.btn_open = new MainBarStartButton(mainbarContainer, SWT.NONE);
		SWTUtils.anchor(btn_open).left(0).right(27, (MainBarButton.SplitFactor / 2)).top(0).bottom(100);
		SWTUtils.setLocalizedText(btn_open, "common.open");
		SWTUtils.setLocalizedToolTipText(btn_open, "common.open");
		this.btn_open.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (MainWindow.this.stateMachine.status.getCurrentState() instanceof OpenState) {
					((OpenState) MainWindow.this.stateMachine.status.getCurrentState()).openFileDialog();
				} else {
					MainWindow.this.stateMachine.jumpToState(new OpenState(
							MainWindow.this.stateMachine));
				}
			}
		});
		this.buttonMap.put(Buttons.OPEN, this.btn_open);

		this.btn_position = new MainBarMiddleButton(mainbarContainer, SWT.NONE);
		SWTUtils.anchor(btn_position).left(27, -1 * (MainBarButton.SplitFactor / 2)).right(54, (MainBarButton.SplitFactor / 2)).top(0).bottom(100);
		SWTUtils.setLocalizedText(btn_position, "main.position");
		SWTUtils.setLocalizedToolTipText(btn_position, "main.position");
		this.btn_position.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				MainWindow.this.stateMachine.jumpToState(new PositioningState(
						MainWindow.this.stateMachine));
			}
		});
		this.buttonMap.put(Buttons.POSITION, this.btn_position);

		this.btn_sign = new MainBarMiddleButton(mainbarContainer, SWT.NONE);
		SWTUtils.anchor(btn_sign).left(54, -1 * (MainBarButton.SplitFactor / 2)).right(81, (MainBarButton.SplitFactor / 2)).top(0).bottom(100);
		SWTUtils.setLocalizedText(btn_sign, "main.signature");
		SWTUtils.setLocalizedToolTipText(btn_sign, "main.signature");
		this.btn_sign.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				MainWindow.this.stateMachine.jumpToState(new BKUSelectionState(
						MainWindow.this.stateMachine));
			}
		});
		this.buttonMap.put(Buttons.SIGN, this.btn_sign);

		this.btn_end = new MainBarEndButton(mainbarContainer, SWT.NONE);
		SWTUtils.anchor(btn_end).left(81, -1 * (MainBarButton.SplitFactor / 2)).right(100).top(0).bottom(100);
		SWTUtils.setLocalizedText(btn_end, "main.done");
		SWTUtils.setLocalizedToolTipText(btn_end, "main.done");
		this.buttonMap.put(Buttons.FINAL, this.btn_end);

		this.container = new Composite(getShell(), SWT.RESIZE);
		this.containerFormData = new FormData();
		this.containerFormData.bottom = new FormAttachment(100, -10);
		this.containerFormData.right = new FormAttachment(100, -10);
		this.containerFormData.top = new FormAttachment(0, Constants.MAINBAR_HEIGHT + 10);
		this.containerFormData.left = new FormAttachment(0, 10);
		this.container.setLayoutData(this.containerFormData);
		this.stack = new StackLayout();
		this.container.setLayout(this.stack);
	}

	/**
	 * Hook up SWT menu under OS X
	 */
	private void hookupOSXMenu() {
		log.debug("Hooking up OS X menu");
		CocoaUIEnhancer.hookApplicationMenu(
			getShell().getDisplay(),
			/* quitListener */
			(Event arg0) -> {
				this.stateMachine.exit();
			},
			/* aboutListener */
			(Event arg0) -> {
				Dialog dialog = new Dialog(getShell(),
						Messages.formatString("main.about", Constants.APP_NAME),
						Constants.APP_NAME_VERSION, BUTTONS.OK, ICON.INFORMATION);
				dialog.open();
			},
			/* preferencesListener */
			(Event arg0) -> {
				if (this.stateMachine.status.behavior.getEnabled(Buttons.CONFIG))
					this.stateMachine.jumpToState(new ConfigurationUIState(this.stateMachine));
			}
		);
	}

	/**
	 * Update MainWindow to fit new status
	 */
	public void applyBehavior() {
		MainWindowBehavior behavior = this.stateMachine.status.behavior;

		log.debug("Updating MainWindow state for : "
				+ this.stateMachine.status.getCurrentState().toString());

		for (Buttons button : Buttons.values()) {
			boolean active = behavior.getActive(button);
			boolean enabled = behavior.getEnabled(button);

			MainBarButton theButton = this.buttonMap.get(button);
			if (theButton != null) {
				theButton.setEnabled(enabled);
				theButton.setActive(active);
			}
		}

		if (behavior.getMainBarVisible()) {
			this.mainBarFormData.bottom = new FormAttachment(0, Constants.MAINBAR_HEIGHT);
			this.containerFormData.top = new FormAttachment(0, Constants.MAINBAR_HEIGHT + 10);
		} else {
			this.mainBarFormData.bottom = new FormAttachment(0, 0);
			this.containerFormData.top = new FormAttachment(0, 10);
		}

		getShell().getDisplay().update();
		this.mainbar.layout(true, true);
		this.mainbar.redraw();
	}

	/**
	 * @return the shell
	 */
	public Shell getShell() {
		return this.shell;
	}
}
