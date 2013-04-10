package at.asit.pdfover.gui;

import at.asit.pdfover.gui.workflow.Workflow;

public class DeveloperMain {

	/**
	 * Developer Main Entry point...
	 * @param args
	 */
	public static void main(String[] args) {
		Workflow flow = new Workflow(args);
		
		flow.Start();
	}

}
