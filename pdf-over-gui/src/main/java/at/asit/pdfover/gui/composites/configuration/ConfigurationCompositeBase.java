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
package at.asit.pdfover.gui.composites.configuration;


import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import at.asit.pdfover.gui.composites.StateComposite;
import at.asit.pdfover.gui.workflow.PDFSigner;
import at.asit.pdfover.gui.workflow.config.ConfigManipulator;
import at.asit.pdfover.gui.workflow.config.ConfigurationContainer;
import at.asit.pdfover.gui.workflow.config.PersistentConfigProvider;
import at.asit.pdfover.gui.workflow.states.State;

/**
 * Base class for configuration composites
 */
public abstract class ConfigurationCompositeBase extends StateComposite {
	
	/**
	 * the configuration container
	 */
	protected ConfigurationContainer configurationContainer;
	
	/**
	 * The PDF Signer used to produce signature block preview
	 */
	protected PDFSigner signer;
	
	/**
	 * @return the signer
	 */
	public PDFSigner getSigner() {
		return this.signer;
	}

	/**
	 * @param signer the signer to set
	 */
	public void setSigner(PDFSigner signer) {
		this.signer = signer;
		this.signerChanged();
	}

	/**
	 * Called when the signer is changed!
	 */
	protected abstract void signerChanged();

	protected static void setFontHeight(Control c, int height)
	{
		FontData[] fD = c.getFont().getFontData();
		fD[0].setHeight(height);
		c.setFont(new Font(Display.getCurrent(), fD[0]));
	}

	protected static class AnchorSetter
	{
		private final Control c;
		private final FormData fd = new FormData();
		private AnchorSetter(Control c) { this.c = c; }

		public void set() { this.c.setLayoutData(this.fd); }

		public AnchorSetter height(int h) { fd.height = h; return this; }
		public AnchorSetter width(int w) { fd.width = w; return this; }

		public AnchorSetter top(FormAttachment a) { fd.top = a; return this; }
		public AnchorSetter left(FormAttachment a) { fd.left = a; return this; }
		public AnchorSetter right(FormAttachment a) { fd.right = a; return this; }
		public AnchorSetter bottom(FormAttachment a) { fd.bottom = a; return this; }

		public AnchorSetter top(Control control, int offset) { return top(new FormAttachment(control, offset)); }
		public AnchorSetter top(int num, int offset) { return top(new FormAttachment(num, offset)); }
		public AnchorSetter top(int num) { return top(new FormAttachment(num)); }

		public AnchorSetter left(Control control, int offset) { return left(new FormAttachment(control, offset)); }
		public AnchorSetter left(int num, int offset) { return left(new FormAttachment(num, offset)); }
		public AnchorSetter left(int num) { return left(new FormAttachment(num)); }

		public AnchorSetter right(Control control, int offset) { return right(new FormAttachment(control, offset)); }
		public AnchorSetter right(int num, int offset) { return right(new FormAttachment(num, offset)); }
		public AnchorSetter right(int num) { return right(new FormAttachment(num)); }

		public AnchorSetter bottom(Control control, int offset) { return bottom(new FormAttachment(control, offset)); }
		public AnchorSetter bottom(int num, int offset) { return bottom(new FormAttachment(num, offset)); }
		public AnchorSetter bottom(int num) { return bottom(new FormAttachment(num)); }
	}

	protected static AnchorSetter anchor(Control c) { return new AnchorSetter(c); }

	/**
	 * @param parent
	 * @param style
	 * @param state
	 * @param configuration 
	 */
	public ConfigurationCompositeBase(Composite parent, int style, State state, ConfigurationContainer configuration) {
		super(parent, style, state);
		this.configurationContainer = configuration;
	}

	/**
	 * Initialize ConfigurationContainer from PersistentConfigProvider
	 * @param provider the PersistentConfigProvider to load config from
	 */
	public abstract void initConfiguration(PersistentConfigProvider provider);

	/**
	 * Load configuration from ConfigurationContainer
	 */
	public abstract void loadConfiguration();

	/**
	 * Store configuration from ConfigurationContainer to ConfigManipulator
	 * @param store the ConfigManipulator to store config to
	 * @param provider the PersistentConfigProvider containing the old config
	 */
	public abstract void storeConfiguration(ConfigManipulator store, PersistentConfigProvider provider);

	/**
	 * Called before exit.
	 * The method validates every setting in the configuration before exit
	 * 
	 * There might be settings when the user can decide to ignore a validation exception
	 * (for example the Outputfolder validation)
	 * In this case, the validator throws a ResumableException, which includes the
	 * validator index to resume from (should the user choose to ignore the error)
	 * 
	 * @param resumeFrom Resume from this validator index (initially 0)
	 * @throws Exception
	 */
	public abstract void validateSettings(int resumeFrom) throws Exception;
}
