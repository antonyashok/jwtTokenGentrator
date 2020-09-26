package ui;

//from https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/custom.htm#CACCFEFD

import javafx.scene.control.TextField;

public class NumberField extends TextField{
	@Override
	public void replaceText(int start, int end, String text) {
		if (text.matches("(\\d)*"))
			super.replaceText(start, end, text);
	}
	
	@Override
	public void replaceSelection(String text) {
		if (text.matches("(\\d)*"))
			super.replaceSelection(text);
	}
}
