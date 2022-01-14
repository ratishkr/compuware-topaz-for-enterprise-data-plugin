package com.compuware.jenkins.ted;

public enum SpecificationType {

	EMPTY("", ""), COMPARE("Compare Pro", "Compare"), CONVERT("Converter Pro", "Convert"), EXTRACT("Related Extract", "Extract"), LOAD("Related Load", "Load"), EXSUITE("Execution Suite", "ExSuite");
	
	private String displayName;
	private String value;
	
	private SpecificationType(String displayName, String value) {
		this.displayName = displayName;
		this.value = value;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public String getValue() {
		return value;
	}
	
}
