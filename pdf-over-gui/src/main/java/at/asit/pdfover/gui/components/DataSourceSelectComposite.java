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
package at.asit.pdfover.gui.components;

// Imports
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.SWT;

import at.asit.pdfover.gui.workflow.Workflow;

/**
 * 
 *
 */
public class DataSourceSelectComposite extends Composite implements StateComposite {

	/**
	 * SFL4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(DataSourceSelectComposite.class);

	private Workflow workflow;
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 * @param flow 
	 */
	public DataSourceSelectComposite(Composite parent, int style, Workflow flow) {
		super(parent, style);
		
		this.workflow = flow;
		
		this.setLayout(new FormLayout());
		
		Color back = new Color(Display.getCurrent(), 77, 190, 250);
		
		this.drop_area = new Composite(this, SWT.RESIZE | SWT.BORDER_DASH);
		FormData fd_drop_area = new FormData();
		fd_drop_area.left = new FormAttachment(0, 0);
		fd_drop_area.right = new FormAttachment(100, 0);
		fd_drop_area.top = new FormAttachment(0, 0);
		fd_drop_area.bottom = new FormAttachment(100, 0);
		this.drop_area.setLayoutData(fd_drop_area);
		this.drop_area.setLayout(new FormLayout());
		this.drop_area.setBackground(back);
		
		final Label lbl_drag = new Label(this.drop_area, SWT.NONE | SWT.RESIZE );
		FormData fd_lbl_drag = new FormData();
		fd_lbl_drag.left = new FormAttachment(5, 5);
		fd_lbl_drag.right = new FormAttachment(100, -5);
		fd_lbl_drag.top = new FormAttachment(5, 5);
		fd_lbl_drag.bottom = new FormAttachment(55, -5);
		lbl_drag.setLayoutData(fd_lbl_drag);
		FontData[] fD = lbl_drag.getFont().getFontData();
		fD[0].setHeight(18);
		lbl_drag.setFont( new Font(Display.getCurrent(),fD[0]));
		lbl_drag.setText("Drag and Drop");
		lbl_drag.setAlignment(SWT.CENTER);
		lbl_drag.setBackground(back);
		
		
		Button btn_open = new Button(this.drop_area, SWT.NATIVE | SWT.RESIZE );
		btn_open.setText("Choose file ...");
		Point size = btn_open.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		FormData fd_btn_open = new FormData();
		fd_btn_open.left = new FormAttachment(100, size.x * -1 - 10);
		fd_btn_open.right = new FormAttachment(100, -5);
		fd_btn_open.top = new FormAttachment(100, size.y * -1 - 10);
		fd_btn_open.bottom = new FormAttachment(100, -5);
		btn_open.setLayoutData(fd_btn_open);
		btn_open.setBackground(back);
		
		this.drop_area.pack();
		
		/*Button btn = new Button(this, SWT.NATIVE);
		btn.setBounds(50, 20, 100, 50);
		btn.setText("Click Me");
		btn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				DataSourceSelectComposite.this.setPress(true);
				DataSourceSelectComposite.this.workflow.update();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});*/
	}
	
	private boolean press = false;

	private Composite drop_area;

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/**
	 * @return the press
	 */
	public boolean isPress() {
		return this.press;
	}

	/**
	 * @param press the press to set
	 */
	public void setPress(boolean press) {
		this.press = press;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.components.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		this.layout(true, true);
		this.drop_area.layout(true, true);
	}
}
