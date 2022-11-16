/*
 * Copyright 2014 by A-SIT, Secure Information Technology Center Austria
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

package at.asit.pdfover.gui.osx;

import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.C;
import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;

/**
 * Provide a hook to connecting the Preferences, About and Quit menu items of
 * the Mac OS X Application menu when using the SWT Cocoa bindings.
 * <p>
 * This code does not require the Cocoa SWT JAR in order to be compiled as it
 * uses reflection to access the Cocoa specific API methods. Use SWT Listeners
 * instead in order to use this class in SWT only applications.
 *
 * </p>
 * <p>
 * This code was influenced by the <a
 * href="http://www.transparentech.com/opensource/cocoauienhancer"
 * >CocoaUIEnhancer - Connect the About, Preferences and Quit menus in Mac OS X
 * Cocoa SWT and JFace applications</a>.
 * </p>
 * <p>
 * This class works with both the 32-bit and 64-bit versions of the SWT Cocoa
 * bindings.
 * <p>
 * <p>
 * This class is released under the Eclipse Public License (<a
 * href="http://www.eclipse.org/legal/epl-v10.html">EPL</a>).
 */
public class CocoaUIEnhancer {
	static final Logger log = LoggerFactory.getLogger(CocoaUIEnhancer.class);

	private static final long kAboutMenuItem = 0;
	private static final long kPreferencesMenuItem = 2;
	// private static final long kServicesMenuItem = 4;
	private static final long kHideApplicationMenuItem = 6;
	private static final long kQuitMenuItem = 10;

	static long sel_toolbarButtonClicked_;
	static long sel_preferencesMenuItemSelected_;
	static long sel_aboutMenuItemSelected_;
	static long sel_hideApplicationMenuItemSelected_;

	static Callback proc3Args;

	/**
	 * Class invoked via the Callback object to run the about and preferences
	 * actions.
	 */
	private static class MenuHookObject {

		final Listener about;
		final Listener pref;

		public MenuHookObject(final Listener aboutListener,
				final Listener preferencesListener) {
			this.about = aboutListener;
			this.pref = preferencesListener;
		}

		/**
		 * Will be called on 32bit SWT.
		 * @param id
		 * @param sel
		 * @param arg0
		 * @return x
		 */
		@SuppressWarnings("unused")
		public int actionProc(final int id, final int sel, final int arg0) {
			return (int) this.actionProc((long) id, (long) sel, (long) arg0);
		}

		/**
		 * Will be called on 64bit SWT.
		 * @param id
		 * @param sel
		 * @param arg0
		 * @return x
		 */
		public long actionProc(final long id, final long sel, final long arg0) {
			if (sel == sel_aboutMenuItemSelected_) {
				if (log.isDebugEnabled()) {
					log.debug("[MenuHookObject - actionProc] : About");
					this.about.handleEvent(null);
				}
			} else if (sel == sel_preferencesMenuItemSelected_) {
				if (log.isDebugEnabled()) {
					log.debug("[MenuHookObject - actionProc] : Preferences");
				}
				this.pref.handleEvent(null);

			} else {
				if (log.isDebugEnabled()) {
					log.debug("[MenuHookObject - actionProc] : Unknow selection!");
				}
			}
			// Return value is not used.
			return 99;
		}

		// Getters and setters
		@SuppressWarnings("unused")
		public Listener getAbout() {
			return this.about;
		}

		@SuppressWarnings("unused")
		public Listener getPref() {
			return this.pref;
		}
	}

	/**
	 * Hook the given Listener to the Mac OS X application Quit menu and the
	 * IActions to the About and Preferences menus.
	 *
	 * @param display
	 *            The Display to use.
	 * @param quitListener
	 *            The listener to invoke when the Quit menu is invoked.
	 * @param aboutListener
	 *            The listener to invoke when the About menu is invoked.
	 * @param preferencesListener
	 *            The listener to invoke when the Preferences menu is invoked.
	 */
	public static void hookApplicationMenu(final Display display,
			final Listener quitListener, final Listener aboutListener,
			final Listener preferencesListener) {
		// This is our callbackObject whose 'actionProc' method will be called
		// when the About or
		// Preferences menuItem is invoked.
		final MenuHookObject target = new MenuHookObject(aboutListener,
				preferencesListener);

		try {
			// Initialize the menuItems.
			initialize(target);
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}

		// Connect the quit/exit menu.
		if (!display.isDisposed()) {
			display.addListener(SWT.Close, quitListener);
		}

		// Schedule disposal of callback object
		display.disposeExec(() -> {
			CocoaUIEnhancer.invoke(proc3Args, "dispose");
		});
	}

