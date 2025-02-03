package gui.tests;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

public class WidgetExitsCondition extends DefaultCondition {
	
	private final String widgetName;

	public WidgetExitsCondition(String widget) {
		this.widgetName = widget;
	}
	
	@Override
	public boolean test() throws Exception {
		return bot.textWithLabel(widgetName).isVisible();
	}

	@Override
	public String getFailureMessage() {
		return String.format("Could not find widget %s", widgetName);
	}

}
