package at.asit.pdfover.gui.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Messages;

public final class SWTUtils {

    private static final Logger log = LoggerFactory.getLogger(SWTUtils.class);

    /* oh how i miss C++, and compile-time safety; this would be so much cleaner as a templated function */
    private static void genericSetText(Object swtObj, String text) {
    	try {
    		Method m = swtObj.getClass().getMethod("setText", String.class);
    		m.invoke(swtObj, text);
    	} catch (NoSuchMethodException | IllegalAccessException e) {
    		log.error("Attempted to setLocalizedText on object of type {}, which does not have an accessible setText method", swtObj.getClass().getSimpleName(), e);
    	} catch (InvocationTargetException e) {
    		log.error("Failed to setLocalizedText on object of type {}", swtObj.getClass().getSimpleName(), e);
    	}
    
    	try {
    		// request re-layout if possible, changing the text content will change the bounding box
    		Method m = swtObj.getClass().getMethod("requestLayout");
    		m.invoke(swtObj);
    	} catch (NoSuchMethodException | IllegalAccessException e) {
    		// do nothing, this may not exist on every control we use
    	} catch (InvocationTargetException e) {
    		log.error("Failed to re-layout {}", swtObj.getClass().getSimpleName(), e);
    	}
    }
    public static void setLocalizedText(Object o, String messageKey) { genericSetText(o, Messages.getString(messageKey)); }
    public static void setLocalizedText(Object o, String formatMessageKey, Object... formatArgs) { genericSetText(o, String.format(Messages.getString(formatMessageKey), formatArgs)); }

	private static void genericSetToolTipText(Object swtObj, String text) {
		try {
			Method m = swtObj.getClass().getMethod("setToolTipText", String.class);
			m.invoke(swtObj, text);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			log.error("Attempted to setLocalizedToolTipText on object of type {}, which does not have an accessible setToolTipText method", swtObj.getClass().getSimpleName(), e);
		} catch (InvocationTargetException e) {
			log.error("Failed to setLocalizedToolTipText on object of type {}", swtObj.getClass().getSimpleName(), e);
		}
	}
	public static void setLocalizedToolTipText(Object o, String messageKey) { genericSetToolTipText(o, Messages.getString(messageKey));}

    public static void disableEventDefault(Control c, int event) {
    	c.addListener(event, (Event e) -> { e.doit = false; });
    }

    public static void setFontHeight(Control c, int height) {
    	FontData[] fD = c.getFont().getFontData();
    	fD[0].setHeight(height);
    	Font font = new Font(c.getDisplay(), fD[0]);
    	c.setFont(font);
    }

    public static void setFontStyle(Control c, int style) {
    	FontData[] fD = c.getFont().getFontData();
    	fD[0].setStyle(style);
    	Font font = new Font(c.getDisplay(), fD[0]);
    	c.setFont(font);
    }

    public static class AnchorSetter {
    	private final Control c;
    	private final FormData fd;
    	private AnchorSetter(Control c, boolean isNew)
		{
			this.c = c;
			if (isNew) {
				this.fd = new FormData();
				this.c.setLayoutData(this.fd);
			} else {
				Object layoutData = this.c.getLayoutData();
				try {
					this.fd = (FormData)layoutData;
				} catch (ClassCastException e) {
					log.error("Tried to reanchor() object with layout data of type {} (not FormData)", layoutData.getClass().getSimpleName(), e);
					throw new RuntimeException("Invalid reanchor() use");
				}
			}
		}
    
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
    public static AnchorSetter anchor(Control c) { return new AnchorSetter(c, true); }
	public static AnchorSetter reanchor(Control c) { return new AnchorSetter(c, false); }
    
	/**
	 * functional-interface wrapper around swtObj.addSelectionListener
	 * @param swtObj SWT widget supporting addSelectionListener
	 * @param callback widgetSelected method
	 */
	public static void addSelectionListener(Object swtObj, Consumer<SelectionEvent> callback) {
		try {
			Method m = swtObj.getClass().getMethod("addSelectionListener", SelectionListener.class);
			m.invoke(swtObj, new SelectionAdapter() { @Override public void widgetSelected(SelectionEvent e) { callback.accept(e); } });
		} catch (NoSuchMethodException | IllegalAccessException e) {
			log.error("Attempted to pass object of type {} to onSelectionChanged; object does not have an accessible addSelectionListener method", swtObj.getClass().getSimpleName(), e);
		} catch (InvocationTargetException e) {
			log.error("Failed to add selection listener on object of type {}", swtObj.getClass().getSimpleName(), e);
		}
	}
}
