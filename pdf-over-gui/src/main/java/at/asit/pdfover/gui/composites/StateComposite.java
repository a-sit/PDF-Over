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

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
// Imports
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.states.State;

/**
 *	Composite interface for workflow state gui implementations
 */
public abstract class StateComposite extends Composite {

    /**
	 * Current State
	 */
	protected State state;

	/**
	 * The base class for state composites
	 *
	 * @param parent The parent Composite
	 * @param style The Composite style
	 * @param state The current State
	 */
	public StateComposite(Composite parent, int style, State state) {
		super(parent, style);
		this.state = state;
	}

	/**
	 * Performs layout for all children in composite
	 * (SWT layout(...) only layouts children no grandchildren!)
	 */
	public abstract void doLayout();

	/**
	 * Reloads the localizeable resources
	 */
	public abstract void reloadResources();

	public static void disableEventDefault(Control c, int event)
	{
		c.addListener(event, (Event e) -> { e.doit = false; });
	}

	public static void setLocalizedText(Label l, String messageKey) { l.setText(Messages.getString(messageKey)); l.requestLayout(); }
	public static void setLocalizedText(Label l, String formatMessageKey, Object... formatArgs) { l.setText(String.format(Messages.getString(formatMessageKey), formatArgs)); l.requestLayout(); }
	public static void setLocalizedText(Button b, String messageKey) { b.setText(Messages.getString(messageKey)); b.requestLayout(); }
	public static void setLocalizedText(Button b, String formatMessageKey, Object... formatArgs) { b.setText(String.format(Messages.getString(formatMessageKey), formatArgs)); b.requestLayout(); }

    public static void setFontHeight(Control c, int height)
    {
    	FontData[] fD = c.getFont().getFontData();
    	fD[0].setHeight(height);
		Font font = new Font(c.getDisplay(), fD[0]);
    	c.setFont(font);
    }

    public static void setFontStyle(Control c, int style)
    {
    	FontData[] fD = c.getFont().getFontData();
    	fD[0].setStyle(style);
		Font font = new Font(c.getDisplay(), fD[0]);
    	c.setFont(font);
    }

	public static class AnchorSetter
    {
    	private final Control c;
    	private final FormData fd = new FormData();
    	public AnchorSetter(Control c) { this.c = c; }

    	public void set() { this.c.setLayoutData(this.fd); }

    	public AnchorSetter height(int h) { fd.height = h; return this; }
    	public AnchorSetter width(int w) { fd.width = w; return this; }

    	public AnchorSetter top(FormAttachment a) { fd.top = a; return this; }
    	public AnchorSetter left(FormAttachment a) { fd.left = a; return this; }
    	public AnchorSetter right(FormAttachment a) { fd.right = a; return this; }
    	public AnchorSetter bottom(FormAttachment a) { fd.bottom = a; return this; }

		public AnchorSetter top(Control control, int offset, int alignment) { return top(new FormAttachment(control, offset, alignment)); }
    	public AnchorSetter top(Control control, int offset) { return top(new FormAttachment(control, offset)); }
		public AnchorSetter top(Control control) { return top(new FormAttachment(control)); }
    	public AnchorSetter top(int num, int offset) { return top(new FormAttachment(num, offset)); }
    	public AnchorSetter top(int num) { return top(new FormAttachment(num)); }

		public AnchorSetter left(Control control, int offset, int alignment) { return left(new FormAttachment(control, offset, alignment)); }
    	public AnchorSetter left(Control control, int offset) { return left(new FormAttachment(control, offset)); }
		public AnchorSetter left(Control control) { return left(new FormAttachment(control)); }
    	public AnchorSetter left(int num, int offset) { return left(new FormAttachment(num, offset)); }
    	public AnchorSetter left(int num) { return left(new FormAttachment(num)); }

		public AnchorSetter right(Control control, int offset, int alignment) { return right(new FormAttachment(control, offset, alignment)); }
    	public AnchorSetter right(Control control, int offset) { return right(new FormAttachment(control, offset)); }
		public AnchorSetter right(Control control) { return right(new FormAttachment(control)); }
    	public AnchorSetter right(int num, int offset) { return right(new FormAttachment(num, offset)); }
    	public AnchorSetter right(int num) { return right(new FormAttachment(num)); }

		public AnchorSetter bottom(Control control, int offset, int alignment) { return bottom(new FormAttachment(control, offset, alignment)); }
    	public AnchorSetter bottom(Control control, int offset) { return bottom(new FormAttachment(control, offset)); }
		public AnchorSetter bottom(Control control) { return bottom(new FormAttachment(control)); }
    	public AnchorSetter bottom(int num, int offset) { return bottom(new FormAttachment(num, offset)); }
    	public AnchorSetter bottom(int num) { return bottom(new FormAttachment(num)); }
    }
    public static AnchorSetter anchor(Control c) { return new AnchorSetter(c); }
}
