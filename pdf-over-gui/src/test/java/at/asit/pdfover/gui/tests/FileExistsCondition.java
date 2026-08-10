package at.asit.pdfover.gui.tests;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import java.io.File;

public class FileExistsCondition extends DefaultCondition {

    private final File file;

    public FileExistsCondition(File file) {
        this.file = file;
    }

    @Override
    public boolean test() {
        return file.exists();
    }

    @Override
    public String getFailureMessage() {
        return String.format("Could not create output file %s", file.getName());
    }

}