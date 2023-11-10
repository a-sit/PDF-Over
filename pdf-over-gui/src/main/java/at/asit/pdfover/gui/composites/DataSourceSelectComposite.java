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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.utils.SWTUtils;
import at.asit.pdfover.gui.workflow.states.State;

/**
 * Composite for input document selection
 */
public class DataSourceSelectComposite extends StateComposite {

	/**
	 * Open the input document selection dialog
	 */
	public void openFileDialog() {
		FileDialog dialog = new FileDialog(
				DataSourceSelectComposite.this.getShell(), SWT.OPEN | SWT.MULTI);
		dialog.setFilterExtensions(new String[] { "*.pdf", "*" });
		dialog.setFilterNames(new String[] {
				Messages.getString("common.PDFExtension_Description"),
				Messages.getString("common.AllExtension_Description") });
		
		String targetDir = this.state.getConfig().getLastOpenedDirectory();
		if (targetDir != null)
			dialog.setFilterPath(targetDir);
		
		dialog.open();
		this.setSelected(dialog.getFilterPath(), dialog.getFileNames());
	}

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(DataSourceSelectComposite.class);

	/**
	 * Set this value through the setter method!!
	 */
	final private List<File> selected = new ArrayList<>();

	public void setSelected(@CheckForNull String basePath, String[] fileNames) {
		this.selected.clear();
		if (fileNames != null) {
			for (String fileName : fileNames) {
				File file = new File(basePath, fileName);
				if (file.exists()) {
					this.selected.add(file);
				}
			}
		}
		this.state.updateStateMachine();
	}

	/**
	 * Gets the selected file
	 *
	 * @return the selected file
	 */
	public List<File> getSelected() {
		return this.selected;
	}

	void MarkDragEnter() {
		this.backgroundColor = this.activeBackground;
		this.borderColor = this.activeBorder;
		this.redrawDrop();
	}

	void MarkDragLeave() {
		this.backgroundColor = this.inactiveBackground;
		this.borderColor = this.inactiveBorder;
		this.redrawDrop();
	}

	void redrawDrop() {
		this.lbl_drag.setBackground(this.backgroundColor);
		this.lbl_drag2.setBackground(this.backgroundColor);
		this.btn_open.setBackground(this.backgroundColor);
		this.drop_area.redraw();
		this.drop_area.layout(true, true);
	}

	Color activeBackground;
	Color inactiveBackground;
	Color inactiveBorder;
	Color activeBorder;
	Color borderColor;
	Color backgroundColor;

	/**
	 * Create the composite.
	 *
	 * @param parent
	 * @param style
	 * @param state
	 */
	public DataSourceSelectComposite(Composite parent, int style, State state) {
		super(parent, style, state);

		this.activeBackground = Constants.MAINBAR_ACTIVE_BACK_LIGHT;
		this.inactiveBackground = this.getBackground();//Constants.MAINBAR_INACTIVE_BACK;
		this.inactiveBorder = Constants.MAINBAR_ACTIVE_BACK_LIGHT;
		this.activeBorder = Constants.MAINBAR_ACTIVE_BACK_DARK;
		this.backgroundColor = this.inactiveBackground;
		this.borderColor = Constants.DROP_BORDER_COLOR;

		this.setLayout(new FormLayout());

		// Color back = new Color(Display.getCurrent(), 77, 190, 250);

		this.drop_area = new Composite(this, SWT.RESIZE);
		SWTUtils.anchor(drop_area).left(0, 30).right(100, -30).top(0, 30).bottom(100, -30);
		this.drop_area.setLayout(new FormLayout());

		this.drop_area.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				Rectangle clientArea = DataSourceSelectComposite.this
						.drop_area.getClientArea();

				//e.gc.setForeground(new Color(getDisplay(),0x6B, 0xA5, 0xD9));
				e.gc.setForeground(DataSourceSelectComposite.this.borderColor);
				e.gc.setLineWidth(3);
				e.gc.setLineStyle(SWT.LINE_DASH);
				e.gc.setBackground(DataSourceSelectComposite.this.backgroundColor);
				e.gc.fillRoundRectangle(clientArea.x,
						clientArea.y, clientArea.width - 2, clientArea.height - 2,
						10, 10);
				e.gc.drawRoundRectangle(clientArea.x,
						clientArea.y, clientArea.width - 2, clientArea.height - 2,
						10, 10);
			}
		});

		DropTarget dnd_target = new DropTarget(this.drop_area, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
		final FileTransfer fileTransfer = FileTransfer.getInstance();
		Transfer[] types = new Transfer[] { fileTransfer };
		dnd_target.setTransfer(types);

		dnd_target.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				if (fileTransfer.isSupportedType(event.currentDataType)) {
					if (event.data == null) {
						log.error("Dropped file name was null");
						return;
					}
					DataSourceSelectComposite.this.setSelected(null, (String[])event.data);
				}
			}

			@Override
			public void dragOperationChanged(DropTargetEvent event) {
				event.detail = DND.DROP_COPY;
			}

			@Override
			public void dragEnter(DropTargetEvent event) {
				// only accept transferable files
				for (int i = 0; i < event.dataTypes.length; i++) {
					if (fileTransfer.isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
						event.detail = DND.DROP_COPY;
						MarkDragEnter();
						return;
					}
				}
				event.detail = DND.DROP_NONE;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.swt.dnd.DropTargetAdapter#dragLeave(org.eclipse.swt.dnd.DropTargetEvent)
			 */
			@Override
			public void dragLeave(DropTargetEvent event) {
				MarkDragLeave();
				super.dragLeave(event);
			}
		});

		this.lbl_drag = new Label(this.drop_area, SWT.NONE | SWT.RESIZE );
		this.lbl_drag2 = new Label(this.drop_area, SWT.NONE | SWT.RESIZE );
		SWTUtils.anchor(lbl_drag).left(0, 10).right(100, -10).bottom(lbl_drag2, -10);
		SWTUtils.anchor(lbl_drag2).left(0, 10).right(100, -10).top(50, -10);
		SWTUtils.setFontHeight(lbl_drag, Constants.TEXT_SIZE_BIG);
		SWTUtils.setFontHeight(lbl_drag2, Constants.TEXT_SIZE_NORMAL);
		this.lbl_drag.setAlignment(SWT.CENTER);
		this.lbl_drag2.setAlignment(SWT.CENTER);

		this.btn_open = new Button(this.drop_area, SWT.NATIVE | SWT.RESIZE);
		SWTUtils.anchor(btn_open).left(lbl_drag2, 0, SWT.CENTER).top(lbl_drag2, 10);
		SWTUtils.setFontHeight(btn_open, Constants.TEXT_SIZE_BUTTON);

		reloadResources();

		SWTUtils.addSelectionListener(btn_open, (e) -> openFileDialog());
		this.drop_area.pack();
		this.redrawDrop();
	}

	Composite drop_area;

	private Label lbl_drag2;

	private Label lbl_drag;

	private Button btn_open;

	@Override
	public void onDisplay() {
		this.drop_area.layout(true, true);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		SWTUtils.setLocalizedText(lbl_drag, "dataSourceSelection.DropLabel");
		SWTUtils.setLocalizedText(lbl_drag2, "dataSourceSelection.DropLabel2");
		SWTUtils.setLocalizedText(btn_open, "dataSourceSelection.browse");
	}
}