	private static void initialize(final Object callbackObject) throws Exception {

		final Class<?> osCls = classForName("org.eclipse.swt.internal.cocoa.OS");

		// Register names in objective-c.
		if (sel_toolbarButtonClicked_ == 0) {
			// sel_toolbarButtonClicked_ = registerName( osCls, "toolbarButtonClicked:" );
			sel_preferencesMenuItemSelected_ = registerName(osCls,
					"preferencesMenuItemSelected:");
			sel_aboutMenuItemSelected_ = registerName(osCls,
					"aboutMenuItemSelected:");
		}

		// Create an SWT Callback object that will invoke the actionProc method
		// of our internal
		// callbackObject.
		proc3Args = new Callback(callbackObject, "actionProc", 3);
		final Method getAddress = Callback.class.getMethod("getAddress",
				new Class[0]);
		Object object = getAddress.invoke(proc3Args, (Object[]) null);
		final long proc3 = convertToLong(object);
		if (proc3 == 0) {
			SWT.error(SWT.ERROR_NO_MORE_CALLBACKS);
		}

		final Class<?> nsmenuCls = classForName("org.eclipse.swt.internal.cocoa.NSMenu");
		final Class<?> nsmenuitemCls = classForName("org.eclipse.swt.internal.cocoa.NSMenuItem");
		final Class<?> nsstringCls = classForName("org.eclipse.swt.internal.cocoa.NSString");
		final Class<?> nsapplicationCls = classForName("org.eclipse.swt.internal.cocoa.NSApplication");

		// Instead of creating a new delegate class in objective-c,
		// just use the current SWTApplicationDelegate. An instance of this
		// is a field of the Cocoa Display object and is already the target
		// for the menuItems. So just get this class and add the new methods
		// to it.
		object = invoke(osCls, "objc_lookUpClass",
				new Object[] { "SWTApplicationDelegate" });
		final long cls = convertToLong(object);

		// Add the action callbacks for Preferences and About menu items.
		invoke(osCls, "class_addMethod", new Object[] { wrapPointer(cls),
				wrapPointer(sel_preferencesMenuItemSelected_),
				wrapPointer(proc3), "@:@" });
		invoke(osCls, "class_addMethod", new Object[] { wrapPointer(cls),
				wrapPointer(sel_aboutMenuItemSelected_), wrapPointer(proc3),
				"@:@" });

		// Get the Mac OS X Application menu.
		final Object sharedApplication = invoke(nsapplicationCls,
				"sharedApplication");
		final Object mainMenu = invoke(sharedApplication, "mainMenu");
		final Object mainMenuItem = invoke(nsmenuCls, mainMenu, "itemAtIndex",
				new Object[] { wrapPointer(0) });
		final Object appMenu = invoke(mainMenuItem, "submenu");

		// Create the About <application-name> menu command
		final Object aboutMenuItem = invoke(nsmenuCls, appMenu, "itemAtIndex",
				new Object[] { wrapPointer(kAboutMenuItem) });
		final Object nsStrAbout = invoke(nsstringCls, "stringWith",
				new Object[] { Messages.formatString("main.about", Constants.APP_NAME) });
		invoke(nsmenuitemCls, aboutMenuItem, "setTitle",
				new Object[] { nsStrAbout });
		// Rename the quit action.
		final Object quitMenuItem = invoke(nsmenuCls, appMenu,
				"itemAtIndex", new Object[] { wrapPointer(kQuitMenuItem) });
		final Object nsStrQuit = invoke(nsstringCls, "stringWith",
				new Object[] { Messages.formatString("main.quit", Constants.APP_NAME) });
		invoke(nsmenuitemCls, quitMenuItem, "setTitle",
				new Object[] { nsStrQuit });

		// Rename the hide action.
		final Object hideMenuItem = invoke(nsmenuCls, appMenu,
				"itemAtIndex",
				new Object[] { wrapPointer(kHideApplicationMenuItem) });
		final Object nsStrHide = invoke(nsstringCls, "stringWith",
				new Object[] { Messages.formatString("main.hide", Constants.APP_NAME) });
		invoke(nsmenuitemCls, hideMenuItem, "setTitle",
				new Object[] { nsStrHide });

		// Enable the Preferences menuItem.
		final Object prefMenuItem = invoke(nsmenuCls, appMenu, "itemAtIndex",
				new Object[] { wrapPointer(kPreferencesMenuItem) });
		invoke(nsmenuitemCls, prefMenuItem, "setEnabled", new Object[] { true });

		// Set the action to execute when the About or Preferences menuItem is
		// invoked.

		// We don't need to set the target here as the current target is the
		// SWTApplicationDelegate
		// and we have registerd the new selectors on it. So just set the new
		// action to invoke the
		// selector.
		invoke(nsmenuitemCls, prefMenuItem, "setAction",
				new Object[] { wrapPointer(sel_preferencesMenuItemSelected_) });
		invoke(nsmenuitemCls, aboutMenuItem, "setAction",
				new Object[] { wrapPointer(sel_aboutMenuItemSelected_) });
	}

