package at.asit.pdfover.gui.utils;

import java.awt.Desktop;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

import at.asit.pdfover.commons.Messages;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SWTUtils {

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
    	} catch (NoSuchMethodException | IllegalAccessException expected) {
    		// do nothing, this may not exist on every control we use
    	} catch (InvocationTargetException e) {
    		log.error("Failed to re-layout {}", swtObj.getClass().getSimpleName(), e);
    	}
    }
    public static void setLocalizedText(Object o, String messageKey) { genericSetText(o, Messages.getString(messageKey)); }
    public static void setLocalizedText(Object o, String formatMessageKey, Object... formatArgs) { genericSetText(o, Messages.formatString(formatMessageKey, formatArgs)); }

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

    public static void scrollPassthrough(Control c) {
    	c.addListener(SWT.MouseVerticalWheel, (Event e) -> {
			// disable default handling
			e.doit = false;

			// find containing ScrolledComposite
			Composite target = c.getParent();
			while ((target != null) && !(target instanceof ScrolledComposite))
				target = target.getParent();
			
			if (target == null)
				return;
			
			// scroll containing ScrolledComposite
			ScrolledComposite sTarget = (ScrolledComposite)target;
			Point origin = sTarget.getOrigin();
			origin.y -= (e.count * 10);
			sTarget.setOrigin(origin);
		});
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
			log.error("Attempted to pass object of type {} to addSelectionListener; object does not have an accessible addSelectionListener method", swtObj.getClass().getSimpleName(), e);
		} catch (InvocationTargetException e) {
			log.error("Failed to add selection listener on object of type {}", swtObj.getClass().getSimpleName(), e);
		}
	}

	/**
	 * @see SWTUtils#addSelectionListener(Object, Consumer)
	 */
	public static void addSelectionListener(Object swtObj, Runnable callback) {
		addSelectionListener(swtObj, (e) -> { callback.run(); });
	}

	/**
	 * functional-interface wrapper around swtObj.addMouseListener
	 * @param swtObj SWT widget supporting addMouseListener
	 * @param callback mouseDown method
	 */
	public static void addMouseDownListener(Object swtObj, Consumer<MouseEvent> callback) {
		try {
			Method m = swtObj.getClass().getMethod("addMouseListener", MouseListener.class);
			m.invoke(swtObj, new MouseAdapter() { @Override public void mouseDown (MouseEvent e) { callback.accept(e); } });
		} catch (NoSuchMethodException | IllegalAccessException e) {
			log.error("Attempted to pass object of type {} to addMouseDownListener; object does not have an accessible addMouseListener method", swtObj.getClass().getSimpleName(), e);
		} catch (InvocationTargetException e) {
			log.error("Failed to add mouse-down listener on object of type {}", swtObj.getClass().getSimpleName(), e);
		}
	}

	/**
	 * @see SWTUtils#addMouseDownListener(Object, Consumer)
	 */
	public static void addMouseDownListener(Object swtObj, Runnable callback) {
		addMouseDownListener(swtObj, (e) -> { callback.run(); });
	}

	/**
	 * functional-interface wrapper around swtObj.addMouseListener
	 * @param swtObj SWT widget supporting addMouseListener
	 * @param callback mouseDown method
	 */
	public static void addMouseUpListener(Object swtObj, Consumer<MouseEvent> callback) {
		try {
			Method m = swtObj.getClass().getMethod("addMouseListener", MouseListener.class);
			m.invoke(swtObj, new MouseAdapter() { @Override public void mouseUp (MouseEvent e) { callback.accept(e); } });
		} catch (NoSuchMethodException | IllegalAccessException e) {
			log.error("Attempted to pass object of type {} to addMouseUpListener; object does not have an accessible addMouseListener method", swtObj.getClass().getSimpleName(), e);
		} catch (InvocationTargetException e) {
			log.error("Failed to add mouse-up listener on object of type {}", swtObj.getClass().getSimpleName(), e);
		}
	}

	/**
	 * @see SWTUtils#addMouseUpListener(Object, Consumer)
	 */
	public static void addMouseUpListener(Object swtObj, Runnable callback) {
		addMouseUpListener(swtObj, (e) -> { callback.run(); });
	}
	
	/**
	 * functional-interface wrapper around swtObj.addFocusListener
	 * @param swtObj SWT widget supporting addFocusListener
	 * @param callback focusGained method
	 */
	public static void addFocusGainedListener(Object swtObj, Consumer<FocusEvent> callback) {
		try {
			Method m = swtObj.getClass().getMethod("addFocusListener", FocusListener.class);
			m.invoke(swtObj, new FocusAdapter() { @Override public void focusGained(FocusEvent e) { callback.accept(e); } });
		} catch (NoSuchMethodException | IllegalAccessException e) {
			log.error("Attempted to pass object of type {} to addFocusGainedListener; object does not have an accessible addFocusListener method", swtObj.getClass().getSimpleName(), e);
		} catch (InvocationTargetException e) {
			log.error("Failed to add focus gained listener on object of type {}", swtObj.getClass().getSimpleName(), e);
		}
	}

	/**
	 * @see SWTUtils#addFocusGainedListener(Object, Consumer)
	 */
	public static void addFocusGainedListener(Object swtObj, Runnable callback) {
		addFocusGainedListener(swtObj, (e) -> { callback.run(); });
	}

	public static void openURL(URI uri) {
		try {
			if (uri == null) return;
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(uri);
			} else {
				Program.launch(uri.toURL().toExternalForm());
			}
		} catch (Exception e) {
			log.warn("Failed to open URI: {}", uri, e);
		}
	}

	public static void openURL(String uri) {
		if (uri == null) return;
		try {
			openURL(new URI(uri));
		} catch (URISyntaxException e) {
			log.warn("Failed to open URI: {}", uri, e);
		}
	}
}
