package at.asit.pdfover.gui.tests;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

public class WidgetExistsCondition extends DefaultCondition {

    private final String widgetName;

    public WidgetExistsCondition(String widget) {
        this.widgetName = widget;
    }

    @Override
    public boolean test() {
        return bot.textWithLabel(widgetName).isVisible();
    }

    @Override
    public String getFailureMessage() {
        return String.format("Could not find widget %s", widgetName);
    }

}