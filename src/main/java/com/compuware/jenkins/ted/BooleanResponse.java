package com.compuware.jenkins.ted;

public enum BooleanResponse {
	EMPTY("", ""), TRUE("TRUE", "true"), FALSE("FALSE", "false");
	
	private String displayName;
	private String value;
	
	private BooleanResponse(String displayName, String value) {
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
