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


import org.eclipse.swt.widgets.Composite;
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