	private static long registerName(final Class<?> osCls, final String name)
			throws IllegalArgumentException, SecurityException {
		final Object object = invoke(osCls, "sel_registerName",
				new Object[] { name });
		return convertToLong(object);
	}

	private static long convertToLong(final Object object) {
		if (object instanceof Integer) {
			final Integer i = (Integer) object;
			return i.longValue();
		}
		if (object instanceof Long) {
			final Long l = (Long) object;
			return l.longValue();
		}
		return 0;
	}

	private static Object wrapPointer(final long value) {
		final Class<?> PTR_CLASS = C.PTR_SIZEOF == 8 ? long.class : int.class;
		if (PTR_CLASS == long.class) {
			return Long.valueOf(value);
		}
		return Integer.valueOf((int) value);
	}

	private static Object invoke(final Class<?> clazz, final String methodName,
			final Object[] args) {
		return invoke(clazz, null, methodName, args);
	}

	private static Object invoke(final Class<?> clazz, final Object target,
			final String methodName, final Object[] args) {
		try {
			final Class<?>[] signature = new Class<?>[args.length];
			for (int i = 0; i < args.length; i++) {
				final Class<?> thisClass = args[i].getClass();
				if (thisClass == Integer.class) {
					signature[i] = int.class;
				} else if (thisClass == Long.class) {
					signature[i] = long.class;
				} else if (thisClass == Byte.class) {
					signature[i] = byte.class;
				} else if (thisClass == Boolean.class) {
					signature[i] = boolean.class;
				} else {
					signature[i] = thisClass;
				}
			}
			final Method method = clazz.getMethod(methodName, signature);
			return method.invoke(target, args);
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static Class<?> classForName(final String classname) {
		try {
			final Class<?> cls = Class.forName(classname);
			return cls;
		} catch (final ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	private static Object invoke(final Class<?> cls, final String methodName) {
		return invoke(cls, methodName, (Class<?>[]) null, (Object[]) null);
	}

	private static Object invoke(final Class<?> cls, final String methodName,
			final Class<?>[] paramTypes, final Object... arguments) {
		try {
			final Method m = cls.getDeclaredMethod(methodName, paramTypes);
			return m.invoke(null, arguments);
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}

	static Object invoke(final Object obj, final String methodName) {
		return invoke(obj, methodName, (Class<?>[]) null, (Object[]) null);
	}

	private static Object invoke(final Object obj, final String methodName,
			final Class<?>[] paramTypes, final Object... arguments) {
		try {
			final Method m = obj.getClass().getDeclaredMethod(methodName,
					paramTypes);
			return m.invoke(obj, arguments);
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
